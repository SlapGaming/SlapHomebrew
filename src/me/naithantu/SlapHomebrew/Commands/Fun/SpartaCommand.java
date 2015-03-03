package me.naithantu.SlapHomebrew.Commands.Fun;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.ErrorMsg;
import me.naithantu.SlapHomebrew.Util.Util;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class SpartaCommand extends AbstractCommand {

	private BukkitTask task;
	
	public SpartaCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	public boolean handle() throws CommandException {
		testPermission("sparta"); //Test Permission
		if (args.length == 0) return false; //Check usage

		final Player target = getOnlinePlayer(args[0], true);

		final int multiplier;

		if (args.length > 1) { //Check if multiplier specified
			try {
				multiplier = -Integer.parseInt(args[1]); //Parse number
			} catch (NumberFormatException e) {
				throw new CommandException(ErrorMsg.notANumber);
			}
		} else {
			multiplier = -2;
		}

		hMsg("Sparta'd " + target.getName());
		
		task = Util.runTimer(new Runnable() {
			private int sec = 1;
			@Override
			public void run() {
				if (!target.isOnline()) sec = 10; //Check if still online
				
				switch (sec) { //Do stuff based on second
				case 1:
					Util.badMsg(target, "THIS");
					break;
				case 2:
					Util.badMsg(target, "IS");
					break;
				case 3:
					Util.badMsg(target, "SPARTA!!!");
					
					double yaw = ((target.getLocation().getYaw() + 90) * Math.PI) / 180; //Calculate stuff
					double x = Math.cos(yaw);
					double z = Math.sin(yaw);
					Vector vector = new Vector(x, -0.2, z).multiply(multiplier);
					target.setVelocity(vector);
					break;
				default:
					task.cancel();
				}
				sec++;
			}
		}, 0, 20);

		return true;
	}
}
