package me.naithantu.SlapHomebrew.Commands.Fun;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.SkullMeta;

public class HeadCommand extends AbstractCommand {

	public HeadCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public boolean handle() throws CommandException {
		Player p = getPlayer(); //Player
		testPermission("head"); //Test perm
		
		//Get player inventory
		PlayerInventory inv = p.getInventory();
		if (inv.firstEmpty() == -1) {
			throw new CommandException("You have no empty spots in your inventory!");
		}
		
		//Create skull
		ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
		SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
		skullMeta.setOwner(p.getName());
		skull.setItemMeta(skullMeta);
		
		//Add to inventory
		inv.addItem(skull);
		hMsg("Your head has been added to your inventory!");
		return true;
	}

}
