package me.naithantu.SlapHomebrew.Commands.Staff;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.UsageException;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class SPromoteCommand extends AbstractCommand {

	public SPromoteCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public boolean handle() throws CommandException {
		testPermission("promote"); //Test permissions
		testPermission("staff");
		
		if (args.length != 2) throw new UsageException("sPromote [Player] [Rank]"); //Check usage
		
		final String playername = getOfflinePlayer(args[0]).getName(); //Get playername
		
		Util.runASync(plugin, new Runnable() {
			
			@Override
			public void run() {				
				PermissionUser user = PermissionsEx.getUser(playername);
				PexRank userRank = null;
				System.out.println(user.getGroupsNames()[0]);
				switch (user.getGroupsNames()[0]) {
				case "Member": userRank = PexRank.Member; break;
				case "Slap": userRank = PexRank.SLAP; break;
				case "VIP": userRank = PexRank.VIP; break;
				case "VIPGuide": userRank = PexRank.VIPGuide; break;
				case "Guide": userRank = PexRank.Guide; break;
				case "Mod": 
					if (user.getPrefix().toLowerCase().contains("trial")) {
						userRank = PexRank.TrialMod;
						break;
					}
				default:
					Util.badMsg(sender, "This user cannot be promoted (any futher this way).");
					return;
				}
				
				PexRank toPexRank = null;
				
				switch(args[1].toLowerCase()) {
				case "guide": case "vipguide":
					if (Util.testPermission(sender, "promote.guide")) {
						toPexRank = PexRank.Guide;
					} else {
						noPermForRank();
						return;
					}
					break;
				case "trial": case "trial-mod": case "trialmod": case "trial-moderator": case "trialmoderator":
					if (Util.testPermission(sender, "promote.trialmod")) {
						toPexRank = PexRank.TrialMod;
					} else {
						noPermForRank();
						return;
					}
					break;
				case "mod": case "moderator":
					if (Util.testPermission(sender, "promote.mod")) {
						toPexRank = PexRank.Mod;
					} else {
						noPermForRank();
						return;
					}
					break;
				default:
					Util.badMsg(sender, "This is not a valid rank.");
					return;
				}
				
				switch (toPexRank) {
				case Guide:
					switch (userRank) {
					case Member: case SLAP:
						resetPrefix(user, userRank);
						user.setGroups(new PermissionGroup[] {PermissionsEx.getPermissionManager().getGroup("Guide")});
						break;
					case VIP:
						resetPrefix(user, userRank);
						user.setGroups(new PermissionGroup[] {PermissionsEx.getPermissionManager().getGroup("VIPGuide")});
						break;
					default:
						cannotPromoteToRank();
						return;
					}
					break;
				case TrialMod:
					switch (userRank) {
					case Guide: case VIPGuide:
						resetPrefix(user, userRank);
						user.setGroups(new PermissionGroup[] {PermissionsEx.getPermissionManager().getGroup("Mod")});
						user.addPermission("-slaphomebrew.sgm");
						user.addPermission("-essentials.gamemode");
						user.addPermission("-myinv.bypass.world.*");
						user.addPermission("-mv.bypass.gamemode.*");
						user.addPermission("-slaphomebrew.gamesinventory");
						user.setPrefix(ChatColor.AQUA + "[Trial-Mod] ", null);
						break;
					default:
						cannotPromoteToRank();
						return;
					}
					break;
				case Mod:
					switch (userRank) {
					case TrialMod:
						resetPrefix(user, userRank);
						user.removePermission("-slaphomebrew.sgm");
						user.removePermission("-essentials.gamemode");
						user.removePermission("-myinv.bypass.world.*");
						user.removePermission("-mv.bypass.gamemode.*");
						user.removePermission("-slaphomebrew.gamesinventory");
						break;
					default:
						cannotPromoteToRank();
						return;
					}
					break;
				default:
					cannotPromoteToRank();
					return;
				}
				promotedMsg(playername, userRank, toPexRank);
				
				//Update tab
				Player fPlayer = plugin.getServer().getPlayer(playername);
				if (fPlayer != null) {
					plugin.getTabController().playerSwitchGroup(fPlayer);
				}
				
				//Log event
				//TODO
				return;
				
			}
		});
		return true;
	}
	
	protected static void resetPrefix(PermissionUser user, PexRank userRank) {
		String prefix = user.getPrefix().toLowerCase();
		switch (userRank) {
		case Member: if (!prefix.contains("member")) resetPrefix(user); break;
		case SLAP: if (!prefix.contains("slap")) resetPrefix(user); break;
		case Guide: if (!prefix.contains("guide")) resetPrefix(user); break;
		case VIP: if (!prefix.contains("vip")) resetPrefix(user); break;
		case VIPGuide: if (!prefix.contains("vipguide")) resetPrefix(user); break;
		case TrialMod: resetPrefix(user); break;
		case Mod: if (!prefix.contains("mod")) resetPrefix(user); break;
		}
	}
	
	protected static void resetPrefix(PermissionUser user) {
		user.setPrefix(null, null);
	}
	
	private void noPermForRank() {
		Util.badMsg(sender, "You are not allowed to promote people to this rank.");
	}
	
	private void cannotPromoteToRank() {
		Util.badMsg(sender, "Cannot promote player to this rank/from players rank.");
	}
	
	
	protected void promotedMsg(String playername, PexRank oldRank, PexRank newRank) {
		hMsg(playername + " has been promoted from " + oldRank.toString() + " to " + newRank.toString());
	}

	protected enum PexRank {
		Member, SLAP, VIP, Guide, VIPGuide, TrialMod, Mod
	}
	
	
}
