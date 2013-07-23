package me.naithantu.SlapHomebrew.Commands;

import me.naithantu.SlapHomebrew.Lag;
import me.naithantu.SlapHomebrew.Lottery;
import me.naithantu.SlapHomebrew.SlapHomebrew;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class CommandHandler {
	SlapHomebrew plugin;
	Lottery lottery;
	Lag lag;

	public CommandHandler(SlapHomebrew plugin, Lottery lottery, Lag lag) {
		this.plugin = plugin;
		this.lottery = lottery;
		this.lag = lag;
	}

	public boolean handle(CommandSender sender, Command cmd, String[] args) {
		String command = cmd.getName().toLowerCase();
		AbstractCommand commandObj = null;
		switch (command) {
		case "backdeath": 		commandObj = new BackdeathCommand(sender, args, plugin); 		break;
		case "blockfaq":		commandObj = new BlockfaqCommand(sender, args, plugin);			break;
		case "boat":			commandObj = new BoatCommand(sender, args, plugin);				break;
		case "bumpdone":		commandObj = new BumpdoneCommand(sender, args, plugin);			break;
		case "cakedefence":		commandObj = new CakedefenceCommand(sender, args, plugin);		break;
		case "creativeextra":	commandObj = new CreativeextraCommand(sender, args, plugin);	break;
		case "ghostfix":		commandObj = new GhostfixCommand(sender, args, plugin);			break;
		case "group":			commandObj = new GroupCommand(sender, args, plugin);			break;
		case "home":			commandObj = new HomeCommand(sender, args, plugin);				break;
		case "homemenu":		commandObj = new HomeMenuCommand(sender, args, plugin);			break;
		case "lag":				commandObj = new LagCommand(sender, args, plugin, lag);			break;
		case "leavecake":		commandObj = new LeavecakeCommand(sender, args, plugin);		break;
		case "message":			commandObj = new MessageCommand(sender, args, plugin);			break;
		case "minecart":		commandObj = new MinecartCommand(sender, args, plugin);			break;
		case "mobcheck":		commandObj = new MobcheckCommand(sender, args, plugin);			break;
		case "note":			commandObj = new NoteCommand(sender, args, plugin);				break;
		case "pay":				commandObj = new PayCommand(sender, args, plugin);				break;
		case "potion":			commandObj = new PotionCommand(sender, args, plugin);			break;
		case "rainbow":			commandObj = new RainbowCommand(sender, args, plugin);			break;
		case "ride":			commandObj = new RideCommand(sender, args, plugin);				break;
		case "roll":			commandObj = new RollCommand(sender, args, plugin, lottery);	break;
		case "searchregion":	commandObj = new SearchregionCommand(sender, args, plugin);		break;
		case "sgm":				commandObj = new SgmCommand(sender, args, plugin);				break;
		case "slap":			commandObj = new SlapCommand(sender, args, plugin, lottery);	break;
		case "sonic":			commandObj = new SonicCommand(sender, args, plugin);			break;
		case "sparta":			commandObj = new SpartaCommand(sender, args, plugin);			break;
		case "te":				commandObj = new TeCommand(sender, args, plugin);				break;
		case "tpallow":			commandObj = new TpallowCommand(sender, args, plugin);			break;
		case "tpblock":			commandObj = new TpBlockCommand(sender, args, plugin);			break;
		case "warpcakedefence":	commandObj = new WarpcakedefenceCommand(sender, args, plugin);	break;
		case "warppvp":			commandObj = new WarppvpCommand(sender, args, plugin);			break;
		case "warpsonic":		commandObj = new WarpsonicCommand(sender, args, plugin);		break;
		case "world":			commandObj = new WorldCommand(sender, args, plugin);			break;
		case "afk":				commandObj = new AfkCommand(sender, args, plugin);				break;
		case "afkreset":		commandObj = new AfkResetCommand(sender, args, plugin);			break;
		case "spawn":			commandObj = new SpawnCommand(sender, args, plugin);			break;
		case "horse":			commandObj = new HorseCommand(sender, args, plugin);			break;
		case "changelog":		commandObj = new ChangeLogCommand(sender, args, plugin);		break;
		case "stafflist":		commandObj = new StaffListCommand(sender, args, plugin);		break;
		case "plot":
			if (args.length == 0) return false;
			switch (args[0].toLowerCase()) {
			case "check": 		commandObj = new PlotcheckCommand(sender, args, plugin); 		break;
			case "done": 		commandObj = new PlotdoneCommand(sender, args, plugin); 		break;
			case "mark": 		commandObj = new PlotmarkCommand(sender, args, plugin); 		break;
			case "tp": 			commandObj = new PlottpCommand(sender, args, plugin); 			break;
			}
			break;
		case "vip":			
			if (args.length == 0) {
				commandObj = new VipCommand(sender, args, plugin);
			} else {
				switch (args[0].toLowerCase()) {
				case "check": 	commandObj = new VipForumCheckCommand(sender, args, plugin); 	break;
				case "done": 	commandObj = new VipForumDoneCommand(sender, args, plugin); 	break;
				case "mark": 	commandObj = new VipForumMarkCommand(sender, args, plugin); 	break;
				default: 		commandObj = new VipCommand(sender, args, plugin);
				}
			}
			break;
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