package me.naithantu.SlapHomebrew.Commands.Fun;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

public class PotionCommand extends AbstractCommand {
	public PotionCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
	}

	public boolean handle() {
		if (!testPermission(sender, "potion")) {
			this.noPermission(sender);
			return true;
		}

		Player potionPlayer;
		int time = 30;
		int power = 3;
		if (!(args.length > 1))
			return false;
		String name = args[0];
		if ((potionPlayer = getTarget(args[1])) == null)
			return true;
		if (name.equals("remove") || name.equals("cleanse")) {
			for (PotionEffect effect : potionPlayer.getActivePotionEffects())
				potionPlayer.removePotionEffect(effect.getType());
			this.msg(sender, "Potion effects removed for player " + potionPlayer.getName() + "!");
			return true;
		}
		if (args.length > 2) {
			try {
				time = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
				return false;
			}
		}
		if (args.length > 3) {
			try {
				power = Integer.parseInt(args[3]);
			} catch (NumberFormatException e) {
				return false;
			}
		}
		if (Util.getPotionEffect(name, time, power) != null) {
			potionPlayer.addPotionEffect(Util.getPotionEffect(name, time, power), true);
			this.msg(sender, "Potion effect added for player " + potionPlayer.getName() + "!");
		} else {
			this.badMsg(sender, "That potion effect does not exist!");
		}
		return true;
	}

	private Player getTarget(String target) {
		Player targetPlayer;
		if (!target.equals("me") && !target.equals("self")) {
			targetPlayer = Bukkit.getServer().getPlayer(args[1]);
			if (targetPlayer == null) {
				this.badMsg(sender, "Player not found!");
				return null;
			}
		} else {
			if (sender instanceof Player) {
				targetPlayer = (Player) sender;
			} else {
				this.badMsg(sender, "You need to be in-game to do that!");
				return null;
			}
		}
		return targetPlayer;
	}
}
