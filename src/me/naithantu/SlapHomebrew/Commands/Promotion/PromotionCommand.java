package me.naithantu.SlapHomebrew.Commands.Promotion;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.UsageException;
import me.naithantu.SlapHomebrew.Util.Util;

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
		
		OfflinePlayer offPlayer; String playername;
		
		switch (args[0].toLowerCase()) {
		case "givemoney": case "addmoney": case "money": //Give the player money
			testPermission("givemoney"); //Perm
			if (args.length != 3) throw new UsageException("promotion givemoney [Player] [Amount]");
			offPlayer = getOfflinePlayer(args[1]); //Get Player
			double amount = parseIntPositive(args[2]); //Parse amount
			playername = offPlayer.getName();
			
			String vipBonus = "";
			PermissionUser user = PermissionsEx.getUser(playername); //Get Pex User
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

}
