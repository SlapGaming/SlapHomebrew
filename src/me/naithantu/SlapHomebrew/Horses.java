package me.naithantu.SlapHomebrew;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

import me.naithantu.SlapHomebrew.Storage.YamlStorage;

import java.util.ArrayList;
import java.util.List;

public class Horses {

	YamlStorage horsesStorage;
	FileConfiguration horsesConfig;
	
	public Horses(SlapHomebrew plugin) {
		horsesStorage = new YamlStorage(plugin, "horses");
		horsesConfig = horsesStorage.getConfig();	
	}
	
	public void tamedHorse(String entityID, String owner) {
		horsesConfig.set(entityID + ".owner", owner);
		save();
	}
	
	public boolean enterHorse(String entityID, Player player) {
		boolean returnBool = false;
		if (horsesConfig.contains(entityID)) {
			if (horsesConfig.getString(entityID + ".owner").equals(player.getName())) {
				returnBool = true;
			} else {
				if (horsesConfig.contains(entityID + ".allowed")) {
					if (horsesConfig.getList(entityID + ".allowed").contains(player.getName())) {
						returnBool = true;
					}
				}
			}
		} else {
			returnBool = true;
		}
		return returnBool;
	}
	
	public void allowOnHorse(String entityID, Player owner, String allowedPlayer) {
		if (horsesConfig.contains(entityID)) {
			if (horsesConfig.getString(entityID + ".owner").equals(owner.getName())) {
				if (horsesConfig.contains(entityID + ".allowed")) {
					List<String> allowedPlayers = horsesConfig.getStringList(entityID + ".allowed");
					if (allowedPlayers.contains(allowedPlayer)) {
						owner.sendMessage(ChatColor.RED + "This player is already allowed on this horse.");
					} else {
						allowedPlayers.add(allowedPlayer);
						horsesConfig.set(entityID + ".allowed", allowedPlayers);
						save();
						owner.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + allowedPlayer + " is now allowed on this horse.");
					}
				} else {
					List<String> allowedPlayers = new ArrayList<String>();
					allowedPlayers.add(allowedPlayer);
					horsesConfig.set(entityID + ".allowed", allowedPlayers);
					save();
					owner.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + allowedPlayer + " is now allowed on this horse.");
				}
			} else {
				owner.sendMessage(ChatColor.RED + "You are not the owner of this horse.");
			}
		} else {
			owner.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "This horse is not claimed yet. Use '/horse claim' to claim it.");
		}
	}
	
	public void denyOnHorse(String entityID, Player owner, String denyPlayer) {
		if (horsesConfig.contains(entityID)) {
			if (horsesConfig.getString(entityID + ".owner").equals(owner.getName())) {
				if (horsesConfig.contains(entityID + ".allowed")) {
					List<String> allowedPlayers = horsesConfig.getStringList(entityID + ".allowed");
					if (allowedPlayers.contains(denyPlayer)) {
						allowedPlayers.remove(denyPlayer);
						if (allowedPlayers.size() > 0) {
							horsesConfig.set(entityID + ".allowed", allowedPlayers);
						} else {
							horsesConfig.set(entityID + ".allowed", null);
						}
						save();
						owner.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + denyPlayer + " is now denied from riding this horse.");
					} else {
						owner.sendMessage(ChatColor.RED + "This player isn't allowed on this horse.");
					}
				} else {
					owner.sendMessage(ChatColor.RED + "You haven't allowed any players on this horse.");
				}
			} else {
				owner.sendMessage(ChatColor.RED + "You are not the owner of this horse.");
			}
		} else {
			owner.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "This horse is not claimed yet. Use '/horse claim' to claim it.");
		}
	}
	
	public void claimHorse(Horse horse, Player owner) {
		String entityID = horse.getUniqueId().toString();
		if (!horsesConfig.contains(entityID)) {
			horsesConfig.set(entityID + ".owner", owner.getName());
			horse.setOwner(owner);
			save();
			owner.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "You now own this horse");
		} else {
			if (horsesConfig.getString(entityID + ".owner").equals(owner.getName())) {
				owner.sendMessage(ChatColor.RED + "You already own this horse.");
			} else {
				owner.sendMessage(ChatColor.RED + "This horse is already claimed.");
			}
		}		
	}
	
	public boolean changeOwner(Horse horse, Player owner, Player newOwner) {
		String entityID = horse.getUniqueId().toString();
		boolean returnBool = false;
		if (horsesConfig.contains(entityID)) {
			if (horsesConfig.getString(entityID + ".owner").equals(owner.getName())) {
				horsesConfig.set(entityID, null);
				horsesConfig.set(entityID + ".owner", newOwner.getName());
				horse.setOwner(newOwner);
				save();
				Entity ejectVehicle = owner.getVehicle();
				if (ejectVehicle != null && ejectVehicle instanceof Horse) {
					ejectVehicle.eject();
				}
				String horseCustomName = horse.getCustomName();
				if (horseCustomName != null) {
					owner.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + newOwner.getName() + " is now the owner of " + horseCustomName + ".");
					newOwner.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + owner.getName() + " made you owner of the horse: " + horseCustomName);
				} else {
					owner.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + newOwner.getName() + " is now the owner of that horse.");
					newOwner.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + owner.getName() + " made you the owner of one of his horses.");
				}
			} else {
				owner.sendMessage(ChatColor.RED + "You are not the owner of this horse.");
			}
		} else {
			owner.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "This horse is not claimed yet. Use '/horse claim' to claim it.");
		}
		return returnBool;
	}
	
	public boolean hasOwner(String entityID) {
		boolean returnBool = false;
		if (horsesConfig.contains(entityID)) {
			returnBool = true;
		}
		return returnBool;
	}
	
	public String getOwner(String entityID) {
		return horsesConfig.getString(entityID + ".owner");
	}
	
	public void removeHorse(String entityID) {
		horsesConfig.set(entityID, null);
		save();
	}
	
	public List<String> getAllowedPlayers(String entityID) {
		if (horsesConfig.contains(entityID + ".allowed")) {
			return horsesConfig.getStringList(entityID + ".allowed");
		}
		return null;
	}
	
	private void save(){
		horsesStorage.saveConfig();
	}
	
}
