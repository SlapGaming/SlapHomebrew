package me.naithantu.SlapHomebrew.Commands;

import me.naithantu.SlapHomebrew.Commands.AFK.AfkCommand;
import me.naithantu.SlapHomebrew.Commands.AFK.AfkInfoCommand;
import me.naithantu.SlapHomebrew.Commands.AFK.SemiAFKCommand;
import me.naithantu.SlapHomebrew.Commands.Basics.*;
import me.naithantu.SlapHomebrew.Commands.Chat.*;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.NoMessageException;
import me.naithantu.SlapHomebrew.Commands.Fun.*;
import me.naithantu.SlapHomebrew.Commands.Games.LeaveCommand;
import me.naithantu.SlapHomebrew.Commands.Homes.*;
import me.naithantu.SlapHomebrew.Commands.Jail.JailCommand;
import me.naithantu.SlapHomebrew.Commands.Jail.UnjailCommand;
import me.naithantu.SlapHomebrew.Commands.Lists.*;
import me.naithantu.SlapHomebrew.Commands.Promotion.PromotionCommand;
import me.naithantu.SlapHomebrew.Commands.Staff.*;
import me.naithantu.SlapHomebrew.Commands.Stats.DeathsCommand;
import me.naithantu.SlapHomebrew.Commands.Stats.KillsCommand;
import me.naithantu.SlapHomebrew.Commands.Stats.OnlineTimeCommand;
import me.naithantu.SlapHomebrew.Commands.Teleport.*;
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
		case "admin":			commandObj = new ChatCommand(sender, "adminchat", args);		break;
		case "afk":				commandObj = new AfkCommand(sender, args);						break;
		case "afkinfo":			commandObj = new AfkInfoCommand(sender, args);					break;
		case "afklist":			commandObj = new AfkInfoCommand(sender, new String[]{"list"});	break;
		case "back":			commandObj = new BackCommand(sender, args);						break;
		case "backdeath": 		commandObj = new BackdeathCommand(sender, args); 				break;
		case "boat":			commandObj = new BoatCommand(sender, args);						break;
		case "bumpdone":		commandObj = new BumpdoneCommand(sender, args);					break;
		case "changelog":		commandObj = new ChangeLogCommand(sender, args);				break;
		case "chattoggle":		commandObj = new ChatToggleCommand(sender, args);				break;
		case "creativeextra":	commandObj = new CreativeextraCommand(sender, args);			break;
		case "deaths":			commandObj = new DeathsCommand(sender, args);					break;
        case "emptybucket":     commandObj = new EmptyBucketCommand(sender, args);              break;
		case "fancymessage":	commandObj = new FancyChatCommand(sender, args);				break;
		case "fireworkshow":	commandObj = new FireworkCommand(sender, args);					break;
		case "flyspeed":		commandObj = new FlySpeedCommand(sender, args);					break;
		case "group":			commandObj = new GroupCommand(sender, args);					break;
		case "guide":			commandObj = new ChatCommand(sender, "guidechat", args);		break;
		case "head":			commandObj = new HeadCommand(sender, args);						break;
		case "home":			commandObj = new HomeCommand(sender, args);						break;
		case "homemenu":		commandObj = new HomeMenuCommand(sender, args);					break;
		case "homeother":		commandObj = new HomeOtherCommand(sender, args);				break;
		case "homes":			commandObj = new HomesCommand(sender, args);					break;
		case "horse":			commandObj = new HorseCommand(sender, args);					break;
		case "improvedregion":	commandObj = new ImprovedRegionCommand(sender, args);			break;
		case "jail":			commandObj = new JailCommand(sender, args);						break;
		case "jails":			commandObj = new JailCommand(sender, new String[]{"list"}); 	break;
		case "kills":			commandObj = new KillsCommand(sender, args);					break;
		case "leave":			commandObj = new LeaveCommand(sender, args);					break;
		case "links":			commandObj = new LinksCommand(sender, args);					break;
		case "list":			commandObj = new ListCommand(sender, args);						break;
		case "mail":			commandObj = new MailCommand(sender, args);						break;
		case "me":				commandObj = new MeCommand(sender, args);						break;
		case "mention":			commandObj = new MentionCommand(sender, args);					break;
		case "message":			commandObj = new MessageCommand(sender, args);					break;
		case "minecart":		commandObj = new MinecartCommand(sender, args);					break;
		case "mobcheck":		commandObj = new MobcheckCommand(sender, args);					break;
		case "mod":				commandObj = new ChatCommand(sender, "modchat", args);			break;
		case "msg":				commandObj = new MsgCommand(sender, args);						break;
        case "mute":            commandObj = new MuteCommand(sender, args);                     break;
        case "names":			commandObj = new NamesCommand(sender, args);				    break;
		case "note":			commandObj = new NoteCommand(sender, args);						break;
		case "onlinetime":		commandObj = new OnlineTimeCommand(sender, args);				break;
		case "pay":				commandObj = new PayCommand(sender, args);						break;
		case "ping":			commandObj = new PingCommand(sender, args);						break;
		case "plot":			commandObj = new PlotCommand(sender, args);						break;
		case "potato":			commandObj = new ChatCommand(sender, "potatochat", args);		break;
		case "potion":			commandObj = new PotionCommand(sender, args);					break;
        case "profiler":        commandObj = new ProfilerCommand(sender, args);                 break;
		case "promotion":		commandObj = new PromotionCommand(sender, args);				break;
		case "ragequit":		commandObj = new RageQuitCommand(sender, args);					break;
		case "rainbow":			commandObj = new RainbowCommand(sender, args);					break;
		case "reply":			commandObj = new ReplyCommand(sender, args);					break;
		case "ride":			commandObj = new RideCommand(sender, args);						break;
		case "roll":			commandObj = new RollCommand(sender, args);						break;
		case "searchregion":	commandObj = new SearchregionCommand(sender, args);				break;
        case "semiafk":         commandObj = new SemiAFKCommand(sender, args);                  break;
		case "serverbroadcast":	commandObj = new ServerBroadcastCommand(sender, args);			break;
		case "sethome":			commandObj = new SetHomeCommand(sender, args);					break;
		case "sgm":				commandObj = new SgmCommand(sender, args);						break;
		case "skick":			commandObj = new SKickCommand(sender, args);					break;
		case "slap":			commandObj = new SlapCommand(sender, args);						break;
		case "sparta":			commandObj = new SpartaCommand(sender, args);					break;
		case "spartapads":		commandObj = new SpartaPadsCommand(sender, args);				break;
		case "spawn":			commandObj = new SpawnCommand(sender, args);					break;
		case "splugins":		commandObj = new PluginsCommand(sender, args);					break;
		case "stafflist":		commandObj = new StaffListCommand(sender, args);				break;
		case "suicide":			commandObj = new SuicideCommand(sender, args);					break;
		case "te":				commandObj = new TeCommand(sender, args);						break;
		case "teleportmob":		commandObj = new TeleportMobCommand(sender, args);				break;
		case "timecheck":		commandObj = new TimecheckCommand(sender, args);				break;
		case "tp":				commandObj = new TeleportCommand(sender, args);					break;
		case "tpaccept":		commandObj = new TeleportAcceptCommand(sender, args);			break;
		case "tpallow":			commandObj = new TpallowCommand(sender, args);					break;
		case "tpask":			commandObj = new TeleportAskCommand(sender, args);				break;
		case "tpaskhere": 		commandObj = new TeleportAskHereCommand(sender, args);			break;
		case "tpblock":			commandObj = new TpBlockCommand(sender, args);					break;
		case "tpcancel":		commandObj = new TeleportCancelCommand(sender, args);			break;
		case "tpdeny":			commandObj = new TeleportDenyCommand(sender, args);				break;
		case "tphere":			commandObj = new TeleportHereCommand(sender, args);				break;
		case "tprequests":		commandObj = new TeleportRequests(sender, args);				break;		
		case "unjail":			commandObj = new UnjailCommand(sender, args);					break;
        case "unmute":          commandObj = new UnmuteCommand(sender, args);                   break;
		case "vip":				commandObj = new VipCommand(sender, args);						break;
		case "vipforum":		commandObj = new VipForumCommand(sender, args);					break;
		case "wave":			commandObj = new WaveCommand(sender, args);						break;
		case "whitelist":		commandObj = new WhitelistCommand(sender, args);				break;
		case "world":			commandObj = new WorldCommand(sender, args);					break;
		case "worldguards":		commandObj = new WorldguardsCommand(sender, args);				break;
		case "worthlist":		commandObj = new WorthListCommand(sender, args);				break;
		case "xray":			commandObj = new XRayCommand(sender, args);						break;
		}
		
		if (commandObj != null) {
			try {
                boolean handled = commandObj.handle();
                if (!handled) {
                    Util.badMsg(sender, cmd.getUsage());
                }
            } catch (NoMessageException e) {
                //Special kind of CommandException. No message is needed.
			} catch (CommandException e) {
				Util.badMsg(sender, ChatColor.RED + e.getMessage());
			}
		}
		return true;
	}
}