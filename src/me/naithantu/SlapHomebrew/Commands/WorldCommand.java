package me.naithantu.SlapHomebrew.Commands;

import me.naithantu.SlapHomebrew.SlapHomebrew;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WorldCommand extends AbstractCommand {
	public WorldCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
	}

	public boolean handle() {
		if (!(sender instanceof Player)) {
			this.badMsg(sender, "You need to be in-game to do that!");
			return true;
		}
		
		if (!testPermission(sender, "world")) {
			this.noPermission(sender);
			return true;
		}
		
		Player player = (Player) sender;

		String worldName = player.getWorld().getName();
		worldName = worldName.replace("world_", "");
		if(worldName.contains("resource"))
			worldName = "resource";
		
		this.msg(sender, "You are in the " + worldName.toLowerCase() + " world.");
		return true;
	}
}
