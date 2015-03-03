package me.naithantu.SlapHomebrew.Commands.Basics;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class FlySpeedCommand extends AbstractCommand {

	public FlySpeedCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public boolean handle() throws CommandException {
		Player p = getPlayer(); //Get player
		testPermission("flyspeed"); //Test perm
		testWorld("world_creative"); //Check if in creative world
		
		if (args.length != 1) return false; //Usage
		
		int speed = parseIntPositive(args[0]); //Parse arg
		if (speed > 3) { //Max speed = 3
			speed = 3;
		}
		float fSpeed = speed / 10.0f; //To float
		p.setFlySpeed(fSpeed); //Set speed
		hMsg("Fly speed has been set to " + speed); //Msg player
		return true;
	}
	
	/**
	 * TabComplete on this command
	 * @param sender The sender of the command
	 * @param args given arguments
	 * @return List of options
	 */
	public static List<String> tabComplete(CommandSender sender, String[] args) {
		return createNewList("1", "2", "3");		
	}

}
