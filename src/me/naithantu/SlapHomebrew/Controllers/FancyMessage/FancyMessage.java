package me.naithantu.SlapHomebrew.Controllers.FancyMessage;

import net.minecraft.server.v1_8_R1.NBTTagCompound;
import org.bukkit.Achievement;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.libs.com.google.gson.JsonArray;
import org.bukkit.craftbukkit.libs.com.google.gson.JsonObject;
import org.bukkit.craftbukkit.v1_8_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class FancyMessage {

	private  List<MessagePart> messageParts;
	private MessagePart latest;
	
	
	public FancyMessage(String firstPartText) {
		messageParts = new ArrayList<MessagePart>();
		messageParts.add(latest = new MessagePart(firstPartText));
	}

	/**
	 * Add a color to the last piece of text
	 * @param color The color
	 */
	public FancyMessage color( ChatColor color) {
		if (!color.isColor()) {
			throw new IllegalArgumentException(color.name() + " is not a color");
		}
		latest.setColor(color);
		return this;
	}

	/**
	 * Add one or more styles to the last bit of text
	 * @param styles The styles
	 */
	public FancyMessage style( ChatColor... styles) {
		for ( ChatColor style : styles) {
			if (!style.isFormat()) {
				throw new IllegalArgumentException(style.name()
						+ " is not a style");
			}
		}
		latest.setStyles(styles);
		return this;
	}

	//TODO
	public FancyMessage file(String path) {
		onClick("open_file", path);
		return this;
	}

	/**
	 * Clicking the last piece of text will open the URL
	 * @param url The URL
	 */
	public FancyMessage linkURL(String url) {
		onClick("open_url", url);
		return this;
	}

	/**
	 * Suggest a command to be run (this will be put in their chat bar)
	 * @param command The command
	 */
	public FancyMessage suggestCommand(String command) {
		onClick("suggest_command", command);
		return this;
	}

	/**
	 * Run a command on clicking the last bit of text
	 * @param command The command (With /)
	 */
	public FancyMessage runCommand(String command) {
		onClick("run_command", command);
		return this;
	}

	/**
	 * Add an achievement tooltip to the last piece of text
	 * @param a The achievement
	 */
	public FancyMessage achievementTooltip(Achievement a) {
		onHover("show_achievement", "achievement." + a.name());
		return this;
	}
	
	//TODO
	public FancyMessage itemTooltip(String itemJSON) {
		onHover("show_item", itemJSON);
		return this;
	}
	
	/**
	 * Create a ToolTip for an item
	 * @param itemStack The item
	 */
	public FancyMessage itemTooltip(ItemStack itemStack) {
		String s = CraftItemStack.asNMSCopy(itemStack).save(new NBTTagCompound()).toString();
		System.out.println(s);
		return itemTooltip(CraftItemStack.asNMSCopy(itemStack).save(new NBTTagCompound()).toString());
	}

	/**
	 * Add a tooltip to the last piece of text
	 * @param text The text in the tooltip
	 */
	public FancyMessage tooltip(String text) {
		onHover("show_text", text);
		return this;
	}

	/**
	 * Add a new piece of text.
	 * This text has no styles, no colors, nothing.
	 * @param String The text
	 */
	public FancyMessage addText(String text) {
		messageParts.add(latest = new MessagePart(text));
		return this;
	}

	/**
	 * Format the message to JSON
	 * @return The json string
	 */
	public String toJSONString() {
		JsonObject object = new JsonObject();
		object.addProperty("text", "");
		JsonArray array = new JsonArray();
		for (MessagePart part : messageParts) {
			part.addJson(array);
		}
		object.add("extra", array);
		return object.toString();
	}

	private void onClick(String name,  String data) {
		latest.setClickActionName(name);
		latest.setClickActionData(data);
	}

	private void onHover(String name,  String data) {
		latest.setHoverActionName(name);
		latest.setHoverActionData(data);
	}

}
