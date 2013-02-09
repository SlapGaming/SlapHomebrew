package me.naithantu.SlapHomebrew.Commands;

import me.naithantu.SlapHomebrew.SlapHomebrew;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class CommandHandler {
	SlapHomebrew plugin;

	public CommandHandler(SlapHomebrew plugin) {
		this.plugin = plugin;
	}

	public boolean handle(CommandSender sender, Command cmd, String[] args) {
		String command = cmd.getName().toLowerCase();
		AbstractCommand commandObj = null;
		if (command.equals("backdeath")) {
			commandObj = new BackdeathCommand(sender, args);
		} else if (command.equals("blockfaq")) {
			commandObj = new BlockfaqCommand(sender, args);
		} else if (command.equals("bumpdone")) {
			commandObj = new BumpdoneCommand(sender, args, plugin);
		} else if (command.equals("cakedefence")) {
			commandObj = new CakedefenceCommand(sender, args, plugin);
		} else if (command.equals("group")) {
			commandObj = new GroupCommand(sender, args);
		} else if (command.equals("leavecake")) {
			commandObj = new LeavecakeCommand(sender, args);
		} else if (command.equals("message")) {
			commandObj = new MessageCommand(sender, args, plugin);
		} else if (command.equals("minecart")) {
			commandObj = new MinecartCommand(sender, args);
		} else if (command.equals("mobcheck")) {
			commandObj = new MobcheckCommand(sender, args);
		} else if (command.equals("note")) {
			commandObj = new NoteCommand(sender, args);
		} else if (command.equals("potion")) {
			commandObj = new PotionCommand(sender, args);
		} else if (command.equals("ride")) {
			commandObj = new RideCommand(sender, args);
		} else if (command.equals("roll")) {
			commandObj = new RollCommand(sender, args, plugin);
		} else if (command.equals("searchregion")) {
			commandObj = new SearchregionCommand(sender, args);
		} else if (command.equals("sgm")) {
			commandObj = new SgmCommand(sender, args);
		} else if (command.equals("te")) {
			commandObj = new TeCommand(sender, args);
		} else if (command.equals("tpallow")) {
			commandObj = new TpallowCommand(sender, args, plugin);
		} else if (command.equals("tpblock")) {
			commandObj = new TpBlockCommand(sender, args);
		} else if (command.equals("vip")) {
			commandObj = new VipCommand(sender, args, plugin);
		} else if (command.equals("warpcakedefence")) {
			commandObj = new WarpcakedefenceCommand(sender, args);
		} else if (command.equals("warppvp")) {
			commandObj = new WarppvpCommand(sender, args);
		}

		if (commandObj != null) {
			boolean handled = commandObj.handle();
			if (!handled) {
				commandObj.badMsg(sender, cmd.getUsage());
			}
		}
		return true;
	}
}