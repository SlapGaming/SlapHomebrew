package me.naithantu.SlapHomebrew.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class SlapHomebrewCommand {
	public boolean handle(CommandSender sender, Command cmd, String[] args){
		String command = cmd.getName().toLowerCase();
		AbstractCommandHandler commandObj = null;
		if(command.equals("minecart")){
			commandObj = new MinecartCommand(sender, args);
		}
		
		if(commandObj != null){
			boolean handled = commandObj.handle();
			if(!handled) {
				commandObj.badMsg(sender, cmd.getUsage());
			}
		}
		return true;
	}
}
