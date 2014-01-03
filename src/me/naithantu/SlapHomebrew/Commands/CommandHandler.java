package me.naithantu.SlapHomebrew.Commands;

import me.naithantu.SlapHomebrew.Commands.AFK.*;
import me.naithantu.SlapHomebrew.Commands.Basics.*;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Fun.*;
import me.naithantu.SlapHomebrew.Commands.Games.*;
import me.naithantu.SlapHomebrew.Commands.Jail.*;
import me.naithantu.SlapHomebrew.Commands.Lists.*;
import me.naithantu.SlapHomebrew.Commands.Staff.*;
import me.naithantu.SlapHomebrew.Commands.Staff.Plot.*;
import me.naithantu.SlapHomebrew.Commands.Staff.VIP.*;
import me.naithantu.SlapHomebrew.Commands.VIP.*;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class CommandHandler {
	
	public boolean handle(CommandSender sender, Command cmd, String[] args) {
		String command = cmd.getName().toLowerCase();
		AbstractCommand commandObj = null;
		switch (command) {
		case "backdeath": 		commandObj = new BackdeathCommand(sender, args); 				break;
		case "boat":			commandObj = new BoatCommand(sender, args);						break;
		case "bumpdone":		commandObj = new BumpdoneCommand(sender, args);					break;
		case "creativeextra":	commandObj = new CreativeextraCommand(sender, args);			break;
		case "group":			commandObj = new GroupCommand(sender, args);					break;
		case "home":			commandObj = new HomeCommand(sender, args);						break;
		case "homemenu":		commandObj = new HomeMenuCommand(sender, args);					break;
		case "lag":				commandObj = new LagCommand(sender, args);						break;
		case "leave":			commandObj = new LeaveCommand(sender, args);					break;
		case "message":			commandObj = new MessageCommand(sender, args);					break;
		case "minecart":		commandObj = new MinecartCommand(sender, args);					break;
		case "mobcheck":		commandObj = new MobcheckCommand(sender, args);					break;
		case "note":			commandObj = new NoteCommand(sender, args);						break;
		case "pay":				commandObj = new PayCommand(sender, args);						break;
		case "potion":			commandObj = new PotionCommand(sender, args);					break;
		case "rainbow":			commandObj = new RainbowCommand(sender, args);					break;
		case "ride":			commandObj = new RideCommand(sender, args);						break;
		case "roll":			commandObj = new RollCommand(sender, args);						break;
		case "searchregion":	commandObj = new SearchregionCommand(sender, args);				break;
		case "sgm":				commandObj = new SgmCommand(sender, args);						break;
		case "slap":			commandObj = new SlapCommand(sender, args);						break;
		case "sparta":			commandObj = new SpartaCommand(sender, args);					break;
		case "te":				commandObj = new TeCommand(sender, args);						break;
		case "tpallow":			commandObj = new TpallowCommand(sender, args);					break;
		case "tpblock":			commandObj = new TpBlockCommand(sender, args);					break;
		case "world":			commandObj = new WorldCommand(sender, args);					break;
		case "afk":				commandObj = new AfkCommand(sender, args);						break;
		case "afkinfo":			commandObj = new AfkInfoCommand(sender, args);					break;
		case "afklist":			commandObj = new AfkInfoCommand(sender, new String[]{"list"});	break;
		case "spawn":			commandObj = new SpawnCommand(sender, args);					break;
		case "horse":			commandObj = new HorseCommand(sender, args);					break;
		case "changelog":		commandObj = new ChangeLogCommand(sender, args);				break;
		case "stafflist":		commandObj = new StaffListCommand(sender, args);				break;
		case "mail":			commandObj = new MailCommand(sender, args);						break;
		case "jail":			commandObj = new JailCommand(sender, args);						break;
		case "jails":			commandObj = new JailCommand(sender, new String[]{"list"}); 	break;
		case "unjail":			commandObj = new UnjailCommand(sender, args);					break;
		case "xray":			commandObj = new XRayCommand(sender, args);						break;
		case "list":			commandObj = new ListCommand(sender, args);						break;
		case "fireworkshow":	commandObj = new FireworkCommand(sender, args);					break;
		case "spromote": 		commandObj = new SPromoteCommand(sender, args);					break;
		case "sdemote":			commandObj = new SDemoteCommand(sender, args); 					break;
		case "worldguards":		commandObj = new WorldguardsCommand(sender, args);				break;
		case "worthlist":		commandObj = new WorthListCommand(sender, args);				break;
		case "skick":			commandObj = new SKickCommand(sender, args);					break;
		case "splugins":		commandObj = new PluginsCommand(sender, args);					break;
		case "suicide":			commandObj = new SuicideCommand(sender, args);					break;
	//	case "deaths":			commandObj = new DeathsCommand(sender, args);					break;
	//	case "kills":			commandObj = new KillsCommand(sender, args);					break;
		case "teleportmob":		commandObj = new TeleportMobCommand(sender, args);				break;
		case "improvedregion":	commandObj = new ImprovedRegionCommand(sender, args);			break;
		case "plot":
			if (args.length == 0) return false;
			switch (args[0].toLowerCase()) {
			case "check": 		commandObj = new PlotcheckCommand(sender, args); 				break;
			case "done": 		commandObj = new PlotdoneCommand(sender, args); 				break;
			case "mark": 		commandObj = new PlotmarkCommand(sender, args); 				break;
			case "tp": 			commandObj = new PlottpCommand(sender, args); 					break;
			}
			break;
		case "vip":			
			if (args.length == 0) {
				commandObj = new VipCommand(sender, args);
			} else {
				switch (args[0].toLowerCase()) {
				case "check": 	commandObj = new VipForumCheckCommand(sender, args); 			break;
				case "done": 	commandObj = new VipForumDoneCommand(sender, args); 			break;
				case "mark": 	commandObj = new VipForumMarkCommand(sender, args); 			break;
				default: 		commandObj = new VipCommand(sender, args);
				}
			}
			break;
		}

		if (commandObj != null) {
			try {
				boolean handled = commandObj.handle();
				if (!handled) {
					Util.badMsg(sender, cmd.getUsage());
				}
			} catch (CommandException e) {
				Util.badMsg(sender, ChatColor.RED + e.getMessage());
			}
		}
		return true;
	}
}