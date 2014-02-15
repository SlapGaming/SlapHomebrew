package me.naithantu.SlapHomebrew.Controllers.MessageStringer;

import org.bukkit.ChatColor;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.PlayerExtension.SlapPlayer;
import me.naithantu.SlapHomebrew.Storage.YamlStorage;
import me.naithantu.SlapHomebrew.Util.Util;

public class MessageCommandCombiner extends MessageCombiner {

	private String messageName;
	
	public MessageCommandCombiner(SlapPlayer slapPlayer, String messageName) {
		super(slapPlayer);
		this.messageName = messageName;
	}
	
	@Override
	public void finish() {
		Util.msg(slapPlayer.p(), "The new message has '" + messageName + "' as name and '" + ChatColor.translateAlternateColorCodes('&', message) + "' as message."); //Message
		slapPlayer.removeMessageCombiner(); //Remove combiner
		YamlStorage messageStorage = SlapHomebrew.getInstance().getMessageStorage(); //Get YamlStorage of messages
		messageStorage.getConfig().set("messages." + messageName, message); //Put message
        messageStorage.saveConfig(); //Save
	}
	
}
