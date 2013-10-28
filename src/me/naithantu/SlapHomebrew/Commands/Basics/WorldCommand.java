package me.naithantu.SlapHomebrew.Commands.Basics;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;

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
		if (worldName.contains("resource"))
			worldName = "resource";
		switch (worldName) {
		case "world":
			this.msg(sender, "You are in the pre-1.6 (old) survival world.");
			break;
		case "survival2":
			this.msg(sender, "You are in the new survival world.");
			break;
		case "start":
			this.msg(sender, "You are in the lobby world.");
			break;
		case "resource":
			this.msg(sender, "You are in the resource world.");
			break;
		case "pvp":
			this.msg(sender, "You are in the PvP world.");
			break;
		case "the_end":
			this.msg(sender, "You are in the end.");
			break;
		case "nether":
			this.msg(sender, "You are in the nether.");
			break;
		case "creative":
			this.msg(sender, "You are in the creative world.");
			break;
		case "sonic":
			this.msg(sender, "You are in the mini-games world.");
			break;
		default:
			this.msg(sender, "You are in an undefined world!");
			break;
		}
		return true;
	}
}
