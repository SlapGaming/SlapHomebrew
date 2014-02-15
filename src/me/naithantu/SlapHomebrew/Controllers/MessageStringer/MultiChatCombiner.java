package me.naithantu.SlapHomebrew.Controllers.MessageStringer;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import me.naithantu.SlapHomebrew.PlayerExtension.SlapPlayer;
import me.naithantu.SlapHomebrew.Util.Log;
import me.naithantu.SlapHomebrew.Util.Util;

public class MultiChatCombiner extends MessageCombiner {

	public MultiChatCombiner(SlapPlayer slapPlayer) {
		super(slapPlayer);
		PermissionUser user = PermissionsEx.getUser(slapPlayer.getName()); //Get PexUser
		message += "<";
		if (user.getPrefix() != null) { //Add Prefix if there is one
			message += user.getPrefix();
		}
		message += slapPlayer.getName();
		if (user.getSuffix() != null) { //Add Suffix if there is one
			message += user.getSuffix();
		}
		message += ChatColor.WHITE + "> ";
	}

	@Override
	public void finish() {
		colorize(); //Colorize message
		Log.info("Chat: " + message); //Log chat message
		for (Player p : Util.getOnlinePlayers()) { //Send message to all players
			p.sendMessage(message);
		}
	}

}
