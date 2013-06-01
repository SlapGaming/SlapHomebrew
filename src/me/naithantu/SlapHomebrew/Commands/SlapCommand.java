package me.naithantu.SlapHomebrew.Commands;

import java.util.HashSet;

import me.naithantu.SlapHomebrew.Book;
import me.naithantu.SlapHomebrew.Lottery;
import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Storage.YamlStorage;
import net.minecraft.server.v1_5_R3.EntityPlayer;
import net.minecraft.server.v1_5_R3.Packet24MobSpawn;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_5_R3.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Ocelot.Type;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.metadata.FixedMetadataValue;

public class SlapCommand extends AbstractCommand {

	Integer used = 0;
	HashSet<String> vipItemsList = new HashSet<String>();
	public static HashSet<String> retroBow = new HashSet<String>();

	Lottery lottery;

	public SlapCommand(CommandSender sender, String[] args, SlapHomebrew plugin) {
		super(sender, args, plugin);
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

		if (!(sender instanceof Player)) {
			this.badMsg(sender, "You need to be in-game to do that.");
			return true;
		}

		final Player player = (Player) sender;

		if (arg.equalsIgnoreCase("retrobow")) {
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
		}

		if (arg.equalsIgnoreCase("wolf")) {
			World world = player.getWorld();
			if (!testPermission(player, "fun")) {
				this.noPermission(sender);
				return true;
			}
			try {
				arg = args[1];
			} catch (ArrayIndexOutOfBoundsException e) {
				// e.printStackTrace();
			}
			Location spawnLocation = player.getEyeLocation();
			int x;
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
		}

		if (arg.equalsIgnoreCase("cat")) {
			World world = player.getWorld();
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
				Location spawnLocation = player.getEyeLocation();
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
					int x;
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
		}
		if (arg.equalsIgnoreCase("removewolf")) {
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
		}
		if (arg.equalsIgnoreCase("removecat")) {
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
		}

		if (arg.equalsIgnoreCase("notify")) {
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
		}

		if (arg.equalsIgnoreCase("savebook")) {
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
		}

		//TODO remove this, just a test command.
		if (arg.equalsIgnoreCase("getbook")) {
			if (!testPermission(sender, "getbook")) {
				this.noPermission(sender);
				return true;
			}
			player.getInventory().addItem(Book.getBook(new YamlStorage(plugin, "book")));
		}

		if (arg.equalsIgnoreCase("crash")) {
			if (player.getName().equals("naithantu") || player.getName().equals("Telluur")) {
				if (args.length < 2) {
					this.badMsg(sender, "Usage: /slap crash [player]");
					return true;
				}
				final Player target = Bukkit.getServer().getPlayer(args[1]);
				if (target == null) {
					this.badMsg(sender, "That player is not online!");
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
		}
		
		if(arg.equalsIgnoreCase("firemob")){
			if (!testPermission(sender, "firemob")) {
				this.noPermission(sender);
				return true;
			}		
			
			if(args.length < 2){
				this.badMsg(sender, "/slap firemob [mob] [amount]");
				return true;
			}
			
			int mobs = 1;
			if(args.length == 3){
				try{
					mobs = Integer.parseInt(args[2]);
				}catch(NumberFormatException e){
					this.badMsg(sender, "Invalid amount!");
					return true;
				}
			}
			
			String mob = args[1];
			EntityType mobType;
			try{
				mobType = EntityType.valueOf(mob.toUpperCase());
			}catch(IllegalArgumentException e){
				this.badMsg(sender, "That's not a mob!");
				return true;
			}
			System.out.println("Mob: " + mobType);
			
			
			Location location = player.getTargetBlock(null, 20).getLocation().add(0,1,0);
			World world = player.getWorld();
			int i = 0;
			while(i < mobs){
				Entity burningMob = world.spawnEntity(location, mobType);
				burningMob.setFireTicks(9999999);
				burningMob.setMetadata("slapBurningMob", new FixedMetadataValue(plugin, true));
				i++;
			}
		}
		return true;
	}
}
