package me.naithantu.SlapHomebrew.Commands.Staff.VIP;

import java.util.List;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class VipForumCheckCommand extends AbstractCommand {

	public VipForumCheckCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	public boolean handle() throws CommandException {
		testPermission("vip.check");

		List<Integer> unfinishedForumVip = plugin.getUnfinishedForumVip();
		if (unfinishedForumVip.isEmpty()) throw new CommandException("There are no pending VIPs promotions/demotions.");
		msg(ChatColor.AQUA + "----- " + unfinishedForumVip.size() + " Vips Waiting To Be Promoted/Demoted On Forums -----");
		for (int vipNumber : unfinishedForumVip) {
			msg(sendVipInfo(vipNumber));
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
