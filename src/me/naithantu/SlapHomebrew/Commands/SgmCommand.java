package me.naithantu.SlapHomebrew.Commands;

import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SgmCommand extends AbstractCommand {
	public SgmCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	public boolean handle() {
		if (!(sender instanceof Player)) {
			this.badMsg(sender, "You need to be in-game to do that!");
			return true;
		}
		Player player = (Player) sender;
		if (player.hasPermission("slaphomebrew.sgm")) {
			if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
				player.setGameMode(GameMode.CREATIVE);
			} else if (player.getGameMode() == GameMode.CREATIVE) {
				player.setGameMode(GameMode.SURVIVAL);
			}
		} else {
			this.noPermission(sender);
		}
		return true;
	}
}
