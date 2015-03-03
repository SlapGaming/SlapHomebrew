package me.naithantu.SlapHomebrew.Runnables;

import me.naithantu.SlapHomebrew.SlapHomebrew;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Random;

public class WeatherTask extends BukkitRunnable {
	
	private ArrayList<World> worlds;
	private Random random;
	
	private int nextWeather; //Time till next weather
	private int stormLeft; //Time till weather stops
	private boolean thunderComing;
	private int thunderLeft; //Time till storm stops
	
	
	public WeatherTask(SlapHomebrew plugin) {
		worlds = new ArrayList<>();
		random = new Random();
		for (World w : plugin.getServer().getWorlds()) {
			String worldname = w.getName().toLowerCase();
			if (worldname.contains("resource") || worldname.equals("world") || worldname.equals("world_survival2") || worldname.equals("world_survival3")) {
				worlds.add(w);
			}
		}
		nextWeather = 0;
		stormLeft = -1;
		thunderLeft = -1;
		thunderComing = false;
	}
	
	
	@Override
	public void run() {
		nextWeather--;
		if (stormLeft > -1) stormLeft--;
		if (thunderLeft > -1) thunderLeft--;	
		if (nextWeather < 1) {
			calculateNextWeather();
			setToStorm();
		} else {
			if (stormLeft == 0) {
				setToSun(true);
			} else {
				if (thunderComing) {
					if (random.nextInt(4) == 2) {
						setToThunder();
					}
				}
			}
		}
	}
	
		
	private void setToStorm() {
		for (World w : worlds) {
			w.setStorm(true);
			w.setWeatherDuration(toTicks(stormLeft));
		}
	}
	
	private void setToThunder() {
		for (World w : worlds) {
			w.setThundering(true);
			if (thunderLeft > stormLeft) {
				w.setThunderDuration(toTicks(stormLeft));
			} else {
				w.setThunderDuration(toTicks(thunderLeft));
			}
			thunderLeft = -1;
			thunderComing = false;
		}
	}
	
	private void setToSun(boolean withTicks) {
		for (World w : worlds) {
			w.setStorm(false);
			w.setThundering(false);
			if (withTicks) w.setWeatherDuration(toTicks(nextWeather));
		}
	}
	
	private void calculateNextWeather() {
		while (nextWeather < 30) {
			nextWeather = random.nextInt(150);
		}
		stormLeft = random.nextInt(5) + 1;
		thunderLeft = -1;
		thunderComing = random.nextBoolean();
		if (thunderComing) {
			thunderLeft = random.nextInt(1) + 1; 
		}
	}
	
	private int toTicks(int minutes) {
		return 20 * 60 * minutes;
	}
	
	

}
