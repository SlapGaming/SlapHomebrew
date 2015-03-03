package me.naithantu.SlapHomebrew.Commands.Fun;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Runnables.RainbowTask;
import me.naithantu.SlapHomebrew.Util.Util;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.List;

public class RainbowCommand extends AbstractCommand {

	public RainbowCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	public boolean handle() throws CommandException {
		Player player = getPlayer(); //Cast & Test permission
		testPermission("rainbow");
		testNotWorld("world_pvp");		
		
		boolean fast = false;
		if (args.length == 1) { //Check if fast rainbow
			if (args[0].equals("fast") && Util.testPermission(player, "rainbow.fast")) {
				fast = true;
			}
		}
		
		if (!fast) { //Normal user
			if (!checkLeatherArmor(player.getInventory())) throw new CommandException("You must be wearing leather armour!"); //Check if wearing leather armor
		}
		
		HashMap<String, Integer> rainbow = plugin.getExtras().getRainbow();  //Get rainbow

		String playername = player.getName();
		if (rainbow.containsKey(playername)) { //Already got /rainbow enabled -> Cancel
			Bukkit.getServer().getScheduler().cancelTask(rainbow.get(playername));
			rainbow.remove(playername);
			hMsg("Your armour will no longer change colour!");
		} else { //Start rainbow
			if (fast) { //If fast set armor to leather stuff
				PlayerInventory playerInv = player.getInventory();
				playerInv.setBoots(new ItemStack(Material.LEATHER_BOOTS));
				playerInv.setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
				playerInv.setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
				playerInv.setHelmet(new ItemStack(Material.LEATHER_HELMET));
			}
			RainbowTask rainbowTask = new RainbowTask(plugin, player, fast);
			rainbowTask.runTaskTimer(plugin, 0, 1);
			rainbow.put(playername, rainbowTask.getTaskId());
			hMsg("Your armour will now have rainbow colours!");
		}
		return true;
	}

	/**
	 * Check if a player is wearing full leather armor
	 * @param inventory The player's inventory
	 * @return wearing leather armor
	 */
	public boolean checkLeatherArmor(PlayerInventory inventory) {
		if (inventory.getBoots() == null ||	inventory.getLeggings() == null || inventory.getChestplate() == null ||	inventory.getHelmet() == null) return false; //Check if wearing armor
		return ( //Check if all armor is correct leather armor
			inventory.getBoots().getType() == Material.LEATHER_BOOTS && 
			inventory.getLeggings().getType() == Material.LEATHER_LEGGINGS && 
			inventory.getChestplate().getType() == Material.LEATHER_CHESTPLATE && 
			inventory.getHelmet().getType() == Material.LEATHER_HELMET
		);
	}
	
	/**
	 * TabComplete on this command
	 * @param sender The sender of the command
	 * @param args given arguments
	 * @return List of options
	 */
	public static List<String> tabComplete(CommandSender sender, String[] args) {
		if (Util.testPermission(sender, "rainbow.fast")) {
			return createNewList("fast");
		} else {
			return createEmptyList();
		}
	}
}
