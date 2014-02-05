package me.naithantu.SlapHomebrew.Commands.Staff;

import org.bukkit.command.CommandSender;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.UsageException;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogging.VipForumControl;
import me.naithantu.SlapHomebrew.Util.Util;

public class VipForumCommand extends AbstractCommand {

	public VipForumCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public boolean handle() throws CommandException {
		testPermission("vipforum");
		if (args.length == 0) return false;
		
		VipForumControl vfc = VipForumControl.getInstance(); //Get instance, throws Exception if not enabled
		
		switch (args[0].toLowerCase()) {
		case "check": case "c": //Check for promotions
			vfc.sendPendingPromotions(sender);
			break;
			
		case "done": case "d": case "f": case "finish": //Finish a forum promotion
			if (args.length == 1) throw new UsageException("vipforum finish [ID] <Comment>"); //Usage
			int[] ids = PlotCommand.parseID(args[1]); //Parse IDs
			String comment = null;
			if (args.length > 2) { //Has comment
				comment = Util.buildString(args, " ", 2); //Build comment
			}
			if (ids.length == 1) { //If only ID given
				vfc.finishPendingPromotion(ids[0], sender.getName(), comment);
			} else { //Iteration + ID given
				vfc.finishPendingPromotion(ids[0], ids[1], sender.getName(), comment);
			}
			hMsg("Forum Promotion done.");
			break;
			
		default: //Usage
			return false;
		}
		return true;
	}

}
