package me.naithantu.SlapHomebrew.Commands;

import java.util.HashMap;
import java.util.List;

import me.naithantu.SlapHomebrew.HomeMenu;
import me.naithantu.SlapHomebrew.SlapHomebrew;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.earth2me.essentials.User;

public class HomeMenuCommand extends AbstractCommand {
	
	private static HashMap<String, HomeMenu> homeMenuMap = new HashMap<>();

	public HomeMenuCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
	}

	@Override
	public boolean handle() {
		if (!testPermission(sender, "homemenu")) {
			noPermission(sender);
			return true;
		}
		
		if (!(sender instanceof Player)) {
			this.badMsg(sender, "You need to be in-game to do that.");
			return true;
		}
		
		User targetPlayer = plugin.getEssentials().getUserMap().getUser(sender.getName()); //Fetch Essentials User (which extends Player)
		List<String> homes = targetPlayer.getHomes();
		if (homes.size() == 0) {
			badMsg(sender, "You currently have no homes set.");
		} else if (homes.size() == 1) {
			HomeCommand.teleportToHome(targetPlayer, homes.get(0));
		} else {
			showHomeMenu(targetPlayer);
		}
		return true;
	}
	
	
	private void showHomeMenu(User player){
		if (homeMenuMap.containsKey(player.getName())) {
			homeMenuMap.get(player.getName()).reCreateHomeMainMenu(player);
		} else {
			homeMenuMap.put(player.getName(), new HomeMenu(player, plugin));
		}
	}
	
}
