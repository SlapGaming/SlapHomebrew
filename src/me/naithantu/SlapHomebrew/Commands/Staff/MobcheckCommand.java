package me.naithantu.SlapHomebrew.Commands.Staff;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Util.Util;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class MobcheckCommand extends AbstractCommand {
	public MobcheckCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	public boolean handle() throws CommandException {
		testPermission("mobcheck"); //Test perm
		int mobLimit = 30, totalMobs = 0, mobRange = 25; //Default settings
		
		if (args.length > 0) { //Parse arguments
			mobLimit = parseInt(args[0]);
			if (args.length > 1) mobRange = parseInt(args[1]); 
		}
		
		if (mobRange >= 1000) throw new CommandException("The radius may not be above 1000."); //Check if range isn't to big.
		
		for (Player mobPlayer : Util.getOnlinePlayers()) {
			int mobCount = 0, animalCount = 0;
			for (Entity entity : mobPlayer.getNearbyEntities(mobRange, mobRange, mobRange)) { //Find all animals
				if (entity instanceof Creature) {
					mobCount++;
					if (entity instanceof Animals) {
						animalCount++;
					}
				}
			}
			if (mobCount > mobLimit) { //Send msg if enough mobs found
				hMsg("Player " + mobPlayer.getName() + " has " + mobCount + " mobs nearby! (" + animalCount + " animals)");
			}
			totalMobs += mobCount;
		}
		hMsg("Total mobs found: " + totalMobs);

		return true;
	}
}
