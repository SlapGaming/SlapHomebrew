package me.naithantu.SlapHomebrew.Commands.Staff.ImprovedRegion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
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
	private HashMap<Long, String> owners;
	private HashMap<Long, String> members;;
	
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
				owners = new HashMap<>();
				members = new HashMap<>();
				
				for (String owner : region.getOwners().getPlayers()) { //Parse all owners
					parsePlayer(owners, owner);
				}
				
				for (String member : region.getMembers().getPlayers()) { //Parse all members
					parsePlayer(members, member);
				}
				
				if (owners.isEmpty()) { //Check if any owners
					hMsg("This region has no owners.");
				} else {
					ArrayList<Long> times = new ArrayList<>(owners.keySet()); //Get all times
					Collections.reverse(times); //Sort
					hMsg("Owners of region " + region.getId()); //Message
					for (Long l : times) { //Send info
						p.sendMessage(ChatColor.GOLD + "  \u2517\u25B6 " + owners.get(l) + " -> " + parseLongToDays(l)); //Send info
					}
				}
				
				if (members.isEmpty()) { //Check if any members
					hMsg("This region has no members.");
				} else {
					ArrayList<Long> times = new ArrayList<>(members.keySet());
					Collections.reverse(times);
					hMsg("Members of region " + region.getId());
					for (Long l : times) {
						p.sendMessage(ChatColor.GOLD + "  \u2517\u25B6 " + members.get(l) + " -> " + parseLongToDays(l));
					}
				}
			}
		});
		
	}
	
	private void parsePlayer(HashMap<Long, String> map, String player) {
		String name = "";
		PermissionUser pexUser = PermissionsEx.getUser(player);
		if (pexUser != null) {
			if (pexUser.getPrefix() != null) {
				name += pexUser.getPrefix();
			}
		}
		name += player;
		if (pexUser != null) {
			if (pexUser.getSuffix() != null) {
				name += pexUser.getSuffix();
			}
		}
		
		User essUser = uMap.getUser(player);
		if (essUser == null) {
			p.sendMessage(ChatColor.RED + "Player " + player + " doesn't have an Essentials file..");
		} else {
            map.put((plugin.getServer().getPlayer(player) != null ? Long.MAX_VALUE : essUser.getLastLogout()), ChatColor.translateAlternateColorCodes('&', name));
        }
	}
	
	private String parseLongToDays(long l) {
		if (l == Long.MAX_VALUE) { //Check if Max Value (Online now)
			return "Currently online.";
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

}
