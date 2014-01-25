package me.naithantu.SlapHomebrew.Controllers.FancyMessage;

import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.libs.com.google.gson.JsonArray;
import org.bukkit.craftbukkit.libs.com.google.gson.JsonObject;

final class MessagePart {

	private ChatColor color = null;
	private ChatColor[] styles = null;
	private String 
		clickActionName = null, 
		clickActionData = null,
		hoverActionName = null,
		hoverActionData = null,
		text;

	MessagePart(final String text) {
		this.text = text;
	}

	public void addJson(JsonArray array) {
		JsonObject newObject = new JsonObject();
		
		newObject.addProperty("text", text);
		if (color != null) {
			newObject.addProperty("color", color.name().toLowerCase());
		}
		
		if (styles != null) {
			for (ChatColor style : styles) {
				newObject.addProperty(style.name().toLowerCase(), true);
			}
		}
		
		if (clickActionName != null && clickActionData != null) {
			JsonObject clickObject = new JsonObject();
			clickObject.addProperty("action", clickActionName);
			clickObject.addProperty("value", clickActionData);
			newObject.add("clickEvent", clickObject);
		}
		
		if (hoverActionData != null && hoverActionName != null) {
			JsonObject hoverObject = new JsonObject();
			hoverObject.addProperty("action", hoverActionName);
			//TODO Add support for multiple line yo.
			hoverObject.addProperty("value", hoverActionData);
			newObject.add("hoverEvent", hoverObject);
		}
		
		array.add(newObject);
	}
	
	public void setClickActionData(String clickActionData) {
		this.clickActionData = clickActionData;
	}
	
	public void setClickActionName(String clickActionName) {
		this.clickActionName = clickActionName;
	}
	
	public void setColor(ChatColor color) {
		this.color = color;
	}
	
	public void setHoverActionData(String hoverActionData) {
		this.hoverActionData = hoverActionData;
	}
	
	public void setHoverActionName(String hoverActionName) {
		this.hoverActionName = hoverActionName;
	}
	
	public void setStyles(ChatColor[] styles) {
		this.styles = styles;
	}

}
