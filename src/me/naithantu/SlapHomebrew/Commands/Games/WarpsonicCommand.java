package me.naithantu.SlapHomebrew.Commands.Games;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.AbstractCommand;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WarpsonicCommand extends AbstractCommand {
	public WarpsonicCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
	}

	public boolean handle() {
		if (!testPermission(sender, "warpsonic")) {
			this.noPermission(sender);
			return true;
		}
		
		if(args.length == 1 && testPermission(sender, "warpsonic.other")){
			Player targetPlayer = Bukkit.getServer().getPlayer(args[0]);
			if(targetPlayer == null){
				this.msg(sender, "targetPlayer not found.");
				return true;
			}
			
			plugin.getSonic().teleportSonic(targetPlayer.getName());
			this.msg(targetPlayer, "You have been teleported to the sonic racetrack!");
			return true;
		} else {
			if (!(sender instanceof Player)) {
				this.badMsg(sender, "You need to be in-game to do that!");
				return true;
			}
			
			final Player player = (Player) sender;
			plugin.getSonic().teleportSonic(player.getName());
			this.msg(sender, "You have been teleported to the sonic racetrack!");
			return true;
		}
	}
}
