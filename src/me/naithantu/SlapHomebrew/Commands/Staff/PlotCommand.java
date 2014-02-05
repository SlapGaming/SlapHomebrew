package me.naithantu.SlapHomebrew.Commands.Staff;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.UsageException;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogging.PlotControl;
import me.naithantu.SlapHomebrew.Util.Util;

public class PlotCommand extends AbstractCommand {

	public PlotCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public boolean handle() throws CommandException {
		Player p = getPlayer();
		testPermission("plot.mod");
		if (args.length == 0) return false; //Usage
		
		PlotControl pc = PlotControl.getInstance(); //Get PlotControl -> Throws commandException if not enabled
		int[] ids;
		String comment = null;
		
		switch (args[0].toLowerCase()) {
		case "check": case "c": //Check modreqs
			if (args.length == 1) { //Getting unfinished plot marks
				pc.getUnfinishedPlotMarks(p);
			} else { //Get 1 plotmark
				ids = parseID(args[1]);
				if (ids.length == 1) { //Only ID given
					pc.getPlotMarkInfo(p, ids[0]);
				} else { //Both ID & Iteration given
					pc.getPlotMarkInfo(p, ids[0], ids[1]);
				}
			}
			break;
			
		case "mark": case "m": //Mark a plot location
			if (args.length > 1) { //If comment given
				comment = Util.buildString(args, " ", 1);
			}
			pc.markPlotLocation(p, comment); //Mark
			break;
		
		case "teleport": case "tp": //Teleport to a plot mark
			if (args.length == 1) throw new UsageException("plot teleport [ID]"); //Usage
			ids = parseID(args[1]);
			if (ids.length == 1) { //Only ID given
				pc.tpToPlot(p, ids[0]);
			} else { //Both ID & Iteration given
				pc.tpToPlot(p, ids[0], ids[1]);
			}
			break;
			
		case "done": case "finish": case "d": case "f": //Finish a plot mark
			testPermission("plot.admin");
			if (args.length == 1) throw new UsageException("plot finish [ID] <Comment..>"); //Usage
			ids = parseID(args[1]);
			if (args.length > 2) { //If comment
				comment = Util.buildString(args, " ", 2);
			}
			if (ids.length == 1) { //Only ID Given
				pc.finishPlotMark(p, ids[0], comment);
			} else { //Both ID & Iteration given
				pc.finishPlotMark(p, ids[0], ids[1], comment);
			}
			break;
						
		default:
			return false;
		}
		return true;
	}
	
	/**
	 * Parse the ID
	 * This can be 2 Formats:
	 *   #[ID]
	 *   #[Iteration].[ID]
	 * @param arg The ID
	 * @return an array of the ints
	 * @throws CommandException if not a valid ID
	 */
	public static int[] parseID(String arg) throws CommandException {
		arg = arg.replace(".", "-").replace("#", ""); //Replace . with - (Split breaks on .) && Remove #
		if (arg.matches("\\d+-\\d+")) {
			String[] split = arg.split("-");
			return new int[]{
				parseIntPositive(split[0]),
				parseIntPositive(split[1])
			};
		} else {
			return new int[]{
				parseIntPositive(arg)
			};
		}
	}

}
