package me.naithantu.SlapHomebrew;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VipForumMarkCommands {
	SlapHomebrew plugin;

	public VipForumMarkCommands(SlapHomebrew instance) {
		plugin = instance;
	}

	private String sendVipInfo(int vipNumber) {
		String[] messageSplit = plugin.getForumVip().get(vipNumber).split("<:>");
		if(messageSplit[2].equals("demote")){
			return ChatColor.GOLD + "#" + vipNumber + " " + messageSplit[0] + " - " + ChatColor.RED + messageSplit[1] + ChatColor.GOLD + " needs to be demoted!";
		}else{
			return ChatColor.GOLD + "#" + vipNumber + " " + messageSplit[0] + " - " + ChatColor.GREEN + messageSplit[1] + ChatColor.GOLD + " needs to be promoted!";
		}
	}

	public boolean markCommand(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			HashMap<Integer, String> forumVip = plugin.getForumVip();
			int amount = forumVip.size() + 1;

			//Add extra information to vipinfo.
			String date = new SimpleDateFormat("MMM.d HH:mm z").format(new Date());
			date = date.substring(0, 1).toUpperCase() + date.substring(1);
			String playerName = args[1];
			forumVip.put(amount, date + "<:>" + playerName + "<:>" + args[2]);
			List<Integer> unfinishedForumVip = plugin.getUnfinishedForumVip();
			unfinishedForumVip.add(amount);
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (player.hasPermission("slaphomebrew.vip.check")) {
					for (int vipNumber : unfinishedForumVip) {
						player.sendMessage(sendVipInfo(vipNumber));
					}
				}
			}
			return true;
		}
		return false;
	}
	public boolean checkCommand(Player player, String[] args) {
		if (!player.hasPermission("slaphomebrew.vip.check")) {
			player.sendMessage(ChatColor.RED + "You do not have access to that command!");
			return true;
		}
		if (args.length == 1) {
			List<Integer> unfinishedForumVip = plugin.getUnfinishedForumVip();
			player.sendMessage(ChatColor.AQUA + "----- " + unfinishedForumVip.size() + " Vips Waiting To Be Promoted/Demoted On Forums -----");
			for (int vipNumber : unfinishedForumVip) {
				player.sendMessage(sendVipInfo(vipNumber));
			}
		}
		return true;
	}

	public boolean doneCommand(Player player, String[] args) {
		if (!player.hasPermission("slaphomebrew.vip.check")) {
			player.sendMessage(ChatColor.RED + "You do not have access to that command!");
			return true;
		}
		if (args.length == 1) {
			player.sendMessage(ChatColor.RED + "Usage: /vip done [number] <comment>");
		}
		int vipNumber;

		try {
			vipNumber = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			player.sendMessage(ChatColor.RED + "Usage: /vip done [number] <comment>");
			return true;
		}

		if (plugin.getUnfinishedForumVip().contains(vipNumber)) {
			HashMap<Integer, String> forumVip = plugin.getForumVip();
			String vipInfo = forumVip.get(vipNumber);
			forumVip.put(vipNumber, vipInfo + "<:>" + "Handled by " + player.getName());
		} else {
			player.sendMessage(ChatColor.RED + "You can not finish that request!");
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
		player.sendMessage(ChatColor.GOLD + "Forum promotion/demotion #" + vipNumber + " was completed by " + player.getName() + "!");
		return true;
	}
}
