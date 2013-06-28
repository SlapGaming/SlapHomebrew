package me.naithantu.SlapHomebrew.Commands;

import java.util.HashMap;
import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Runnables.RainbowTask;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

public class RainbowCommand extends AbstractCommand {

	public RainbowCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
	}

	public boolean handle() {
		if (!testPermission(sender, "rainbow")) {
			this.noPermission(sender);
			return true;
		}

		if (!(sender instanceof Player)) {
			this.badMsg(sender, "You need to be in-game to do that!");
			return true;
		}

		Player player = (Player) sender;
		if (!checkLeatherArmor(player.getInventory())) {
			this.badMsg(sender, "You must be wearing leather armour!");
			return true;
		}

		HashMap<String, Integer> rainbow = plugin.getExtras().getRainbow();

		if (rainbow.containsKey(sender.getName())) {
			Bukkit.getServer().getScheduler().cancelTask(rainbow.get(sender.getName()));
			rainbow.remove(sender.getName());
			this.msg(sender, "Your armour will no longer change colour!");
		} else {
			RainbowTask rainbowTask = new RainbowTask(plugin, player, false);
			rainbowTask.runTaskTimer(plugin, 0, 1);
			rainbow.put(sender.getName(), rainbowTask.getTaskId());
			this.msg(sender, "Your armour will now have rainbow colours!");
		}
		plugin.getExtras().setRainbow(rainbow);
		return true;
	}

	public boolean checkLeatherArmor(PlayerInventory inventory) {
		if (inventory.getBoots() == null || inventory.getLeggings() == null || inventory.getChestplate() == null || inventory.getHelmet() == null)
			return false;
		return inventory.getBoots().getType() == Material.LEATHER_BOOTS && inventory.getLeggings().getType() == Material.LEATHER_LEGGINGS
				&& inventory.getChestplate().getType() == Material.LEATHER_CHESTPLATE && inventory.getHelmet().getType() == Material.LEATHER_HELMET;
	}
}
