package me.naithantu.SlapHomebrew.Commands.Staff;

import java.text.ParseException;
import java.util.*;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.UsageException;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogging.SessionLogger;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogging.SessionLogger.LeaderboardEntry;
import me.naithantu.SlapHomebrew.PlayerExtension.UUIDControl;
import me.naithantu.SlapHomebrew.Util.DateUtil;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class TimecheckCommand extends AbstractCommand {

	private SessionLogger logger;
	private boolean guides;
	
	public TimecheckCommand(CommandSender sender, String[] args) {
		super(sender, args);
		guides = true;
	}

	@Override
	public boolean handle() throws CommandException {
		testPermission("timecheck"); //Test permission
		
		checkDoingCommand(); //Check doing command
		
		logger = SessionLogger.getInstance(); //Get logger
		
		if (args.length == 0) { //Usage
			throw new UsageException("timecheck < [Player] | list <Staff | GuideStaff | [Group Name]> | Leaderboard [Entries] > <FromDate> <ToDate>");
		}
		
		switch (args[0].toLowerCase()) { //Switch first argument
		case "list": //List of staff/Group
			if (args.length < 2) {
				throw new UsageException("timecheck list <Staff | GuideStaff | [Group name]>");
			}
			switch (args[1].toLowerCase()) {
			case "staff": //Mods+ 
				guides = false;
			case "allstaff": case "guidestaff": case "gstaff": case "guidesstaff": //Guides+
				final Date[] staffDates = parseDates(2);
				addDoingCommand();
				Util.runASync(new Runnable() {
					@Override
					public void run() {
                        //Create lists
						HashSet<UUIDControl.UUIDProfile> profiles = new HashSet<>();
                        HashMap<String, PermissionUser> uuidToUser = new HashMap<>();
						List<PermissionGroup> groups = PermissionsEx.getPermissionManager().getGroupList(); //Get all groups

						for (PermissionGroup group : groups) { //Switch thru groups
							String groupname = group.getName().toLowerCase(); //To LC
							switch (groupname) {
							case "guide": case "vipguide": //Only do VIP if specified
								if (!guides) break;
							case "superadmin": case "admin": case "mod":
								for (PermissionUser user : group.getUsers()) { //Get all users
                                    //Get Profile
                                    UUIDControl.UUIDProfile profile = UUIDControl.getInstance().getUUIDProfile(user.getIdentifier());
                                    //Put in Map & Set
                                    profiles.add(profile);
                                    uuidToUser.put(profile.getUUID(), user);
								}
								break;
							}
						}

                        //Create an array of all Profiles
                        UUIDControl.UUIDProfile[] profileArray = profiles.toArray(new UUIDControl.UUIDProfile[profiles.size()]);
						HashMap<String, Long> playedMap = logger.getPlayedTimes(profileArray, staffDates[0], staffDates[1]); //Get played times
						ArrayList<LeaderboardEntry> entries = logger.createSortLeaderboardEntries(playedMap); //Create Leaderboard out the times
						hMsg("Staff onlinetimes. " + (guides ? " (With guides)" : "")); //Hmsg
						int rank = 1;
						for (LeaderboardEntry entry : entries) { //Start sending the players
							String prefix = "";
							PermissionUser user = uuidToUser.get(entry.getUUID()); //Get user
							if (user.getPrefix() != null) { //Has prefix
								prefix = user.getPrefix(); //Get prefix
							}
							uuidToUser.remove(entry.getUUID()); //Remove from map
							
							sender.sendMessage( //Send message
								ChatColor.GREEN + String.valueOf(rank) + ". " + 
								ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', prefix) + user.getName() + 
								ChatColor.WHITE + " - " + ChatColor.GOLD + Util.getTimePlayedString(entry.getPlaytime())
							);
							rank++;
						}
						
						for (PermissionUser user : uuidToUser.values()) { //Send any players that haven't played
							String prefix = "";
							if (user.getPrefix() != null) { //Has prefix
								prefix = user.getPrefix(); //Get prefix
							}
							sender.sendMessage(
								ChatColor.GREEN + String.valueOf(rank) + ". " +
								ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', prefix) + user.getName() +
								ChatColor.WHITE + " - " + ChatColor.GOLD + "No time."
							);
						}
						
						removeDoingCommand();
					}
				});
				break;
				
			default: //Name of group
				final PermissionGroup group = PermissionsEx.getPermissionManager().getGroup(args[1]); //Get group
				if (group == null) {
					throw new CommandException(args[1] + " is not a valid permission group.");
				}
				if (group.getName().equals("builder") || group.getName().equals("Member") || group.getName().equals("VIP")) { //Check if not builder, Member of VIP
					throw new CommandException("Not gonna check that group. Way to many users.");
				}
				final Date[] groupDates = parseDates(2);
				addDoingCommand(); //Add doing command
				Util.runASync(new Runnable() {
					@Override
					public void run() {
						Set<PermissionUser> permissionUsers = group.getUsers(); //Get users
						UUIDControl.UUIDProfile[] userArray = new UUIDControl.UUIDProfile[permissionUsers.size()];

                        UUIDControl uuidControl = UUIDControl.getInstance();

						int x = 0;
						for (PermissionUser user : permissionUsers) {
                            userArray[x] = uuidControl.getUUIDProfile(user.getIdentifier());
                            x++;
						}
						HashMap<String, Long> map = logger.getPlayedTimes(userArray, groupDates[0], groupDates[1]); //Get played times, from all players
						ArrayList<LeaderboardEntry> lb = logger.createSortLeaderboardEntries(map); //Sort
						sendLeaderboard(lb); //Send
						removeDoingCommand();
					}
				});
				break;
			}
			
			break;
			
		case "leaderboard": case "lb": //Get leaderboard
			if (args.length < 2) { //Check usage
				throw new UsageException("timecheck leaderboard [Entries] <FromDate> <ToDate>");
			}
			final int entries = parseIntPositive(args[1]); //Parse arg
			if (entries > 50) { //Check if not to many
				throw new CommandException("Max number of entries: 50");
			}
			final Date[] lbDates = parseDates(2); //Parse dates
			addDoingCommand(); //Add doing command
			Util.runASync(new Runnable() {
				@Override
				public void run() {
					ArrayList<LeaderboardEntry> lb = logger.getLeaderboard(lbDates[0], lbDates[1], entries);
					String hMsgString = "Leaderboard with " + entries + " " + (entries == 1 ? "entry" : "entries") + " ";
					if (lbDates[0] == null) {
						hMsgString += "since 5th of January.";
					} else {
						hMsgString += "since " + DateUtil.format("dd/MM/yyyy", lbDates[0]);
						if (lbDates[1] == null) {
							hMsgString += ".";
						} else {
							hMsgString += " till " + DateUtil.format("dd/MM/yyyy", lbDates[1]) + ".";
						}
					}
					hMsg(hMsgString);
					sendLeaderboard(lb);
					removeDoingCommand();
				}
			});
			break;
			
		default: //Get time for a player
			final UUIDControl.UUIDProfile offPlayer = getOfflinePlayer(args[0]); //Get player
			final Date[] playerDates = parseDates(1); //Parse dates, if given
			addDoingCommand(); //Add doing command
			Util.runASync(new Runnable() {
				@Override
				public void run() {
					long time = logger.getPlayedTime(offPlayer, playerDates[0], playerDates[1]); //Get time
					if (time == 0) { //If not played
						if (playerDates[0] == null) { //No timeframe given
							Util.badMsg(sender, offPlayer.getCurrentName() + " hasn't played yet since the 5th of january.");
						} else {
							Util.badMsg(sender, offPlayer.getCurrentName() + " hasn't played in this timeframe.");
						}
					} else { //Send playedtime
						hMsg(offPlayer.getCurrentName() + " has played " + Util.getTimePlayedString(time));
					}
					removeDoingCommand();
				}
			});
			break;
		}
		return true;
	}

	/**
	 * Parse the date arguments if given
	 * @param firstDateArg The argument number where the first Date (the from date) should be.
	 * @return Array with [0] From Date and [1] To Date.
	 * @throws CommandException if wrong date format, or from date is in the future, or from date is after to date
	 */
	private Date[] parseDates(int firstDateArg) throws CommandException {
		if (args.length <= firstDateArg) return new Date[]{null, null}; //No dates given
		
		Date fromDate = null, toDate = null;
				
		try {
			fromDate = DateUtil.parse("dd/MM/yyyy", args[firstDateArg]);
			Date now = new Date(); //Get current date
			if (fromDate.after(now)) { //FromDate can't be in the future
				throw new CommandException("The specified from date is in the future!");
			}
			if (firstDateArg + 1 < args.length) { //If toDate specified
				toDate = DateUtil.parse("dd/MM/yyyy", args[firstDateArg + 1]);
				if (fromDate.after(toDate)) { //If fromDate after toDate
					throw new CommandException("The from date cannot be after the to date!");
				}
			}
		} catch (ParseException e) {
			throw new CommandException("Wrong date format! dd/mm/yyyy");
		}
		return new Date[]{fromDate, toDate};	
	}
	
	/**
	 * Send the leaderboard to the player
	 * @param lb The leaderboard
	 */
	private void sendLeaderboard(ArrayList<LeaderboardEntry> lb) {
		int rank = 1;
		for (LeaderboardEntry entry : lb) { //Loop thru entries
			String playername = UUIDControl.getInstance().getUUIDProfile(entry.getUUID()).getCurrentName();
			sender.sendMessage(ChatColor.GREEN + String.valueOf(rank) + ". " + ChatColor.GOLD + playername +  ChatColor.WHITE + " - " + Util.getTimePlayedString(entry.getPlaytime())); //Send score
			rank++;
		}
	}

	/**
	 * TabComplete on this command
	 * @param sender The sender of the command
	 * @param args given arguments
	 * @return List of options
	 */
	public static List<String> tabComplete(CommandSender sender, String[] args) {
		if (!Util.testPermission(sender, "timecheck")) return createEmptyList();
		
		if (args.length == 1) {
			List<String> players = listAllPlayers(sender.getName()); //List all players
			players.add(0, "leaderboard"); //Add other options
			players.add(0, "list");
			return filterResults(players, args[0]); //Filter and return
		} else if (args.length == 2 && args[0].equalsIgnoreCase("list")) { //If first argument is list
			List<String> options = createNewList("staff", "guidesstaff", "allstaff", "Guide", "VIPGuide", "Mod", "Admin", "SuperAdmin"); //Options
			return filterResults(options, args[1]); //Filter results
		} else {
			return null;
		}
	}
	
}
