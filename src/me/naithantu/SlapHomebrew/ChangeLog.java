package me.naithantu.SlapHomebrew;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class ChangeLog {

	private int pages;
	private ArrayList<ChangeLog.LoggedChange> changeLogArray;
	
	public ChangeLog() {
		changeLogArray = new ArrayList<>();
		createChangeLog();
		pages = (int)Math.ceil((double)(changeLogArray.size() / 5));
	}
	
	public void showPage(CommandSender targetPlayer, int page) {
		if (page > pages || page < 1) {
			targetPlayer.sendMessage(ChatColor.RED + "There are only " + pages + " pages.");
			return;
		}
		targetPlayer.sendMessage(ChatColor.YELLOW + "==================== " + ChatColor.GOLD  + "Changelog" + ChatColor.YELLOW  + " ====================");
		int xCount = (page - 1) * 5;
		int last = (page * 5);
		while (xCount < last) {
			try {
				sendMessage(targetPlayer, changeLogArray.get(xCount));
			} catch (ArrayIndexOutOfBoundsException e) {
				break;
			}
			xCount++;
		}
		targetPlayer.sendMessage(ChatColor.YELLOW + "================== " + ChatColor.GOLD  + "Page " + page + " out of " + pages + ChatColor.YELLOW  + " ==================");
	}
	
	public void sendMessage(CommandSender targetPlayer, LoggedChange loggedChange) {
		targetPlayer.sendMessage(ChatColor.GOLD + "[" + loggedChange.date + "] " + ChatColor.WHITE + loggedChange.change);
	}
	
	
	public void createChangeLog() {
		changeLogArray.add(0, new LoggedChange("28/06/2013", "Membership applies are now handeld automaticly!"));
		changeLogArray.add(0, new LoggedChange("29/06/2013", "Custom AFK messages added. '/afk [reason]'"));
		changeLogArray.add(0, new LoggedChange("01/07/2013", "Ocelot spawner eggs added to the spawn shop!"));
		changeLogArray.add(0, new LoggedChange("02/07/2013", "Lottery fixed, multiple people can win now!"));
		changeLogArray.add(0, new LoggedChange("04/07/2013", "/world command added, see what world you are in!"));
		changeLogArray.add(0, new LoggedChange("06/07/2013", "Teleport to a specific spawn. Try it out: /spawn [new/old/sonic/rw/pvp/end]"));
		changeLogArray.add(0, new LoggedChange("08/07/2013", "Iconmenu for /home added, VIP only! Try it out: /homemenu"));
		changeLogArray.add(0, new LoggedChange("09/07/2013", "Extra messages added to notify you when someone teleports to you (/te)."));
		changeLogArray.add(0, new LoggedChange("12/07/2013", "Manage your horses! For more info: /horse help"));
		changeLogArray.add(0, new LoggedChange("12/07/2013", "This (/new | /changelog) has been added!"));
		changeLogArray.add(0, new LoggedChange("14/07/2013", "Fences are now lockable."));
		changeLogArray.add(0, new LoggedChange("14/07/2013", "/stafflist now replaces /modlist!"));
		changeLogArray.add(0, new LoggedChange("14/07/2013", "Horse spawn eggs are now available in the shop."));
		changeLogArray.add(0, new LoggedChange("15/07/2013", "Mutate your horses in skeleton or zombie horses, check out /horse help 2 [VIP/Donate only]!"));
		changeLogArray.add(0, new LoggedChange("28/07/2013", "WorldGaurd owners can now add/remove members to/from their worldgaurd."));
		changeLogArray.add(0, new LoggedChange("29/07/2013", "Players have a 5% chance of dropping their head in PvP."));
		changeLogArray.add(0, new LoggedChange("31/07/2013", "Mail is now an actual mail plugin. Check out /mail help or the forums!"));
		changeLogArray.add(0, new LoggedChange("06/08/2013", "Added [AFK] tags to /list & /stafflist."));
		changeLogArray.add(0, new LoggedChange("10/08/2013", "Tab is now customized, yay colors!"));
	}
	
	private class LoggedChange {
		
		public String date;
		public String change;
		
		public LoggedChange(String date, String change) {
			this.date = date;
			this.change = change;
		}
		
	}
	
}
