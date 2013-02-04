package me.naithantu.SlapHomebrew.Commands;

import me.naithantu.SlapHomebrew.SlapHomebrew;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class CommandHandler {
	SlapHomebrew plugin;
	public CommandHandler(SlapHomebrew plugin){
		this.plugin = plugin;
	}
	
	public boolean handle(CommandSender sender, Command cmd, String[] args){
		String command = cmd.getName().toLowerCase();
		AbstractCommand commandObj = null;
		if(command.equals("minecart")){
			commandObj = new MinecartCommand(sender, args);
		} else if (command.equals("sgm")){
			commandObj = new SgmCommand(sender, args);
		}else if (command.equals("bumpdone")){
			commandObj = new BumpdoneCommand(sender, args, plugin);
		} else if (command.equals("te")){
			commandObj = new TeCommand(sender, args);
		} else if (command.equals("warpcakedefence")){
			commandObj = new WarpcakedefenceCommand(sender, args);
		} else if (command.equals("leavecake")){
			commandObj = new LeavecakeCommand(sender, args);
		} else if (command.equals("tpblock")){
			commandObj = new TeBlockCommand(sender, args);
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