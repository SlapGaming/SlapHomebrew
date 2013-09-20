package me.naithantu.SlapHomebrew.Controllers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Util.Util;

public class DuelArena {

	private SlapHomebrew plugin;
	private World pvpWorld;
	
	private boolean gameInProgress;
	private String pvpTag = ChatColor.DARK_RED + "[PVP] " + ChatColor.WHITE;
	
	//Player 1
	private Player p1;
	private boolean onPad1;
	private BukkitTask p1Task;
	
	//Player 2
	private Player p2;
	private boolean onPad2;
	private BukkitTask p2Task;
	
	//Game
	private BukkitTask gameTask;
	private Location arenaLoc1;
	private Location arenaLoc2;
	private Location gameLoc;
	
	//Signs
	private ArrayList<Sign> signs;
	private String[] newGame;
	private String noPlayer;
	
	public DuelArena(SlapHomebrew plugin) {
		this.plugin = plugin;
		pvpWorld = plugin.getServer().getWorld("world_pvp");
		gameInProgress = false;
		onPad1 = false;
		onPad2 = false;
		arenaLoc1 = new Location(pvpWorld, 949.5, 29, -671.5, 0, 0);
		arenaLoc2 = new Location(pvpWorld, 949.5, 29, -635.5, 180, 0);
		gameLoc = new Location(pvpWorld, 949.5, 29, -628.5, 180, 0);
		signs = new ArrayList<>();
		signs.add((Sign) pvpWorld.getBlockAt(932, 32, -654).getState());
		signs.add((Sign) pvpWorld.getBlockAt(966, 32, -654).getState());
		signs.add((Sign) pvpWorld.getBlockAt(949, 30, -631).getState());
		noPlayer = ChatColor.DARK_GRAY + "Available";
		newGame = new String[] {"-- Pad 1 --", noPlayer, "-- Pad 2 --", noPlayer};
		setLines(newGame);
	}
	
	//Game methods
	private void startGameCountdown() {
		if (p1Task instanceof BukkitTask) {
			p1Task.cancel();
			p1Task = null;
		}
		
		if (p2Task instanceof BukkitTask) {
			p2Task.cancel();
			p2Task = null;
		}
		
		gameTask = Util.getScheduler(plugin).runTaskTimer(plugin, new GameStartTask(), 0, 5);
	}
	
	protected void stopGameCountdown() {
		gameTask.cancel();
		gameTask = null;
		if (onPad1) {
			p1Task = Util.getScheduler(plugin).runTaskTimer(plugin, new PadCheckerTask(p1, 1), 5, 5);
		}
		if (onPad2) {
			p2Task = Util.getScheduler(plugin).runTaskTimer(plugin, new PadCheckerTask(p2, 2), 5, 5);
		}
	}
	
	
	private void startGame() {
		gameInProgress = true;
		onPad1 = false;
		onPad2 = false;
		if (gameTask instanceof BukkitTask) {
			gameTask.cancel();
			gameTask = null;
		}
		setLines(new String[] {"Dueling:", ChatColor.WHITE + p1.getName(), "Versus.", ChatColor.WHITE + p2.getName()});
		p1.teleport(arenaLoc1);
		p2.teleport(arenaLoc2);
	}
	
	private void stopGame() {
		gameInProgress = false;
		Player[] ps = new Player[] {p1, p2};
		for (Player p : ps) {
			if (p != null && !p.isDead()) {
				p.teleport(gameLoc);
				p.setHealth(p1.getMaxHealth());
				p.setFoodLevel(20);
				for (PotionEffect effect : new ArrayList<>(p.getActivePotionEffects())) {
					p.removePotionEffect(effect.getType());
				}
			}
		}
		p1 = null; 
		p2 = null;
		setLines(newGame);
	}
	
	public World getPvpWorld() {
		return pvpWorld;
	}
	
	public int isAPlayer(Player p) {
		if (p == p1) {
			return 1;
		} else if (p == p2) {
			return 2;
		} else {
			return 0;
		}
	}
	
	/*
	 * Signs
	 */
	
	private void setLines(String[] lines) {
		for (Sign sign : signs) {
			int x = 0;
			for (String line : lines) {
				sign.setLine(x, line);
				x++;
			}
			sign.update();
		}
	}
	
	private void setLine(int index, String line) {
		for (Sign sign : signs) {
			sign.setLine(index, line);
			sign.update();
		}
	}
	
	/*
	 * Step on pad
	 */
	public void stepOnPad1(Player p) {
		double money = plugin.getEconomy().getBalance(p.getName());
		if (money < 200) {
			p.sendMessage(pvpTag + "You need atleast 200 dollars to enter a duel.");
			return;
		}
		p.sendMessage(pvpTag + "Stay on the pad!");
		setLine(1, ChatColor.WHITE + p.getName());
		p1 = p;
		onPad1 = true;
		if (onPad2) {
			startGameCountdown();
		} else {
			p1Task = Util.getScheduler(plugin).runTaskTimer(plugin, new PadCheckerTask(p1, 1), 5, 5);
		}
	}

	public void stepOnPad2(Player p) {
		double money = plugin.getEconomy().getBalance(p.getName());
		if (money < 200) {
			p.sendMessage(pvpTag + "You need atleast 200 dollars to enter a duel.");
			return;
		}
		p.sendMessage(pvpTag + "Stay on the pad!");
		setLine(3, ChatColor.WHITE + p.getName());
		p2 = p;
		onPad2 = true;
		if (onPad1) {
			startGameCountdown();
		} else {
			p2Task = Util.getScheduler(plugin).runTaskTimer(plugin, new PadCheckerTask(p2, 2), 5, 5);
		}
	}
	
	/*
	 * Leave pad
	 */
	protected void leftPad1() {
		p1.sendMessage(pvpTag + "You left the pad!");
		player1QuitsOnPad();
	}
	
	protected void leftPad2() {
		p2.sendMessage(pvpTag + "You left the pad!");
		player2QuitsOnPad();
	}
	
	/*
	 * Player dies
	 */
	public void player1Dies(PlayerDeathEvent event) {
		playerDies(p1, p2, event);
		p1 = null;
		stopGame();
	}
	
	public void player2Dies(PlayerDeathEvent event) {
		playerDies(p2, p1, event);
		p2 = null;
		stopGame();
	}
	
	private void playerDies(Player died, Player wins, PlayerDeathEvent event) {
		try {
			PlayerInventory inv = wins.getInventory();
			for (ItemStack item : event.getDrops()) {
				inv.addItem(item);
			}
		} catch (Exception e) {}
		broadcastWorldMessage(died.getName() + " died! " + wins.getName() + " has won the duel!");
		stopGame();
	}
	
	/*
	 * Player quits
	 */
	public void player1Quits() {
		plugin.getEconomy().withdrawPlayer(p1.getName(), 200);
		broadcastWorldMessage(p1.getName() + " forfeited and lost 200$! " + p2.getName() + " has won the duel!");
		stopGame();
	}
	
	public void player2Quits() {
		plugin.getEconomy().withdrawPlayer(p2.getName(), 200);
		broadcastWorldMessage(p2.getName() + " forfeited and lost 200$! " + p1.getName() + " has won the duel!");
		stopGame();
	}
	
	public void player1QuitsOnPad() {
		onPad1 = false;
		if (p1Task instanceof BukkitTask) {
			p1Task.cancel();
			p1Task = null;
		}
		setLine(1, noPlayer);
		p1 = null;
	}
	
	public void player2QuitsOnPad() {
		onPad2 = false;
		if (p2Task instanceof BukkitTask) {
			p2Task.cancel();
			p2Task = null;
		}
		setLine(3, noPlayer);
		p2 = null;
	}
	
	public void playerQuitsOnPad() {
		if (gameTask instanceof BukkitTask) {
			stopGameCountdown();
			if (p1 instanceof Player) {
				p1.sendMessage(pvpTag + "The other player has logged out.");
			}
			if (p2 instanceof Player) {
				p2.sendMessage(pvpTag + "The other player has logged out.");
			}
		}
	}
	
	
	/*
	 * Booleans
	 */
	public boolean isGameInProgress() {
		return gameInProgress;
	}
	
	public boolean isOnPad1() {
		return onPad1;
	}
	
	public boolean isOnPad2() {
		return onPad2;
	}
	
	
	private class PadCheckerTask extends BukkitRunnable {

		private Player p;
		private int pad;
		
		public PadCheckerTask(Player p, int pad) {
			this.p = p;
			this.pad = pad;
		}
		
		@Override
		public void run() {
			Location loc = p.getLocation();
			switch (pad) {
			case 1:
				if (!isOnPad1(loc)) {
					leftPad1();
				}
				break;
			case 2:
				if (!isOnPad2(loc)) {
					leftPad2();
				}
				break;
			}
		}
		
	}
	
	private class GameStartTask extends BukkitRunnable {

		private int quarterSeconds = -1;
		
		@Override
		public void run() {
			try {
				quarterSeconds++;
				Location p1Loc = p1.getLocation();
				Location p2Loc = p2.getLocation();
				
				if (!isOnPad1(p1Loc)) {
					leftPad1();
				}
				if (!isOnPad2(p2Loc)) {
					leftPad2();
				}
				
				if (!onPad1) {
					p2.sendMessage(pvpTag + "The other player left the pad! Countdown stopped.");
					stopGameCountdown();
				}
				if (!onPad2) {
					p1.sendMessage(pvpTag + "The other player left the pad! Countdown stopped.");
					stopGameCountdown();
				}
				
				if (!onPad1 || !onPad2) return;
				
				switch (quarterSeconds) {
				case 0:
					p1.sendMessage(pvpTag + "3 seconds remaining!");
					p2.sendMessage(pvpTag + "3 seconds remaining!");
					break;
				case 4:
					p1.sendMessage(pvpTag + "2 seconds remaining!");
					p2.sendMessage(pvpTag + "2 seconds remaining!");
					break;
				case 8:
					p1.sendMessage(pvpTag + "1 second remaining!");
					p2.sendMessage(pvpTag + "1 second remaining!");				
					break;
				case 12:
					broadcastWorldMessage(p1.getName() + " is dueling " + p2.getName() + "!");
					startGame();
					break;
				}
			} catch (Exception e) {
				System.out.println("Exception thrown");
				this.cancel();
			}
		}		
	}
	
	
	//Check Locations
	public boolean isOnPad1(Location loc) {
		int x = loc.getBlockX();
		int y = loc.getBlockY();
		int z = loc.getBlockZ();
		if (x == 931 && y == 29 && z == -654) return true;
		else return false;
	}
	
	public boolean isOnPad2(Location loc) {
		int x = loc.getBlockX();
		int y = loc.getBlockY();
		int z = loc.getBlockZ();
		if (x == 967 && y == 29 && z == -654) return true;
		else return false;
	}
	
	//Broadcast message
	public void broadcastWorldMessage(String msg) {
		List<Player> players = pvpWorld.getPlayers();
		for (Player p : players) {
			p.sendMessage(pvpTag + msg);
		}
	}
	
	public String getPvPTag() {
		return pvpTag;
	}
	
}
