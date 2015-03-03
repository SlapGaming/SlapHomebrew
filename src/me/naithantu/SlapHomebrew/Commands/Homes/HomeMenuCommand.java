package me.naithantu.SlapHomebrew.Commands.Homes;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Controllers.HomeMenu;
import me.naithantu.SlapHomebrew.Controllers.Homes;
import me.naithantu.SlapHomebrew.Util.Util;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;

public class HomeMenuCommand extends AbstractCommand {
	public HomeMenuCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public boolean handle() throws CommandException {
		Player p = getPlayer();
		testPermission("homemenu");
		String playername = p.getName();
		
		Homes homesControl = plugin.getHomes(); //Get homescontroller
		
		List<String> homes = homesControl.getHomes(playername);
		if (homes.size() == 0) {
			Util.badMsg(sender, "You currently have no homes set.");
		} else if (homes.size() == 1) {
			homesControl.teleportToHome(p, homes.get(0));
		} else {
			showHomeMenu(p);
		}
		return true;
	}
	
	private void showHomeMenu(Player player){
		HashMap<String, HomeMenu> homeMenus = plugin.getExtras().getHomeMenus();
		if (homeMenus.containsKey(player.getName())) {
			homeMenus.get(player.getName()).reCreateHomeMainMenu();
		} else {
			homeMenus.put(player.getName(), new HomeMenu(player));
		}
	}
	
}
