package me.naithantu.SlapHomebrew.Commands.Promotion;

import java.util.List;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.ErrorMsg;
import me.naithantu.SlapHomebrew.Commands.Exception.UsageException;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogging.PromotionLogger;

public class PromotionRankCommand extends AbstractCommand {

	private String usage;
	
	public PromotionRankCommand(CommandSender sender, String[] args) {
		super(sender, args);
		usage = "promotion rank <promote | demote | logs>";
	}

	@Override
	public boolean handle() throws CommandException {
		try {
			if (args.length == 1) throw new UsageException(usage);
			
			Rank senderRank; //Rank of the CommandSender
			if (sender instanceof Player) { //Parse the player's Rank if player
				PermissionUser senderUser = PermissionsEx.getUser(sender.getName()); //Get User
				senderRank = Rank.parseRank(senderUser); //Parse rank
			} else {
				senderRank = Rank.SuperAdmin; //Console = OP/SuperAdmin
			}
			
			OfflinePlayer targetPlayer;
			PermissionUser targetUser;
			Rank toRank, fromRank;
			boolean changed;
			
			switch (args[1].toLowerCase()) {
			case "promote": case "promotion": case "p": //Promote a user
				testPermission("promote"); //Test perm
				if (args.length != 4) throw new UsageException("promotion rank promote [Player] [toRank]"); //usage
				
				//Get player info
				targetPlayer = getOfflinePlayer(args[2]); //Get player
				targetUser = PermissionsEx.getUser(targetPlayer.getName()); //Get user
				fromRank = Rank.parseRank(targetUser); //Get the current rank of that player
				
				//Do checks
				if (fromRank.getRanking() >= senderRank.getRanking()) { //See if the changer's Rank is higher than the rank of the player that is being changed
					throw new CommandException("You're not allowed to change the rank of this player.");
				}
				toRank = Rank.parseRank(args[3]); //Parse the to rank
				if (toRank.getRanking() == fromRank.getRanking()) { //Check if not same ranking
					throw new CommandException("The player is already on that rank (level).");
				}
				if (!toRank.isToRank() || toRank.getRanking() >= senderRank.getRanking()) { //Check if possible to promote to that rank & if not promoting someone to higher than self
					throw new CommandException("You cannot promote someone to that rank.");
				}
				if (toRank.getRanking() < fromRank.getRanking()) { //Check if not demoting instead of promoting
					throw new CommandException("That would be a demotion instead of a promotion.");
				}
				
				//Change the rank
				fromRank.fromRank(targetUser); //Remove rank leftovers
				toRank.toRank(targetUser); //Promote to rank
				
				changed = plugin.getVip().checkRank(targetPlayer, false, false); //Check if any VIP changes have to be made (Guide -> VIPGuide etc)
				if (changed) { //If the VIP check changed the rank
					toRank = Rank.parseRank(targetUser); //Parse that rank
				}
				PromotionLogger.logRankChange(targetPlayer.getName(), fromRank.name(), toRank.name(), true, "Command - " + sender.getName()); //Log it
				if (targetPlayer.getPlayer() != null) { //Check if player is online
					plugin.getTabController().playerSwitchGroup(targetPlayer.getPlayer()); //Update TAB
				}
				hMsg("Promoted " + targetPlayer.getName() + " from " + fromRank.name() + " to " + toRank.name() + "!");
				break;
				
			case "demote": case "demotion": case "d": //Demote a user
				testPermission("demote"); //Test perm
				if (args.length != 4) throw new UsageException("promotion rank demote [Player] [toRank]"); //usage
				
				//Get player info
				targetPlayer = getOfflinePlayer(args[2]); //Get player
				targetUser = PermissionsEx.getUser(targetPlayer.getName()); //Get user
				fromRank = Rank.parseRank(targetUser); //Get the current rank of that player
				toRank = Rank.parseRank(args[3]); //Parse the to rank
				
				//Do checks
				if (fromRank.getRanking() >= senderRank.getRanking()) { //See if the changer's Rank is higher than the rank of the player that is being changed
					throw new CommandException("You're not allowed to change the rank of this player.");
				}
				if (toRank.getRanking() == fromRank.getRanking()) { //Check if not same ranking
					throw new CommandException("The player is already on that rank (level).");
				}
				if (!toRank.isToRank() || toRank.getRanking() >= senderRank.getRanking()) { //Check if possible to promote to that rank & if not promoting someone to higher than self
					throw new CommandException("You cannot demote someone to that rank.");
				}
				if (toRank.getRanking() > fromRank.getRanking()) { //Check if not promoting instead of demoting
					throw new CommandException("That would be a promotion instead of a demotion.");
				}
				
				//Change the rank
				fromRank.fromRank(targetUser); //Remove rank leftovers
				toRank.toRank(targetUser); //Promote to rank
				
				changed = plugin.getVip().checkRank(targetPlayer, false, false); //Check if any VIP changes have to be made (Guide -> VIPGuide etc)
				if (changed) { //If the VIP check changed the rank
					toRank = Rank.parseRank(targetUser); //Parse that rank
				}
				PromotionLogger.logRankChange(targetPlayer.getName(), fromRank.name(), toRank.name(), false, "Command - " + sender.getName()); //Log it
				if (targetPlayer.getPlayer() != null) { //Check if player is online
					plugin.getTabController().playerSwitchGroup(targetPlayer.getPlayer()); //Update TAB
				}
				hMsg("Demoted " + targetPlayer.getName() + " from " + fromRank.name() + " to " + toRank.name() + "!");
				break;
			
			case "log": case "logs": case "l": //Get a log of all the promtions
				//TODO
				throw new CommandException(ErrorMsg.notSupportedYet);
				
			default:
				throw new UsageException(usage);
			
			}
		} catch (IllegalArgumentException e) {
			throw new CommandException("This is not a valid rank!");
		}
		return true;
	}

	@Override
	protected void testPermission(String perm) throws CommandException {
		super.testPermission("promotion.rank." + perm);
	}

	
	private enum Rank {
		builder(1, false),
		Member(2),
		VIP(2),
		Slap(2, false),
		Guide(3),
		VIPGuide(3),
		TrialMod(4),
		Mod(5),
		Admin(6),
		SuperAdmin(7, false);
		
		
		int ranking;
		boolean toRank;
		private Rank(int ranking) {
			this.ranking = ranking;
			toRank = true;
		}
		
		private Rank(int ranking, boolean toRank) {
			this.ranking = ranking;
			this.toRank = toRank;
		}
		
		public int getRanking() {
			return ranking;
		}
		
		public boolean isToRank() {
			return toRank;
		}
		
		/**
		 * Promote the user to this rank
		 * @param user The player
		 */
		public void toRank(PermissionUser user) {
			if (this == TrialMod) {
				user.setGroups(new String[]{"Mod"});
				user.setPrefix("&c[Trial-Mod] ", null);
				user.addPermission("-slaphomebrew.sgm");
				user.addPermission("-essentials.gamemode");
				user.addPermission("-myinv.bypass.world.*");
				user.addPermission("-mv.bypass.gamemode.*");
			} else {
				user.setGroups(new String[]{this.toString()});
			}
		}
		
		/**
		 * Remove any extra things this rank added to the player
		 * @param user The player
		 */
		public void fromRank(PermissionUser user) {
			if (this == TrialMod) {
				user.removePermission("-slaphomebrew.sgm");
				user.removePermission("-essentials.gamemode");
				user.removePermission("-myinv.bypass.world.*");
				user.removePermission("-mv.bypass.gamemode.*");
				user.removePermission("-slaphomebrew.gamesinventory");

			}
		}
		
		
		
		public static Rank parseRank(PermissionGroup group) {
			return Rank.valueOf(group.getName());
		}
		
		public static Rank parseRank(PermissionUser user) {
			Rank r = parseRank(user.getGroups()[0]);
			if (r == Mod) {
				String prefix = user.getPrefix();
				if (prefix != null) {
					if (prefix.toLowerCase().contains("trial")) {
						r = TrialMod;
					}
				}
			}
			return r;
		}
		
		public static Rank parseRank(String rank) {
			switch (rank.toLowerCase()) {
			case "builder":
				return builder;
			case "member":
				return Member;
			case "vip":
				return VIP;
			case "slap":
				return Slap;
			case "guide":
				return Guide;
			case "vipguide":
				return VIPGuide;
			case "trial": case "trial-mod": case "trialmod":
				return TrialMod;
			case "mod":
				return Mod;
			case "admin":
				return Admin;
			case "superadmin": case "op":
				return SuperAdmin;
			default:
				throw new IllegalArgumentException();
			}
		}
		
	}
	
	/**
	 * TabComplete on this command
	 * @param sender The sender of the command
	 * @param args given arguments
	 * @return List of options
	 */
	public static List<String> tabComplete(CommandSender sender, String[] args) {
		if (args.length == 2) {
			return filterResults(createNewList("promote", "demote", "logs"), args[1]); //Return sub commands
		} else if (args.length == 3 && !args[2].equalsIgnoreCase("logs")) {
			return listAllPlayers(sender.getName()); //Return players
		} else if (args.length == 4 && !args[2].equalsIgnoreCase("logs")) {
			List<String> ranks = createEmptyList();
			for (Rank r : Rank.values()) { //Loop thru ranks
				if (r.toRank) { //If to rank add it
					ranks.add(r.toString());
				}
			}
			return filterResults(ranks, args[3]); //Return ranks
		}
		return createEmptyList();
	}
	
}
