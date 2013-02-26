package me.naithantu.SlapHomebrew.Commands;

import me.naithantu.SlapHomebrew.SlapHomebrew;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LeavecakeCommand extends AbstractCommand {
	public LeavecakeCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
	}

	public boolean handle() {
		if (!(sender instanceof Player)) {
			this.badMsg(sender, "You need to be in-game to do that!");
			return true;
		}

		if (!testPermission(sender, "leavecake")) {
			this.noPermission(sender);
			return true;
		}
		Player player = (Player) sender;
		player.getInventory().clear();
		World world = Bukkit.getServer().getWorld("world");
		player.teleport(world.getSpawnLocation());
		this.msg(sender, "You have been teleported back to spawn and your inventory has been cleared!");
		return true;
	}
}
