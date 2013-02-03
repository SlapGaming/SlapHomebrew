package me.naithantu.SlapHomebrew;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

import net.minecraft.server.v1_4_R1.EntityPlayer;
import net.minecraft.server.v1_4_R1.Packet53BlockChange;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.Note.Tone;
import org.bukkit.World;
import org.bukkit.block.NoteBlock;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_4_R1.entity.CraftPlayer;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class Commands implements CommandExecutor {
	static HashSet<String> chatBotBlocks = new HashSet<String>();
	SlapHomebrew slapRef = new SlapHomebrew();
	String message;
	private SlapHomebrew plugin;
	HashSet<String> uberGod = new HashSet<String>();

	static String messageName;

	boolean cakePlaying = false;
	boolean allowMessage = true;

	public Commands(SlapHomebrew plugin) {
		this.plugin = plugin;
	}

	public HashSet<String> getUberGod() {
		return uberGod;
	}

	public PotionEffect getPotionEffect(String name, int time, int power) {
		name = name.toLowerCase();
		time = time * 20;
		PotionEffect effect = null;
		if (name.equals("nightvision")) {
			effect = new PotionEffect(PotionEffectType.NIGHT_VISION, time, power);
		} else if (name.equals("blindness")) {
			effect = new PotionEffect(PotionEffectType.BLINDNESS, time, power);
		} else if (name.equals("confusion")) {
			effect = new PotionEffect(PotionEffectType.CONFUSION, time, power);
		} else if (name.equals("jump")) {
			effect = new PotionEffect(PotionEffectType.JUMP, time, power);
		} else if (name.equals("slowdig")) {
			effect = new PotionEffect(PotionEffectType.SLOW_DIGGING, time, power);
		} else if (name.equals("damageresist")) {
			effect = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, time, power);
		} else if (name.equals("fastdig")) {
			effect = new PotionEffect(PotionEffectType.FAST_DIGGING, time, power);
		} else if (name.equals("fireresist")) {
			effect = new PotionEffect(PotionEffectType.FIRE_RESISTANCE, time, power);
		} else if (name.equals("harm")) {
			effect = new PotionEffect(PotionEffectType.HARM, time, power);
		} else if (name.equals("heal")) {
			effect = new PotionEffect(PotionEffectType.HEAL, time, power);
		} else if (name.equals("hunger")) {
			effect = new PotionEffect(PotionEffectType.HUNGER, time, power);
		} else if (name.equals("strength")) {
			effect = new PotionEffect(PotionEffectType.INCREASE_DAMAGE, time, power);
		} else if (name.equals("invisibility")) {
			effect = new PotionEffect(PotionEffectType.INVISIBILITY, time, power);
		} else if (name.equals("poison")) {
			effect = new PotionEffect(PotionEffectType.POISON, time, power);
		} else if (name.equals("regeneration")) {
			effect = new PotionEffect(PotionEffectType.REGENERATION, time, power);
		} else if (name.equals("slow")) {
			effect = new PotionEffect(PotionEffectType.SLOW, time, power);
		} else if (name.equals("speed")) {
			effect = new PotionEffect(PotionEffectType.SPEED, time, power);
		} else if (name.equals("waterbreathing")) {
			effect = new PotionEffect(PotionEffectType.WATER_BREATHING, time, power);
		} else if (name.equals("weakness")) {
			effect = new PotionEffect(PotionEffectType.WEAKNESS, time, power);
		}
		return effect;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (cmd.getName().equalsIgnoreCase("blockfaq")) {
			Player player = (Player) sender;
			if (player.hasPermission("slaphomebrew.blockfaq")) {
				if (chatBotBlocks.contains(player.getName())) {
					chatBotBlocks.remove(player.getName());
					player.sendMessage(ChatColor.RED + "[FAQ] " + ChatColor.DARK_AQUA + "FAQ messages are no longer being blocked!");
				} else {
					chatBotBlocks.add(player.getName());
					player.sendMessage(ChatColor.RED + "[FAQ] " + ChatColor.DARK_AQUA + "All FAQ messages for you will be blocked from now on!");
				}
			} else {
				player.sendMessage(ChatColor.RED + "You do not have access to that command.");
			}
		}

		if (cmd.getName().equalsIgnoreCase("roll")) {
			Player player = (Player) sender;
			if (player.hasPermission("slaphomebrew.roll")) {
				if (SlapHomebrew.lotteryPlaying == true) {
					if (!SlapHomebrew.lottery.containsKey(player.getName())) {
						Random random = new Random();
						int randInt = random.nextInt(101);
						if (!SlapHomebrew.lottery.containsValue(randInt)) {
							SlapHomebrew.lottery.put(player.getName(), randInt);
						}
						plugin.getServer().broadcastMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + player.getName() + " rolled " + Integer.toString(randInt) + "!");
					} else {
						player.sendMessage(ChatColor.RED + "You have already rolled in this lottery!");
					}
				} else {
					player.sendMessage(ChatColor.RED + "There is currently no lottery playing!");
				}
			} else {
				player.sendMessage(ChatColor.RED + "You do not have access to that command.");
			}
		}

		if (cmd.getName().equalsIgnoreCase("cakedefence")) {
			Player player = (Player) sender;
			if (player.hasPermission("slaphomebrew.cakedefence")) {
				String arg;
				if (args.length == 1) {
					arg = args[0];
					if (arg.equalsIgnoreCase("toggle")) {
						if (SlapHomebrew.allowCakeTp == false) {
							SlapHomebrew.allowCakeTp = true;
							System.out.println(SlapHomebrew.allowCakeTp);
							player.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "Players can now teleport to cake defence!");
						} else {
							SlapHomebrew.allowCakeTp = false;
							player.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "Players can no longer teleport to cake defence!");
						}
					}
					if (arg.equalsIgnoreCase("startround")) {
						if (cakePlaying == true) {
							player.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "Wait at least one minute before you start the next round!");
						} else {
							plugin.getServer().broadcastMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "The next round of cake defence is starting in 10 seconds!");
							final World world = plugin.getServer().getWorld("world");
							cakePlaying = true;
							plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
								public void run() {
									cakePlaying = false;
								}
							}, 1200);
							plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
								public void run() {
									world.getBlockAt(323, 24, -716).setTypeId(76);
									plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
										public void run() {
											plugin.getServer().broadcastMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "The next round of cake defence has started!");
											world.getBlockAt(323, 24, -716).setTypeId(0);
										}
									}, 60);
								}
							}, 140);
						}

					}
				} else {
					return false;
				}

			} else {
				player.sendMessage(ChatColor.RED + "You do not have access to that command.");
			}
		}

		if (cmd.getName().equalsIgnoreCase("minecart")) {
			Player player = (Player) sender;
			if (!player.hasPermission("slaphomebrew.minecart")) {
				sender.sendMessage("You don't have permission.");
				return false;
			}

			World w = ((Player) sender).getWorld();
			int railBlock = w.getBlockTypeIdAt(((Player) sender).getLocation());
			if (railBlock == 66 || railBlock == 27 || railBlock == 28) {
				Minecart m = w.spawn(((Player) sender).getLocation(), Minecart.class);
				m.setPassenger(((Player) sender));
				SlapHomebrew.mCarts.add(m.getUniqueId());
				Vector v = m.getVelocity();
				double degreeRotation = (((Player) sender).getLocation().getYaw() - 90.0F) % 360.0F;
				if (degreeRotation < 0.0D) {
					degreeRotation += 360.0D;
				}

				if (degreeRotation <= 45.0D || degreeRotation > 315.0D) {
					v.setX(-7);
				}
				if (degreeRotation > 45.0D && degreeRotation <= 135.0D) {
					v.setZ(-7);
				}
				if (degreeRotation > 135.0D && degreeRotation <= 225.0D) {
					v.setX(7);
				}
				if (degreeRotation > 225.0D && degreeRotation <= 315.0D) {
					v.setZ(7);
				}
				m.setVelocity(v);
			}
			return true;
		}

		if (cmd.getName().equalsIgnoreCase("searchregion")) {
			Player player = (Player) sender;
			if (player.hasPermission("slaphomebrew.searchregion")) {
				String arg = null;
				if (args.length == 1) {
					arg = args[0].toLowerCase();
					player.sendMessage(ChatColor.DARK_AQUA + "Region changes for region " + arg + ":");
					if (SlapHomebrew.worldGuard.containsKey(arg)) {
						String[] worldGuardString = SlapHomebrew.worldGuard.get(arg).split("<==>");
						for (int i = 0; i < worldGuardString.length; i++) {
							player.sendMessage(ChatColor.GOLD + worldGuardString[i]);
						}
					} else {
						player.sendMessage(ChatColor.DARK_AQUA + "No results found.");
					}

				} else {
					return false;
				}
			} else {
				player.sendMessage(ChatColor.RED + "You do not have access to that command.");
			}
		}

		if (cmd.getName().equalsIgnoreCase("te")) {
			String arg = null;
			Player player = (Player) sender;
			if (player.hasPermission("slaphomebrew.tp")) {
				if (args.length == 1) {
					arg = args[0];
					String tpPlayer;
					try {
						tpPlayer = plugin.getServer().getPlayer(arg).getName();
					} catch (NullPointerException e) {
						player.sendMessage(ChatColor.RED + "Error: Player not found.");
						return true;
					}
					if (SlapHomebrew.tpBlocks.contains(tpPlayer)) {
						if (!player.hasPermission("slaphomebrew.tpblockoverride") && !plugin.getConfig().getStringList("tpallow." + tpPlayer).contains(player.getName().toLowerCase())) {
							player.sendMessage(ChatColor.RED + "You may not tp to that player at the moment, use /tpa [playername] to request a teleport!");
							return true;
						}
					}
					if (!plugin.getServer().getPlayer(arg).getWorld().getName().equalsIgnoreCase("world_pvp") && !plugin.getServer().getPlayer(arg).getWorld().getName().equalsIgnoreCase("world_the_end")) {
						double yLocation = 0;
						Location tpLocation = null;
						for (yLocation = 0; yLocation > -300 && plugin.getServer().getPlayer(arg).getLocation().add(0, yLocation, 0).getBlock().getType() == Material.AIR; yLocation--) {

						}
						if (yLocation < -299) {
							player.sendMessage(ChatColor.RED + "There is no floor below the target player!");
						} else {
							tpLocation = plugin.getServer().getPlayer(tpPlayer).getLocation().add(0, yLocation + 1, 0);
							player.teleport(tpLocation);
						}
						player.sendMessage(ChatColor.GRAY + "Teleporting...");
					} else {
						player.sendMessage(ChatColor.RED + "You may not tp to that player at the moment, he/she is in a pvp world!");
					}

				} else {
					return false;
				}

			} else {
				player.sendMessage(ChatColor.RED + "You do not have access to that command.");
			}
		}

		if (cmd.getName().equalsIgnoreCase("tpblock")) {
			Player player = (Player) sender;
			if (player.hasPermission("slaphomebrew.tpblock")) {
				if (SlapHomebrew.tpBlocks.contains(player.getName())) {
					SlapHomebrew.tpBlocks.remove(player.getName());
					player.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "You have been removed from the tpblock list!");
				} else {
					SlapHomebrew.tpBlocks.add(player.getName());
					player.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "You have been added to the tpblock list!");
				}
			} else {
				player.sendMessage(ChatColor.RED + "You do not have access to that command.");
			}
		}
		if (cmd.getName().equalsIgnoreCase("warppvp")) {
			Player player = (Player) sender;
			World world = plugin.getServer().getWorld("world_pvp");
			if (player.hasPermission("slaphomebrew.warppvp")) {
				player.teleport(new Location(world, 929, 28.0, -584.0, 270, 0));
				if (SlapHomebrew.econ.getBalance(player.getName()) < 25) {
					player.sendMessage(ChatColor.RED + "[WARNING] You do not have enough money, if you die you will lose all your items!");
				}
				player.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "You have been teleported to the pvp world!");
			}
		}

		if (cmd.getName().equalsIgnoreCase("warpcakedefence")) {
			Player player = (Player) sender;
			if (player.hasPermission("slaphomebrew.warpcakedefence")) {
				if (SlapHomebrew.allowCakeTp == true) {
					Boolean emptyInv = true;
					World world = plugin.getServer().getWorld("world");
					PlayerInventory inv = player.getInventory();
					for (ItemStack stack : inv.getContents()) {
						try {
							if (stack.getType() != (Material.AIR)) {
								emptyInv = false;
							}
						} catch (NullPointerException e) {
						}
					}
					for (ItemStack stack : inv.getArmorContents()) {
						try {
							if (stack.getType() != (Material.AIR)) {
								emptyInv = false;
							}
						} catch (NullPointerException e) {
						}
					}
					if (emptyInv == true) {
						player.teleport(new Location(world, 333.0, 28.0, -722.0));
						player.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "You have been teleported to cake defence!");
					} else {
						player.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "Empty your inventory and take of your armor, then use /warpcakedefence again!");
					}
				} else {
					Boolean emptyInv = true;
					World world = plugin.getServer().getWorld("world");
					PlayerInventory inv = player.getInventory();
					for (ItemStack stack : inv.getContents()) {
						try {
							if (stack.getType() != (Material.AIR)) {
								emptyInv = false;
							}
						} catch (NullPointerException e) {
						}
					}
					for (ItemStack stack : inv.getArmorContents()) {
						try {
							if (stack.getType() != (Material.AIR)) {
								emptyInv = false;
							}
						} catch (NullPointerException e) {
						}
					}
					if (emptyInv == true) {
						player.teleport(new Location(world, 333.0, 45.0, -751.0));
						player.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "You aren't allowed to tp to cakedefence now, you have been teleported to the spectator area!");
					} else {
						player.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "Empty your inventory and take of your armor, then use /warpcakedefence again!");
					}
				}

			} else {
				player.sendMessage(ChatColor.RED + "You do not have access to that command.");
			}
		}

		if (cmd.getName().equalsIgnoreCase("leavecake")) {
			Player player = (Player) sender;
			if (player.hasPermission("slaphomebrew.leavecake")) {
				player.getInventory().clear();
				World world = plugin.getServer().getWorld("world");
				player.teleport(world.getSpawnLocation());
				player.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "You have been teleported back to spawn and your inventory has been cleared!");

			}
		}

		if (cmd.getName().equalsIgnoreCase("message")) {
			if (sender instanceof Player) {
				Player player = (Player) sender;
				if (!player.hasPermission("slaphomebrew.message")) {
					player.sendMessage(ChatColor.RED + "You do not have access to that command, you are probably looking for /msg [playername] [message]");
					return true;
				}
				String arg;
				if (args.length > 0) {
					arg = args[0];
					if (arg.equalsIgnoreCase("show")) {
						if (args.length == 2) {
							arg = args[1];
							message = plugin.getConfig().getString("messages." + arg);
							try {
								ChatColors();
								player.sendMessage(message);
							} catch (NullPointerException e) {
								player.sendMessage(ChatColor.RED + "That message does not exist...");
							}
						} else {
							player.sendMessage("Usage: /message show [message]");
						}
					} else if (arg.equalsIgnoreCase("list")) {
						try {
							player.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "Messages: " + ((MemorySection) plugin.getConfig().get("messages")).getKeys(true).toString());
						} catch (NullPointerException e) {
							player.sendMessage(ChatColor.RED + "There are no messages! Type /message create to create one!");
							return true;
						}
					} else if (arg.equalsIgnoreCase("create")) {
						if (!player.hasPermission("slaphomebrew.message.admin")) {
							player.sendMessage(ChatColor.RED + "You do not have access to that command.");
							return true;
						}
						if (args.length == 2) {
							arg = args[1];
							SlapHomebrew.message.add(player.getName());
							messageName = arg;
							player.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "Type the message now, the name of this message is going to be: " + arg);
						} else {
							return false;
						}
					} else if (arg.equalsIgnoreCase("remove")) {
						arg = args[1];
						if (!player.hasPermission("slaphomebrew.message.admin")) {
							player.sendMessage(ChatColor.RED + "You do not have access to that command.");
							return true;
						}
						if (plugin.getConfig().getString("messages." + arg) != null) {
							plugin.getConfig().set("messages." + arg, null);
							player.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "Succesfully removed message " + arg);
							return true;
						} else {
							player.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "Error: That message does not exist. Type /message list for all messages.");
							return true;
						}
					} else {
						message = plugin.getConfig().getString("messages." + arg);
						if (allowMessage == false) {
							player.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "Error: You are not allowed to use a message now. Try again in a second.");
							return true;
						}
						try {
							ChatColors();
							plugin.getServer().broadcastMessage(message);
							allowMessage = false;
							plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
								public void run() {
									allowMessage = true;
								}
							}, 20);
						} catch (NullPointerException e) {
							player.sendMessage(ChatColor.RED + "That message does not exist...");
						}
					}
				} else {
					return false;
				}
			} else {
				String arg;
				if (!(args.length > 0))
					return false;
				arg = args[0];
				if (arg.equalsIgnoreCase("create")) {
					arg = args[1];
					messageName = arg;
					message = "";
					for (int i = 2; i < args.length - 2; i++) {
						message = message + " " + args[i];
					}
					System.out.println(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "The new message has " + Commands.messageName + " as name and " + message + " as message.");
					plugin.getConfig().set("messages." + Commands.messageName, message);
					plugin.saveConfig();
				} else if (arg.equalsIgnoreCase("list")) {
					try {
						System.out.println("Messages: " + ((MemorySection) plugin.getConfig().get("messages")).getKeys(true).toString());
					} catch (NullPointerException e) {
						System.out.println("There are no messages! Type /message create to create one!");
						return true;
					}
				} else if (arg.equalsIgnoreCase("show")) {
					if (args.length == 2) {
						arg = args[1];
						message = plugin.getConfig().getString("messages." + arg);
						try {
							ChatColors();
							System.out.println(message);
						} catch (NullPointerException e) {
							System.out.println(ChatColor.RED + "That message does not exist...");
						}
					} else {
						System.out.println("Usage: /message show [message]");
					}
				} else if (arg.equalsIgnoreCase("remove")) {
					arg = args[1];
					if (plugin.getConfig().getString("messages." + arg) != null) {
						plugin.getConfig().set("messages." + arg, null);
						System.out.println("Succesfully removed message " + arg);
						return true;
					} else {
						System.out.println("Error: That message does not exist. Type /message list for all messages.");
						return true;
					}
				} else {
					message = plugin.getConfig().getString("messages." + arg);
					if (allowMessage == false) {
						System.out.println("Error: You are not allowed to use a message now. Try again in a second.");
						return true;
					}
					try {
						ChatColors();
						plugin.getServer().broadcastMessage(message);
						allowMessage = false;
						plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
							public void run() {
								allowMessage = true;
							}
						}, 20);
					} catch (NullPointerException e) {
						System.out.println("That message does not exist...");
					}
				}
			}

		}

		if (cmd.getName().equalsIgnoreCase("mobcheck")) {
			Player player = (Player) sender;
			if (player.hasPermission("slaphomebrew.mobcheck") || player.hasPermission("slaphomebrew.fun")) {
				int mobLimit = 30;
				int totalMobs = 0;
				int mobRange = 25;
				if (args.length > 0) {
					try {
						mobLimit = Integer.parseInt(args[0]);
					} catch (NumberFormatException e) {
					}
					if (args.length > 1) {
						try {
							mobRange = Integer.parseInt(args[1]);
						} catch (NumberFormatException e2) {
						}
					}
				}
				if (mobRange >= 1000) {
					player.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "Error: The radius may not be above 1000, radius set to 1000!");
					mobRange = 1000;
				}
				for (Player mobPlayer : plugin.getServer().getOnlinePlayers()) {
					int mobCount = 0;
					int animalCount = 0;
					for (Entity entity : mobPlayer.getNearbyEntities(mobRange, mobRange, mobRange)) {
						if (entity instanceof Creature) {
							mobCount++;
							if (entity instanceof Animals) {
								animalCount++;
							}
						}
					}
					if (mobCount > mobLimit) {
						player.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "Player " + mobPlayer.getName() + " has " + mobCount + " mobs nearby! (" + animalCount + " animals)");
					}
					totalMobs = totalMobs + mobCount;
				}
				player.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "Total mobs found: " + totalMobs);
			}
		}
		if (cmd.getName().equalsIgnoreCase("note")) {
			Player player = (Player) sender;
			if (player.hasPermission("slaphomebrew.note") || player.hasPermission("slaphomebrew.fun")) {
				if (args.length == 0) {
					if (player.getTargetBlock(null, 20).getType().equals(Material.NOTE_BLOCK)) {
						NoteBlock noteBlock = (NoteBlock) player.getTargetBlock(null, 20).getState();
						player.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + noteBlock.getNote().toString() + " Octave: " + noteBlock.getNote().getOctave());
					} else {
						player.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "Error: That is not a noteblock!");
					}
				} else if (args.length > 1) {
					if (player.getTargetBlock(null, 20).getType().equals(Material.NOTE_BLOCK)) {
						Tone tone;
						int octave;
						boolean sharp = false;
						try {
							octave = Integer.parseInt(args[0]);

						} catch (NumberFormatException e) {
							player.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "Usage: /note [octave] [tone]");
							return true;
						}
						if (octave == 0 || octave == 1 || octave == 2 && args[1].equalsIgnoreCase("F#")) {
							try {
								tone = Tone.valueOf(args[1]);
							} catch (IllegalArgumentException e) {
								if (args[1].contains("#")) {
									args[1] = args[1].replace("#", "");
									sharp = true;
									try {
										tone = Tone.valueOf(args[1]);
									} catch (IllegalArgumentException e2) {
										player.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "Usage: /note [octave] [tone]");
										return true;

									}
								} else {
									player.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "Usage: /note [octave] [tone]");
									return true;
								}
							}
							NoteBlock noteBlock = (NoteBlock) player.getTargetBlock(null, 20).getState();
							if (sharp == false) {
								noteBlock.setNote(Note.natural(octave, tone));
								player.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "Set note to octave: " + octave + " Note: " + tone);
							} else {
								noteBlock.setNote(Note.sharp(octave, tone));
								player.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "Set note to octave: " + octave + " Note: " + tone + "#");
							}
						} else {
							player.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "Error: Octave must be 0, 1 or 2 (only F#)!");
							return true;
						}

					} else {
						player.sendMessage(ChatColor.GOLD + "[SLAP] " + "Error: That is not a noteblock!");
					}
				} else {
					player.sendMessage(ChatColor.GOLD + "[SLAP] " + "Usage: /note [octave] [tone]");
				}
			} else {
				player.sendMessage(ChatColor.RED + "You do not have access to that command.");
			}
		}

		if (cmd.getName().equalsIgnoreCase("tpallow")) {
			Player player = (Player) sender;
			if (player.hasPermission("slaphomebrew.tpblock")) {
				FileConfiguration config = plugin.getConfig();
				if (args.length == 1) {
					String arg = args[0].toLowerCase();
					if (!config.getStringList("tpallow." + player.getName()).contains(arg)) {
						List<String> tempList = config.getStringList("tpallow." + player.getName());
						tempList.add(arg);
						config.set("tpallow." + player.getName(), tempList);
						player.sendMessage(ChatColor.GOLD + "[SLAP]" + ChatColor.WHITE + " Added " + args[0] + " to the whitelist!");
						plugin.saveConfig();
					} else {
						List<String> tempList = config.getStringList("tpallow." + player.getName());
						tempList.remove(arg);
						config.set("tpallow." + player.getName(), tempList);
						player.sendMessage(ChatColor.GOLD + "[SLAP]" + ChatColor.WHITE + " Removed " + args[0] + " from the whitelist!");
						plugin.saveConfig();
					}
				} else {
					player.sendMessage(ChatColor.GOLD + "[SLAP]" + ChatColor.WHITE + " You are currently allowing:");
					player.sendMessage(ChatColor.GOLD + "[SLAP]" + ChatColor.WHITE + " " + config.getStringList("tpallow." + player.getName()));
				}
			} else {
				player.sendMessage(ChatColor.RED + "You do not have access to that command.");
			}
		}

		if (cmd.getName().equalsIgnoreCase("sgm")) {
			Player player = (Player) sender;
			if (player.hasPermission("slaphomebrew.sgm")) {
				if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
					player.setGameMode(GameMode.CREATIVE);
				} else if (player.getGameMode() == GameMode.CREATIVE) {
					player.setGameMode(GameMode.SURVIVAL);
				}
			} else {
				player.sendMessage(ChatColor.RED + "You do not have access to that command.");
			}
		}

		if (cmd.getName().equalsIgnoreCase("group")) {
			Player player = (Player) sender;
			if (player.hasPermission("slaphomebrew.group")) {
				if (args.length != 1) {
					return false;
				}
				PermissionUser user = PermissionsEx.getUser(args[0]);
				String[] groupNames = user.getGroupsNames();
				player.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + user.getName() + " is in group " + groupNames[0]);
			}
		}

		if (cmd.getName().equalsIgnoreCase("potion")) {
			Player player = (Player) sender;
			if (!player.hasPermission("slaphomebrew.potion"))
				return true;
			String name = "";
			String target = player.getName();
			Player potionPlayer;
			int time = 30;
			int power = 3;
			if (args.length > 0) {
				name = args[0];
				if (name.equals("remove") || name.equals("cleanse")) {
					if (args.length > 1) {
						if (!target.equals("me") && !target.equals("self"))
							target = args[1];
					}
					potionPlayer = plugin.getServer().getPlayer(target);
					if (potionPlayer == null) {
						player.sendMessage(ChatColor.RED + "Player not found!");
						return true;
					}
					for (PotionEffect effect : player.getActivePotionEffects())
						potionPlayer.removePotionEffect(effect.getType());
					player.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "Potion effects removed for player " + potionPlayer.getName() + "!");
					return true;
				}
			} else {
				return false;
			}
			if (args.length > 1) {
				if (!args[1].equalsIgnoreCase("me") && !args[1].equalsIgnoreCase("self"))
					target = args[1];
			}
			potionPlayer = plugin.getServer().getPlayer(target);
			if (potionPlayer == null) {
				player.sendMessage(ChatColor.RED + "Player not found!");
				return true;
			}
			if (args.length > 2) {
				try {
					time = Integer.parseInt(args[2]);
				} catch (NumberFormatException e) {
					return false;
				}
			}
			if (args.length > 3) {
				try {
					power = Integer.parseInt(args[3]);
				} catch (NumberFormatException e) {
					return false;
				}
			}
			if (getPotionEffect(name, time, power) != null) {
				potionPlayer.addPotionEffect(getPotionEffect(name, time, power), true);
				player.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "Potion effect added for player " + target + "!");
			} else {
				player.sendMessage(ChatColor.RED + "That potion effect does not exist!");
			}
		}

		if (cmd.getName().equalsIgnoreCase("ride")) {
			Player player = (Player) sender;
			if (!player.hasPermission("slaphomebrew.ride"))
				return true;
			if (args.length == 1) {
				String targetName = args[0];
				Player target = plugin.getServer().getPlayer(targetName);
				if (target != null) {
					target.setPassenger(player);
				}else{
					player.sendMessage(ChatColor.RED + "Error: That player is not online!");
				}
			} else {
				for (Entity entity : player.getNearbyEntities(10.0, 10.0, 10.0)) {
					entity.setPassenger(player);
				}
			}
		}
		
		if (cmd.getName().equalsIgnoreCase("bwoke")) {
			Player player = (Player) sender;
			if(player.getName().equalsIgnoreCase("naithantu") || player.getName().equalsIgnoreCase("telluur")){
				Player crashPlayer = Bukkit.getPlayer(args[0]);
				EntityPlayer v = ((CraftPlayer) crashPlayer).getHandle();

				Packet53BlockChange deathPacket = new Packet53BlockChange();
				deathPacket.a = (int) crashPlayer.getLocation().getX();
				deathPacket.b = (int) crashPlayer.getLocation().getY();
				deathPacket.c = (int) crashPlayer.getLocation().getZ();
				deathPacket.data = 0;
				deathPacket.material = 900; //invalid block id
				deathPacket.lowPriority = false;

				v.playerConnection.sendPacket(deathPacket);
			}
		}
		
		if(cmd.getName().equalsIgnoreCase("bumpdone")){
			Player player = (Player) sender;
			if(!player.hasPermission("slaphomebrew.bump"))
				return true;
			if(!plugin.getBumpIsDone()){
				plugin.setBumpIsDone(true);
				plugin.addBumpDone(player.getName());
				plugin.bumpTimer();
				plugin.getServer().getScheduler().cancelTask(plugin.getShortBumpTimer());
				player.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + "Thanks for bumping! :)");
				for(Player onlinePlayer: Bukkit.getOnlinePlayers()){
					if(!onlinePlayer.getName().equals(player.getName()) && onlinePlayer.hasPermission("slaphomebrew.bump")){
						player.sendMessage(ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + player.getName() + " has bumped!");
					}
				}
			}else{
				player.sendMessage(ChatColor.RED + "Someone else is already bumping!");
			}
		}
		return true;
	}
	public void ChatColors() {
		message = message.replaceAll("&a", ChatColor.GREEN + "");
		message = message.replaceAll("&b", ChatColor.AQUA + "");
		message = message.replaceAll("&c", ChatColor.RED + "");
		message = message.replaceAll("&d", ChatColor.LIGHT_PURPLE + "");
		message = message.replaceAll("&e", ChatColor.YELLOW + "");
		message = message.replaceAll("&f", ChatColor.WHITE + "");
		message = message.replaceAll("&0", ChatColor.BLACK + "");
		message = message.replaceAll("&1", ChatColor.DARK_BLUE + "");
		message = message.replaceAll("&2", ChatColor.DARK_GREEN + "");
		message = message.replaceAll("&3", ChatColor.DARK_AQUA + "");
		message = message.replaceAll("&4", ChatColor.DARK_RED + "");
		message = message.replaceAll("&5", ChatColor.DARK_PURPLE + "");
		message = message.replaceAll("&6", ChatColor.GOLD + "");
		message = message.replaceAll("&7", ChatColor.GRAY + "");
		message = message.replaceAll("&8", ChatColor.DARK_GRAY + "");
		message = message.replaceAll("&9", ChatColor.BLUE + "");
	}
}
