package me.naithantu.SlapHomebrew.Commands.VIP;

import me.naithantu.SlapHomebrew.Commands.AbstractVipCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Util.Util;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeCommand extends AbstractVipCommand {

	public TeCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	public boolean handle() throws CommandException {
		Player player = getPlayer(); //Cast to player
		testPermission("tp"); //Test perm
		if (args.length != 1) return false; //Check usage

		Player targetPlayer = getOnlinePlayer(args[0], false);
		String targetPlayername = targetPlayer.getName();
		
		if (plugin.getTpBlocks().contains(targetPlayername)) {
			if (!Util.testPermission(player, "tpblockoverride") && !plugin.getConfig().getStringList("tpallow." + targetPlayername).contains(player.getName().toLowerCase())) {
				throw new CommandException("You may not TP to that player. Use /tpa [playername] to request a teleport.");
			}
		}
		
		String worldname = targetPlayer.getWorld().getName(); 
		if (worldname.contains("world_resource")) worldname = "world_resource"; //Check if a resource world
		
		switch(targetPlayer.getWorld().getName()) {
		case "world": case "world_survival2": case "world_survival3": case "world_resource": case "world_the_end": case "world_lobby": case "world_creative": //Only allow TPing to certain worlds
			Util.safeTeleport(player, targetPlayer.getLocation(), targetPlayer.isFlying(), true); //Safe teleport
			if (!Util.testPermission(player, "staff")) { //If teleporting player not staff -> notify target player
				targetPlayer.sendMessage(ChatColor.GRAY + player.getName() + " has teleported to you!");
			}
			break;
		default:
			throw new CommandException("You cannot teleport to this player at this moment.");
		}
		
		return true;
	}
}
