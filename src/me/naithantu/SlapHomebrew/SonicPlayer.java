package me.naithantu.SlapHomebrew;

import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class SonicPlayer {

	String playerName;
	int lastCheckpoint = -1;
	long startTime;
	Sonic sonic;
	long[] checkpointTimes = new long[6];

	public SonicPlayer(Sonic sonic, String playerName) {
		this.sonic = sonic;
		this.playerName = playerName;
	}

	public void addCheckpoint(int checkpoint) {
		if (lastCheckpoint + 1 == checkpoint) {
			if (checkpoint == 0) {
				float yaw = Bukkit.getServer().getPlayer(playerName).getLocation().getYaw();
				//Start round only if they walk over the start line in the correct direction.
				if (yaw < 90 || yaw > 270){
					System.out.println("[SLAP] Sonic: Yaw is wrong - not passing start line!");
					return;
				}

				Util.broadcastToWorld("world_sonic", ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + playerName + " started racing.");
				startTime = new Date().getTime();
			} else {
				long currentTime = (new Date().getTime() - startTime);
				Util.broadcastToWorld("world_sonic", ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + playerName + " passed checkpoint " + checkpoint + ". Time: " + Util.changeTimeFormat(currentTime));
				checkpointTimes[lastCheckpoint] = currentTime;
			}
			lastCheckpoint++;
		} else if (lastCheckpoint == 5 && checkpoint == 0) {
			long currentTime = (new Date().getTime() - startTime);
			checkpointTimes[lastCheckpoint] = currentTime;
			sonic.addHighscore(playerName, checkpointTimes);
			Util.broadcastToWorld("world_sonic", ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + playerName + " finished with a time of " + Util.changeTimeFormat(currentTime) + " seconds.");
			lastCheckpoint = -1;
			return;
		}
		return;
	}
}
