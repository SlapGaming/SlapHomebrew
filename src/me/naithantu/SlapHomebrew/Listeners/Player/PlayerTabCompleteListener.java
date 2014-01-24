package me.naithantu.SlapHomebrew.Listeners.Player;

import java.util.Collection;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;

import me.naithantu.SlapHomebrew.Listeners.AbstractListener;
import me.naithantu.SlapHomebrew.Util.Util;

public class PlayerTabCompleteListener extends AbstractListener {
	
	@EventHandler
	public void onTabComplete(PlayerChatTabCompleteEvent event) {
		Collection<String> suggestions = event.getTabCompletions();
		if (suggestions.isEmpty()) { //Check if empty
			String[] split = event.getChatMessage().split(" "); //Split it on spaces
			String completor = split[split.length - 1]; //We only need the last one, which is being completed
			if (completor.substring(0, 1).equals("@")) { //Check if starts with @
				if (completor.length() == 1) { //No name given, just @
					for (Player p : Util.getOnlinePlayers()) { //Add all players to suggestions
						suggestions.add("@" + p.getName());
					}
				} else { //Part of a name is given. Find any suggestions
					String name = completor.substring(1); //Get start of name
					int length = name.length(); //
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
