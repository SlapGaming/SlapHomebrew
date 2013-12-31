package me.naithantu.SlapHomebrew.Controllers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import me.naithantu.SlapHomebrew.Storage.YamlStorage;
import me.naithantu.SlapHomebrew.Util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Horses extends AbstractController {

	private YamlStorage horsesStorage;
	private FileConfiguration horsesConfig;
	private HashMap<String, Boolean> infoClick;
	
	public Horses() {
		horsesStorage = new YamlStorage(plugin, "horses");
		horsesConfig = horsesStorage.getConfig();	
		infoClick = new HashMap<>();
	}
	
	public void tamedHorse(String entityID, String owner) {
		horsesConfig.set("horse." + entityID + ".owner", owner);
		save();
	}
	
	public boolean enterHorse(String entityID, Player player) {
		boolean returnBool = false;
		if (horsesConfig.contains("horse." + entityID)) {
			if (getOwner(entityID).equals(player.getName())) {
				returnBool = true;
			} else {
				if (horsesConfig.contains("horse." + entityID + ".allowed")) {
					List<String> list = horsesConfig.getStringList("horse." + entityID + ".allowed");
					if (list.contains("public") || list.contains(player.getName())) {
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
		if (horsesConfig.contains("horse." + entityID)) {
			if (getOwner(entityID).equals(owner.getName())) {
				if (horsesConfig.contains("horse." + entityID + ".allowed")) {
					List<String> allowedPlayers = horsesConfig.getStringList("horse." + entityID + ".allowed");
					if (allowedPlayers.contains(allowedPlayer)) {
						owner.sendMessage(ChatColor.RED + "This player is already allowed on this horse.");
					} else if (allowedPlayers.contains("public")) {
						Util.badMsg(owner, "This horse is public, anyone can acces it already.");
					} else {
						allowedPlayers.add(allowedPlayer);
						horsesConfig.set("horse." + entityID + ".allowed", allowedPlayers);
						save();
						owner.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + allowedPlayer + " is now allowed on this horse.");
					}
				} else {
					List<String> allowedPlayers = new ArrayList<String>();
					allowedPlayers.add(allowedPlayer);
					horsesConfig.set("horse." + entityID + ".allowed", allowedPlayers);
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
		if (horsesConfig.contains("horse." + entityID)) {
			if (getOwner(entityID).equals(owner.getName())) {
				if (horsesConfig.contains("horse." + entityID + ".allowed")) {
					List<String> allowedPlayers = horsesConfig.getStringList("horse." + entityID + ".allowed");
					if (allowedPlayers.contains(denyPlayer)) {
						allowedPlayers.remove(denyPlayer);
						if (allowedPlayers.size() > 0) {
							horsesConfig.set("horse." + entityID + ".allowed", allowedPlayers);
						} else {
							horsesConfig.set("horse." + entityID + ".allowed", null);
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
		if (!horse.isTamed()) {
			Util.badMsg(owner, "The horse needs to be tamed first.");
			return;
		}
		String entityID = horse.getUniqueId().toString();
		if (!horsesConfig.contains("horse." + entityID)) {
			horsesConfig.set("horse." + entityID + ".owner", owner.getName());
			horse.setOwner(owner);
			save();
			owner.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "You now own this horse");
		} else {
			if (getOwner(entityID).equals(owner.getName())) {
				owner.sendMessage(ChatColor.RED + "You already own this horse.");
			} else {
				owner.sendMessage(ChatColor.RED + "This horse is already claimed.");
			}
		}		
	}
	
	public void unclaimHorse(Horse horse, Player owner) {
		String entityID = horse.getUniqueId().toString();
		if (horsesConfig.contains("horse." + entityID)) {
			if (getOwner(entityID).equals(owner.getName())) {
				horse.eject();
				horse.leaveVehicle();
				horse.setTamed(false);
				
				HorseInventory inventory = horse.getInventory();
				Inventory inv = owner.getInventory();
				try {
					for (ItemStack item : inventory.getContents()) {
						inv.addItem(item);
					}
				} catch (IllegalArgumentException e) {
					owner.getInventory();
				}
				
				inventory.setContents(new ItemStack[] {new ItemStack(Material.AIR)});
				
				horsesConfig.set("horse." + entityID, null);
				if (horse.getVariant() == Variant.SKELETON_HORSE || horse.getVariant() == Variant.UNDEAD_HORSE) {
					horse.setHealth(0);
					if (horsesConfig.contains("player." + owner.getName() + ".horses")) {
						List<String> list = horsesConfig.getStringList("player." + owner.getName() + ".horses");
						list.remove(entityID);
						horsesConfig.set("player." + owner.getName() + ".horses", list);
					}
					badMsg(owner, "The mutated horse was lost without you and died :(..");
				} else {
					owner.sendMessage(Util.getHeader() + "The horse is once again free.");
				}
			} else {
				badMsg(owner, "This is not your horse!");
			}
		} else {
			badMsg(owner, "This horse is not claimed!");
		}
	}
	
	public boolean changeOwner(Horse horse, Player owner, Player newOwner) {
		String entityID = horse.getUniqueId().toString();
		boolean returnBool = false;
		if (horsesConfig.contains("horse." + entityID)) {
			if (getOwner(entityID).equals(owner.getName())) {
				horsesConfig.set("horse." + entityID, null);
				horsesConfig.set("horse." + entityID + ".owner", newOwner.getName());
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
	
	public boolean setHorsePublic(Horse horse, Player owner) {
		String entityID = horse.getUniqueId().toString();
		boolean returnBool = false;
		if (horsesConfig.contains("horse." + entityID)) {
			if (getOwner(entityID).equals(owner.getName())) {
				List<String> list = new ArrayList<>();
				list.add("public");
				horsesConfig.set("horse." + entityID + ".allowed", list);
				returnBool = true;
			} else {
				Util.badMsg(owner, "You are not the owner of this horse.");
			}
		} else {
			owner.sendMessage(Util.getHeader() + "This horse is not claimed yet. Use '/horse claim' to claim it.");
		}
		return returnBool;
	}
	
	public boolean setHorsePrivate(Horse horse, Player owner) {
		String entityID = horse.getUniqueId().toString();
		boolean returnBool = false;
		if (horsesConfig.contains("horse." + entityID)) {
			if (getOwner(entityID).equals(owner.getName())) {
				horsesConfig.set("horse." + entityID + ".allowed", null);
				returnBool = true;
			} else {
				Util.badMsg(owner, "You are not the owner of this horse.");
			}
		} else {
			owner.sendMessage(Util.getHeader() + "This horse is not claimed yet. Use '/horse claim' to claim it.");
		}
		return returnBool;
	}
	
	public boolean hasOwner(String entityID) {
		boolean returnBool = false;
		if (horsesConfig.contains("horse." + entityID)) {
			returnBool = true;
		}
		return returnBool;
	}
	
	public String getOwner(String entityID) {
		return horsesConfig.getString("horse." + entityID + ".owner");
	}
	
	public void removeHorse(String entityID) {
		if (horsesConfig.contains("horse." + entityID + ".type")) {
			List<String> horses = horsesConfig.getStringList("player." + getOwner(entityID) + ".horses");
			horses.remove(entityID);
			horsesConfig.set("player." + getOwner(entityID) + ".horses", horses);			
		}
		horsesConfig.set("horse." + entityID, null);
		save();
	}
	
	public List<String> getAllowedPlayers(String entityID) {
		if (horsesConfig.contains("horse." + entityID + ".allowed")) {
			return horsesConfig.getStringList("horse." + entityID + ".allowed");
		}
		return null;
	}
	
	private void save(){
		horsesStorage.saveConfig();
	}
	
	
	//Info click
	public void issuedInfoCommand(String player){
		infoClick.put(player, true);
	}
	
	public boolean isInfoClick(String player){
		if (infoClick.containsKey(player)) {
			infoClick.remove(player);
			return true;
		}
		return false;
	}
	
	public void infoHorse(Player player, String horseID) {
		if (hasOwner(horseID)) {
			player.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "This horse belongs to " + getOwner(horseID));
		} else {
			player.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "This horse doesn't have an owner or is not claimed.");
		}
	}
	
	//Fence leads
	public void placedLeashOnFence(String fenceID, String player) {
		horsesConfig.set("fence." + fenceID, player);
		save();
	}
	
	public boolean isOwnerOfLeash(String fenceID, String player) {
		if (horsesConfig.contains("fence." + fenceID)) {
			if (horsesConfig.get("fence." + fenceID).equals(player)) {
				horsesConfig.set("fence." + fenceID, null);
				save();
				return true;
			}
			return false;
		}
		return true;
	}
	
	
	//Donate undead/zombie horse
	private boolean isInConfig(String player) {
		if (horsesConfig.contains("player." + player)) {
			return true;
		}
		return false;
	}
	
	private int allowedSpecialHorses(String player) {
		return horsesConfig.getInt("player." + player + ".allowed");
	}
	
	private int hasSpecialHorses(String player) {
		return horsesConfig.getStringList("player." + player + ".horses").size();
	}
	
	private boolean isVipPlayer(String player) {
		PermissionUser user = PermissionsEx.getUser(player);
		if (user.inGroup("VIP")) {
			return true;
		}
		return false;
	}
	
	public void playerDonatedForHorses(String player) {
		if (isInConfig(player)) {
			setAllowedHorses(player, allowedSpecialHorses(player) + 5);
		} else {
			if (isVipPlayer(player)) {
				setAllowedHorses(player, 6);
			} else {
				setAllowedHorses(player, 5);
			}
		}
	}
	
	private void setAllowedHorses(String player, int number) {
		horsesConfig.set("player." + player + ".allowed", number);
		save();
	}
	
	public boolean isAllowedSpecialHorse(Player player) {
		String playerName = player.getName();
		if (isInConfig(playerName)) {
			if (allowedSpecialHorses(playerName) > hasSpecialHorses(playerName)) {
				return true;
			} else {
				badMsg(player, "You already have " + allowedSpecialHorses(playerName) + " claimed special horses.");
			}
		} else if (isVipPlayer(playerName)) {
			setAllowedHorses(playerName, 1);
			return true;
		} else {
			badMsg(player, "You are not allowed to have special horses. Become VIP or donate for horses @ www.slapgaming.com/donate");
		}
		return false;
	}
	
	private void createSpecialHorse(String horseID, String player, String type) {
		horsesConfig.set("horse." + horseID + ".type", type);
		if (horsesConfig.contains("player." + player + ".horses")) {
			List<String> horses = horsesConfig.getStringList("player." + player + ".horses");
			horses.add(horseID);
			horsesConfig.set("player." + player + ".horses", horses);
		} else {
			ArrayList<String> horses = new ArrayList<>();
			horses.add(horseID);
			horsesConfig.set("player." + player + ".horses", horses);
		}
		save();
	}
	
	public void createSpecialHorseCommand(Horse horse, Player player, String type) {
		String playerName = player.getName();
		if (isAllowedSpecialHorse(player)) {
			if (!(horse.getVariant() == Variant.SKELETON_HORSE || horse.getVariant() == Variant.UNDEAD_HORSE)) {
				if (horse.getCustomName() != null) {
					if (horse.getInventory().getArmor() == null) {
						createSpecialHorse(horse.getUniqueId().toString(), playerName, type);
						switch (type) {
						case "zombie":
							horse.setVariant(Variant.UNDEAD_HORSE);
							break;
						case "skeleton":
							horse.setVariant(Variant.SKELETON_HORSE);
							break;
						}
						player.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + horse.getCustomName() + " mutated into a " + type + " horse...");
					} else {
						badMsg(player, "Remove the armor, a " + type + " horse will not like having that armor on him.");
					}
				} else {
					badMsg(player, "The horse needs to be named using a name tag.");
				}
			} else {
				badMsg(player, "This horse is already a special horse.");
			}
		}
		
	}
	
	private void badMsg(Player player, String message) {
		player.sendMessage(ChatColor.RED + message);
	}
	
	public void specialHorsesStats(Player player) {
		int allowedSpecials = allowedSpecialHorses(player.getName());
		if (allowedSpecials == 0 && isVipPlayer(player.getName())) {
			setAllowedHorses(player.getName(), 1);
			allowedSpecials = 1;
		}
		if (allowedSpecials == 0) {
			player.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "You are not allowed to have any special horses. Want some? Become a VIP or donate for horses @ www.slapgaming.com/donate");
		} else {
			player.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "You currently have " + hasSpecialHorses(player.getName()) + " out of " + allowedSpecials + " special horses.");
		}
		
	}
	
	public void specialHorsesStats(String player, Player mod) {
		int allowedSpecials = allowedSpecialHorses(player);
		if (allowedSpecials == 0 && isVipPlayer(player)) {
			setAllowedHorses(player, 1);
			allowedSpecials = 1;
		}
		if (allowedSpecials == 0) {
			mod.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + player + " is not allowed to have any special horses.");
		} else {
			mod.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + player + " currently has " + hasSpecialHorses(player) + " out of " + allowedSpecials + " special horses.");
		}
	}
	
    @Override
    public void shutdown() {
    	//Not needed
    }
}
