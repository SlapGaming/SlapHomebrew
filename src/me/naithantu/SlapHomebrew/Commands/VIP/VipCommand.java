package me.naithantu.SlapHomebrew.Commands.VIP;

import java.util.List;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.ErrorMsg;
import me.naithantu.SlapHomebrew.Commands.Exception.NotVIPException;
import me.naithantu.SlapHomebrew.Controllers.Vip;
import me.naithantu.SlapHomebrew.Util.DateUtil;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VipCommand extends AbstractCommand {
		
	public VipCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public boolean handle() throws CommandException {
		Player p = getPlayer();
		String playername = p.getName();
		
		Vip vip = plugin.getVip(); //Get VIP
		
		if (args.length == 0) { //If no paramaters => Check VIP days
			if (vip.isVip(playername)) { //Is VIP
				if (vip.isLifetimeVIP(playername)) { //Lifetime VIP
					hMsg("You have lifetime VIP! :D");
				} else { //Temporary VIP
					long vipEnds = vip.getVIPExpiration(playername);
					if (vipEnds < System.currentTimeMillis()) { //Check if VIP hasn't ended yet (will be removed pretty soon)
						hMsg("Warning! Your VIP is about to end!");
					} else {
						hMsg("Your VIP ends: " + ChatColor.GREEN + DateUtil.format("dd MMM. yyyy HH:mm zzz", vipEnds));
					}
				}
			} else { //Not VIP
				throw new NotVIPException();
			}
			return true;
		}
		
		if (!vip.isVip(playername)) { //Check if VIP
			throw new NotVIPException();
		}
		
		switch (args[0].toLowerCase()) {
		case "help": //Help
			String is = "========================";
			String[] helpArray = new String[] {
					ChatColor.YELLOW + is + ChatColor.DARK_AQUA + " VIP " + ChatColor.YELLOW + is,
					ChatColor.DARK_AQUA + "/vip : " + ChatColor.WHITE + "Check your remaining days of VIP.",
					ChatColor.DARK_AQUA + "/vip grant : " + ChatColor.WHITE + "Open the VIP grant menu.",
					ChatColor.DARK_AQUA + "/te [player] : " + ChatColor.WHITE + "Teleport to a player.",
					ChatColor.DARK_AQUA + "/tpa [player] : " + ChatColor.WHITE + "Request a player to teleport to him/her.",
					ChatColor.DARK_AQUA + "/tpahere [player] : " + ChatColor.WHITE + "Request a player to teleport to you.",
					ChatColor.DARK_AQUA + "/backdeath : " + ChatColor.WHITE + "Teleport to your death location."
			};
			p.sendMessage(helpArray);
			break;
			
		case "grant": case "g": //Grant items
			testPermission("grant"); //Test Permission
			testNotWorld(new String[]{"world_sonic", "world_creative", "world_pvp"}); //Check if correct world
			
			int usesLeft = vip.getVipGrantUsesLeft(playername);
			if (usesLeft <= 0) { //Check if any uses left
				throw new CommandException(ErrorMsg.alreadyUsedVipGrant);
			}
			plugin.getExtras().getMenus().getVipMenu().open(p); //Open VIP Grant Menu
			break;
			
		default: //Usage
			return false;
		}
		return true;
	}
	
	/**
	 * TabComplete on this command
	 * @param sender The sender of the command
	 * @param args given arguments
	 * @return List of options
	 */
	public static List<String> tabComplete(CommandSender sender, String[] args) {
		if (args.length == 1) {
			return filterResults(
				createNewList("help", "grant"),
				args[0]
			);
		}
		return createEmptyList();
	}

}
