package me.naithantu.SlapHomebrew.Controllers.MessageStringer;

import java.util.Arrays;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import me.naithantu.SlapHomebrew.PlayerExtension.SlapPlayer;
import me.naithantu.SlapHomebrew.Util.Util;

public class MultiChatCombiner extends MessageCombiner {

	private String prefix;
	
	public MultiChatCombiner(SlapPlayer slapPlayer) {
		super(slapPlayer);
		PermissionUser user = PermissionsEx.getUser(slapPlayer.getName()); //Get PexUser
		prefix = "<";
		if (user.getPrefix() != null) { //Add Prefix if there is one
			prefix += user.getPrefix();
		}
		prefix += slapPlayer.getName();
		if (user.getSuffix() != null) { //Add Suffix if there is one
			prefix += user.getSuffix();
		}
		prefix += ChatColor.WHITE + "> ";
	}

	@Override
	public void finish() {
		//Colorize message
		prefix = ChatColor.translateAlternateColorCodes('&', prefix);
		colorize();
		
		//Get target players
		final HashSet<Player> players = new HashSet<Player>(Arrays.asList(Util.getOnlinePlayers()));
		
		
		//=> Move to A-Sync
		Util.runASync(new Runnable() {
			@Override
			public void run() {
				AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(true, slapPlayer.getPlayer(), message, players);
				Bukkit.getPluginManager().callEvent(event);
				if (!event.isCancelled()) {
					Util.broadcast(prefix + message);
				}
			}
		});
	}

}
