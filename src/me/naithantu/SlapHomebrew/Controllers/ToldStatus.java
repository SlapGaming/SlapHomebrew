package me.naithantu.SlapHomebrew.Controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class ToldStatus extends AbstractController {

	private HashMap<String, ToldSender> senders;
	private ArrayList<String> toldList;
	
	public ToldStatus() {
		senders = new HashMap<>();
		loadMessages();
	}
	
	/**
	 * Load the ToldStatus messages
	 */
	private void loadMessages() {
		toldList = new ArrayList<>();
		try {
			BufferedReader bf = new BufferedReader(new FileReader(plugin.getDataFolder() + File.separator + "ToldStatus.txt"));
			String line;
			while ((line = bf.readLine()) != null) {
				toldList.add(line.trim().replace("<=>", "'"));
			}
			bf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Send a told status to the player
	 * @param p The player
	 * @param ticksInterval The ticks interval between messages (Seconds * 20)
	 * @param stopWhenOff should stop when the player goes off
	 * @throws CommandException if already being told
	 */
	public void sendToldStatus(Player p, int ticksInterval, boolean stopWhenOff) throws CommandException {
		if (senders.containsKey(p.getName())) { //Check if not already being told
			throw new CommandException("This player is already being told.");
		}
		
		p.sendMessage(Util.getHeader() + "TOLD STATUS:");
		ToldSender told = new ToldSender(p, stopWhenOff); //Create new Runnable
		BukkitTask task = Util.runTimer(plugin, told, 1, ticksInterval); //Run task
		told.setOwnTask(task); //Set task in ToldSender
		senders.put(p.getName(), told); //Put in map
	}
	
	/**
	 * Stop a player's Told Status
	 * @param playername The player's name. Case sensitive.
	 * @throws CommandException if not being told
	 */
	public void stopToldStatus(String playername) throws CommandException {
		if (!senders.containsKey(playername)) { //Check if being told
			throw new CommandException("This player isn't being told.");
		}
		senders.get(playername).shutdown();
	}
	
	private class ToldSender extends BukkitRunnable {
		
		private BukkitTask ownTask;
		private int lineNr;
		private int size;
		private ArrayList<String> list;
		private Player player;
		private boolean stopWhenOff;
		
		public ToldSender(Player victim, boolean stopWhenOff) {
			list = new ArrayList<>(toldList);
			lineNr = 0;
			size = list.size();
			player = victim;
			this.stopWhenOff = stopWhenOff;
		}
		
		@Override
		public void run() {
			if (lineNr >= size) { //Done
				shutdown();
				return;
			}
			if (player.isOnline()) { //If player is online, send message
				player.sendMessage(toldList.get(lineNr));
				lineNr++;
			} else {
				if (stopWhenOff) {
					lineNr = size;
				}
			}
		}
		
		public void setOwnTask(BukkitTask ownTask) {
			this.ownTask = ownTask;
		}
		
		private void shutdown() {
			ownTask.cancel();
			senders.remove(player.getName());
		}
	}

	@Override
	public void shutdown() {

	}

}
