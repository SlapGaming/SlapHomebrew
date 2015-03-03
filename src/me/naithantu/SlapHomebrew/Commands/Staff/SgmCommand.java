package me.naithantu.SlapHomebrew.Commands.Staff;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SgmCommand extends AbstractCommand {
	public SgmCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	public boolean handle() throws CommandException {
		Player player = getPlayer(); //Get player
		testPermission("sgm"); //Test perm

		if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
			player.setGameMode(GameMode.CREATIVE);
		} else if (player.getGameMode() == GameMode.CREATIVE) {
			player.setGameMode(GameMode.SURVIVAL);
		}
		return true;
	}
}
