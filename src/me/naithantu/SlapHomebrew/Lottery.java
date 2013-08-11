package me.naithantu.SlapHomebrew;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

public class Lottery {
	public boolean lotteryPlaying = false;
	public boolean lotteryEnabled = true;
	public HashMap<String, Integer> lottery = new HashMap<>();
	public HashMap<String, ItemStack> storedPrices = new HashMap<>();
	
	private boolean fakeLotteryPlaying = false;
	private String fakeLotteryWinner;
	private HashMap<String, Integer> fakeLotteryPlayers = new HashMap<>();
	private int taskID;
	
	SlapHomebrew plugin;
	
	int lotteryTimer;

	public Lottery(SlapHomebrew plugin) {
		this.plugin = plugin;
		lotteryTimer();
	}

	private void lotteryTimer() {
		lotteryTimer = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				fakeLotteryPlayers.clear();
				fakeLotteryWinner = null;
				lottery.clear();
				if (fakeLotteryPlaying == true) {
					Bukkit.getScheduler().cancelTask(taskID);
					fakeLotteryPlaying = false;
				}
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
					
					ArrayList<String> winningPlayers = new ArrayList<>();
										
					//Loop through the hashmap from top to bottom (keep original order!)
					for (String playerName : lottery.keySet()) {
						//Get number that the player rolled.
						int rolledNumber = lottery.get(playerName);
						if (rolledNumber == highestNumber) {
							//Even high as the current winner
							winningPlayers.add(playerName);
						} else if (rolledNumber > highestNumber) {
							//Number is higher than currently highestNumber
							highestNumber = rolledNumber;
							winningPlayers.clear();
							winningPlayers.add(playerName);
						}
					}
					
					if (winningPlayers.size() == 0) {
						//No winners
						plugin.getServer().broadcastMessage(ChatColor.GOLD + "[SLAP]" + ChatColor.WHITE + " The lottery is over! But no one played...");
						lottery.clear();
						lotteryPlaying = false;
						return;					
					} else {
						//Someone won!
						int randomNumber = new Random().nextInt(101);
						ItemStack price; String priceName;
						
						if (randomNumber == 0) {
							//Jackpot = 5 Diamonds
							price = new ItemStack(Material.DIAMOND, 5); priceName = "the jackpot! 5 diamonds!";
						} else if (randomNumber < 6) {
							//Cookies!
							price = new ItemStack(Material.COOKIE, 64); priceName = "a stack of cookies!";
						} else {
							//A Cake
							price = new ItemStack(Material.CAKE, 1); priceName = "a cake!";
						}
						
						if (winningPlayers.size() == 1) {
							//1 Winner
							plugin.getServer().broadcastMessage(ChatColor.GOLD + "[SLAP]" + ChatColor.WHITE + " The lottery is over! " + winningPlayers.get(0) + " has won " + priceName);
							givePrice(winningPlayers.get(0), price);
						} else if (winningPlayers.size() > 1) {
							//Multiple winners
							String winningPlayerNames = ""; int xCount = 0; boolean first = true;
							while (xCount < winningPlayers.size()) {
								if (first) {
									winningPlayerNames = winningPlayers.get(xCount);
									first = false;
								} else {
									String nextPlayerString;
									if ((xCount + 1) == winningPlayers.size()) nextPlayerString = " & "; else nextPlayerString = ", ";
									winningPlayerNames = winningPlayerNames + nextPlayerString + winningPlayers.get(xCount);
								}
								xCount++;
							}
							plugin.getServer().broadcastMessage(ChatColor.GOLD + "[SLAP]" + ChatColor.WHITE + " The lottery is over! " + winningPlayerNames + " have won " + priceName);
							for (String winningPlayer : winningPlayers) {
								givePrice(winningPlayer, price);
							}
						}	
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
	
	public boolean inStoredPrices(String playerName){
		boolean returnBool = false;
		if (storedPrices.get(playerName) != null) {
			returnBool = true;
		}
		return returnBool;
	}
	
	public ItemStack getStoredPrice(String playerName) {
		ItemStack returnStack = storedPrices.get(playerName);
		return returnStack;
	}
	
	public void removeStoredPrice(String playerName) {
		storedPrices.remove(playerName);
	}
	
	public void givePrice(String playerName, ItemStack price) {
		Player targetPlayer = plugin.getServer().getPlayer(playerName);
		String worldName = targetPlayer.getWorld().getName();
		if (!worldName.equals("world_sonic") && !worldName.equals("world_creative") && !worldName.equals("world_pvp")) {
			if (targetPlayer.getInventory().firstEmpty() != -1) {
				targetPlayer.getInventory().addItem(price);
			} else {
				//Player's inventory is full
				storedPrices.put(playerName, price);
				targetPlayer.sendMessage(ChatColor.RED + "You will get your prize when you make space in your inventory.");
			}
		} else {
			//Player is in Creative/Sonic world -> Store price
			storedPrices.put(playerName, price);
			targetPlayer.sendMessage(ChatColor.RED + "You will get your prize when you return to a survival world.");
		}
	}
	
	/* ---Fake Lottery Stuff--- */	
	public void startFakeLottery(String winner){
		Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "[SLAP]" + ChatColor.WHITE + " The lottery has started! Type /roll to play!");
		fakeLotteryPlaying = true;
		fakeLotteryWinner = winner;
		fakeLotteryPlayers.clear();
		BukkitTask task  = Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			
			@Override
			public void run() {
				stopFakeLottery();
			}
		}, 1200);
		taskID = task.getTaskId();
	}
	
	public boolean isFakeLotteryPlaying(){
		return fakeLotteryPlaying;
	}
	
	public String getFakeLotteryWinner(){
		return fakeLotteryWinner;
	}
	
	public boolean hasAlreadyFakeRolled(String player){
		if (fakeLotteryPlayers.containsKey(player)) {
			return true;
		} else {
			return false;
		}
	}
	
	public void fakeRoll(String playerName, int roll) {
		fakeLotteryPlayers.put(playerName, roll);
	}
	
	public void stopFakeLottery(){
		if (fakeLotteryPlayers.containsKey(fakeLotteryWinner)) {
			plugin.getServer().broadcastMessage(ChatColor.GOLD + "[SLAP]" + ChatColor.WHITE + " The lottery is over! " + fakeLotteryWinner + " has won a stack of diamond blocks!");
		} else {
			plugin.getServer().broadcastMessage(ChatColor.GOLD + "[SLAP]" + ChatColor.WHITE + " The lottery is over! " + fakeLotteryWinner + " didn't roll but wins anyways. Here's a stack of diamonds.");
		}
		fakeLotteryPlaying = false;
		fakeLotteryWinner = null;
	}
	
	
	
	
	
	
}
