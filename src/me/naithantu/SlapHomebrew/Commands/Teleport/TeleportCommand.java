package me.naithantu.SlapHomebrew.Commands.Teleport;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.ErrorMsg;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class TeleportCommand extends AbstractCommand {

	public TeleportCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	@Override
	public boolean handle() throws CommandException {
		Player p = getPlayer();
        boolean directPerm = Util.testPermission(sender, "tp.direct");
        boolean worldPerm = Util.testPermission(sender, "tp.direct.world");
        if (!directPerm && !worldPerm) {
            throw new CommandException(ErrorMsg.noPermission);
        }

        //Check usage
		if (args.length != 1) return false;

        //Get the target
		Player targetPlayer = getOnlinePlayer(args[0], false);

        //Check if allowed to TP
        if (!directPerm) {
            try {
                //Check if the same world
                String targetWorld = targetPlayer.getWorld().getName();
                if (!targetWorld.equalsIgnoreCase(p.getWorld().getName())) {
                    //Get PEX user to check other world permission
                    boolean otherWorldPerm = PermissionsEx.getUser(p).has("tp.direct.world", targetWorld);
                    if (!otherWorldPerm) {
                        //Throw a NoPermissions exception if no permission
                        throw new CommandException("You don't have permission to teleport from this world to that world!");
                    }
                }
            } catch (Exception e) {
                if (e instanceof CommandException) {
                    //Rethrow the CommandException
                    throw e;
                } else {
                    //Failed to check if the user has that permission. Throw an error.
                    throw new CommandException("Could not verify your permissions in the target world!");
                }
            }
        }

        //Teleport the player
		boolean tpUnder = Util.safeTeleport(p, targetPlayer.getLocation(), targetPlayer.isFlying(), true); //Teleport the player
		p.sendMessage(ChatColor.GRAY + "Teleported" + (tpUnder ? " under the player" : "") + "..."); //Msg
		return true;
	}

}
