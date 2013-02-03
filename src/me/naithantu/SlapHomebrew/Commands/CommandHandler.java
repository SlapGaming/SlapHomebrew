package me.naithantu.SlapHomebrew.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class CommandHandler {
	public boolean handle(CommandSender sender, Command cmd, String[] args){
		String command = cmd.getName().toLowerCase();
		AbstractCommand commandObj = null;
		if(command.equals("minecart")){
			commandObj = new MinecartCommand(sender, args);
		} else if (command.equals("sgm")){
			commandObj = new SgmCommand(sender, args);
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
