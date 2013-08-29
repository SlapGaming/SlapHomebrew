package me.naithantu.SlapHomebrew.Commands.Fun;

import java.util.HashMap;
import java.util.List;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class RideCommand extends AbstractCommand {

	private static HashMap<String, Boolean> rightClicks = new HashMap<>();
	
	public RideCommand(CommandSender sender, String[] args, SlapHomebrew plugin){
		super(sender, args, plugin);
	}

	@Override
	public boolean handle() {
		if (!(sender instanceof Player)) {
			this.badMsg(sender, "You need to be in-game to do that!");
			return true;
		}

		if (!testPermission(sender, "ride")) {
			this.noPermission(sender);
			return true;
		}
		
		Player player = (Player) sender;
		
		if(player.isInsideVehicle()){
			player.getVehicle().eject();
		}
		
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("click")) {
				player.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "Right-click an entity to ride it.");
				rightClicks.put(player.getName(), true);
				return true;
			}
			String targetName = args[0];
			Player target = Bukkit.getServer().getPlayer(targetName);
			if (target != null) {
				if (target.getName().equals(player.getName())) {
					badMsg(sender, "You trying to break the server m8?");
					return true;
				}
				target.setPassenger(player);
			}else{
				this.badMsg(sender, "Error: That player is not online!");
			}
		} else {
			List<Entity> entities = player.getNearbyEntities(10.0, 10.0, 10.0);
			if (entities.size() > 0) {
				if (player.getPassenger() != null) {
					entities.get(0).setPassenger(player);
				}
			}
		}
		return true;
	}
	
	public static boolean rightClick(String player){
		if (rightClicks.containsKey(player)) {
			rightClicks.remove(player);
			return true;
		}
		return false;
	}
	
}
