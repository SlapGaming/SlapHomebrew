package me.naithantu.SlapHomebrew.Commands.Staff.ImprovedRegion;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.IRGException;
import me.naithantu.SlapHomebrew.Commands.Exception.UsageException;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogging.RegionLogger;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogging.RegionLogger.ChangeType;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogging.RegionLogger.ChangerIsA;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.bukkit.commands.RegionPrintoutBuilder;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class FlagCommand extends AbstractImprovedRegionCommand {

	private boolean allFlags;
	
	/**
	 * Make a new FlagCommand
	 * @param plugin The main plugin
	 * @param p The player
	 * @param args The command parameters
	 * @param allFlags Is allowed to do all flags
	 */
	public FlagCommand(SlapHomebrew plugin, Player p, String[] args, boolean allFlags) {
		super(p, args);
		this.allFlags = allFlags;
	}

	@Override
	protected void action() throws CommandException {
		if (args.length < 4) throw new UsageException("irg flag <Region ID> <Flag> <Flag Parameter(s)>");
		
		validateRegionID(args[1]); //Check the region
		
		//Get the region
		ProtectedRegion region = getRegion(args[1]);
		
		Flag<?> flag;
		
		//Parse flag
		if (!allFlags) { //Not able to do all flags
			flag = getFlag(p, args[2]);
		} else { //Allowed to do all flags
			flag = DefaultFlag.fuzzyMatchFlag(args[2]); //Get fuzzy flag
		}
		
		if (flag == null) throw new IRGException("No flag found (that you are allowed to use). " + ChatColor.GRAY + "Allowed flags: \n" + allowedFlags(allFlags));
		
		String value;
		switch (args[3].toLowerCase()) { //Parse the given value
		case "remove": case "none": case "delete": case "del":
			value = "none";
			break;
		case "allow": case "true": case "a": case "t": case "on":
			value = "allow";
			break;
		case "deny": case "d": case "den": case "false": case "off":
			value = "deny";
			break;
		default:
			if (!allFlags) { //If not all flags (impossible to have any other value)
				throw new IRGException(	"Given value doesn't work for this flag. Expected values: \n" +
										"none (remove/delete/del), allow (true/on), deny (false/off)");
			} else {
				value = args[3]; //Value = something else than those 3
			}
		}
		
		try {
			setFlag(region, flag, value); //Set the flag
		} catch (InvalidFlagFormat e) {
			throw new IRGException("Wrong value. " + e.getMessage());
		}
		
		//Try to save
		saveChanges();
		
		//Send message
		hMsg("Region flag '" + flag.getName() + "' for region '" + region.getId() + "' has been set to '" + value + "'.");
				
		//Send current flags
		RegionPrintoutBuilder printout = new RegionPrintoutBuilder(region);
        printout.append(ChatColor.GRAY);
        printout.append("(Current flags: ");
        printout.appendFlagsList(false);
        printout.append(")");
        printout.send(p);
        
        //Log
        RegionLogger.logRegionChange(region, p, ChangerIsA.staff, ChangeType.flag, flag.getName() + " " + value);
	}
	
	/**
	 * Set the flag of a region
	 * @param region The region
	 * @param flag The flag
	 * @param value The parameter value
	 * @throws InvalidFlagFormat Value is incorrect 
	 */
    private <V> void setFlag(ProtectedRegion region, Flag<V> flag, String value) throws InvalidFlagFormat {
        region.setFlag(flag, flag.parseInput(wg, p, value));
    }
	
	/**
	 * Get all allowed flags
	 * @param allFlags is allowed to do all flags
	 * @return String with flags
	 */
	public static String allowedFlags(boolean allFlags) {
		if (allFlags) {
			StringBuilder builder = new StringBuilder();
			for (Flag<?> flag : DefaultFlag.getFlags()) { //Loop thru flags
				if (builder.length() > 0) { //If already an entry in the builder
					builder.append(", ");
				}
				builder.append(flag.getName()); //Add the flag
			}
			return builder.toString();
		} else {
			return "snow-fall, snow-melt, ice-form, ice-melt, mushroom-growth, grass-growth, mycelium-spread & vine-growth";
		}
	}
	
	/**
	 * Get the flag (Limited flags)
	 * @param p The player
	 * @param flagParameter The specified name of the flag
	 * @return The flag
	 * @throws IRGException if not a valid flag
	 */
	public static Flag<?> getFlag(Player p, String flagParameter) throws IRGException {
		Flag<?> flag;
		String flagArg = flagParameter.toLowerCase().replace("-", "").replace("_", "");
		switch(flagArg) {
		case "snowfall": case "sf": case "snowf": case "sfall":
			flag = DefaultFlag.SNOW_FALL;
			break;
		case "snowmelt": case "sm": case "snowm": case "smelt":
			flag = DefaultFlag.SNOW_MELT;
			break;
		case "iceform": case "if": case "icef": case "iform":
			flag = DefaultFlag.ICE_FORM;
			break;
		case "icemelt": case "im": case "icem": case "imelt":
			flag = DefaultFlag.ICE_MELT;
			break;
		case "mushroomgrowth": case "mushgrowth": case "mg": case "mgrowth": case "mushroomg": case "mushroomgrow":
			flag = DefaultFlag.MUSHROOMS;
			break;
		case "grassgrowth": case "grasgrowth": case "grassgrow": case "gg": case "grassg": case "ggrowth":
			flag = DefaultFlag.GRASS_SPREAD;
			break;
		case "myceliumspread": case "mspread": case "ms": case "myceliums":
			flag = DefaultFlag.MYCELIUM_SPREAD;
			break;
		case "vinegrowth": case "vg": case "vineg": case "vinegrow": case "vgrow": case "vgrowth":
			flag = DefaultFlag.VINE_GROWTH;
			break;
		default: //No flag found. Send allowed flags
			throw new IRGException("No flag found (that you are allowed to use). " + ChatColor.GRAY + "Allowed flags: \n" + allowedFlags(false));
		}
		return flag;
	}

}
