package me.naithantu.SlapHomebrew.Commands.Basics;

import me.naithantu.SlapHomebrew.Commands.AbstractCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Stoux on 18/01/2015.
 */
public class EmptyBucketCommand extends AbstractCommand {

    public EmptyBucketCommand(CommandSender sender, String[] args) {
        super(sender, args);
    }

    @Override
    public boolean handle() throws CommandException {
        Player p = getPlayer();
        testPermission("emptybucket");

        //Check if the player is holding a bucket
        ItemStack stack = p.getItemInHand();
        switch (stack.getType()) {
            case LAVA_BUCKET: case MILK_BUCKET: case WATER_BUCKET:
                p.setItemInHand(new ItemStack(Material.BUCKET, 1));
                hMsg("You emptied the bucket.");
                break;

            case BUCKET:
                hMsg("That bucket is already empty.");
                break;

            default:
                hMsg("You need to have a filled bucket in your hand!");
                break;
        }

        return true;
    }

}
