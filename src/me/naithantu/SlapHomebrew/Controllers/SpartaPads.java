package me.naithantu.SlapHomebrew.Controllers;

import me.naithantu.SlapHomebrew.Storage.YamlStorage;
import me.naithantu.SlapHomebrew.Util.Log;
import me.naithantu.SlapHomebrew.Util.Util;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;

public class SpartaPads extends AbstractController {

	//Storage
	private YamlStorage yaml;
	private FileConfiguration config;
	
	//Boost Timer. Keeps a record of the last time someone used a boost.
	//K[Playername] => V[Timestamp of last boost]
	private HashMap<String, Long> lastBoosts;
	
	//SpartaPads
	//K:[Location of pad] => V:[SpartaPad]
	private HashMap<Location, SpartaPad> spartaPadMap;
	//K:[ID of pad] => V:[SpartaPad]
	private HashMap<String, SpartaPad> spartaIdMap;
	
	//K:[Name of player] => V:[Creating SpartaPad]
	private HashMap<String, SpartaPad> creatingSpartaPads; //Map of SpartaPads that are being created
	
	public SpartaPads() {
		//Load storage
		yaml = new YamlStorage(plugin, "SpartaPads");
		config = yaml.getConfig();
		
		//Create maps
		lastBoosts = new HashMap<>();
		spartaPadMap = new HashMap<>();
		spartaIdMap = new HashMap<>();
		creatingSpartaPads = new HashMap<>();
		
		//Load spartapads
		loadSpartaPads();
	}
	
	/**
	 * Load all sparta pads from the config
	 */
	private void loadSpartaPads() {
		ConfigurationSection pads = config.getConfigurationSection("pads");
		if (pads == null) return; //No pads section found
		
		//Loop thru pads
		for (String id : pads.getKeys(false)) {
			//General info
			String creator = pads.getString(id + ".creator");
			long createdTimestamp = pads.getLong(id + ".created");
			
			//Locations
			Location pad = YamlStorage.loadLocationFromConfig(config, "pads." + id + ".pad");
			Location target = YamlStorage.loadLocationFromConfig(config, "pads." + id + ".target");
			
			//Multiplier
			int multiplier = pads.getInt(id + ".multiplier");
			
			//Possible message
			String message = null;
			if (pads.contains(id + ".message")) {
				message = pads.getString(id + ".message");
			}
			
			//Check if all arguments are correct
			if (creator == null || pad == null || target == null) {
				Log.severe("[SpartaPads] Missing arguments for ID: " + id);
				continue; //Skip to next one
			}
			
			//Location checks
			if (pad.getWorld() != target.getWorld()) {
				Log.severe("[SpartaPads] Jumping to different worlds.. ID: " + id);
				continue;
			}
			
			//Check if the fromLocation is a pad
			switch (pad.getBlock().getType()) {
			case WOOD_PLATE: case STONE_PLATE: case GOLD_PLATE: case IRON_PLATE:
				//Everything cool
				break;
			default:
				//Something else
				Log.severe("[SpartaPads] From location is not a pressure plate, ID: " + id);
				continue;
			}
			
			//Create pad
			SpartaPad spartaPad = new SpartaPad(id, creator, createdTimestamp, pad, target, multiplier, message);
			//	=> Put in maps
			spartaPadMap.put(spartaPad.padBlockLocation.getBlock().getLocation(), spartaPad);
			spartaIdMap.put(spartaPad.ID, spartaPad);
		}		
	}
	
	/*
	 *************** 
	 * Get Methods *
	 ***************
	 */
	
	/**
	 * Check if a certain block location is a SpartaPad
	 * @param blockLocation The block location
	 * @return is spartapad
	 */
	public boolean isSpartaPad(Location blockLocation) {
		return spartaPadMap.containsKey(blockLocation);
	}
		
	/**
	 * Check if a certain ID exists as a SpartaPad
	 * @param ID
	 * @return
	 */
	public boolean isSpartaPad(String ID) {
		return spartaIdMap.containsKey(ID.toLowerCase());
	}
	
	/**
	 * Get a SpartaPad based on it's location
	 * @param location The location
	 * @return the SpartaPad or null if none
	 */
	public SpartaPad getSpartaPad(Location location) {
		return spartaPadMap.get(location); //Return the spartapad
	}
	
	/**
	 * Get a SpartaPad based on its ID
	 * @param ID The ID of the pad
	 * @return The SpartaPad or null if none
	 */
	public SpartaPad getSpartaPad(String ID) {
		return spartaIdMap.get(ID.toLowerCase());
	}
	
	/*
	 ****************
	 * Pad Creation *
	 ****************
	 */
	
	/**
	 * Create a new pad
	 * This pad will be semi-filled and will not be saved or be used.
	 * The creator will need to finish the rest of the setup using the other commands.
	 * 
	 * @param ID The ID of the new Pad
	 * @param creator The name of the creator
	 * @param padLocation The location of the pressure plate //Assumes it is already checked
	 * @param multiplier A possible multiplier (will default to 1 if null), must be between 1 <=> 10
	 */
	public void createPad(String ID, String creator, Location padLocation, Integer multiplier) {
		//Create a partially empty SpartaPad
		SpartaPad creationPad = new SpartaPad(ID.toLowerCase(), creator, System.currentTimeMillis(), padLocation, null, 1, null);
		
		//Set the multiplier if there's any
		if (multiplier != null) {
			creationPad.multiplier = multiplier;
		} else {
			creationPad.multiplier = 1;
		}
		
		//Put in map
		creatingSpartaPads.put(creator, creationPad);
	}
	
	/**
	 * Finish the creation of a new pad
	 * @param creator The name of the creator
	 * @param targetLocation The TargetLocation to launch to
	 * @return finished
	 */
	public boolean finishPadCreation(String creator, Location targetLocation) {
		//Get the created SpartaPad
		SpartaPad creationPad = creatingSpartaPads.get(creator);
		
		//Check if from => to are in the same world
		if (!creationPad.getPadBlockLocation().getWorld().getName().equals(targetLocation.getWorld().getName())) {
			return false;
		}
		
		//Set the target & calculate the vector
		creationPad.targetLocation = targetLocation;
		creationPad.calculateVector();
		
		//Remove from creatingMap
		creatingSpartaPads.remove(creator);
		
		//Move to new maps
		spartaIdMap.put(creationPad.getID(), creationPad);
		spartaPadMap.put(creationPad.getPadBlockLocation().getBlock().getLocation(), creationPad);
		
		//Save to YML
		String savePath = "pads." + creationPad.getID() + ".";
		
		//	=> General info
		config.set(savePath + "creator", creationPad.getCreator());
		config.set(savePath + "created", creationPad.getCreatedTimestamp());
		
		//	=> Locations
		YamlStorage.putLocationInConfig(config, savePath + "pad", creationPad.getPadBlockLocation());
		YamlStorage.putLocationInConfig(config, savePath + "target", creationPad.getTargetLocation());
		
		//	=> Multiplier
		config.set(savePath + "multiplier", creationPad.getMultiplier());
		
		//Save
		yaml.saveConfig();
		return true;
	}
	
	/**
	 * Stop the creation of a SpartaPad
	 * @param creator The name of the creator
	 */
	public void cancelPadCreation(String creator) {
		creatingSpartaPads.remove(creator);
	}
	
	/**
	 * Check if the player is creating a pad
	 * @param creator The name of the creator
	 * @return is creating a pad
	 */
	public boolean isCreatingSpartaPad(String creator) {
		return creatingSpartaPads.containsKey(creator);
	}
	
	/**
	 * Check if a pad is already being created with this ID
	 * @param ID The ID
	 * @return is being created
	 */
	public boolean isCreatingSpartaPadID(String ID) {
		for (SpartaPad pad : creatingSpartaPads.values()) {
			if (pad.getID().equals(ID)) return true;
		}
		return false;
	}
	
	/**
	 * Remove a SpartaPad
	 * @param pad The pad
	 */
	public void removeSpartaPad(SpartaPad pad) {
		//Remove from maps
		spartaIdMap.remove(pad.getID());
		spartaPadMap.remove(pad.getPadBlockLocation().getBlock().getLocation());
		
		//Remove from config
		config.set("pads." + pad.getID(), null);
		yaml.saveConfig();
	}
		
	/**
	 * Class containing all info of a SpartaPad
	 */
	public class SpartaPad {
		
		//General info
		private String ID;
		private String creator;
		private long createdTimestamp;
		
		//From => To Locations
		private Location padBlockLocation;
		private Location targetLocation;
		
		//Boost vector
		private int multiplier;
		private Vector launchVector;
		
		//Possible message on boost
		private String message;
		
		public SpartaPad(String ID, String creator, long createdTimestamp, Location padBlockLocation, Location targetLocation, int multiplier, String message) {
			this.ID = ID;
			this.creator = creator;
			this.createdTimestamp = createdTimestamp;
			this.padBlockLocation = padBlockLocation;
			this.targetLocation = targetLocation;
			this.multiplier = multiplier;
			this.message = message;
			
			//Calculate vector
			if (padBlockLocation != null && targetLocation != null) {
				calculateVector();
			}
		}
		
		/**
		 * Method that calculates the launch vector
		 */
		private void calculateVector() {
			double dX = padBlockLocation.getX() - targetLocation.getBlockX();
			double dY = padBlockLocation.getY() - targetLocation.getY();
			double dZ = padBlockLocation.getZ() - targetLocation.getZ();
			
			double yaw = Math.atan2(dZ, dX);
			double pitch = Math.atan2(Math.sqrt(dZ * dZ + dX * dX), dY) + Math.PI;
					
			double vX = Math.sin(pitch) * Math.cos(yaw);
			double vY = Math.sin(pitch) * Math.sin(yaw);
			double vZ = Math.cos(pitch);
			
			//Create the new vector
			Vector vector = new Vector(vX, vZ, vY);
			
			//Multiply it
			launchVector = vector.multiply(multiplier);
		}
		
		/**
		 * Launch a player
		 * @param p The player
		 */
		public void launch(final Player p) {
			//Check for last boost
			if (lastBoosts.containsKey(p.getName())) {
				long lastBoost = lastBoosts.get(p.getName());
				if (System.currentTimeMillis() - lastBoost < 500) {
					//Can't use boost yet
					return;
				}
			}
			
			//Set lastBoost
			lastBoosts.put(p.getName(), System.currentTimeMillis());
			
			//Launch with 1 tick delay
			Util.runASyncLater(new Runnable() {
				
				@Override
				public void run() {
					//Set the velocity of the player
					p.setVelocity(launchVector);
					
					//If message available, send it
					if (message != null) {
						p.sendMessage(message);
					}
				}
			}, 1);			
		}
		
		/**
		 * Get the ID of the spartapad
		 * @return the ID
		 */
		public String getID() {
			return ID;
		}
		
		/**
		 * Get the timestamp this spartapad was created
		 * @return the time
		 */
		public long getCreatedTimestamp() {
			return createdTimestamp;
		}
		
		/**
		 * Get the name of the player who made this spartapad
		 * @return the name
		 */
		public String getCreator() {
			return creator;
		}
		
		/**
		 * Get the message of this spartapad 
		 * @return the message or null
		 */
		public String getMessage() {
			return message;
		}
		
		/**
		 * Set the message the player gets upon launch
		 * @param message the message
		 */
		public void setMessage(String message) {
			this.message = ChatColor.translateAlternateColorCodes('&', message);
			
			//Set in config
			config.set("pads." + ID + ".message", message);
			yaml.saveConfig();
		}
		
		/**
		 * Get the current vector multiplier
		 * @return the multiplier
		 */
		public int getMultiplier() {
			return multiplier;
		}
		
		/**
		 * Set the multiplier of this spartapad, range: 1 <=> 10
		 * This recalculates the launch vector.
		 * @param multiplier The multiplier
		 */
		public void setMultiplier(int multiplier) {
			//Multiplier check
			if (multiplier > 10) {
				multiplier = 10;
			} else if (multiplier < 1) {
				multiplier = 1;
			}
			
			if (multiplier == this.multiplier) return; //Ignore if same multiplier
			
			this.multiplier = multiplier;
			
			//Set in config
			config.set("pads." + ID + ".multiplier", multiplier);
			yaml.saveConfig();
			
			
			//Recalc vector
			calculateVector();
		}
		
		/**
		 * Get the location where the pad is
		 * @return the location
		 */
		public Location getPadBlockLocation() {
			return padBlockLocation;
		}
		
		/**
		 * Get the target location
		 * @return the target
		 */
		public Location getTargetLocation() {
			return targetLocation;
		}
		
		/**
		 * Set a new target location
		 * This recalculates the launch vector.
		 * @param targetLocation the location
		 */
		public void setTargetLocation(Location targetLocation) {
			this.targetLocation = targetLocation;
			
			//Calc Vector
			calculateVector();
			
			//Set in config
			YamlStorage.putLocationInConfig(config, "pads." + ID + ".target", targetLocation);
			yaml.saveConfig();
		}
		
	}
	
	

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub

	}

}
