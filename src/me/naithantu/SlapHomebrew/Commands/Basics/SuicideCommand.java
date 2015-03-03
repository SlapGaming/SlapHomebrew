package me.naithantu.SlapHomebrew.Commands.Basics;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogging.DeathLogger;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SuicideCommand extends AbstractCommand {

	
	public SuicideCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public boolean handle() throws CommandException {
		Player p = getPlayer();
		testPermission("suicide");
		
		if (p.getHealth() == 0) {
			throw new CommandException("You are already dead o.O");
		} else {
			p.sendMessage(ChatColor.GRAY + "Goodbye world D:");
            String UUID = p.getUniqueId().toString();
			plugin.getPlayerLogger().commitsSuicide(p.getName());
			DeathLogger.playerCommitsSuicide(UUID);
			p.setHealth(0);
		}
		return true;
	}
	
	

}
