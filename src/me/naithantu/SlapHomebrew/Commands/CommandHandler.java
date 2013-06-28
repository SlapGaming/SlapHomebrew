package me.naithantu.SlapHomebrew.Commands;

import me.naithantu.SlapHomebrew.Lottery;
import me.naithantu.SlapHomebrew.SlapHomebrew;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class CommandHandler {
	SlapHomebrew plugin;
	Lottery lottery;

	public CommandHandler(SlapHomebrew plugin, Lottery lottery) {
		this.plugin = plugin;
		this.lottery = lottery;
	}

	public boolean handle(CommandSender sender, Command cmd, String[] args) {
		String command = cmd.getName().toLowerCase();
		AbstractCommand commandObj = null;
		if (command.equals("backdeath")) {
			commandObj = new BackdeathCommand(sender, args, plugin);
		} else if (command.equals("blockfaq")) {
			commandObj = new BlockfaqCommand(sender, args, plugin);
		} else if (command.equals("boat")) {
			commandObj = new BoatCommand(sender, args, plugin);
		} else if (command.equals("bumpdone")) {
			commandObj = new BumpdoneCommand(sender, args, plugin);
		} else if (command.equals("cakedefence")) {
			commandObj = new CakedefenceCommand(sender, args, plugin);
		} else if (command.equals("creativeextra")) {
			commandObj = new CreativeextraCommand(sender, args, plugin);
		} else if (command.equals("group")) {
			commandObj = new GroupCommand(sender, args, plugin);
		} else if (command.equals("leavecake")) {
			commandObj = new LeavecakeCommand(sender, args, plugin);
		} else if (command.equals("message")) {
			commandObj = new MessageCommand(sender, args, plugin);
		} else if (command.equals("minecart")) {
			commandObj = new MinecartCommand(sender, args, plugin);
		} else if (command.equals("mobcheck")) {
			commandObj = new MobcheckCommand(sender, args, plugin);
		} else if (command.equals("note")) {
			commandObj = new NoteCommand(sender, args, plugin);
		} else if (command.equals("pay")) {
			commandObj = new PayCommand(sender, args, plugin);
		} else if (command.equals("potion")) {
			commandObj = new PotionCommand(sender, args, plugin);
		} else if (command.equals("rainbow")) {
			commandObj = new RainbowCommand(sender, args, plugin);
		} else if (command.equals("ride")) {
			commandObj = new RideCommand(sender, args, plugin);
		} else if (command.equals("roll")) {
			commandObj = new RollCommand(sender, args, plugin, lottery);
		} else if (command.equals("searchregion")) {
			commandObj = new SearchregionCommand(sender, args, plugin);
		} else if (command.equals("sgm")) {
			commandObj = new SgmCommand(sender, args, plugin);
		} else if (command.equals("sonic")) {
			commandObj = new SonicCommand(sender, args, plugin);
		} else if (command.equals("sparta")) {
			commandObj = new SpartaCommand(sender, args, plugin);
		} else if (command.equals("te")) {
			commandObj = new TeCommand(sender, args, plugin);
		} else if (command.equals("tpallow")) {
			commandObj = new TpallowCommand(sender, args, plugin);
		} else if (command.equals("tpblock")) {
			commandObj = new TpBlockCommand(sender, args, plugin);
		} else if (command.equals("vip")) {
			//TODO Remove the plugin.get stuff, just pass it through the constructor.
			if (args.length == 0) {
				commandObj = new VipCommand(sender, args, plugin, plugin.getVipStorage(), plugin.getVip());
			} else {
				String arg = args[0].toLowerCase();
				if (arg.equals("check")) {
					commandObj = new VipForumCheckCommand(sender, args, plugin);
				} else if (arg.equals("done")) {
					commandObj = new VipForumDoneCommand(sender, args, plugin);
				} else if (arg.equals("mark")) {
					commandObj = new VipForumMarkCommand(sender, args, plugin);
				} else {
					commandObj = new VipCommand(sender, args, plugin, plugin.getVipStorage(), plugin.getVip());
				}
			}
		} else if (command.equals("warpcakedefence")) {
			commandObj = new WarpcakedefenceCommand(sender, args, plugin);
		} else if (command.equals("warppvp")) {
			commandObj = new WarppvpCommand(sender, args, plugin);
		} else if (command.equals("warpsonic")) {
			commandObj = new WarpsonicCommand(sender, args, plugin);
		} else if (command.equals("world")) {
			commandObj = new WorldCommand(sender, args, plugin);
		} else if (command.equals("slap")) {
			commandObj = new SlapCommand(sender, args, plugin);
		} else if (command.equals("plot")) {
			if (args.length == 0)
				return false;
			String arg = args[0].toLowerCase();
			if (arg.equals("check")) {
				commandObj = new PlotcheckCommand(sender, args, plugin);
			} else if (arg.equals("done")) {
				commandObj = new PlotdoneCommand(sender, args, plugin);
			} else if (arg.equals("mark")) {
				commandObj = new PlotmarkCommand(sender, args, plugin);
			} else if (arg.equals("tp")) {
				commandObj = new PlottpCommand(sender, args, plugin);
			}
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