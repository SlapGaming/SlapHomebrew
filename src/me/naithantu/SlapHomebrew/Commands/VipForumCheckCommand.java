package me.naithantu.SlapHomebrew.Commands;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import me.naithantu.SlapHomebrew.SlapHomebrew;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VipForumCheckCommand extends AbstractCommand {

	public VipForumCheckCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
	}

	public boolean handle() {
		if (!testPermission(sender, "vip.check")) {
			this.noPermission(sender);
			return true;
		}

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
				if (this.testPermission(sender, "vip.check")) {
					for (int vipNumber : unfinishedForumVip) {
						player.sendMessage(sendVipInfo(vipNumber));
					}
				}
			}
			return true;
		}
		return true;
	}
	
	private String sendVipInfo(int vipNumber) {
		String[] messageSplit = plugin.getForumVip().get(vipNumber).split("<:>");
		if(messageSplit[2].equals("demote")){
			return ChatColor.GOLD + "#" + vipNumber + " " + messageSplit[0] + " - " + ChatColor.RED + messageSplit[1] + ChatColor.GOLD + " needs to be demoted!";
		}else{
			return ChatColor.GOLD + "#" + vipNumber + " " + messageSplit[0] + " - " + ChatColor.GREEN + messageSplit[1] + ChatColor.GOLD + " needs to be promoted!";
		}
	}
}
