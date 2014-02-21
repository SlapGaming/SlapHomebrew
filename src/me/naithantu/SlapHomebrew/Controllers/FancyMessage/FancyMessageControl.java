package me.naithantu.SlapHomebrew.Controllers.FancyMessage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

import org.bukkit.ChatColor;

import me.naithantu.SlapHomebrew.Controllers.AbstractController;

public class FancyMessageControl extends AbstractController {

	//Variable JsonMessage
	private HashMap<String, String> jsonMap;
	
	//Hardcoded JsonMessages
	private String playerRequester;
	private String playerRequested;
	private String teleportRequestOutgoing;
	private String teleportRequestIncoming;
	
	public FancyMessageControl() {
		jsonMap = new HashMap<>();
		loadFile();
		createHardcodedMessages();
	}
	
	/**
	 * Create the hardcoded messages
	 */
	private void createHardcodedMessages() {
		//A player recieves a request 
		playerRequested = 
			new FancyMessage("  \u2517\u25B6 ").color(ChatColor.GOLD)
				.addText("Click: ")
				.addText("[Accept]").color(ChatColor.GREEN).runCommand("/tpaccept %NAME%")
				.addText(" ")
				.addText("[Deny]").color(ChatColor.RED).runCommand("/tpdeny %NAME%")
				.toJSONString();
		
		//A player sends a request
		playerRequester =
				new FancyMessage("[SLAP] ").color(ChatColor.GOLD)
				.addText("You've requested %TEXT%. ")
				.addText("[Cancel]").color(ChatColor.GRAY).runCommand("/tpcancel")
				.toJSONString();
		
		//The player has sent an outgoing teleport request, check it using /tprequests
		teleportRequestOutgoing =
			new FancyMessage(" Outgoing: ")
				.addText("%NAME%").color(ChatColor.GOLD)
				.addText(" - ")
				.addText("[Cancel]").color(ChatColor.GRAY).runCommand("/tpcancel")
				.toJSONString();
		
		//The player has recieved a teleport request and checks it with /tprequests
		teleportRequestIncoming =
			new FancyMessage("  \u2517\u25B6 ")
				.addText("%NAME%").color(ChatColor.GOLD)
				.addText(" - ").color(ChatColor.WHITE)
				.addText("[Accept]").color(ChatColor.GREEN).runCommand("/tpaccept %NAME%")
				.addText(" ")
				.addText("[Deny]").color(ChatColor.RED).runCommand("/tpdeny %NAME%")
				.toJSONString();
	}
	
	/**
	 * Get player requested JSON String
	 * Format:
	 * 		[SLAP] Click: [Accept] [Deny]
	 * Var: %NAME%
	 * 	Replace with name of the requester
	 * 
	 * @param requesterName Name of the requester
	 * @return the JSON String
	 */
	public String getPlayerRequested(String requesterName) {
		return playerRequested.replaceAll("%NAME%", requesterName);
	}
	
	/**
	 * Get player requester JSON String
	 * Format:
	 * 		[SLAP] You've requested to [be teleported | teleport] to %NAME%. [Cancel]
	 * 
	 * @param here Requesting a HereTeleport or not
	 * @param requestedName Name of the requested person
	 * @return the JSON String
	 */
	public String getPlayerRequester(boolean here, String requestedName) {
		return playerRequester.replace("%TEXT%", (here ? requestedName + " to be teleported to you!" : "to teleport to " + requestedName));
	}
	
	/**
	 * Get incoming teleport request string
	 * Format:
	 * 		 -> %NAME% [Accept] [Deny]
	 * @param name The name of the player
	 * @return the Json string
	 */
	public String getTeleportRequestIncoming(String name) {
		return teleportRequestIncoming.replaceAll("%NAME%", name);
	}
	
	/**
	 * Get outgoing teleport request string
	 * Format:
	 * 		Outgoing: %NAME% [Cancel]
	 * @param name The name of the player
	 * @return the Json String
	 */
	public String getTeleportRequestOutgoing(String name) {
		return teleportRequestOutgoing.replaceAll("%NAME%", name);
	}
	
	
	/**
	 * Load the file with messages
	 */
	private void loadFile() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(plugin.getDataFolder() + File.separator + "fancymessage.txt"));
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] split = line.split("<=>", 2);
				jsonMap.put(split[0].toLowerCase().trim(), split[1].trim());
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Reload the file with messages
	 */
	public void reloadFile() {
		jsonMap.clear();
		loadFile();
	}
	
	/**
	 * Check if there is a message with this name
	 * @param name The name
	 * @return is message
	 */
	public boolean isMessage(String name) {
		return jsonMap.containsKey(name.toLowerCase());
	}
	
	/**
	 * Get the json message
	 * @param name The name of the message
	 * @return Json message or Null
	 */
	public String getJsonMessage(String name) {
		return jsonMap.get(name.toLowerCase());
	}

	@Override
	public void shutdown() {
		jsonMap.clear();
		
	}
	
	/**
	 * Get all fancy message names
	 * @return the names
	 */
	public Collection<String> getMessageNames() {
		return jsonMap.keySet();
	}

}
