package me.naithantu.SlapHomebrew.Commands.Staff;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import com.earth2me.essentials.User;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Staff.SPromoteCommand.PexRank;
import me.naithantu.SlapHomebrew.Util.Util;

public class SDemoteCommand extends AbstractCommand {

	public SDemoteCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
	}
	
	@Override
	public boolean handle() {
		if (!testPermission(sender, "demote") || !testPermission(sender, "staff")) {
			noPermission(sender);
			return true;
		}
		
		// /sDemote [Player] [Rank]
		if (args.length != 2) {
			badMsg(sender, "Usage: /sDemote [Player] [Rank]");
			return true;
		}
		
Util.runASync(plugin, new Runnable() {
			
			@Override
			public void run() {
				
				User u = plugin.getEssentials().getUserMap().getUser(args[0]);
				if (u == null) {
					badMsg(sender, "This player hasn't been on the server.");
					return;
				}
				
				PermissionUser user = PermissionsEx.getUser(u.getName());
				PexRank userRank = null;
				switch (user.getGroupsNames()[0]) {
				case "VIPGuide": 
					if (testPermission(sender, "demote.guide")) {
						userRank = PexRank.VIPGuide; 
					} else {
						noPermForRank();
						return;
					}
					break;
				case "Guide": 
					if (testPermission(sender, "demote.guide")) {
						userRank = PexRank.Guide; 
					} else {
						noPermForRank();
						return;
					}
					break;
				case "Mod": 
					if (user.getPrefix().toLowerCase().contains("trial")) {
						if (testPermission(sender, "demote.trialmod")) {
							userRank = PexRank.TrialMod;
						} else {
							noPermForRank();
							return;
						}
					} else {
						if (testPermission(sender, "demote.mod")) {
							userRank = PexRank.Mod;
						} else {
							noPermForRank();
							return;
						}
					}
					break;
				default:
					badMsg(sender, "This user cannot be demoted (any futher this way).");
					return;
				}
								
				PexRank toPexRank = null;
				int vipDays = plugin.getVip().getVipDays(u.getName());
				
				switch(args[1].toLowerCase()) {
				case "member": case "vip":
					if (vipDays > 0 || vipDays == -1) {
						toPexRank = PexRank.VIP;
					} else {
						toPexRank = PexRank.Member;
					}
					break;
				case "guide": case "vipguide":
					if (testPermission(sender, "promote.guide")) {
						if (vipDays > 0 || vipDays == -1) {
							toPexRank = PexRank.VIPGuide;
						} else {
							toPexRank = PexRank.Guide;
						}
					} else {
						noPermForRank();
						return;
					}
					break;
				default:
					badMsg(sender, "This is not a valid rank/cannot demote to this rank.");
					return;
				}
				
				switch (toPexRank) {
				case Member:
					resetPrefix(user, userRank);
					demoteTrial(user, userRank);
					user.setGroups(new PermissionGroup[] {PermissionsEx.getPermissionManager().getGroup("Member")});
					break;
				case VIP:
					resetPrefix(user, userRank);
					demoteTrial(user, userRank);
					user.setGroups(new PermissionGroup[] {PermissionsEx.getPermissionManager().getGroup("VIP")});
					break;
				case Guide:
					switch (userRank) {
					case TrialMod: case Mod:
						resetPrefix(user, userRank);
						demoteTrial(user, userRank);
						user.setGroups(new PermissionGroup[] {PermissionsEx.getPermissionManager().getGroup("Guide")});
						break;
					default:
						cannotDemoteToRank();
						return;	
					}
					break;
				case VIPGuide:
					switch (userRank) {
					case TrialMod: case Mod:
						resetPrefix(user, userRank);
						demoteTrial(user, userRank);
						user.setGroups(new PermissionGroup[] {PermissionsEx.getPermissionManager().getGroup("VIPGuide")});
						break;
					default:
						cannotDemoteToRank();
						return;	
					}
					break;
				default:
					cannotDemoteToRank();
					return;
				}
				demotedMsg(u.getName(), userRank, toPexRank);
				//Update tab
				if (u.isOnline()) {
					plugin.getTabController().playerSwitchGroup(u.getPlayer());
				}
				
				//Log event
				String senderName = "Console";
				if (sender instanceof Player) {
					senderName = ((Player)sender).getName();
				}
				plugin.getPlayerLogger().logPromotion(senderName, u.getName(), userRank.toString(), toPexRank.toString(), true);
				return;
				
			}
		});
		return true;
	}
	
	private void noPermForRank() {
		badMsg(sender, "You are not allowed to demote people to this rank.");
	}
	
	private void resetPrefix(PermissionUser user, PexRank userRank) {
		SPromoteCommand.resetPrefix(user, userRank);
	}
	
	
	protected void demotedMsg(String playername, PexRank oldRank, PexRank newRank) {
		msg(sender, playername + " has been promoted from " + oldRank.toString() + " to " + newRank.toString());
	}
	
	private void cannotDemoteToRank() {
		badMsg(sender, "Cannot demote player to this rank/from players rank.");
	}
	
	private void demoteTrial(PermissionUser user, PexRank rank) {
		if (rank == PexRank.TrialMod) {
			user.removePermission("-slaphomebrew.sgm");
			user.removePermission("-essentials.gamemode");
			user.removePermission("-myinv.bypass.world.*");
			user.removePermission("-mv.bypass.gamemode.*");
			user.removePermission("-slaphomebrew.gamesinventory");
		}
	}
	
}
