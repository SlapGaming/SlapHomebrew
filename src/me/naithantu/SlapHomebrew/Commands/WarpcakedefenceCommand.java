package me.naithantu.SlapHomebrew.Commands;

import me.naithantu.SlapHomebrew.SlapHomebrew;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class WarpcakedefenceCommand extends AbstractCommand {
	public WarpcakedefenceCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	public boolean handle() {
		if (!(sender instanceof Player)) {
			this.badMsg(sender, "You need to be in-game to do that!");
			return true;
		}

		if (!testPermission(sender, "warpcakedefence")) {
			this.noPermission(sender);
			return true;
		}

		Player player = (Player) sender;
		if (SlapHomebrew.allowCakeTp == true) {
			if (emptyInv()) {
				player.teleport(new Location(Bukkit.getServer().getWorld("world") , 333.0, 28.0, -722.0));
				this.msg(sender, "You have been teleported to cake defence!");
			} else {
				this.badMsg(sender, "Empty your inventory and take of your armor, then use /warpcakedefence again!");
			}
		} else {
			if (emptyInv()) {
				player.teleport(new Location(Bukkit.getServer().getWorld("world"), 333.0, 45.0, -751.0));
				this.msg(sender, "You aren't allowed to tp to cakedefence now, you have been teleported to the spectator area!");
			} else {
				this.badMsg(sender, "Empty your inventory and take of your armor, then use /warpcakedefence again!");
			}
		}
		return true;
	}
	
	private boolean emptyInv(){
		Boolean emptyInv = true;
		PlayerInventory inv = ((Player) sender).getInventory();
		for (ItemStack stack : inv.getContents()) {
			try {
				if (stack.getType() != (Material.AIR)) {
					emptyInv = false;
				}
			} catch (NullPointerException e) {
			}
		}
		for (ItemStack stack : inv.getArmorContents()) {
			try {
				if (stack.getType() != (Material.AIR)) {
					emptyInv = false;
				}
			} catch (NullPointerException e) {
			}
		}
		return emptyInv;
	}
}
