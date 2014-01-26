package me.naithantu.SlapHomebrew.Controllers.FancyMessage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

import me.naithantu.SlapHomebrew.Controllers.AbstractController;

public class FancyMessageControl extends AbstractController {

	private HashMap<String, String> jsonMap;
	
	public FancyMessageControl() {
		jsonMap = new HashMap<>();
		loadFile();
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
