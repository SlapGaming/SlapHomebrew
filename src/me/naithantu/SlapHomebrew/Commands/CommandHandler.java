package me.naithantu.SlapHomebrew.Commands;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.Staff.*;
import me.naithantu.SlapHomebrew.Commands.Staff.Plot.*;
import me.naithantu.SlapHomebrew.Commands.Staff.VIP.*;
import me.naithantu.SlapHomebrew.Commands.VIP.*;
import me.naithantu.SlapHomebrew.Commands.AFK.*;
import me.naithantu.SlapHomebrew.Commands.Basics.*;
import me.naithantu.SlapHomebrew.Commands.Fun.*;
import me.naithantu.SlapHomebrew.Commands.Games.*;
import me.naithantu.SlapHomebrew.Commands.Jail.*;
import me.naithantu.SlapHomebrew.Commands.Lists.*;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class CommandHandler {
	
	private SlapHomebrew plugin;

	public CommandHandler(SlapHomebrew plugin) {
		this.plugin = plugin;
	}

	public boolean handle(CommandSender sender, Command cmd, String[] args) {
		String command = cmd.getName().toLowerCase();
		AbstractCommand commandObj = null;
		switch (command) {
		case "backdeath": 		commandObj = new BackdeathCommand(sender, args, plugin); 			break;
		case "blockfaq":		commandObj = new BlockfaqCommand(sender, args, plugin);				break;
		case "boat":			commandObj = new BoatCommand(sender, args, plugin);					break;
		case "bumpdone":		commandObj = new BumpdoneCommand(sender, args, plugin);				break;
		case "cakedefence":		commandObj = new CakedefenceCommand(sender, args, plugin);			break;
		case "creativeextra":	commandObj = new CreativeextraCommand(sender, args, plugin);		break;
		case "ghostfix":		commandObj = new GhostfixCommand(sender, args, plugin);				break;
		case "group":			commandObj = new GroupCommand(sender, args, plugin);				break;
		case "home":			commandObj = new HomeCommand(sender, args, plugin);					break;
		case "homemenu":		commandObj = new HomeMenuCommand(sender, args, plugin);				break;
		case "lag":				commandObj = new LagCommand(sender, args, plugin);					break;
		case "leavecake":		commandObj = new LeavecakeCommand(sender, args, plugin);			break;
		case "leave":			commandObj = new LeaveCommand(sender, args, plugin);				break;
		case "message":			commandObj = new MessageCommand(sender, args, plugin);				break;
		case "minecart":		commandObj = new MinecartCommand(sender, args, plugin);				break;
		case "mobcheck":		commandObj = new MobcheckCommand(sender, args, plugin);				break;
		case "note":			commandObj = new NoteCommand(sender, args, plugin);					break;
		case "pay":				commandObj = new PayCommand(sender, args, plugin);					break;
		case "potion":			commandObj = new PotionCommand(sender, args, plugin);				break;
		case "rainbow":			commandObj = new RainbowCommand(sender, args, plugin);				break;
		case "ride":			commandObj = new RideCommand(sender, args, plugin);					break;
		case "roll":			commandObj = new RollCommand(sender, args, plugin);					break;
		case "searchregion":	commandObj = new SearchregionCommand(sender, args, plugin);			break;
		case "sgm":				commandObj = new SgmCommand(sender, args, plugin);					break;
		case "slap":			commandObj = new SlapCommand(sender, args, plugin);					break;
		case "sonic":			commandObj = new SonicCommand(sender, args, plugin);				break;
		case "sparta":			commandObj = new SpartaCommand(sender, args, plugin);				break;
		case "te":				commandObj = new TeCommand(sender, args, plugin);					break;
		case "tpallow":			commandObj = new TpallowCommand(sender, args, plugin);				break;
		case "tpblock":			commandObj = new TpBlockCommand(sender, args, plugin);				break;
		case "warpcakedefence":	commandObj = new WarpcakedefenceCommand(sender, args, plugin);		break;
		case "warppvp":			commandObj = new WarppvpCommand(sender, args, plugin);				break;
		case "warpsonic":		commandObj = new WarpsonicCommand(sender, args, plugin);			break;
		case "world":			commandObj = new WorldCommand(sender, args, plugin);				break;
		case "afk":				commandObj = new AfkCommand(sender, args, plugin);					break;
		case "afkreset":		commandObj = new AfkResetCommand(sender, args, plugin);				break;
		case "spawn":			commandObj = new SpawnCommand(sender, args, plugin);				break;
		case "horse":			commandObj = new HorseCommand(sender, args, plugin);				break;
		case "changelog":		commandObj = new ChangeLogCommand(sender, args, plugin);			break;
		case "stafflist":		commandObj = new StaffListCommand(sender, args, plugin);			break;
		case "mail":			commandObj = new MailCommand(sender, args, plugin);					break;
		case "jail":			commandObj = new JailCommand(sender, args, plugin);					break;
		case "jails":			commandObj = new JailCommand(sender, new String[]{"list"}, plugin); break;
		case "unjail":			commandObj = new UnjailCommand(sender, args, plugin);				break;
		case "xray":			commandObj = new XRayCommand(sender, args, plugin);					break;
		case "list":			commandObj = new ListCommand(sender, args, plugin);					break;
		case "fireworkshow":	commandObj = new FireworkCommand(sender, args, plugin);				break;
		case "timecheck":		commandObj = new TimecheckCommand(sender, args, plugin);			break;
		case "spromote": 		commandObj = new SPromoteCommand(sender, args, plugin);				break;
		case "sdemote":			commandObj = new SDemoteCommand(sender, args, plugin); 				break;
		case "worldguards":		commandObj = new WorldguardsCommand(sender, args, plugin);			break;
		case "worthlist":		commandObj = new WorthListCommand(sender, args, plugin);			break;
		case "skick":			commandObj = new SKickCommand(sender, args, plugin);				break;
		case "splugins":		commandObj = new PluginsCommand(sender, args, plugin);				break;
		case "plot":
			if (args.length == 0) return false;
			switch (args[0].toLowerCase()) {
			case "check": 		commandObj = new PlotcheckCommand(sender, args, plugin); 			break;
			case "done": 		commandObj = new PlotdoneCommand(sender, args, plugin); 			break;
			case "mark": 		commandObj = new PlotmarkCommand(sender, args, plugin); 			break;
			case "tp": 			commandObj = new PlottpCommand(sender, args, plugin); 				break;
			}
			break;
		case "vip":			
			if (args.length == 0) {
				commandObj = new VipCommand(sender, args, plugin);
			} else {
				switch (args[0].toLowerCase()) {
				case "check": 	commandObj = new VipForumCheckCommand(sender, args, plugin); 		break;
				case "done": 	commandObj = new VipForumDoneCommand(sender, args, plugin); 		break;
				case "mark": 	commandObj = new VipForumMarkCommand(sender, args, plugin); 		break;
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