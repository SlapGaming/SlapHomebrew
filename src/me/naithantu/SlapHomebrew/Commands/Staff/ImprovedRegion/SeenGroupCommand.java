package me.naithantu.SlapHomebrew.Commands.Staff.ImprovedRegion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.PlayerExtension.UUIDControl;
import me.naithantu.SlapHomebrew.Util.Util;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import com.earth2me.essentials.User;
import com.earth2me.essentials.UserMap;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class SeenGroupCommand extends AbstractImprovedRegionCommand {
	
	private UserMap uMap;
	private ArrayList<SeenObject> owners;
	private ArrayList<SeenObject> members;;
	
	public SeenGroupCommand(Player p, String[] args) {
		super(p, args);
	}

	@Override
	protected void action() throws CommandException {
		final ProtectedRegion region;
		if (args.length == 1) { //No Region given, get Region @ that spot
			region = getHighestPriorityRegion();
		} else { //Region specified
			validateRegionID(args[1]);
			region = getRegion(args[1]);
		}
		p.sendMessage(ChatColor.GRAY + "Collecting info for region: " + region.getId());
		
		
		Util.runASync(new Runnable() {
			
			@Override
			public void run() {
				uMap = plugin.getEssentials().getUserMap();
				
				//Create new maps
				owners = new ArrayList<>();
				members = new ArrayList<>();
				
				for (UUID owner : region.getOwners().getUniqueIds()) { //Parse all owners
					owners.add(parsePlayer(new SeenObject(owner)));
				}
				
				for (UUID member : region.getMembers().getUniqueIds()) { //Parse all members
                    members.add(parsePlayer(new SeenObject(member)));
				}
				
				if (owners.isEmpty()) { //Check if any owners
					hMsg("This region has no owners.");
				} else {
					Collections.sort(owners); //Sort
					hMsg("Owners of region " + region.getId()); //Message
					for (SeenObject owner : owners) { //Send info
						p.sendMessage(ChatColor.GOLD + "  \u2517\u25B6 " + owner.displayname + " -> " + parseLongToDays(owner.lastOnline)); //Send info
					}
				}
				
				if (members.isEmpty()) { //Check if any members
					hMsg("This region has no members.");
				} else {
					Collections.reverse(members);
					hMsg("Members of region " + region.getId());
					for (SeenObject member : members) {
						p.sendMessage(ChatColor.GOLD + "  \u2517\u25B6 " + member.displayname + " -> " + parseLongToDays(member.lastOnline));
					}
				}
			}
		});
		
	}

    /**
     * Parse a player's data to fill a SeenObject
     * @param player The SeenObject with only the player's UUID
     * @return The filled SeenObject
     */
	private SeenObject parsePlayer(SeenObject player) {
        //Get the current name
        String currentName = UUIDControl.getInstance().getUUIDProfile(player.playerUUID).getCurrentName();

        //Get the prefix & suffix from PEX
		String name = "";
		PermissionUser pexUser = PermissionsEx.getPermissionManager().getUser(player.playerUUID);
		if (pexUser != null) {
			if (pexUser.getPrefix() != null) {
				name += pexUser.getPrefix();
			}
		}
		name += currentName;
		if (pexUser != null) {
			if (pexUser.getSuffix() != null) {
				name += pexUser.getSuffix();
			}
		}
        //=> Set in object
        player.displayname = ChatColor.translateAlternateColorCodes('&', name);

        //Get the last seen from Essentials
		User essUser = uMap.getUser(player.playerUUID);
		if (essUser == null) {
			p.sendMessage(ChatColor.RED + "Player " + currentName + " doesn't have an Essentials file..");
            player.lastOnline = -1;
		} else {
            player.lastOnline = (plugin.getServer().getPlayer(player.playerUUID) != null ? Long.MAX_VALUE : essUser.getLastLogout());
        }

        //Return the object
        return player;
	}
	
	private String parseLongToDays(long l) {
		if (l == Long.MAX_VALUE) { //Check if Max Value (Online now)
			return "Currently online.";
		} else if (l == -1) {
            return "Unknown.";
        } else {
			long pastTime = System.currentTimeMillis() - l;
			int days = (int)Math.floor(pastTime / 1000 / 86400.00);
			if (days == 0) {
				return "Was online today.";
			} else {
				return "Was online " + days + (days == 1 ? " day" : " days") + " ago.";
			}
		}
	}

    private class SeenObject implements Comparable<SeenObject> {

        private UUID playerUUID;
        private long lastOnline;
        private String displayname;

        private SeenObject(UUID playerUUID) {
            this.playerUUID = playerUUID;
        }

        @Override
        public int compareTo(SeenObject o) {
            return Long.valueOf(lastOnline).compareTo(Long.valueOf(o.lastOnline));
        }
    }
}
