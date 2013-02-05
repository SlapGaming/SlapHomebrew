package me.naithantu.SlapHomebrew.Commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class MobcheckCommand extends AbstractCommand {
	public MobcheckCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	public boolean handle() {
		if (!testPermission(sender, "potion")) {
			this.noPermission(sender);
			return true;
		}

		int mobLimit = 30;
		int totalMobs = 0;
		int mobRange = 25;
		if (args.length > 0) {
			try {
				mobLimit = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
			}
			if (args.length > 1) {
				try {
					mobRange = Integer.parseInt(args[1]);
				} catch (NumberFormatException e2) {
				}
			}
		}
		if (mobRange >= 1000) {
			this.badMsg(sender, "Error: The radius may not be above 1000, radius set to 1000!");
			mobRange = 1000;
		}
		for (Player mobPlayer : Bukkit.getServer().getOnlinePlayers()) {
			int mobCount = 0;
			int animalCount = 0;
			for (Entity entity : mobPlayer.getNearbyEntities(mobRange, mobRange, mobRange)) {
				if (entity instanceof Creature) {
					mobCount++;
					if (entity instanceof Animals) {
						animalCount++;
					}
				}
			}
			if (mobCount > mobLimit) {
				this.msg(sender, "Player " + mobPlayer.getName() + " has " + mobCount + " mobs nearby! (" + animalCount + " animals)");
			}
			totalMobs = totalMobs + mobCount;
		}
		this.msg(sender, "Total mobs found: " + totalMobs);

		return true;
	}
}
