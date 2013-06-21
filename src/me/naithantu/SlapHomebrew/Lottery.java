package me.naithantu.SlapHomebrew;

import java.util.HashMap;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Lottery {
	public boolean lotteryPlaying = false;
	public boolean lotteryEnabled = true;
	public HashMap<String, Integer> lottery = new HashMap<String, Integer>();

	SlapHomebrew plugin;
	
	int lotteryTimer;

	public Lottery(SlapHomebrew plugin) {
		this.plugin = plugin;
		lotteryTimer();
	}

	private void lotteryTimer() {
		lotteryTimer = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				if (lotteryEnabled == true) {
					lotteryPlaying = true;
					Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "[SLAP]" + ChatColor.WHITE + " The lottery has started! Type /roll to play!");
					shortLotteryTimer();
					lotteryTimer();
				}
			}
		}, 72000);
	}

	private void shortLotteryTimer() {
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				if (!lottery.isEmpty()) {
					int highestNumber = -1;
					Player winningPlayer = null;
					//Loop through the hashmap from top to bottom (keep original order!)
					for (String playerName : lottery.keySet()) {
						//Get number that the player rolled.
						int rolledNumber = lottery.get(playerName);
						// If number is higher than currently highestNumber and player is online, 
						//make it the new highestNumber and change the winningPlayer.	
						if (rolledNumber > highestNumber && Bukkit.getServer().getPlayer(playerName) != null) {
							highestNumber = rolledNumber;
							winningPlayer = Bukkit.getServer().getPlayer(playerName);
						}
					}

					//If winningPlayer is null, no one played, return.
					if (winningPlayer == null) {
						Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "[SLAP]" + ChatColor.WHITE + " The lottery is over! But no one played...");
						lottery.clear();
						lotteryPlaying = false;
						return;
					}

					//Give reward to winner.
					Random random = new Random();
					if (random.nextInt(101) == 0) {
						winningPlayer.getInventory().addItem(new ItemStack(Material.DIAMOND, 5));
						Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "[SLAP]" + ChatColor.WHITE + " Jackpot! " + winningPlayer.getName() + " gets 5 diamonds!");
					} else if (random.nextInt(101) < 5) {
						winningPlayer.getInventory().addItem(new ItemStack(Material.COOKIE, 64));
						Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "[SLAP]" + ChatColor.WHITE + " Cookies! " + winningPlayer.getName() + " gets a stack of cookies!");
					} else {
						winningPlayer.getInventory().addItem(new ItemStack(Material.CAKE, 1));
						Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "[SLAP]" + ChatColor.WHITE + " The lottery is over! The winner is " + winningPlayer.getName() + "!");
					}

					lottery.clear();
					lotteryPlaying = false;
				} else {
					lotteryPlaying = false;
					Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "[SLAP]" + ChatColor.WHITE + " The lottery is over! But noone played...");
				}
			}
		}, 1200);
	}

	public void clearLottery() {
		lottery.clear();
	}

	public HashMap<String, Integer> getLottery() {
		return lottery;
	}
	
	public boolean toggleLottery(){
		Bukkit.getScheduler().cancelTask(lotteryTimer);
		return lotteryEnabled = !lotteryEnabled;
	}
	
	public boolean getPlaying(){
		return lotteryPlaying;
	}
	
	public void startLottery(){
		if(!lotteryPlaying){
			lotteryPlaying = true;
			Bukkit.getScheduler().cancelTask(lotteryTimer);
			shortLotteryTimer();
		}
	}
}
