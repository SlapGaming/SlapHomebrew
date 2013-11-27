package me.naithantu.SlapHomebrew.Commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Commands.Basics.SpawnCommand;
import me.naithantu.SlapHomebrew.Controllers.Book;
import me.naithantu.SlapHomebrew.Controllers.Lottery;
import me.naithantu.SlapHomebrew.Controllers.PlayerLogger;
import me.naithantu.SlapHomebrew.Controllers.TabController.TabGroup;
import me.naithantu.SlapHomebrew.Runnables.RainbowTask;
import me.naithantu.SlapHomebrew.Storage.YamlStorage;
import me.naithantu.SlapHomebrew.Util.Util;
import net.minecraft.server.v1_6_R3.EntityPlayer;
import net.minecraft.server.v1_6_R3.Packet24MobSpawn;
import net.minecraft.server.v1_6_R3.Packet70Bed;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_6_R3.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Ocelot.Type;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import com.earth2me.essentials.User;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import fr.ribesg.bukkit.ntheendagain.NTheEndAgain;

public class SlapCommand extends AbstractCommand {

	Integer used = 0;
	HashSet<String> vipItemsList = new HashSet<String>();
	public static HashSet<String> retroBow = new HashSet<String>();
	private static Lottery lottery;

	public SlapCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
		if (lottery == null) {
			lottery = plugin.getLottery();
		}
	}

	public boolean handle() {
		String arg;
		try {
			arg = args[0];
		} catch (ArrayIndexOutOfBoundsException e) {
			this.msg(sender, "SlapHomebrew is a plugin which adds extra commands to the SlapGaming Server.");
			this.msg(sender, "Current SlapHomebrew Version: " + plugin.getDescription().getVersion());
			return true;
		}

		if (arg.equalsIgnoreCase("reload")) {
			if (this.testPermission(sender, "manage")) {
				Bukkit.getPluginManager().disablePlugin(plugin);
				Bukkit.getPluginManager().enablePlugin(plugin);
				sender.sendMessage(ChatColor.GRAY + "SlapHomebrew has been reloaded...");
			}
		}

		switch (arg.toLowerCase()) {
		case "removeocelots":
			if (!testPermission(sender, "removeocelots")) {
				noPermission(sender);
				return true;
			}
			int ocelotsRemoved = 0;
			for (World serverWorld : Bukkit.getWorlds()) {
				for (Entity entity : serverWorld.getEntities()) {
					if (entity instanceof Ocelot) {
						Ocelot ocelot = (Ocelot) entity;
						if(ocelot.getOwner() == null){
							entity.remove();
							ocelotsRemoved++;
						}
					}
				}
			}
			Util.msg(sender, "You have removed " + ocelotsRemoved + " ocelots!");
			break;
		case "tabupdate": case "updatetab":
			if (!testPermission(sender, "updatetab")) {
				noPermission(sender);
				return true;
			}
			if (args.length < 2) {
				badMsg(sender, "Usage: /slap updatetab [player]");
				return true;
			}
			Player updateTabPlayer = plugin.getServer().getPlayer(args[1]);
			if (updateTabPlayer == null) {
				badMsg(sender, "This player is not online.");
			} else {
				plugin.getTabController().playerSwitchGroup(updateTabPlayer);
			}
			break;
		case "cleartab":
			if (!testPermission(sender, "updatetab")) {
				noPermission(sender);
				return true;
			}
			plugin.getTabController().reEnable();
			break;
		case "maxplayers": case "setmaxplayers":
			if (!testPermission(sender, "setmaxplayers")) {
				noPermission(sender);
				return true;
			}
			if (args.length != 2) {
				badMsg(sender, "Usage: /slap SetMaxPlayers [Number of Players]");
				return true;
			}
			try {
				int maxPlayers = Integer.parseInt(args[1]);
				plugin.getTabController().setMaxPlayers(maxPlayers);
			} catch (NumberFormatException e) {
				badMsg(sender, args[1] + " is not a valid number!");
			}
			break;
		case "header":
			if (!testPermission(sender, "header")) {
				noPermission(sender);
				return true;
			}
			if (args.length < 2) {
				badMsg(sender, "Usage: /slap header [msg..]");
				return true;
			}
			String msg = args[1]; int xCount = 2;
			while (xCount < args.length) {
				msg = msg + " " + args[xCount];
				xCount++;
			}
			plugin.getServer().broadcastMessage(Util.getHeader() + msg);
			break;
		case "promotions":
			if (!testPermission(sender, "promotions")) {
				noPermission(sender);
				return true;
			}
			if (args.length != 2) {
				badMsg(sender, "Usage: /slap promotions [number of promotions]");
				return true;
			}
			try {
				final int nrOfPromtions = Integer.parseInt(args[1]);
				if (nrOfPromtions < 1) throw new NumberFormatException();
				Util.runASync(plugin, new Runnable() {
					
					@Override
					public void run() {
						plugin.getPlayerLogger().getPromotions(sender, nrOfPromtions);
					}
				});
			} catch (NumberFormatException ex) {
				badMsg(sender, args[1] + " is not a valid number.");
				return true;
			}
			break;
		case "setrw": case "rw": case "resourceworld":
			if (!testPermission(sender, "setrw")) {
				noPermission(sender);
				return true;
			}
			if (args.length != 2) {
				badMsg(sender, "Usage: /slap setrw [worldnumber]");
				return true;
			}
			try {
				int worldnumber = Integer.parseInt(args[1]);
				if (worldnumber < 1) throw new NumberFormatException();
				SpawnCommand.setResourceWorldName("world_resource" + worldnumber);
				plugin.getConfig().set("resourceworld", "world_resource" + worldnumber);
				plugin.saveConfig();
				sender.sendMessage(Util.getHeader() + "/spawn rw has been set to: world_resource" + worldnumber);
			} catch (NumberFormatException e) {
				badMsg(sender, args[1] + " is not a valid number.");
			}
			break;
		case "tabgroup":
			if (!testPermission(sender, "tabgroup")) {
				noPermission(sender); 
				return true;
			}
			switch (args.length) {
			case 2: case 3:
				User user = plugin.getEssentials().getUserMap().getUser(args[1]);
				if (user == null) {
					badMsg(sender, args[1] + " has never been on the server.");
					return true;
				}
				PermissionUser pexUser = PermissionsEx.getUser(user.getName());
				if (pexUser.getGroups()[0].getName().equals("SuperAdmin")) {
					switch (args.length) {
					case 2: 
						TabGroup group = plugin.getPlayerLogger().getSuperAdminGroup(user.getName());
						if (group == null) {
							badMsg(sender, "This player is in the owners Tab (Not registered)");
						} else {
							sender.sendMessage(Util.getHeader() + "This player is in the TabGroup: " + group.toString());
						}
						break;
					case 3:
						if (plugin.getPlayerLogger().setSuperAdminGroup(user.getName(), args[2])) {
							sender.sendMessage(Util.getHeader() + "Group has been set.");
							plugin.getTabController().reEnable();
						} else {
							badMsg(sender, "Invalid group. See: /slap tabgroups");
						}
						break;
					}
				} else {
					badMsg(sender, "This player is not a SuperAdmin.");
				}
				break;
			default:
				badMsg(sender, "Usage: /slap tabgroup [Player] <group>");
				badMsg(sender, "See tabgroups for all the groups: /slap tabgroups");
			}
			break;
		case "tabgroups":
			if (!testPermission(sender, "tabgroup")) {
				noPermission(sender);
				return true;
			}
			sender.sendMessage("Tabgroups: " + Arrays.toString(TabGroup.values()));
			break;
		case "spawnenderdragon":
			if (!testPermission(sender, "spawnenderdragon")) {
				noPermission(sender);
				return true;
			}
			Util.runASync(plugin, new Runnable() {
				
				@Override
				public void run() {
					final Server server = plugin.getServer();
					Plugin foundPlugin = server.getPluginManager().getPlugin("NTheEndAgain");
					if (foundPlugin != null && foundPlugin instanceof NTheEndAgain) {
						NTheEndAgain theEnd = (NTheEndAgain) foundPlugin;
						try {
							int x = theEnd.getWorldHandlers().get("worldTheEnd").getNumberOfAliveEnderDragons();
							if (x == 0) {
								server.dispatchCommand(server.getConsoleSender(), "nend regen world_the_end");
								Util.runLater(plugin, new Runnable() {
									
									@Override
									public void run() {
										server.dispatchCommand(server.getConsoleSender(), "nend respawnED world_the_end");
									}
								}, 30 * 20);
							}
						} catch (NullPointerException e) {
							badMsg(sender, "Something went wrong.. Exception: " + e.getMessage());
						}
					} else {
						badMsg(sender, "Couldn't find the EndAgain plugin.");
					}
				}
			});
			break;
		default:
			if (!(sender instanceof Player)) {
				this.badMsg(sender, "You need to be in-game to do that!");
				return true;
			}

			final Player player = (Player) sender;
			World world;
			Location spawnLocation;
			int x;
			int i;
			int mobs;
			EntityType mobType;
			String mob;

			switch (arg.toLowerCase()) {
			case "retrobow":
				if (!testPermission(player, "fun")) {
					this.noPermission(sender);
					return true;
				}
				if (!retroBow.contains(player.getName())) {
					retroBow.add(player.getName());
					this.msg(sender, "Retrobow mode has been turned on!");
				} else {
					retroBow.remove(player.getName());
					this.msg(sender, "Retrobow mode has been turned off!");
				}
				break;
			case "wolf":
				world = player.getWorld();
				if (!testPermission(player, "fun")) {
					this.noPermission(sender);
					return true;
				}
				try {
					arg = args[1];
				} catch (ArrayIndexOutOfBoundsException e) {
					// e.printStackTrace();
				}
				spawnLocation = player.getEyeLocation();
				try {
					Integer.valueOf(arg);
				} catch (NumberFormatException e) {
					world.spawn(spawnLocation, Wolf.class).setOwner(player);
					return true;
				}
				if (Integer.valueOf(arg) <= 20) {
					for (x = 0; x < Integer.valueOf(arg); x++) {
						world.spawn(spawnLocation, Wolf.class).setOwner(player);
					}
				} else {
					player.sendMessage(ChatColor.RED + "You are not allowed to spawn more then 20 wolves at the same time!");
				}
				break;
			case "cat":
				world = player.getWorld();
				Type type = null;
				if (!testPermission(player, "fun")) {
					this.noPermission(sender);
					return true;
				}
				try {
					arg = args[1];
				} catch (ArrayIndexOutOfBoundsException e) {
					return true;
				}
				if (arg.equalsIgnoreCase("siamesecat")) {
					type = Type.SIAMESE_CAT;
				} else if (arg.equalsIgnoreCase("blackcat")) {
					type = Type.BLACK_CAT;
				} else if (arg.equalsIgnoreCase("redcat")) {
					type = Type.RED_CAT;
				} else if (arg.equalsIgnoreCase("wildocelot")) {
					type = Type.WILD_OCELOT;
				} else {
					player.sendMessage(ChatColor.RED + "Type not recognized. Only siamesecat, blackcat, wildocelot and redcat are allowed!");
				}
				if (type != null) {
					spawnLocation = player.getEyeLocation();
					try {
						arg = args[2];
					} catch (ArrayIndexOutOfBoundsException e) {
						world.spawn(spawnLocation, Ocelot.class);
						for (Entity entity : player.getNearbyEntities(1, 1, 1)) {
							if (entity instanceof Ocelot) {
								((Tameable) entity).setOwner(player);
								((Ocelot) entity).setCatType(type);
							}
						}
						return true;
					}
					try {
						Integer.valueOf(arg);
					} catch (NumberFormatException e) {
						world.spawn(spawnLocation, Ocelot.class);
						for (Entity entity : player.getNearbyEntities(1, 1, 1)) {
							if (entity instanceof Ocelot) {
								((Tameable) entity).setOwner(player);
								((Ocelot) entity).setCatType(type);
							}
						}
						return true;
					}
					if (Integer.valueOf(arg) <= 20) {
						for (x = 0; x < Integer.valueOf(arg); x++) {
							world.spawn(spawnLocation, Ocelot.class);
							for (Entity entity : player.getNearbyEntities(1, 1, 1)) {
								if (entity instanceof Ocelot) {
									((Tameable) entity).setOwner(player);
									((Ocelot) entity).setCatType(type);
								}
							}
						}
					} else {
						player.sendMessage(ChatColor.RED + "You are not allowed to spawn more then 20 cats at the same time!");
					}
				}
				break;
			case "removewolf":
				if (!testPermission(player, "fun")) {
					this.noPermission(sender);
					return true;
				}
				for (Entity entity : player.getNearbyEntities(50, 50, 50)) {
					if (entity instanceof Wolf) {
						Wolf wolf = (Wolf) entity;
						if (wolf.getOwner() != null && wolf.getOwner().equals(player)) {
							entity.remove();
						}
					}
				}
				break;
			case "removecat":
				if (!testPermission(player, "fun")) {
					this.noPermission(sender);
					return true;
				}
				for (Entity entity : player.getNearbyEntities(50, 50, 50)) {
					if (entity instanceof Ocelot) {
						Ocelot ocelot = (Ocelot) entity;
						if (ocelot.getOwner() != null && ocelot.getOwner().equals(player)) {
							entity.remove();
						}
					}
				}
				break;
			case "notify":
				if (!testPermission(player, "notify")) {
					this.noPermission(sender);
					return true;
				}
				player.playSound(player.getLocation(), Sound.NOTE_PIANO, 1, 1);
				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
					public void run() {
						player.playSound(player.getLocation(), Sound.NOTE_PIANO, 1, 2);
						plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
							public void run() {
								player.playSound(player.getLocation(), Sound.NOTE_PIANO, 1, 1);
								plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
									public void run() {
										player.playSound(player.getLocation(), Sound.NOTE_PIANO, 2000, 2);
									}
								}, 5);
							}
						}, 5);
					}
				}, 5);
				break;
			case "savebook":
				if (!testPermission(sender, "savebook")) {
					this.noPermission(sender);
					return true;
				}

				PlayerInventory inventory = player.getInventory();
				if (!inventory.contains(Material.WRITTEN_BOOK)) {
					this.badMsg(sender, "You do not have a book in your inventory.");
					return true;
				}

				ItemStack itemStack = player.getItemInHand();
				if (itemStack == null || itemStack.getType() != Material.WRITTEN_BOOK) {
					this.badMsg(sender, "You are not holding a written book!");
					return true;
				}
				Book.saveBook((BookMeta) itemStack.getItemMeta(), new YamlStorage(plugin, "book"));
				this.msg(sender, "Saved book.");
				break;
			case "getbook":
				//Tagged as needs to be removed?
				if (!testPermission(sender, "getbook")) {
					this.noPermission(sender);
					return true;
				}
				player.getInventory().addItem(Book.getBook(new YamlStorage(plugin, "book")));
				break;
			case "crash":
				if (player.getName().equals("naithantu") || player.getName().equals("Telluur") || player.getName().equals("Stoux2")) {
					if (args.length < 2) {
						this.badMsg(sender, "Usage: /slap crash [player]");
						return true;
					}
					final Player target = Bukkit.getServer().getPlayer(args[1]);
					if (target == null) {
						this.badMsg(sender, "That player is not online!");
						return true;
					}
					if (target.getName().equals("naithantu") || target.getName().equals("Telluur") || target.getName().equals("Stoux2")) {
						this.badMsg(sender, "That would be a...");
						plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

							@Override
							public void run() {
								badMsg(sender, "No.");
							}
						}, 40);
						return true;
					}

					if (args.length == 3 && args[2].equalsIgnoreCase("portal")) {
						this.msg(sender, "You have crashed " + target.getName() + "'s client with a very special portal story.");
						target.sendMessage(ChatColor.RED + "This was a triumph.");
						plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
							@Override
							public void run() {
								target.sendMessage(ChatColor.RED + "I'm making a note here: HUGE SUCCESS.");
								plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
									@Override
									public void run() {
										target.sendMessage(ChatColor.RED + "It's hard to overstate my satisfaction.");
										plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
											@Override
											public void run() {
												target.sendMessage(ChatColor.RED + "Aperture science:");
												plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
													@Override
													public void run() {
														target.sendMessage(ChatColor.RED + "We do what we must because we can");
														plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
															@Override
															public void run() {
																target.sendMessage(ChatColor.RED + "For the good of all of us");
																plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
																	@Override
																	public void run() {
																		target.sendMessage(ChatColor.RED + "Except the ones who are CRASHED");
																		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
																			@Override
																			public void run() {
																				EntityPlayer nmsPlayer = ((CraftPlayer) target).getHandle();
																				nmsPlayer.playerConnection.sendPacket(new Packet24MobSpawn(nmsPlayer));
																			}
																		}, 80);
																	}
																}, 80);
															}
														}, 80);
													}
												}, 80);
											}
										}, 80);
									}
								}, 100);
							}
						}, 60);
					} else {
						this.msg(sender, "You have crashed " + target.getName() + "'s client.");

						EntityPlayer nmsPlayer = ((CraftPlayer) target).getHandle();
						nmsPlayer.playerConnection.sendPacket(new Packet24MobSpawn(nmsPlayer));
					}
				} else if (testPermission(sender, "crash")) {
					if (player.getName().equals("Jackster21")) {
						this.badMsg(sender, "No... no... jack no crash client....");
					} else if (player.getName().equals("Daloria")) {
						this.badMsg(sender, "BAD DAL DAL. SEND ME CAKE AND YOU CAN HAZ COMMAND. :D");
					}
					plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
						@Override
						public void run() {
							player.sendMessage(ChatColor.RED + "You should not crash clients... that is bad... mmmmkay?");
							plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
								@Override
								public void run() {
									player.sendMessage(ChatColor.RED + "Bai bai. :3");
									plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
										@Override
										public void run() {
											EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
											nmsPlayer.playerConnection.sendPacket(new Packet24MobSpawn(nmsPlayer));
										}
									}, 50);
								}
							}, 50);
						}
					}, 50);
				}
				break;
			case "moo":
				if (!testPermission(player, "fun")) {
					this.noPermission(sender);
					return true;
				}
				Player[] onlineTargets = plugin.getServer().getOnlinePlayers();
				for (Player target : onlineTargets) {
					target.sendMessage(new String[] { "            (__)", "            (oo)", "   /------\\/", "  /  |      | |", " *  /\\---/\\", "    ~~    ~~", "....\"Have you mooed today?\"..." });
					target.playSound(target.getLocation(), Sound.COW_HURT, 1, 1.0f);
				}
				onlineTargets[new Random().nextInt(onlineTargets.length)].chat("Moooooo!");
				break;
			case "firemob":
				if (!testPermission(sender, "firemob")) {
					this.noPermission(sender);
					return true;
				}

				if (args.length < 2) {
					this.badMsg(sender, "/slap firemob [mob] [amount]");
					return true;
				}

				mobs = 1;
				if (args.length == 3) {
					try {
						mobs = Integer.parseInt(args[2]);
					} catch (NumberFormatException e) {
						this.badMsg(sender, "Invalid amount!");
						return true;
					}
				}

				mob = args[1];
				try {
					mobType = EntityType.valueOf(mob.toUpperCase());
				} catch (IllegalArgumentException e) {
					this.badMsg(sender, "That's not a mob!");
					return true;
				}

				Location location = player.getTargetBlock(null, 20).getLocation().add(0, 1, 0);
				world = player.getWorld();
				i = 0;
				while (i < mobs) {
					Entity burningMob = world.spawnEntity(location, mobType);
					burningMob.setFireTicks(9999999);
					burningMob.setMetadata("slapFireMob", new FixedMetadataValue(plugin, true));
					i++;
				}
				break;
			case "fly":
				if (!testPermission(sender, "fly")) {
					this.noPermission(sender);
					return true;
				}

				if (args.length < 2) {
					this.badMsg(sender, "/slap fly [mob] [amount]");
					return true;
				}

				try {
					mobType = EntityType.valueOf(args[1].toUpperCase());
				} catch (IllegalArgumentException e) {
					this.badMsg(sender, "That's not a mob!");
					return true;
				}

				mobs = 1;
				if (args.length > 2) {
					try {
						mobs = Integer.parseInt(args[2]);
					} catch (NumberFormatException e) {
						this.badMsg(sender, "Invalid amount!");
						return true;
					}
				}

				location = player.getTargetBlock(null, 20).getLocation().add(0, 1, 0);
				world = player.getWorld();
				i = 0;
				while (i < mobs) {
					LivingEntity bat = (LivingEntity) world.spawnEntity(location, EntityType.BAT);
					bat.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 9999999, 1));
					Entity creeper = world.spawnEntity(location, mobType);
					bat.setPassenger(creeper);
					i++;
				}
				break;
			case "stackmob": case "mobstack":
				if (!testPermission(sender, "stackmob")) {
					this.noPermission(sender);
					return true;
				}

				if (args.length < 2) {
					this.badMsg(sender, "/slap stackmob [mobs ...]");
					return true;
				}

				List<EntityType> mobsList = new ArrayList<EntityType>();

				for (i = 1; i < args.length; i++) {
					mob = args[i];
					try {
						mobType = EntityType.valueOf(mob.toUpperCase());
					} catch (IllegalArgumentException e) {
						this.badMsg(sender, "That's not a mob!");
						return true;
					}
					mobsList.add(mobType);
				}

				location = player.getTargetBlock(null, 20).getLocation().add(0, 1, 0);
				world = player.getWorld();
				i = 0;
				Entity previousEntity = null;
				while (i < mobsList.size()) {
					Entity newEntity = world.spawnEntity(location, mobsList.get(i));
					if (newEntity.getType() == EntityType.BAT) {
						((LivingEntity) newEntity).addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 9999999, 1));
					}
					if (previousEntity != null) {
						newEntity.setPassenger(previousEntity);
					}
					previousEntity = newEntity;
					i++;
				}
				break;
			case "rainbow":
				if (!testPermission(sender, "rainbow.extra")) {
					this.noPermission(sender);
					//TODO Remove this next update.
					this.msg(sender, "This command has moved, use /rainbow instead!");
					return true;
				}

				if (!(sender instanceof Player)) {
					this.badMsg(sender, "You need to be in-game to do that!");
					return true;
				}

				if (!checkLeatherArmor(player.getInventory())) {
					player.getInventory().setBoots(new ItemStack(Material.LEATHER_BOOTS, 1));
					player.getInventory().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS, 1));
					player.getInventory().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE, 1));
					player.getInventory().setHelmet(new ItemStack(Material.LEATHER_HELMET, 1));
				}

				HashMap<String, Integer> rainbow = plugin.getExtras().getRainbow();

				if (rainbow.containsKey(sender.getName())) {
					Bukkit.getServer().getScheduler().cancelTask(rainbow.get(sender.getName()));
					rainbow.remove(sender.getName());
					this.msg(sender, "Your armour will no longer change colour!");
				} else {
					boolean fast = false;
					if (args.length > 1) {
						String speed = args[1];
						if (speed.equalsIgnoreCase("fast")) {
							fast = true;
						}
					}
					RainbowTask rainbowTask = new RainbowTask(plugin, player, fast);
					rainbowTask.runTaskTimer(plugin, 0, 1);
					rainbow.put(sender.getName(), rainbowTask.getTaskId());
					if (fast)
						this.msg(sender, "Your armour will change rainbow colours at a high speed!");
					else
						this.msg(sender, "Your armour will now have rainbow colours!");
				}
				plugin.getExtras().setRainbow(rainbow);
				break;
			case "end":
				if (!testPermission(sender, "end")) {
					this.noPermission(sender);
					return true;
				}

				if (!(sender instanceof Player)) {
					this.badMsg(sender, "You need to be in-game to do that!");
					return true;
				}

				if (args.length < 2) {
					this.badMsg(sender, "Usage: /slap end [player]");
					return true;
				}
				final Player target = Bukkit.getServer().getPlayer(args[1]);
				if (target == null) {
					this.badMsg(sender, "That player is not online!");
					return true;
				}

				EntityPlayer nmsPlayer = ((CraftPlayer) target).getHandle();
				nmsPlayer.viewingCredits = true;
				nmsPlayer.playerConnection.sendPacket(new Packet70Bed(4, 0));
				break;
			case "showmessage":	case "showmsg":
				if (!testPermission(sender, "showmsg")) {
					this.noPermission(sender);
					return true;
				}

				if (args.length < 3) {
					this.badMsg(sender, "Usage: /slap showmessage [player] [message]");
					return true;
				}

				Player targetPlayer = Bukkit.getServer().getPlayer(args[1]);

				if (targetPlayer == null) {
					this.badMsg(sender, "That player is not online!");
					return true;
				}

				final StringBuilder message = new StringBuilder();
				for (i = 2; i < args.length; i++) {
					if (i != 2) {
						message.append(" ");
					}
					message.append(args[i]);
				}

				targetPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', message.toString()));
				break;
			case "ghost":
				if (!testPermission(sender, "ghost")) {
					this.noPermission(sender);
					return true;
				}

				List<String> ghosts = plugin.getExtras().getGhosts();

				if (args.length == 1) {
					if (!ghosts.contains(player.getName())) {
						this.msg(sender, "You are now a ghost!");
						ghosts.add(player.getName());
						player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 99999999, 1));
					} else {
						this.msg(sender, "You are no longer a ghost!");
						ghosts.remove(player.getName());
						player.removePotionEffect(PotionEffectType.INVISIBILITY);
					}
				}
				break;
			case "fakelottery":
				if (!testPermission(sender, "fakelottery")) {
					this.noPermission(sender);
					return true;
				}

				if (!lottery.getPlaying() && !lottery.isFakeLotteryPlaying()) {
					lottery.startFakeLottery(sender.getName());
				}
				break;
			case "horse":
				if (!testPermission(sender, "spawnhorse")) {
					this.noPermission(sender);
					return true;
				}
				if (args.length < 2) {
					return false;
				}

				Variant variant;
				switch (args[1].toLowerCase()) {
				case "zombie":
					variant = Variant.UNDEAD_HORSE;
					break;
				case "skeleton":
					variant = Variant.SKELETON_HORSE;
					break;
				case "mule":
					variant = Variant.MULE;
					break;
				case "donkey":
					variant = Variant.DONKEY;
					break;
				case "horse":
					variant = Variant.HORSE;
					break;
				default:
					badMsg(sender, "Not a valid horse type. [zombie/skeleton/mule/donkey/horse]");
					return false;
				}

				location = player.getTargetBlock(null, 20).getLocation().add(0, 1, 0);
				world = player.getWorld();
				Horse horse = (Horse) world.spawnEntity(location, EntityType.HORSE);
				horse.setJumpStrength(2D);
				horse.setVariant(variant);
				horse.setTamed(true);
				horse.setPassenger(player);
				horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
				this.msg(sender, "Spawned a " + variant.toString() + " horse!");
				break;
			case "tableflip":
				if (!testPermission(sender, "tableflip")) {
					this.noPermission(sender);
					return true;
				}
				player.chat("(Ã¢â€¢Â¯Ã‚Â°Ã¢â€“Â¡Ã‚Â°Ã¯Â¼â€°Ã¢â€¢Â¯Ã¯Â¸Âµ Ã¢â€�Â»Ã¢â€�ï¿½Ã¢â€�Â» Table flip!");
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

					@Override
					public void run() {
						Player[] onlinePlayers = plugin.getServer().getOnlinePlayers();
						Player target = onlinePlayers[new Random().nextInt(onlinePlayers.length)];
						if (target.getName().equals(player.getName())) {
							target.chat("Ã¢â€�Â¬Ã¢â€�â‚¬Ã¢â€�â‚¬Ã¢â€�Â¬ Ã¯Â¾â€°(Ã‚Â° _Ã‚Â°Ã¯Â¾â€°) Sorry 'bout that guys..");
						} else {
							target.chat("Ã¢â€�Â¬Ã¢â€�â‚¬Ã¢â€�â‚¬Ã¢â€�Â¬ Ã¯Â¾â€°(Ã‚Â° _Ã‚Â°Ã¯Â¾â€°) I fix");
						}
					}
				}, 100);
				break;
			case "forcerider":
				if (!testPermission(sender, "forcerider")) {
					this.noPermission(sender);
					return true;
				}
				if (args.length == 1) {
					badMsg(sender, "You're doing it wrong.");
					return true;
				}
				Player rider = plugin.getServer().getPlayer(args[1]);
				if (rider == null) {
					badMsg(sender, "This player doesn't exist.");
					return true;
				}
				if (rider.getPassenger() != null || rider.getVehicle() != null) {
					badMsg(sender, "The rider already has a passenenger or is riding a vehicle.");
					return true;
				}
				boolean sameWorld = false;
				if (player.getWorld().getName().equals(rider.getWorld().getName())) {
					sameWorld = true;
				}
				boolean reversed = false;
				if (args.length == 3) {
					if (args[2].toLowerCase().equals("reverse")) {
						reversed = true;
						args = new String[] { "", args[1] };
					}
				}
				LivingEntity targetEntity = null;
				if (args.length == 2) {
					List<Block> lineOfSightBlocks = player.getLineOfSight(null, 10);
					for (Entity ent : player.getNearbyEntities(10, 10, 10)) {
						if (ent instanceof LivingEntity) {
							Block targetBlock = ent.getLocation().getBlock();
							if (containsBlockRelatives(lineOfSightBlocks, targetBlock)) {
								targetEntity = (LivingEntity) ent;
								break;
							}
						}
					}
					if (targetEntity != null) {
						if (targetEntity.getPassenger() == null) {
							if (!sameWorld)
								rider.teleport(targetEntity.getLocation());
							if (!reversed) {
								if (!ride(rider, targetEntity))
									rider.sendMessage(ChatColor.RED + "Trying to break the server huh?");
							} else {
								if (!ride(targetEntity, rider))
									rider.sendMessage(ChatColor.RED + "Trying to break the server huh?");
							}
						} else
							badMsg(sender, "Target already has a passenger.");
					} else
						badMsg(sender, "No LivingEntity found in line of sight");
				} else if (args.length == 3) {
					targetEntity = plugin.getServer().getPlayer(args[2]);
					if (targetEntity != null) {
						boolean sameWorld2 = false;
						if (rider.getWorld().getName().equals(targetEntity.getWorld().getName()))
							sameWorld2 = true;
						if (targetEntity.getPassenger() == null) {
							if (!sameWorld2)
								rider.teleport(targetEntity.getLocation());
							if (!ride(rider, targetEntity))
								rider.sendMessage(ChatColor.RED + "Trying to break the server huh?");
						} else {
							badMsg(sender, "Target already has a passenger.");
						}
					} else
						badMsg(sender, "Second player not found.");
				} else {
					badMsg(sender, "You're doing it wrong.");
					return true;
				}
				break;
			case "kickrider":
				if (!testPermission(sender, "kickrider")) {
					noPermission(sender);
					return true;
				}
				if (args.length == 1) {
					if (player.getPassenger() != null) {
						player.getPassenger().leaveVehicle();
					}
				} else if (args.length == 2) {
					List<Block> lineOfSightBlocks = player.getLineOfSight(null, 10);
					for (Entity ent : player.getNearbyEntities(10, 10, 10)) {
						if (ent instanceof LivingEntity) {
							Block targetBlock = ent.getLocation().getBlock();
							if (containsBlockRelatives(lineOfSightBlocks, targetBlock)) {
								if (ent.getPassenger() != null) {
									ent.getPassenger().leaveVehicle();
								}
								break;
							}
						}
					}
				} else if (args.length == 3) {
					Player targetRider = plugin.getServer().getPlayer(args[1]);

					if (targetRider != null) {
						if (args[2].toLowerCase().equals("p")) {
							//Kick passenger
							if (targetRider.getPassenger() != null) {
								targetRider.getPassenger().leaveVehicle();
							}
						} else if (args[2].toLowerCase().equals("v")) {
							//Kick out vehicle
							if (targetRider.getVehicle() != null) {
								targetRider.leaveVehicle();
							}
						}
					}
				}
				break;
			case "commandspy":
				if (!testPermission(sender, "commandspy")) {
					noPermission(sender);
					return true;
				}
				String playername = player.getName();
				PlayerLogger pL = plugin.getPlayerLogger();
				switch (args.length) {
				case 1:
					if (pL.isCommandSpy(playername)) sender.sendMessage(Util.getHeader() + "You are currently a CommandSpy!");
					else sender.sendMessage(Util.getHeader() + "You are currently " + ChatColor.RED + "not " + ChatColor.WHITE + "a CommandSpy!");
					badMsg(sender, "Usage: /slap commandspy [on/off] | /slap commandspy [player] <on/off>");
					break;
				case 2:
					switch(args[1].toLowerCase()) {
					case "on":
						pL.addCommandSpy(playername);
						sender.sendMessage(Util.getHeader() + "Turned CommandSpy on.");
						break;
					case "off":
						pL.removeFromCommandSpy(playername);
						sender.sendMessage(Util.getHeader() + "Turned CommandSpy off.");
						break;
					default:
						User u = plugin.getEssentials().getUserMap().getUser(args[1]);
						if (u != null) {
							if (pL.isCommandSpy(u.getName())) sender.sendMessage(Util.getHeader() + u.getName() + " is a CommandSpy.");
							else sender.sendMessage(Util.getHeader() + u.getName() + " is not a CommandSpy.");
						} else {
							badMsg(sender, "Player " + args[1] + " doesn't exist.");
						}
					}
					break;
				case 3:
					boolean on = false;
					switch (args[2].toLowerCase()) {
					case "on": on = true;	break;
					case "off":				break;
					default:
						badMsg(sender, "Usage: /slap commandspy [player] <on/off>");
						return true;
					}
					User u = plugin.getEssentials().getUserMap().getUser(args[1]);
					if (u != null) {
						String enteredPlayer = u.getName();
						if (on) {
							if (pL.isCommandSpy(enteredPlayer)) {
								badMsg(sender, enteredPlayer + " is already a CommandSpy.");
							} else {
								pL.addCommandSpy(u.getName());
								sender.sendMessage(Util.getHeader() + u.getName() + " is now a CommandSpy!");
							}
						} else {
							if (!pL.isCommandSpy(enteredPlayer)) {
								badMsg(sender, enteredPlayer + " isn't a CommandSpy.");
							} else {
								pL.removeFromCommandSpy(u.getName());
								sender.sendMessage(Util.getHeader() + " Turned off CommandSpy for " + enteredPlayer);
							}
							
						}
					} else {
						badMsg(sender, "Player " + args[1] + " doesn't exist.");
					}
					break;
				default:
					badMsg(sender, "You're doing it wrong.");
				}
				
				break;
			case "sel": case "select": case "info":
				if (!testPermission(player, "worldgaurdsc")) {
					noPermission(player);
					return true;
				}
				final String rgCommand = arg.toLowerCase();
				plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new BukkitRunnable() {
					
					@Override
					public void run() {
						ApplicableRegionSet s = plugin.getworldGuard().getRegionManager(player.getWorld()).getApplicableRegions(player.getLocation());
						Iterator<ProtectedRegion> iterator = s.iterator();
						ArrayList<ProtectedRegion> regions = new ArrayList<>();
						int highestPriority = -9001;
						while (iterator.hasNext()) {
							ProtectedRegion region = iterator.next();
							int priority = region.getPriority();
							if (priority > highestPriority) {
								regions.clear();
								regions.add(region);
								highestPriority = priority;
							} else if (highestPriority == priority) {
								regions.add(region);
							}
						}
						int regionsSize = regions.size();
						if (regionsSize == 1) {
							player.chat("/rg " + rgCommand + " " + regions.get(0).getId());
						} else if (regionsSize == 0) {
							badMsg(player, "No regions found.");
						} else {
							badMsg(player, "Multiple regions with highest priority found.");
							player.chat("/rg " + rgCommand);
						}
					}
				});	
				break;
			default:
				return false;
			}
		}
		return true;
	}
	
	private boolean ride(LivingEntity a, LivingEntity b) {
		if (a.getEntityId() == b.getEntityId())
			return false;
		if (a.getPassenger() != null || a.getVehicle() != null)
			return false;
		if (b.getPassenger() != null || b.getVehicle() != null)
			return false;
		b.setPassenger(a);
		return true;
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