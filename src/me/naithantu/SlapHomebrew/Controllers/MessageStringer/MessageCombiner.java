package me.naithantu.SlapHomebrew.Controllers.MessageStringer;

import me.naithantu.SlapHomebrew.PlayerExtension.SlapPlayer;
import me.naithantu.SlapHomebrew.Util.Util;
import org.bukkit.ChatColor;

public abstract class MessageCombiner {
	
	protected String message;
	protected SlapPlayer slapPlayer;
	
	protected int messagesAdded;
	
	public MessageCombiner(SlapPlayer slapPlayer) {
		message = "";
		this.slapPlayer = slapPlayer;
		messagesAdded = 0;
	}
	
	/**
	 * Add text to the message
	 * @param text The text
	 * @return this for chaining
	 */
	public MessageCombiner addText(String text) {
		if (messagesAdded++ > 0) {
			message += " ";
		}
		message += text;
		return this;
	}
	
	/**
	 * Colorize the string
	 * @return this for chaining
	 */
	public MessageCombiner colorize() {
		message = ChatColor.translateAlternateColorCodes('&', message);
		return this;
	}
	
	/**
	 * Get the owner of this MessageCombiner
	 * @return the player
	 */
	public SlapPlayer getSlapPlayer() {
		return slapPlayer;
	}
	
	/**
	 * Get the combined message
	 * @return The message
	 */
	public String getMessage() {
		return message;
	}
	
	/**
	 * Will notify how to end this message
	 */
	public void notifyHowToEnd() {
		String red = ChatColor.RED.toString();
		String white = ChatColor.WHITE.toString();
		Util.msg(slapPlayer.p(), "Added! Type " + red + "*" + white + ", " + red + "stop" + white + " or " + red + "end" + white + " to finish the message.");
	}
	
	/**
	 * Check if the given message is the end
	 * @param message The message
	 * @return is ending
	 */
	public boolean isEnding(String message) {
		return (message.equals("*") || message.equalsIgnoreCase("stop") || message.equalsIgnoreCase("end"));
	}
	
	
	/**
	 * Finish the message
	 */
	public abstract void finish();
	
	
	
	
	
}
