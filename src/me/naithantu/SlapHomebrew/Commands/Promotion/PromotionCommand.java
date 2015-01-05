package me.naithantu.SlapHomebrew.Commands.Promotion;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.UsageException;
import me.naithantu.SlapHomebrew.Util.Util;
import nl.stoux.SlapPlayers.Model.Profile;
import org.bukkit.command.CommandSender;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import java.util.List;

public class PromotionCommand extends AbstractCommand {

	private String usage;
	
	public PromotionCommand(CommandSender sender, String[] args) {
		super(sender, args);
		usage = "promotion <VIP | Homes | Rank | givemoney [Player] [Amount]>";
	}

	@Override
	public boolean handle() throws CommandException {
		super.testPermission("promotion"); //Test perm
		if (args.length == 0) { //Usage
			throw new UsageException(usage);
		}
		
		Profile offPlayer; String playername;
		
		switch (args[0].toLowerCase()) {
		case "givemoney": case "addmoney": case "money": //Give the player money
			testPermission("givemoney"); //Perm
			if (args.length != 3) throw new UsageException("promotion givemoney [Player] [Amount]");
			offPlayer = getOfflinePlayer(args[1]); //Get Player
			double amount = parseIntPositive(args[2]); //Parse amount
			playername = offPlayer.getCurrentName();
			
			String vipBonus = "";
			PermissionUser user = PermissionsEx.getPermissionManager().getUser(offPlayer.getUUID()); //Get Pex User
			if (user != null) {
				switch (user.getGroupsNames()[0].toLowerCase()) {
				case "vip": case "vipguide": case "mod": case "admin": //If VIP or staff
					amount = amount * 1.2; //Multiply by 1.2
					vipBonus = " (with VIP bonus).";
					break;
				}
			}
			plugin.getEconomy().depositPlayer(playername, amount); //Add money
			hMsg("Gave player " + playername + " " + amount + " dollars" + vipBonus); //Message
			break;
		
		case "homes": case "home": //Add homes to a player
			new PromotionHomesCommand(sender, args).handle();
			break;
			
		case "vip": //Vip commands, redirect to PromotionVIP
			new PromotionVIPCommand(sender, args).handle();
			break;
			
		case "rank": case "ranks":
			Util.runASync(new Runnable() {
				@Override
				public void run() {
					try {
						new PromotionRankCommand(sender, args).handle();
					} catch (CommandException e) {
						Util.badMsg(sender, e.getMessage());
					}
				}
			});
			break;
			
		default:
			throw new UsageException(usage);	
		}		
		return true;
	}
	
	@Override
	protected void testPermission(String perm) throws CommandException {
		super.testPermission("promotion." + perm);
	}
	
	/**
	 * TabComplete on this command
	 * @param sender The sender of the command
	 * @param args given arguments
	 * @return List of options
	 */
	public static List<String> tabComplete(CommandSender sender, String[] args) {
		if (!Util.testPermission(sender, "permission")) return createEmptyList(); //No permission
		
		if (args.length == 1) { //First argument
			return filterResults(createNewList("addmoney", "givemoney", "money", "homes", "vip", "ranks"), args[0]); //Add commands & Filter
		} else {
			switch(args[0].toLowerCase()) {
			case "givemoney": case "addmoney": case "money": //Money command given
				if (args.length == 2) {
					return listAllPlayers(sender.getName()); //List players
				} else {
					return null;
				}
			case "homes": case "home": //Homes
				return PromotionHomesCommand.tabComplete(sender, args);
			case "vip": //Vip
				return PromotionVIPCommand.tabComplete(sender, args);
			case "rank": case "ranks": //Ranks
				return PromotionRankCommand.tabComplete(sender, args);
			default:
				Util.badMsg(sender, "Invalid first argument.");
			}
		}
		return null;
	}

}
