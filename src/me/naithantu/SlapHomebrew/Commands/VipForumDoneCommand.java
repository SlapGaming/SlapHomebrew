package me.naithantu.SlapHomebrew.Commands;

import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import me.naithantu.SlapHomebrew.SlapHomebrew;

public class VipForumDoneCommand extends AbstractCommand{
	
	public VipForumDoneCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
	}

	public boolean handle() {
		if (!this.testPermission(sender, "vip.check")) {
			this.noPermission(sender);
			return true;
		}
		if (args.length == 1) {
			this.badMsg(sender, "Usage: /vip done [number] <comment>");
		}
		int vipNumber;

		try {
			vipNumber = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			this.badMsg(sender, "Usage: /vip done [number] <comment>");
			return true;
		}

		if (plugin.getUnfinishedForumVip().contains(vipNumber)) {
			HashMap<Integer, String> forumVip = plugin.getForumVip();
			String vipInfo = forumVip.get(vipNumber);
			forumVip.put(vipNumber, vipInfo + "<:>" + "Handled by " + sender.getName());
		} else {
			this.badMsg(sender, "You can not finish that request!");
			return true;
		}

		//Remove vip from unfinished vips list.
		List<Integer> unfinishedForumVip = plugin.getUnfinishedForumVip();
		int index = 0;
		int indexToRemove = 0;
		for (int i : unfinishedForumVip) {
			if (i == vipNumber) {
				indexToRemove = index;
			}
			index++;
		}
		unfinishedForumVip.remove(indexToRemove);
		sender.sendMessage(ChatColor.GOLD + "Forum promotion/demotion #" + vipNumber + " was completed by " + sender.getName() + "!");
		return true;
	}

}
