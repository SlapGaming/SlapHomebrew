package me.naithantu.SlapHomebrew.Commands.Staff;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Controllers.MessageFactory;
import me.naithantu.SlapHomebrew.Storage.YamlStorage;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;

public class MessageCommand extends AbstractCommand {
	boolean allowMessage = true;

    private YamlStorage messageStorage;
    private FileConfiguration messageConfig;
	
	public MessageCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
        messageStorage = plugin.getMessageStorage();
        messageConfig = messageStorage.getConfig();
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
					if (!messageConfig.contains("messages." + arg)) {
						this.badMsg(sender, "That message does not exist...");
						return true;
					}
					message = messageConfig.getString("messages." + arg);
					message = ChatColor.translateAlternateColorCodes('&', message);
					sender.sendMessage(message);
				} else {
					this.badMsg(sender, "Usage: /message show [message]");
				}
			} else if (arg.equalsIgnoreCase("list")) {
				if (messageConfig.contains("messages")) {
					this.msg(sender, "Messages: " + ((MemorySection) messageConfig.get("messages")).getKeys(true).toString());
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
                    plugin.getMessages().getMessagePlayers().put(sender.getName(), new MessageFactory(args[1]));
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
					if (messageConfig.getString("messages." + arg) != null) {
						messageConfig.set("messages." + arg, null);
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
			} else if (arg.equalsIgnoreCase("reload")) {
                if (!testPermission(sender, "message.admin")) {
                    this.noPermission(sender);
                    return true;
                }

                messageStorage.reloadConfig();
                this.msg(sender, "Reloaded message config!");
            } else {
				String message;
				if (!messageConfig.contains("messages." + arg)) {
					this.badMsg(sender, "That message does not exist...");
					return true;
				}

				message = messageConfig.getString("messages." + arg);
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
