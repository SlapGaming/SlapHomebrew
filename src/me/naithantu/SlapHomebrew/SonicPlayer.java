package me.naithantu.SlapHomebrew;

import java.util.Date;

import org.bukkit.ChatColor;

public class SonicPlayer {
	
	String playerName;
	int lastCheckpoint = -1;
	long startTime;
	Sonic sonic;
	
	public SonicPlayer(Sonic sonic, String playerName){
		this.sonic = sonic;
		this.playerName = playerName;
	}

	public Long addCheckpoint(int checkpoint){
		if(lastCheckpoint + 1 == checkpoint){
			lastCheckpoint++;
			if (checkpoint == 0){
				Util.broadcastToWorld("world_sonic", ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + playerName + " started racing.");
				startTime = new Date().getTime();
			} else{
				long currentTime = (new Date().getTime() - startTime);
				System.out.println("Time: " + currentTime);
				Util.broadcastToWorld("world_sonic", ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + playerName + " passed checkpoint " + checkpoint + ". Time: " + Util.changeTimeFormat(currentTime));
			}
		} else if (lastCheckpoint == 5 && checkpoint == 0){
			lastCheckpoint = -1;
			long totalTime = (new Date().getTime() - startTime);
			sonic.addHighscore(playerName, totalTime);
			Util.broadcastToWorld("world_sonic", ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + playerName + " finished with a time of " + Util.changeTimeFormat(totalTime) + " seconds.");
			return totalTime;
		}
		return null;
	}
}
