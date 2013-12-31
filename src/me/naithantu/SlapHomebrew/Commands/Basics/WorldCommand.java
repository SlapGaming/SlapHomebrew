package me.naithantu.SlapHomebrew.Commands.Basics;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WorldCommand extends AbstractCommand {
	public WorldCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	public boolean handle() throws CommandException {
		Player player = getPlayer();
		testPermission("world");

		String worldName = player.getWorld().getName();
		worldName = worldName.replace("world_", "");
		if (worldName.contains("resource"))
			worldName = "resource";
		switch (worldName) {
		case "world":
			hMsg("You are in the pre-1.6 (old) survival world.");
			break;
		case "survival2":
			hMsg("You are in the disabled 1.6 survival world.");
			break;
		case "survival3":
			hMsg("You are in the new 1.7 survival world!");
			break;
		case "start":
			hMsg("You are in the lobby world.");
			break;
		case "resource":
			hMsg("You are in the resource world.");
			break;
		case "pvp":
			hMsg("You are in the PvP world.");
			break;
		case "the_end":
			hMsg("You are in the end.");
			break;
		case "nether":
			hMsg("You are in the nether.");
			break;
		case "creative":
			hMsg("You are in the creative world.");
			break;
		case "sonic":
			hMsg("You are in the mini-games world.");
			break;
		default:
			hMsg("You are in an undefined world!");
			break;
		}
		return true;
	}
}
