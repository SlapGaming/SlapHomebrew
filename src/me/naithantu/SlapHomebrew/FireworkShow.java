package me.naithantu.SlapHomebrew;

import java.util.HashMap;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitTask;

public class FireworkShow {

	private SlapHomebrew plugin;
	
	private HashMap<Integer, DispenserPillar> pillars;
	private HashMap<Integer, FireworkEffect> effects;
	
	private World world;
	private boolean showRunning = false;
	private boolean teleportAllow = false;
	
	private int showPillar = 0;
	private BukkitTask taskId = null;
	
	
	public FireworkShow(SlapHomebrew plugin) {
		this.plugin = plugin;
		pillars = new HashMap<>();
		effects = new HashMap<>();
		world = plugin.getServer().getWorld("world_survival2");
		createPillars();
		createEffects();
	}
	
	private void createPillars(){
		pillars.put(1, new DispenserPillar(new Location(world, -4658, 67, -4564)));
		pillars.put(2, new DispenserPillar(new Location(world, -4667, 67, -4564)));
		pillars.put(3, new DispenserPillar(new Location(world, -4676, 67, -4564)));
		pillars.put(4, new DispenserPillar(new Location(world, -4685, 67, -4564)));
		pillars.put(5, new DispenserPillar(new Location(world, -7694, 67, -4564)));
		pillars.put(6, new DispenserPillar(new Location(world, -4703, 67, -4564)));
		pillars.put(7, new DispenserPillar(new Location(world, -4712, 67, -4564)));
		pillars.put(8, new DispenserPillar(new Location(world, -4721, 67, -4564)));
		pillars.put(9, new DispenserPillar(new Location(world, -4730, 67, -4564)));
	}
	
	private void createEffects(){
		effects.put(1, createFireworkEffect(new Color[]{Color.AQUA, Color.RED}, FireworkEffect.Type.BALL_LARGE, true, false, new Color[]{Color.WHITE}));
		effects.put(2, createFireworkEffect(new Color[]{Color.YELLOW}, FireworkEffect.Type.BURST, false, true));
		effects.put(3, createFireworkEffect(new Color[]{Color.YELLOW}, FireworkEffect.Type.BURST, true, true, new Color[]{Color.FUCHSIA}));
		effects.put(4, createFireworkEffect(new Color[]{Color.FUCHSIA}, FireworkEffect.Type.BURST, true, true, new Color[]{Color.RED, Color.AQUA}));
		effects.put(5, createFireworkEffect(new Color[]{Color.TEAL}, FireworkEffect.Type.STAR, false, true, new Color[]{Color.LIME}));
		effects.put(6, createFireworkEffect(new Color[]{Color.GREEN}, FireworkEffect.Type.CREEPER, false, false));
		effects.put(7, createFireworkEffect(new Color[]{Color.ORANGE}, FireworkEffect.Type.BALL, true, false, new Color[]{Color.SILVER}));
		effects.put(8, createFireworkEffect(new Color[]{Color.ORANGE}, FireworkEffect.Type.BALL_LARGE, true, false, new Color[]{Color.SILVER}));
		effects.put(9, createFireworkEffect(new Color[]{Color.ORANGE, Color.YELLOW, Color.RED}, FireworkEffect.Type.BALL, false, true, new Color[]{Color.SILVER, Color.GRAY, Color.WHITE}));
		effects.put(10, createFireworkEffect(new Color[]{Color.SILVER, Color.GRAY, Color.WHITE}, FireworkEffect.Type.BALL, true, true, new Color[]{Color.ORANGE, Color.YELLOW, Color.RED}));
		effects.put(11, createFireworkEffect(new Color[]{Color.PURPLE}, FireworkEffect.Type.BURST, true, false));
		effects.put(12, createFireworkEffect(new Color[]{Color.LIME}, FireworkEffect.Type.BURST, true, false));
		effects.put(13, createFireworkEffect(new Color[]{Color.YELLOW}, FireworkEffect.Type.BALL_LARGE, true, true));
	}
	
	public void launch(){
		showRunning = true;
		taskId = plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {
			
			@Override
			public void run() {
				DispenserPillar p; FireworkEffect e; FireworkEffect ex; int xCount;
				switch (showPillar) {
					case 1:	world.setTime(14000); pillars.get(1).launchFromLoc(1, effects.get(2), 2); break;
					case 2:	pillars.get(2).launchFromLoc(1, effects.get(2), 2); break;
					case 3:	pillars.get(3).launchFromLoc(1, effects.get(2), 2); break;
					case 4:	pillars.get(4).launchFromLoc(1, effects.get(2), 2); break;
					case 5:	pillars.get(5).launchFromLoc(1, effects.get(2), 2); break;
					case 6:	pillars.get(6).launchFromLoc(1, effects.get(2), 2); break;
					case 7:	pillars.get(7).launchFromLoc(1, effects.get(2), 2); break;
					case 8:	pillars.get(8).launchFromLoc(1, effects.get(2), 2); break;
					case 9:	pillars.get(9).launchFromLoc(1, effects.get(2), 2); break;
					case 11: pillars.get(9).launchFromLoc(1, effects.get(2), 2); break;
					case 12: pillars.get(8).launchFromLoc(1, effects.get(2), 2); break;
					case 13: pillars.get(7).launchFromLoc(1, effects.get(2), 2); break;
					case 14: pillars.get(6).launchFromLoc(1, effects.get(2), 2); break;
					case 15: pillars.get(5).launchFromLoc(1, effects.get(2), 2); break;
					case 16: pillars.get(4).launchFromLoc(1, effects.get(2), 2); break;
					case 17: pillars.get(3).launchFromLoc(1, effects.get(2), 2); break;
					case 18: pillars.get(2).launchFromLoc(1, effects.get(2), 2); break;
					case 19: pillars.get(1).launchFromLoc(1, effects.get(2), 2); break;
					case 30: launchAllPillars(2, effects.get(2), 1); break;
					case 35: launchAllPillars(2, effects.get(3), 1); break;
					case 40: launchAllPillars(2, effects.get(4), 1); break;
					case 50:
						pillars.get(1).launchFromLoc(4, effects.get(1), 2);
						pillars.get(9).launchFromLoc(4, effects.get(1), 2);
						break;
					case 55:
						pillars.get(2).launchFromLoc(4, effects.get(1), 2);
						pillars.get(8).launchFromLoc(4, effects.get(1), 2);
						break;
					case 65:
						pillars.get(3).launchFromLoc(4, effects.get(1), 2);
						pillars.get(7).launchFromLoc(4, effects.get(1), 2);
						break;
					case 70:
						pillars.get(4).launchFromLoc(4, effects.get(1), 2);
						pillars.get(6).launchFromLoc(4, effects.get(1), 2);
						break;
					case 75:
						p = pillars.get(5);
						p.launchFromLoc(1, effects.get(1), 2);
						p.launchFromLoc(2, effects.get(1), 2);
						p.launchFromLoc(3, effects.get(1), 1);
						p.launchFromLoc(4, effects.get(1), 1);
						break;
					case 78:
						launchWholePillar(5, effects.get(1), 2);
						break;
					case 82:
						p = pillars.get(5);
						p.launchFromLoc(1, effects.get(1), 2);
						p.launchFromLoc(2, effects.get(1), 2);
						p.launchFromLoc(3, effects.get(1), 3);
						p.launchFromLoc(4, effects.get(1), 3);
						break;
					case 95:
						xCount = 1;
						e = effects.get(2); ex = effects.get(5);
						while (xCount < 10) {
							p = pillars.get(xCount);
							p.launchFromLoc(1, e, 0);
							p.launchFromLoc(3, ex, 2);
							xCount++;
						}
						break;
					case 105:	pillars.get(5).launchFromLoc(1, effects.get(6), 2); break;
					case 110:	pillars.get(5).launchFromLoc(2, effects.get(6), 1); break;
					case 115:	pillars.get(5).launchFromLoc(3, effects.get(6), 0); break;
					case 125:
						launchWholePillar(1, effects.get(2), 0);
						launchWholePillar(9, effects.get(2), 0);
						break;
					case 129:
						launchWholePillar(2, effects.get(2), 0);
						launchWholePillar(8, effects.get(2), 0);
						break;
					case 132:
						launchWholePillar(3, effects.get(2), 0);
						launchWholePillar(7, effects.get(2), 0);
						break;
					case 134:
						launchWholePillar(4, effects.get(2), 0);
						launchWholePillar(6, effects.get(2), 0);
						break;
					case 135:
						launchWholePillar(5, effects.get(2), 0);
						launchWholePillar(5, effects.get(2), 0);
						break;
					case 145: launchAllPillars(1, effects.get(6), 1); break;
					case 155: launchAllPillars(1, effects.get(7), 1); break;
					case 160: launchAllPillars(1, effects.get(8), 1); break;
					case 165: launchAllPillars(1, effects.get(8), 2); break;
					case 180: launchWholePillar(1, effects.get(9), 2); break;
					case 183: launchWholePillar(2, effects.get(9), 2); break;
					case 186: launchWholePillar(3, effects.get(9), 2); break;
					case 192: launchWholePillar(9, effects.get(10), 2); break;
					case 195: launchWholePillar(8, effects.get(10), 2); break;
					case 198: launchWholePillar(7, effects.get(10), 2); break;
					case 200:
						launchWholePillar(4, effects.get(9), 3);
						launchWholePillar(6, effects.get(9), 3);
						break;	
					case 205:
						launchWholePillar(4, effects.get(10), 2);
						launchWholePillar(6, effects.get(10), 2);
						break;	
					case 210:
						launchWholePillar(5, effects.get(9), 2);
						launchWholePillar(5, effects.get(10), 3);
						break;	
					case 220:
						launchWholePillar(4, effects.get(11), 3);
						launchWholePillar(6, effects.get(12), 3);
						break;
					case 225: launchWholePillar(5, effects.get(8), 2); break;
					case 230: launchWholePillar(5, effects.get(8), 1); break;
					case 235: launchWholePillar(5, effects.get(8), 2); break;
					case 237: launchWholePillar(5, effects.get(8), 1); break;
					case 239: launchAllPillars(1, effects.get(8), 1); break;
					case 242: launchAllPillars(1, effects.get(8), 1); break;
					case 250:
						launchAllPillars(2, effects.get(8), 2);
						launchAllPillars(2, effects.get(8), 3);
						break;
					case 270:
						pillars.get(1).launchFromLoc(1, effects.get(2), 0);
						pillars.get(9).launchFromLoc(1, effects.get(2), 0);
						break;
					case 271:
						pillars.get(2).launchFromLoc(1, effects.get(2), 0);
						pillars.get(8).launchFromLoc(1, effects.get(2), 0);
						break;
					case 272:
						pillars.get(3).launchFromLoc(1, effects.get(2), 0);
						pillars.get(7).launchFromLoc(1, effects.get(2), 0);
						break;
					case 273:
						pillars.get(4).launchFromLoc(1, effects.get(2), 0);
						pillars.get(6).launchFromLoc(1, effects.get(2), 0);
						break;
					case 274:
						pillars.get(5).launchFromLoc(1, effects.get(2), 0);
						break;
					case 276:
						pillars.get(4).launchFromLoc(1, effects.get(2), 0);
						pillars.get(6).launchFromLoc(1, effects.get(2), 0);
						break;
					case 277:
						pillars.get(3).launchFromLoc(1, effects.get(2), 0);
						pillars.get(7).launchFromLoc(1, effects.get(2), 0);
						break;
					case 278:
						pillars.get(2).launchFromLoc(1, effects.get(2), 0);
						pillars.get(8).launchFromLoc(1, effects.get(2), 0);
						break;
					case 279:
						pillars.get(1).launchFromLoc(1, effects.get(2), 0);
						pillars.get(9).launchFromLoc(1, effects.get(2), 0);
						break;
					case 282: launchAllPillars(1, effects.get(2), 0); break;
					case 285: launchAllPillars(1, effects.get(13), 1); break;
					case 288: launchAllPillars(1, effects.get(13), 0); break;
					case 292: launchAllPillars(1, effects.get(7), 0); break;
					case 295: launchAllPillars(1, effects.get(8), 1); break;
					case 297: 
						launchAllPillars(1, effects.get(7), 1);
						pillars.get(5).launchFromLoc(3, effects.get(8), 2);
						break;
					case 300:
						launchAllPillars(1, effects.get(8), 1);
						pillars.get(5).launchFromLoc(3, effects.get(8), 2);
						break;
					case 303: launchAllPillars(3, effects.get(8), 2); break;
					case 320:
						showRunning = false;
						teleportAllow = false;
						showPillar = 0;
						taskId.cancel();
						break;
				}
				showPillar++;
			}
		}, 20, 5);
	}
	
	private void launchWholePillar(int pillar, FireworkEffect f, int time) {
		DispenserPillar p = pillars.get(pillar);
		p.launchFromLoc(1, f, time);
		p.launchFromLoc(2, f, time);
		p.launchFromLoc(3, f, time);
		p.launchFromLoc(4, f, time);
	}
	
	private void launchAllPillars(int loc, FireworkEffect f, int time) {
		int xCount = 1;
		while (xCount < 10) {
			DispenserPillar p = pillars.get(xCount);
			p.launchFromLoc(1, f, time);
			xCount++;
		}
	}
	
	public boolean isShowRunning(){
		return showRunning;
	}
	
	public boolean isTeleportAllowed(){
		return teleportAllow;
	}
	
	public boolean toggleTeleportAllowed(){
		if (teleportAllow) teleportAllow = false;
		else teleportAllow = true;
		return teleportAllow;
	}
	
	private FireworkEffect createFireworkEffect(Color[] colors, FireworkEffect.Type type, boolean flicker, boolean trail){
		return createFireworkEffect(colors, type, flicker, trail, null);
	}
	
	private FireworkEffect createFireworkEffect(Color[] colors, FireworkEffect.Type type, boolean flicker, boolean trail, Color[] fade){
		Builder builder = FireworkEffect.builder();
		builder.withColor(colors);
		builder.with(type);
		if (flicker) builder.withFlicker();
		if (trail) builder.withTrail();
		if (fade != null) {
			builder.withFade(fade);
		}
		return builder.build();
	}
	
	private class DispenserPillar {
		
		private Location s1;
		private Location s2;
		private Location s3;
		private Location s4;
		private World world;
		
		public DispenserPillar(Location s1) {
			this.s1 = s1;
			world = s1.getWorld();
			int x = s1.getBlockX();
			int y = s1.getBlockY();
			int z = s1.getBlockZ();
			s2 = new Location(world, x - 1, y, z);
			s3 = new Location(world, x - 1, y, z + 1);
			s4 = new Location(world, x, y, z + 1);
		}
		
		public void launchFromLoc(int spot, FireworkEffect effect, int flightSeconds) {
			Location launchLoc;
			switch (spot) {
			case 1: launchLoc = s1; break;
			case 2: launchLoc = s2; break;
			case 3: launchLoc = s3; break;
			case 4: launchLoc = s4; break;
			default: launchLoc = s1;
			}
			Firework f = world.spawn(launchLoc, Firework.class);
			FireworkMeta fMeta = f.getFireworkMeta();
			fMeta.addEffect(effect);
			fMeta.setPower(flightSeconds);
			f.setFireworkMeta(fMeta);
		}
		
	}
	
}
