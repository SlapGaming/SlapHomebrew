package me.naithantu.SlapHomebrew.Commands;

import java.util.List;

import me.naithantu.SlapHomebrew.IconMenu;
import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.IconMenu.OptionClickEvent;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.earth2me.essentials.User;

public class HomeMenuCommand extends AbstractCommand {

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
			teleportPlayer(targetPlayer, homes.get(0));
		} else {
			showHomeMenu(targetPlayer, homes);
		}
		return true;
	}
	
	
	private void showHomeMenu(User player, List<String> homes){
		final User targetPlayer = player;
		int inventorySize = (int) Math.ceil(homes.size() / 9.0) * 9;
		IconMenu homeMenu = new IconMenu("Homes Menu", inventorySize, new IconMenu.OptionClickEventHandler() { //Make new IconMenu
			@Override
			public void onOptionClick(OptionClickEvent event) {
				teleportPlayer(targetPlayer, event.getName());
			}
		}, plugin);
		
		int xCount = 0;
		for (String home : homes) {
			if (xCount < 55) {
				try {
					Location homeLoc = targetPlayer.getHome(home);
					String worldName = homeLoc.getWorld().getName();
					if (worldName.contains("world_resource")) { //Set any resourceworld name -> world_resource
						worldName = "world_resource";
					}
					switch (worldName) {
					case "world": 
					case "world_survival2":	homeMenu.setOption(xCount, new ItemStack(Material.GRASS, 0), home, "Home " + home + ": Survival World");			break;
					case "world_nether":	homeMenu.setOption(xCount, new ItemStack(Material.NETHER_BRICK, 0), home, "Home " + home + ": The Nether");			break;
					case "world_creative":	homeMenu.setOption(xCount, new ItemStack(Material.DIAMOND_BLOCK, 0), home, "Home " + home + ": Creative World");	break;
					case "world_resource":	homeMenu.setOption(xCount, new ItemStack(Material.COBBLESTONE, 0), home, "Home " + home + ": Resource World");		break;
					case "world_the_end":	homeMenu.setOption(xCount, new ItemStack(Material.DRAGON_EGG, 0), home, "Home " + home + ": The End");				break;
					case "world_pvp":		homeMenu.setOption(xCount, new ItemStack(Material.DIAMOND_SWORD, 0), home, "Home " + home + ": PVP World");			break;
					default:				homeMenu.setOption(xCount, new ItemStack(Material.COMPASS, 0), home, "Home " + home + ": Unknown World");
					}
					xCount++;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		homeMenu.open(player);
	}
	
	private void teleportPlayer(User targetPlayer, String home){
		try {
			targetPlayer.teleport(targetPlayer.getHome(home));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
