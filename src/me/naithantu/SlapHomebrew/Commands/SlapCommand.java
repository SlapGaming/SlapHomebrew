package me.naithantu.SlapHomebrew.Commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import me.naithantu.SlapHomebrew.Book;
import me.naithantu.SlapHomebrew.Lottery;
import me.naithantu.SlapHomebrew.SlapHomebrew;
import me.naithantu.SlapHomebrew.Runnables.RainbowTask;
import me.naithantu.SlapHomebrew.Storage.YamlStorage;
import net.minecraft.server.v1_6_R2.EntityPlayer;
import net.minecraft.server.v1_6_R2.Packet24MobSpawn;
import net.minecraft.server.v1_6_R2.Packet70Bed;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftPlayer;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SlapCommand extends AbstractCommand {

	Integer used = 0;
	HashSet<String> vipItemsList = new HashSet<String>();
	public static HashSet<String> retroBow = new HashSet<String>();
	Lottery lottery;
	
	public SlapCommand(CommandSender sender, String[] args, SlapHomebrew plugin, Lottery lottery) {
		super(sender, args, plugin);
		this.lottery = lottery;
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
		
		if (arg.equalsIgnoreCase("moo")) {
			if (!testPermission(player, "fun")) {
				this.noPermission(sender);
				return true;
			}
			if (args.length < 2) {
				this.badMsg(sender, "Usage: /slap moo [player]");
				return true;
			}
			final Player target = Bukkit.getServer().getPlayer(args[1]);
			if (target == null) {
				this.badMsg(sender, "That player is not online!");
				return true;
			}
			target.sendMessage(new String[]
					{
						"            (__)", "            (oo)", "   /------\\/", "  /  |      | |", " *  /\\---/\\", "    ~~    ~~", "....\"Have you mooed today?\"..."
					});
			target.playSound(player.getLocation(), Sound.COW_HURT, 1, 1.0f);
			this.msg(player, "You Moo'd " + target.getName());
		}

		if (arg.equalsIgnoreCase("firemob")) {
			if (!testPermission(sender, "firemob")) {
				this.noPermission(sender);
				return true;
			}

			if (args.length < 2) {
				this.badMsg(sender, "/slap firemob [mob] [amount]");
				return true;
			}

			int mobs = 1;
			if (args.length == 3) {
				try {
					mobs = Integer.parseInt(args[2]);
				} catch (NumberFormatException e) {
					this.badMsg(sender, "Invalid amount!");
					return true;
				}
			}

			String mob = args[1];
			EntityType mobType;
			try {
				mobType = EntityType.valueOf(mob.toUpperCase());
			} catch (IllegalArgumentException e) {
				this.badMsg(sender, "That's not a mob!");
				return true;
			}
			System.out.println("Mob: " + mobType);

			Location location = player.getTargetBlock(null, 20).getLocation().add(0, 1, 0);
			World world = player.getWorld();
			int i = 0;
			while (i < mobs) {
				Entity burningMob = world.spawnEntity(location, mobType);
				burningMob.setFireTicks(9999999);
				burningMob.setMetadata("slapFireMob", new FixedMetadataValue(plugin, true));
				i++;
			}
		}

		if (arg.equalsIgnoreCase("fly")) {
			if (!testPermission(sender, "fly")) {
				this.noPermission(sender);
				return true;
			}

			if (args.length < 2) {
				this.badMsg(sender, "/slap fly [mob] [amount]");
				return true;
			}

			EntityType mobType;
			try {
				mobType = EntityType.valueOf(args[1].toUpperCase());
			} catch (IllegalArgumentException e) {
				this.badMsg(sender, "That's not a mob!");
				return true;
			}

			int mobs = 1;
			if (args.length > 2) {
				try {
					mobs = Integer.parseInt(args[2]);
				} catch (NumberFormatException e) {
					this.badMsg(sender, "Invalid amount!");
					return true;
				}
			}

			Location location = player.getTargetBlock(null, 20).getLocation().add(0, 1, 0);
			World world = player.getWorld();
			int i = 0;
			while (i < mobs) {
				LivingEntity bat = (LivingEntity) world.spawnEntity(location, EntityType.BAT);
				bat.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 9999999, 1));
				Entity creeper = world.spawnEntity(location, mobType);
				bat.setPassenger(creeper);
				i++;
			}
		}

		if (arg.equalsIgnoreCase("stackmob")) {
			if (!testPermission(sender, "stackmob")) {
				this.noPermission(sender);
				return true;
			}

			if (args.length < 2) {
				this.badMsg(sender, "/slap stackmob [mobs ...]");
				return true;
			}

			List<EntityType> mobs = new ArrayList<EntityType>();

			for (int i = 1; i < args.length; i++) {
				String mob = args[i];
				EntityType mobType;
				try {
					mobType = EntityType.valueOf(mob.toUpperCase());
				} catch (IllegalArgumentException e) {
					this.badMsg(sender, "That's not a mob!");
					return true;
				}
				mobs.add(mobType);
			}

			Location location = player.getTargetBlock(null, 20).getLocation().add(0, 1, 0);
			World world = player.getWorld();
			int i = 0;
			Entity previousEntity = null;
			while (i < mobs.size()) {
				Entity newEntity = world.spawnEntity(location, mobs.get(i));
				if (newEntity.getType() == EntityType.BAT) {
					((LivingEntity) newEntity).addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 9999999, 1));
				}
				if (previousEntity != null) {
					newEntity.setPassenger(previousEntity);
				}
				previousEntity = newEntity;
				i++;
			}
		}

		if (arg.equalsIgnoreCase("rainbow")) {
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
		}

		if (arg.equalsIgnoreCase("end")) {
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

		}

		if (arg.equalsIgnoreCase("showmessage") || arg.equalsIgnoreCase("showmsg")) {
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
			for (int i = 2; i < args.length; i++) {
				if (i != 2) {
					message.append(" ");
				}
				message.append(args[i]);
			}

			targetPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', message.toString()));
		}
		
		if (arg.equalsIgnoreCase("ghost")) {
			if (!testPermission(sender, "ghost")) {
				this.noPermission(sender);
				return true;
			}
			
			List<String> ghosts = plugin.getExtras().getGhosts();
			
			if(args.length == 1){
				if(!ghosts.contains(player.getName())){
					this.msg(sender, "You are now a ghost!");
					ghosts.add(player.getName());
					player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 99999999, 1));
				} else {
					this.msg(sender, "You are no longer a ghost!");
					ghosts.remove(player.getName());
					player.removePotionEffect(PotionEffectType.INVISIBILITY);
				}
			}
		}
		
		if (arg.equalsIgnoreCase("fakelottery")) {
			if (testPermission(sender, "fakelottery")) {
				if (!lottery.getPlaying() && !lottery.isFakeLotteryPlaying()) {
					lottery.startFakeLottery(sender.getName());
				}
			}
		}

		if (arg.equalsIgnoreCase("horse")) {
			if (testPermission(sender, "horse")) {
				if(args.length < 2)
					return false;
				
				Variant variant;
				try{
					variant = Variant.valueOf(args[1].toUpperCase());
				} catch (IllegalArgumentException e){
					this.badMsg(sender, "Invalid horse variant!");
					return true;
				}
				
				Location location = player.getTargetBlock(null, 20).getLocation().add(0, 1, 0);
				World world = player.getWorld();
				Horse horse = (Horse) world.spawnEntity(location, EntityType.HORSE);
				horse.setJumpStrength(2D);
				horse.setVariant(variant);
				horse.setTamed(true);
				horse.setPassenger(player);
				horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
				this.msg(sender, "Spawned a " + variant.toString() + " horse!");
			}
		}
		
		
		return true;
	}

	public boolean checkLeatherArmor(PlayerInventory inventory) {
		if (inventory.getBoots() == null || inventory.getLeggings() == null || inventory.getChestplate() == null || inventory.getHelmet() == null)
			return false;
		return inventory.getBoots().getType() == Material.LEATHER_BOOTS && inventory.getLeggings().getType() == Material.LEATHER_LEGGINGS
				&& inventory.getChestplate().getType() == Material.LEATHER_CHESTPLATE && inventory.getHelmet().getType() == Material.LEATHER_HELMET;
	}
}
