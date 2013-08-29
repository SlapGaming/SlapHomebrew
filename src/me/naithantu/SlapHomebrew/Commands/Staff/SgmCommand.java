package me.naithantu.SlapHomebrew.Commands.Staff;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;

import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SgmCommand extends AbstractCommand {
	public SgmCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
	}

	public boolean handle() {
		if (!(sender instanceof Player)) {
			this.badMsg(sender, "You need to be in-game to do that!");
			return true;
		}

		if (!testPermission(sender, "sgm")) {
			this.noPermission(sender);
			return true;
		}

		Player player = (Player) sender;
		if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
			player.setGameMode(GameMode.CREATIVE);
		} else if (player.getGameMode() == GameMode.CREATIVE) {
			player.setGameMode(GameMode.SURVIVAL);
		}
		return true;
	}
}
