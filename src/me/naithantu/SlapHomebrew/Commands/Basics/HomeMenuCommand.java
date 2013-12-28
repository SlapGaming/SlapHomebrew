package me.naithantu.SlapHomebrew.Commands.Basics;

import java.util.HashMap;
import java.util.List;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Controllers.HomeMenu;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.command.CommandSender;

import com.earth2me.essentials.User;

public class HomeMenuCommand extends AbstractCommand {
	public HomeMenuCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
	}

	@Override
	public boolean handle() throws CommandException {
		getPlayer();
		testPermission("homemenu");
		
		User targetPlayer = plugin.getEssentials().getUserMap().getUser(sender.getName()); //Fetch Essentials User (which extends Player)
		List<String> homes = targetPlayer.getHomes();
		if (homes.size() == 0) {
			Util.badMsg(sender, "You currently have no homes set.");
		} else if (homes.size() == 1) {
			HomeCommand.teleportToHome(targetPlayer, homes.get(0));
		} else {
			showHomeMenu(targetPlayer);
		}
		return true;
	}
	
	
	private void showHomeMenu(User player){
		HashMap<String, HomeMenu> homeMenus = plugin.getExtras().getHomeMenus();
		if (homeMenus.containsKey(player.getName())) {
			homeMenus.get(player.getName()).reCreateHomeMainMenu(player);
		} else {
			homeMenus.put(player.getName(), new HomeMenu(player, plugin));
		}
	}
	
}
