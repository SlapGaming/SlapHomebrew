package me.naithantu.SlapHomebrew.Listeners.Player;

import java.util.Collection;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Listeners.AbstractListener;
import me.naithantu.SlapHomebrew.Util.Util;

public class PlayerTabCompleteListener extends AbstractListener {
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onTabComplete(PlayerChatTabCompleteEvent event) {
		Collection<String> suggestions = event.getTabCompletions(); //Get suggestions
		if (suggestions.isEmpty()) { //If empty add suggestions
			//Get the message
			String[] split = event.getChatMessage().split(" "); //Split it on spaces
			String completor = split[split.length - 1]; //We only need the last one, which is being completed
			
			//See if the completor string contains @, otherwise ignore it
			if (completor.substring(0, 1).equals("@")) {
				
				//Check if a name is given
				if (completor.length() == 1) {
					//	=> No name given, add all players from this server
					for (Player p : Util.getOnlinePlayers()) {
						suggestions.add("@" + p.getName());
					}

				} else {
					//	=> Name given, add players but filter them
					String name = completor.substring(1); //Get start of name
					int length = name.length();
					
					//	=> Add players from this server
					for (Player p : Util.getOnlinePlayers()) { //Loop thru players
						String playername = p.getName(); //Get name of player
						if (playername.length() >= length) { //Check if current @[name] isn't longer than the player name
							if (name.equalsIgnoreCase(playername.substring(0, length))) { //Check if matches
								suggestions.add("@" + playername); //Add to suggestions
							}
						}
					}
				}
			}
		}
	}
}
