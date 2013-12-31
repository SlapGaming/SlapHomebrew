package me.naithantu.SlapHomebrew.Commands.Staff.VIP;

import java.util.HashMap;
import java.util.List;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.UsageException;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class VipForumDoneCommand extends AbstractCommand{
	
	public VipForumDoneCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	public boolean handle() throws CommandException {
		testPermission("vip.check"); //Test perm
		if (args.length == 1) throw new UsageException("vip done [number] <comment>"); //Check usage

		int vipNumber = parseInt(args[1]);
		List<Integer> unfinishedForumVip = plugin.getUnfinishedForumVip();
		if (!unfinishedForumVip.contains(vipNumber)) throw new CommandException("This ID is not valid."); //Check if ID is valid (and still pending) 
		
		HashMap<Integer, String> forumVip = plugin.getForumVip();
		String vipInfo = forumVip.get(vipNumber);
		forumVip.put(vipNumber, vipInfo + "<:>" + "Handled by " + sender.getName());
		
		

		//Remove vip from unfinished vips list.
		int index = 0;
		for (int i : unfinishedForumVip) { //Loop thru list
			if (i == vipNumber) { //If found
				unfinishedForumVip.remove(index); //Remove from list
				break; //And stop looping
			}
			index++;
		}
		sender.sendMessage(ChatColor.GOLD + "Forum promotion/demotion #" + vipNumber + " was completed by " + sender.getName() + "!");
		return true;
	}

}
