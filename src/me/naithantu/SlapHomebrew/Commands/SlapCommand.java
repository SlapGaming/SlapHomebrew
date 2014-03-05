package me.naithantu.SlapHomebrew.Commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import me.naithantu.SlapHomebrew.TabFidling;
import me.naithantu.SlapHomebrew.Commands.Basics.SpawnCommand;
import me.naithantu.SlapHomebrew.Commands.Exception.CommandException;
import me.naithantu.SlapHomebrew.Commands.Exception.UsageException;
import me.naithantu.SlapHomebrew.Controllers.Lottery;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogger;
import me.naithantu.SlapHomebrew.Controllers.TabController.TabGroup;
import me.naithantu.SlapHomebrew.Controllers.MessageStringer.MultiChatCombiner;
import me.naithantu.SlapHomebrew.PlayerExtension.PlayerControl;
import me.naithantu.SlapHomebrew.PlayerExtension.SlapPlayer;
import me.naithantu.SlapHomebrew.Util.Util;
import net.minecraft.server.v1_7_R1.EntityPlayer;
import net.minecraft.server.v1_7_R1.PacketPlayOutSpawnEntityLiving;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Ocelot.Type;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import fr.ribesg.bukkit.ntheendagain.NTheEndAgain;

public class SlapCommand extends AbstractCommand {

	Integer used = 0;
	HashSet<String> vipItemsList = new HashSet<String>();
	public static HashSet<String> retroBow = new HashSet<String>();
	
	private BukkitTask cancelableTask;

	public SlapCommand(CommandSender sender, String[] args) {
		super(sender, args);
	}

	public boolean handle() throws CommandException {
		if (args.length == 0) {
			hMsg("SlapHomebrew is a plugin which adds extra commands to the SlapGaming Server.");
			hMsg("Current SlapHomebrew Version: " + plugin.getDescription().getVersion());
			return true;
		}
		
		//Values
		OfflinePlayer offPlayer; EntityPlayer nmsPlayer; Player targetPlayer;
		
		switch (args[0].toLowerCase()) {
		case "a":
			new TabFidling().run(getPlayer());
			break;
		
		case "reload": //Reload the plugin
			testPermission("manage");
			Bukkit.getPluginManager().disablePlugin(plugin);
			Bukkit.getPluginManager().enablePlugin(plugin);
			msg(ChatColor.GRAY + "SlapHomebrew has been reloaded...");
			break;
			
		case "removeocelots": //Remove all the untamed ocelots
			testPermission("removeocelots");
			int ocelotsRemoved = 0;
			for (World serverWorld : Bukkit.getWorlds()) { //Loop thru worlds
				for (Entity entity : serverWorld.getEntities()) { //Loop thru entities of the world
					if (entity instanceof Ocelot) { //See if ocelot
						Ocelot ocelot = (Ocelot) entity;
						if(ocelot.getOwner() == null){ //Check if they don't have an owner
							entity.remove();
							ocelotsRemoved++;
						}
					}
				}
			}
			hMsg("You have removed " + ocelotsRemoved + " ocelots!");
			break;
			
		case "tabupdate": case "updatetab": //Force update a player's tab group
			testPermission("updatetab");
			if (args.length != 2) throw new UsageException("slap updatetab [player]");
			Player updateTabPlayer = getOnlinePlayer(args[1], false);
			plugin.getTabController().playerSwitchGroup(updateTabPlayer);
			break;
			
		case "cleartab": //Force reset the tab.
			testPermission("updatetab");
			plugin.getTabController().reEnable();
			break;
			
		case "maxplayers": case "setmaxplayers": //Set max players (in tab & list)
			testPermission("setmaxplayers");
			if (args.length != 2) throw new UsageException("slap setmaxplayers [number]");
			int maxPlayers = parseIntPositive(args[1]);
			plugin.getTabController().setMaxPlayers(maxPlayers);
			break;
			
		case "header": //Broadcast a message with the [SLAP] header
			testPermission("header");
			if (args.length < 2) throw new UsageException("slap header [msg...]");
			String msg = Util.buildString(args, " ", 1);
			Util.broadcastHeader(ChatColor.translateAlternateColorCodes('&', msg));
			break;
			
		case "showmessage": case "showmsg": case "sendmsg": //Send a message to a player
			testPermission("showmessage");
			if (args.length < 3) throw new UsageException("slap showmessage [player] [message].."); //Usage
			targetPlayer = getOnlinePlayer(args[1], false);
			targetPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', Util.buildString(args, " ", 2))); //Send message
			break;
			
		case "setrw": case "rw": case "resourceworld": //Set resource world number
			testPermission("setrw");
			if (args.length != 2) throw new UsageException("slap setrw [world number]");
			int worldnumber = parseIntPositive(args[1]);
			SpawnCommand.setResourceWorldName("world_resource" + worldnumber); //Set in command
			plugin.getConfig().set("resourceworld", "world_resource" + worldnumber); //Set in config
			plugin.saveConfig();
			hMsg("/spawn rw has been set to: world_resource" + worldnumber);
			break;
			
		case "tabgroup": //Set|Check the TabGroup of a SuperAdmin
			testPermission("tabgroup");
			if (args.length != 2 && args.length != 3) throw new UsageException("slap tabgroup [Player] <group> " + ChatColor.GRAY + "(See /slap tabgroups for groups)");
			offPlayer = getOfflinePlayer(args[1]);
			PermissionUser pexUser = PermissionsEx.getUser(offPlayer.getName());
			if (pexUser == null || !pexUser.getGroups()[0].getName().equals("SuperAdmin")) throw new CommandException("This player is not a SuperAdmin.");
			if (args.length == 2) { //Checking tabgroup
				TabGroup group = plugin.getPlayerLogger().getSuperAdminGroup(offPlayer.getName());
				if (group == null) throw new CommandException("This player is in the owners Tab (Not registered)");
				hMsg("This player is in the TabGroup: " + group.toString());
			} else { //Seting tabgroup
				if (!plugin.getPlayerLogger().setSuperAdminGroup(offPlayer.getName(), args[2])) throw new CommandException("nvalid group. See: /slap tabgroups");
				hMsg("Group has been set.");
				if (offPlayer.getPlayer() != null) plugin.getTabController().playerSwitchGroup(offPlayer.getPlayer()); //Refresh Tab if player is online
			}
			break;
			
		case "tabgroups": //Get a list of all the tabgroups
			testPermission("tabgroup");
			hMsg("Tabgroups: " + Arrays.toString(TabGroup.values()));
			break;
			
		case "commandinfo": case "command": //Get the plugin this command belongs to
			testPermission("commandinfo");
			if (args.length != 2) throw new UsageException("slap commandinfo [command]"); //Usage
			String commandLc = args[1].toLowerCase();
			PluginCommand command = plugin.getServer().getPluginCommand(commandLc); //Try to get the command
			if (command == null) { //If no command found
				throw new CommandException("No command found.");
			}
			hMsg("This command belongs to the plugin: " + command.getPlugin().getName()); //Message which plugin this command belongs to
			break;
			
		case "spawnenderdragon": //Spawn a new enderdragon
			testPermission("spawnenderdragon");
			Util.runASync(new Runnable() {
				
				@Override
				public void run() {
					try {
						final Server server = plugin.getServer();
						Plugin foundPlugin = server.getPluginManager().getPlugin("NTheEndAgain"); //Find NTheEndAgain
						if (foundPlugin == null || !(foundPlugin instanceof NTheEndAgain) || !foundPlugin.isEnabled()) throw new CommandException("NTheEndAgain plugin was not found.");
						NTheEndAgain theEnd = (NTheEndAgain) foundPlugin;
						int x = theEnd.getWorldHandlers().get("worldTheEnd").getNumberOfAliveEnderDragons(); //Get number of alive enderdragons
						if (x == 0) { //If 0 then spawn a new one
							Util.broadcastHeader("Regenerating the end.. Possible lag incoming.");
							server.dispatchCommand(server.getConsoleSender(), "nend regen world_the_end");
							Util.runLater(new Runnable() {
								@Override
								public void run() {
									server.dispatchCommand(server.getConsoleSender(), "nend respawnED world_the_end"); //Spawn a new Enderdragon 30 secs after regenerating the end
								}
							}, 30 * 20);
						}
					} catch (CommandException e) {
						Util.badMsg(sender, e.getMessage());
					} catch (NullPointerException e) {
						Util.badMsg(sender, "Something went wrong. Exception: " + e.getMessage());
					}
				}
			});
			break;
			
		case "crash": //Crash a client
			testPermission("crash");
			if (args.length < 2) throw new UsageException("slap crash [player] <portal>"); //Usage
			final Player crashPlayer = getOnlinePlayer(args[1], false);
			String targetName = crashPlayer.getName();
			if (targetName.equals("Stoux2") || targetName.equals("Telluur") || targetName.equals("naithantu")) throw new UsageException("That would be a no."); //Can't use on us.
			
			boolean portal = (args.length == 3 && args[2].equalsIgnoreCase("portal")); //Check if portal
			
			if (portal) { //Portal Story
				cancelableTask = Util.runTimer(new Runnable() {
					private int x = 0;
					@Override
					public void run() {
						if (!crashPlayer.isOnline()) x = 9001; //Will call default -> Cancel the timer.
						switch (x) {
						case 0: Util.badMsg(crashPlayer, "This was a triumph."); break;
						case 1: Util.badMsg(crashPlayer, "I'm making a note here: HUGE SUCCESS."); break;
						case 2: Util.badMsg(crashPlayer, "It's hard to overstate my satisfaction."); break;
						case 3: Util.badMsg(crashPlayer, "Aperture science:"); break;
						case 4: Util.badMsg(crashPlayer, "We do what we must because we can"); break;
						case 5: Util.badMsg(crashPlayer, "For the good of all of us"); break;
						case 6: Util.badMsg(crashPlayer, "Except the ones who are CRASHED"); break;
						case 7:
							EntityPlayer nmsPlayer = ((CraftPlayer) crashPlayer).getHandle(); //Get ServerPlayer
							nmsPlayer.playerConnection.sendPacket(new PacketPlayOutSpawnEntityLiving(nmsPlayer)); //Send to the player that they spawned.
							break;
						default:
							cancelableTask.cancel();
						}
						x++;
					}
				}, 0, 80);
			} else { //Just crash.
				nmsPlayer = ((CraftPlayer) crashPlayer).getHandle(); //Get ServerPlayer
				nmsPlayer.playerConnection.sendPacket(new PacketPlayOutSpawnEntityLiving(nmsPlayer)); //Send to the player that they spawned.
			}
			hMsg("You've crashed " + targetName + (portal ? " with a very special portal story." : "."));
			break;
			
		case "toldstatus": case "told": //Send a told status
			testPermission("toldstatus");
			if (args.length < 3) throw new UsageException("slap toldstatus [Player] [Interval] [Don't stop when player goes offline]"); //Usage
			targetPlayer = getOnlinePlayer(args[1], false); //Get player
			
			boolean ticks = false;
			int interval;
			if (args[2].toLowerCase().contains("t")) { //Contains t -> It's ticks
				ticks = true;
				interval = parseIntPositive(args[2].toLowerCase().replaceFirst("t", "")); //Remove the t & parse the ticks
			} else { //Seconds
				interval = parseIntPositive(args[2]);
			}
			
			boolean stop = true;
			if (args.length > 3) { //Check if stop is specified
				stop = false;
			}
			
			plugin.getToldStatus().sendToldStatus(targetPlayer, (ticks ? interval : interval * 20), stop); //Send toldstatus
			hMsg("Sending told status to " + targetPlayer.getName() + ". Interval: " + interval + (ticks ? " ticks." : " seconds."));
			break;
			
		case "stoptoldstatus": case "stoptold": //Stop a told status sender
			testPermission("stoptoldstatus"); //Perms
			if (args.length != 2) throw new UsageException("slap stoptoldstatus [Player]"); //usage
			offPlayer = getOfflinePlayer(args[1]); //Get player
			plugin.getToldStatus().stopToldStatus(offPlayer.getName()); //Stop told status
			hMsg("Stopping told status for player: " + offPlayer.getName());
			break;
			
		case "sudo": case "sudochat": case "chat": //Talk as a different player
			testPermission("sudochat"); //Perms
			if (args.length < 3) throw new UsageException("slap sudochat [Player] <Message...>"); //Usage
			targetPlayer = getOnlinePlayer(args[1], false); //Get player
			targetPlayer.chat(Util.buildString(args, " ", 2)); //Chat as other player
			break;
			
		default: //Player issued commands
			final Player player = getPlayer();
			final String playername = player.getName();

			//Variables
			World world; Location spawnLocation; int x, i, mobs; EntityType mobType; boolean bool; Block targetBlock;
			switch(args[0].toLowerCase()) {
			case "retrobow": //Shoot arrows from your hand on interact event
				testPermission("retrobow");
				if (bool = retroBow.contains(playername)) retroBow.remove(playername); //Turn retrobow off
				else retroBow.add(playername); //Turn retrobow on
				hMsg("Retrobow mode has been turned " + (bool ? "off!" : "on!"));
				break;
				
			case "wolf": case "wolfs": //Spawn a horde of wolf pets
				testPermission("wolfhorde");
				if (args.length != 2) throw new UsageException("slap wolf [amount]");
				mobs = parseIntPositive(args[1]);
				if (mobs > 20) throw new CommandException("You can only spawn max 20 wolfs at once.");
				spawnLocation = player.getEyeLocation();
				world = spawnLocation.getWorld();
				for (x = 0; x < mobs; x++) { //Spawn wolfs
					world.spawn(spawnLocation, Wolf.class).setOwner(player);
				}
				break;
				
			case "cat": case "cats": //Spawn a horde of cat pets
				testPermission("cathorde");
				if (args.length != 3) throw new UsageException("slap cat [siamesecat|blackcat|redcat|wildocelot] [amount]");
				mobs = parseInt(args[2]);
				if (mobs > 20) throw new CommandException("You can only spawn max 20 cats at once.");
				Type type;
				switch (args[1].toLowerCase()) { //Parse type of cat
				case "siamesecat": 	case "siamese":	type = Type.SIAMESE_CAT; 	break;
				case "blackcat":	case "black":	type = Type.BLACK_CAT;		break;
				case "redcat":		case "red":		type = Type.RED_CAT;		break;
				case "wildocelot":	case "wild":	type = Type.WILD_OCELOT;	break;
				default: throw new CommandException("Type not recognized. Types: siamesecat, blackcat, redcat, wildocelot");
				}
				
				spawnLocation = player.getEyeLocation();
				world = spawnLocation.getWorld();
				for (x = 0; x < mobs; x++) { //Spawn cats
					Ocelot cat = (Ocelot) world.spawn(spawnLocation, Ocelot.class);
					cat.setOwner(player);
					cat.setCatType(type);
				}
				break;
				
			case "removewolf": //Remove all wolfs spawned by wolfs command
				testPermission("removewolf");
				for (Entity entity : player.getNearbyEntities(50, 50, 50)) {
					if (entity instanceof Wolf) {
						Wolf wolf = (Wolf) entity;
						if (wolf.getOwner() != null && wolf.getOwner().equals(player)) {
							entity.remove();
						}
					}
				}
				break;
				
			case "removecat": //Remove all cats spawned by cats command
				testPermission("removecat");
				for (Entity entity : player.getNearbyEntities(50, 50, 50)) {
					if (entity instanceof Ocelot) {
						Ocelot ocelot = (Ocelot) entity;
						if (ocelot.getOwner() != null && ocelot.getOwner().equals(player)) {
							entity.remove();
						}
					}
				}
				break;
								
			case "moo": //An extension of /ess moo 
				testPermission("moo");
				Player[] onlinePlayers = Util.getOnlinePlayers(); //Get players
				if (onlinePlayers.length == 0) throw new CommandException("There are no players online."); //Check for players
				for (Player target : onlinePlayers) { //Loop thru players & Send stuff
					target.sendMessage(new String[] { "            (__)", "            (oo)", "   /------\\/", "  /  |      | |", " *  /\\---/\\", "    ~~    ~~", "....\"Have you mooed today?\"..." });
					target.playSound(target.getLocation(), Sound.COW_HURT, 1, 1.0f);
				}
				onlinePlayers[new Random().nextInt(onlinePlayers.length)].chat("Moooooo!"); //Pick a random user & sudo it 'moo'
				break;
				
			case "firemob": //Spawn a horde of entities that are on fire
				testPermission("firemob");
				if (args.length != 3) throw new UsageException("slap firemob [mob] [amount]"); //Check usage
				mobs = parseIntPositive(args[2]); //Get the amount of mobs
				mobType = parseEntityType(args[1].toUpperCase()); //Parse MobType
				targetBlock = Util.getTargetBlock(player, 25); //Get targetBlock
				spawnLocation = targetBlock.getLocation().add(0, 1, 0); //Spawn Loc & World
				world = spawnLocation.getWorld();
				for (i = 0; i < mobs; i++) { //Spawn the mobs
					Entity burningMob = world.spawnEntity(spawnLocation, mobType);
					burningMob.setFireTicks(Integer.MAX_VALUE);
					burningMob.setMetadata("slapFireMob", new FixedMetadataValue(plugin, true));
				}
				break;
				
			case "fly": case "flymob": //Spawn 'flying' mobs (Actually riding bats).
				testPermission("flymob");
				if (args.length != 3) throw new UsageException("slap flymob [mob] [amount]"); //Check usage
				mobs = parseIntPositive(args[2]); //Get the amount of mobs
				mobType = parseEntityType(args[1].toUpperCase()); //Parse MobType
				targetBlock = Util.getTargetBlock(player, 25); //Get targetBlock
				spawnLocation = targetBlock.getLocation().add(0, 1, 0); //Spawn Loc & World
				world = spawnLocation.getWorld();
				for (i = 0; i < mobs; i++) { //Spawn the mobs
					LivingEntity bat = (LivingEntity) world.spawnEntity(spawnLocation, EntityType.BAT);
					bat.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
					Entity passengerMob = world.spawnEntity(spawnLocation, mobType);
					bat.setPassenger(passengerMob);
				}
				break;
				
			case "stackmob": case "mobstack": //Spawn a stack of mobs
				testPermission("stackmob");
				if (args.length < 3) throw new UsageException("slap stackmob [Bottom Mob] ..<Mobs>.. [Top Mob]"); //Usage
				List<EntityType> entityList = new ArrayList<EntityType>();
				for (i = 1; i < args.length; i++) { //Loop thru arguments
					mobType = parseEntityType(args[i].toUpperCase()); //Parse MobType
					entityList.add(mobType); //Add to list
				}
				targetBlock = Util.getTargetBlock(player, 25); //Get block
				spawnLocation = targetBlock.getLocation().add(0, 1, 0); //SpawnLocation & World
				world = player.getWorld();
				
				Entity previousEntity = null;
				for (EntityType entityType : entityList) { //Loop thru EntityTypes
					Entity newEntity = world.spawnEntity(spawnLocation, entityType);
					if (newEntity.getType() == EntityType.BAT) { //If a bat -> Make invisable
						((LivingEntity) newEntity).addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
					}
					if (previousEntity != null) { //If not the first mob, setPassenger of previous one
						previousEntity.setPassenger(newEntity);
					}
					previousEntity = newEntity;
				}
				break;
				
			case "ghost": //Go invisible
				testPermission("ghost");
				HashSet<String> ghosts = plugin.getExtras().getGhosts();
				boolean isGhost = ghosts.contains(playername); //Check if already a ghost
				if (!isGhost) { //Create ghost 
					ghosts.add(playername);
					player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
				} else { //Remove ghost
					ghosts.remove(playername);
					player.removePotionEffect(PotionEffectType.INVISIBILITY);
				}
				hMsg("You are " + (isGhost ? "no longer" : "now") + " a ghost!");
				break;
				
			case "fakelottery": case "fakeroll": //Start a fake lottery
				testPermission("fakelottery");
				Lottery lottery = plugin.getLottery(); //Get lottery
				if (lottery.getPlaying() || lottery.isFakeLotteryPlaying()) throw new CommandException("There is already a lottery running."); //Check if running
				targetPlayer = player;
				if (args.length > 1) { //Check if target given
					targetPlayer = getOnlinePlayer(args[1], false);
				}
				lottery.startFakeLottery(targetPlayer.getName()); //Start fake lottery with target as winner
				break;
				
			case "tableflip": case "fliptable": //Flip a table
				testPermission("tableflip");
				player.chat("(\u256F\u00B0\u25A1\u00B0\uFF09\u256F\uFE35 \u253B\u2501\u253B Table Flip!!"); //Flip the table
				Util.runLater(new Runnable() {
					@Override
					public void run() { //5 Seconds later let someone fix the table
						Player[] onlinePlayers = Util.getOnlinePlayers();
						if (onlinePlayers.length == 0) return; //Check if anyone online
						Random r = new Random();
						Player tPlayer = onlinePlayers[r.nextInt(onlinePlayers.length)];
						String end;
						if (tPlayer.getName().equals(player.getName())) { //Same player
							end = "Sorry about that..";
						} else {
							end = "I Fiiix.";
						}
						tPlayer.chat("\u252C\u2500\u252C\u30CE( \u00BA _ \u00BA\u30CE) " + end);
					}
				}, 100);
				break;
				
			case "spawnhorse": case "horse": //Spawn a maxed-out horse
				testPermission("spawnhorse");
				if (args.length != 2) throw new UsageException("slap spawnhorse [horse|mule|donkey|skeleton|zombie]"); //Usage
				Variant variant;
				switch (args[1].toLowerCase()) {
				case "zombie":		variant = Variant.UNDEAD_HORSE;		break;
				case "skeleton":	variant = Variant.SKELETON_HORSE;	break;
				case "mule":		variant = Variant.MULE;				break;
				case "donkey":		variant = Variant.DONKEY;			break;
				case "horse":		variant = Variant.HORSE;			break;
				default:
					throw new CommandException(args[1] + " is not a valid horsetype. '/slap spawnhorse' for usage help.");
				}
				targetBlock = Util.getTargetBlock(player, 25); //Get targetblock, loc & spawn
				spawnLocation = targetBlock.getLocation().add(0, 1, 0);
				world = player.getWorld();

				Horse horse = (Horse) world.spawnEntity(spawnLocation, EntityType.HORSE); //Spawn horse
				horse.setJumpStrength(2D); //Set horse attributes
				horse.setVariant(variant);
				horse.setTamed(true);
				horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
				horse.setPassenger(player); //Set player as rider
				hMsg("Spawned a " + (variant == Variant.HORSE ? "horse!" : args[1] + " horse!"));
				break;
				
			case "forcerider": case "stackplayers": case "stackplayer": case "forceriders": //Stack players
				testPermission("forcerider");
				String usage = "slap forcerider [Reverse] | [Player1] [Player 2] <Player 3>";
				if (args.length == 1) throw new UsageException(usage); //Usage
				if (args.length == 2) { //Player is trying to force a entity on their own head
					if (!args[1].equalsIgnoreCase("reverse")) throw new UsageException(usage); //Usage 
					LivingEntity targetEntity = null;
					
					List<Block> lineOfSightBlocks = Util.getBlocksInLineOfSight(player, 10); //Get blocks in line of sight
					for (Entity ent : player.getNearbyEntities(10, 10, 10)) { //Get entities
						if (ent instanceof LivingEntity) { //Check if Living
							targetBlock = ent.getLocation().getBlock();
							if (containsBlockRelatives(lineOfSightBlocks, targetBlock)) { //Check if the location is in the line of sight of the player
								targetEntity = (LivingEntity) ent;
								break;
							}
						}
					}
					if (targetEntity == null) throw new CommandException("No entities found in line of sight!"); //No entities found
					ride(targetEntity, player);
				} else {
					world = player.getWorld();
					ArrayList<Player> stacking = new ArrayList<Player>();
					for (x = 1; x < args.length; x++) { //Loop thru arguments
						Player stackPlayer = getOnlinePlayer(args[x], false); //Get the player
						if (stacking.contains(stackPlayer)) throw new CommandException("You've entered the same name twice!"); //Check if not same name
						checkRider(stackPlayer); //Check if free
						if (stackPlayer.getWorld() != world) throw new CommandException(stackPlayer.getName() + " is in a different world."); //Check world
						stacking.add(stackPlayer); //Add to stack
					}
					Player previousStackPlayer = null;
					for (Player stackPlayer : stacking) { //Start stacking players
						if (previousStackPlayer != null) {
							previousStackPlayer.setPassenger(stackPlayer);
						}
						previousStackPlayer = stackPlayer;
					}
				}
				break;
				
			case "kickrider": //Kick a rider/vehicle 
				testPermission("kickrider");
				if (args.length == 1) { //Kick passenger & Vehicle of this player
					player.leaveVehicle();
					player.eject();
				} else if (args.length == 2) { //Else clear a player
					targetPlayer = getOnlinePlayer(args[1], false);
					targetPlayer.eject();
					targetPlayer.leaveVehicle();
				}
				break;
				
			case "commandspy": //Enable/Disable spying on player commands.
				testPermission("commandspy");
				if (args.length == 1) throw new UsageException("slap commandspy [Player] <on|off>"); //Usage
				offPlayer = getOfflinePlayer(args[1]);
				String targetname = offPlayer.getName();
				PlayerLogger pl = plugin.getPlayerLogger(); //Get PL
				boolean isCS = pl.isCommandSpy(targetname);
				if (args.length == 2) {
					hMsg(targetname + " is currently " + (isCS ? ChatColor.GREEN + "a" : ChatColor.RED + "not a") + ChatColor.WHITE + " CommandSpy!");
				} else {
					switch (args[2].toLowerCase()) {
					case "on": //Turn CommandSpy for player on
						if (isCS) throw new CommandException(targetname + " is already a CommandSpy!");
						pl.addCommandSpy(targetname);
						break;
					case "off": //Turn CommandSpy for player off
						if (!isCS) throw new CommandException(targetname + "'s CommandSpy is already disabled.");
						pl.removeFromCommandSpy(targetname);
						break;
					default: //Doing it wrong -> Usage
						throw new UsageException("slap commandspy [Player] <on|off>");
					}
					hMsg("Turned CommandSpy for player " + targetname + ": " + ((isCS) ? ChatColor.RED + "Off." : ChatColor.GREEN + "On.")); //Msg
				}
				break;
				
			case "multimessage": case "doublemsg": case "multimsg": case "*--": //Multi line combined chat message
				testPermission("multimessage");
				SlapPlayer slapPlayer = PlayerControl.getPlayer(player); //Get SlapPlayer
				if (slapPlayer.isCombiningMessage()) { //Check if not already combining a message
					throw new CommandException("Already combining a message!");
				}
				slapPlayer.setMessageCombiner(new MultiChatCombiner(slapPlayer)); //Set new Combiner
				hMsg("You're now creating a multi lined combined message.");
				break;
								
			default: 
				hMsg("SlapHomebrew is a plugin which adds extra commands to the SlapGaming Server.");
				hMsg("Current SlapHomebrew Version: " + plugin.getDescription().getVersion());
			}			
		}
		return true;
	}
	
	/**
	 * Let entity A ride entity B
	 * @param a First entity
	 * @param b Second Entity
	 * @throws CommandException if cannot ride
	 */
	private void ride(LivingEntity a, LivingEntity b) throws CommandException {
		if (a.getEntityId() == b.getEntityId() || a.getPassenger() != null || a.getVehicle() != null || b.getPassenger() != null || b.getVehicle() != null) {
			throw new CommandException("This entity cannot ride the other one.");
		}
		b.setPassenger(a);
	}
	
	/**
	 * Check if a rider has no passenger & no vehicle.
	 * @param a The entity
	 * @throws CommandException if passenger/vehicle
	 */
	private void checkRider(LivingEntity a) throws CommandException {
		if (a.getPassenger() != null || a.getVehicle() != null) {
			throw new CommandException("This entity cannot ride the other one.");
		}
	}

	
	public boolean checkLeatherArmor(PlayerInventory inventory) {
		if (inventory.getBoots() == null || inventory.getLeggings() == null || inventory.getChestplate() == null || inventory.getHelmet() == null)
			return false;
		return inventory.getBoots().getType() == Material.LEATHER_BOOTS && inventory.getLeggings().getType() == Material.LEATHER_LEGGINGS
				&& inventory.getChestplate().getType() == Material.LEATHER_CHESTPLATE && inventory.getHelmet().getType() == Material.LEATHER_HELMET;
	}

	private static boolean containsBlockRelatives(List<Block> l, Block o) {
		boolean returnBool = false;
		if (l.contains(o))
			returnBool = true;
		else {
			for (BlockFace face : BlockFace.values()) {
				if (l.contains(o.getRelative(face))) {
					returnBool = true;
					break;
				}
			}
		}
		return returnBool;
	}
	
}