package me.naithantu.SlapHomebrew.Commands.Games;

import java.util.HashSet;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WarppvpCommand extends AbstractCommand {
	static HashSet<String> chatBotBlocks = new HashSet<String>();

	public WarppvpCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
	}

	public boolean handle() {
		if (!(sender instanceof Player)) {
			this.badMsg(sender, "You need to be in-game to do that!");
			return true;
		}

		if (!testPermission(sender, "warppvp")) {
			this.noPermission(sender);
			return true;
		}

		Player player = (Player) sender;
		World world = Bukkit.getServer().getWorld("world_pvp");
		player.teleport(new Location(world, 929, 28.0, -584.0, 270, 0));
		this.msg(sender, "You have been teleported to the pvp world!");

		return true;
	}
}