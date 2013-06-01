package me.naithantu.SlapHomebrew;

import java.util.Date;

import org.bukkit.ChatColor;

public class SonicPlayer {
	
	String playerName;
	int lastCheckpoint = -1;
	long startTime;
	
	public SonicPlayer(String playerName){
		this.playerName = playerName;
	}

	public Long addCheckpoint(int checkpoint){
		if(lastCheckpoint + 1 == checkpoint){
			lastCheckpoint++;
			if (checkpoint == 0){
				startTime = new Date().getTime();
			}else if(checkpoint == 6){
				lastCheckpoint = -1;
				long totalTime = (new Date().getTime() - startTime);
				Util.broadcastToWorld("world_sonic", ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + playerName + " finished with a time of " + Util.changeTimeFormat(totalTime) + " seconds.");
				return totalTime;
			} else{
				long currentTime = (new Date().getTime() - startTime);
				System.out.println("Time: " + currentTime);
				Util.broadcastToWorld("world_sonic", ChatColor.GOLD + "[SLAP] " + ChatColor.WHITE + playerName + " passed checkpoint " + checkpoint + ". Time: " + Util.changeTimeFormat(currentTime));
			}
		}
		return null;
	}
}
