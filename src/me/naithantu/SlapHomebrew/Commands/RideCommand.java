package me.naithantu.SlapHomebrew.Commands;

import me.naithantu.SlapHomebrew.SlapHomebrew;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class RideCommand extends AbstractCommand {

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
			String targetName = args[0];
			Player target = Bukkit.getServer().getPlayer(targetName);
			if (target != null) {
				target.setPassenger(player);
			}else{
				this.badMsg(sender, "Error: That player is not online!");
			}
		} else {
			for (Entity entity : player.getNearbyEntities(10.0, 10.0, 10.0)) {
				entity.setPassenger(player);
			}
		}
		return false;
	}
}
