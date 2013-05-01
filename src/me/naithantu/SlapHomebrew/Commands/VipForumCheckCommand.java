package me.naithantu.SlapHomebrew.Commands;

import java.util.List;

import me.naithantu.SlapHomebrew.SlapHomebrew;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class VipForumCheckCommand extends AbstractCommand {

	public VipForumCheckCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
	}

	public boolean handle() {
		if (!testPermission(sender, "vip.check")) {
			this.noPermission(sender);
			return true;
		}

		List<Integer> unfinishedForumVip = plugin.getUnfinishedForumVip();
		sender.sendMessage(ChatColor.AQUA + "----- " + unfinishedForumVip.size() + " Vips Waiting To Be Promoted/Demoted On Forums -----");
		for (int vipNumber : unfinishedForumVip) {
			sender.sendMessage(sendVipInfo(vipNumber));
		}

		return true;
	}

	private String sendVipInfo(int vipNumber) {
		String[] messageSplit = plugin.getForumVip().get(vipNumber).split("<:>");
		if (messageSplit[2].equals("demote")) {
			return ChatColor.GOLD + "#" + vipNumber + " " + messageSplit[0] + " - " + ChatColor.RED + messageSplit[1] + ChatColor.GOLD + " needs to be demoted!";
		} else {
			return ChatColor.GOLD + "#" + vipNumber + " " + messageSplit[0] + " - " + ChatColor.GREEN + messageSplit[1] + ChatColor.GOLD + " needs to be promoted!";
		}
	}
}
