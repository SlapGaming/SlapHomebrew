package me.naithantu.SlapHomebrew.Commands;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.MemorySection;

public class MessageCommand extends AbstractCommand {
	SlapHomebrew plugin;

	boolean allowMessage;
	public static String messageName;
	
	public MessageCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args);
		this.plugin = plugin;
	}

	public boolean handle() {
		if (!testPermission(sender, "message")) {
			this.noPermission(sender);
			return true;
		}

		String arg;
		if (args.length > 0) {
			arg = args[0];
			if (arg.equalsIgnoreCase("show")) {
				String message;
				if (args.length == 2) {
					arg = args[1];
					if (!plugin.getConfig().contains("messages." + arg)) {
						this.badMsg(sender, "That message does not exist...");
						return true;
					}
					message = plugin.getConfig().getString("messages." + arg);
					message = ChatColor.translateAlternateColorCodes('&', message);
					this.msg(sender, message);
				} else {
					this.badMsg(sender, "Usage: /message show [message]");
				}
			} else if (arg.equalsIgnoreCase("list")) {
				if (plugin.getConfig().contains("messages")) {
					this.msg(sender, "Messages: " + ((MemorySection) plugin.getConfig().get("messages")).getKeys(true).toString());
				} else {
					this.badMsg(sender, "There are no messages! Type /message create [messagename] to create one!");
					return true;
				}
			} else if (arg.equalsIgnoreCase("create")) {
				if (!testPermission(sender, "message.admin")) {
					this.noPermission(sender);
					return true;
				}

				if (args.length == 2) {
					arg = args[1];
					SlapHomebrew.message.add(sender.getName());
					messageName = arg;
					this.msg(sender, "Type the message now, the name of this message is going to be: " + arg);
				} else {
					this.badMsg(sender, "Usage: /message create [message]");
					return true;
				}
			} else if (arg.equalsIgnoreCase("remove")) {
				if (!testPermission(sender, "message.admin")) {
					this.noPermission(sender);
					return true;
				}

				if (args.length == 2) {
					arg = args[1];
					if (plugin.getConfig().getString("messages." + arg) != null) {
						plugin.getConfig().set("messages." + arg, null);
						this.msg(sender, "Succesfully removed message " + arg);
						return true;
					} else {
						this.msg(sender, "Error: That message does not exist. Type /message list for all messages.");
						return true;
					}
				} else {
					this.badMsg(sender, "Usage: /message remove [message]");
					return true;
				}
			} else {
				String message;
				if (!plugin.getConfig().contains("messages." + arg)) {
					this.badMsg(sender, "That message does not exist...");
					return true;
				}

				message = plugin.getConfig().getString("messages." + arg);
				if (allowMessage == false) {
					this.badMsg(sender, "Error: You are not allowed to use a message now. Try again in a second.");
					return true;
				}
				message = ChatColor.translateAlternateColorCodes('&', message);
				plugin.getServer().broadcastMessage(message);
				allowMessage = false;
				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
					public void run() {
						allowMessage = true;
					}
				}, 20);
			}
		} else {
			return false;
		}
		return true;
	}
}
