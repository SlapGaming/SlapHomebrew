package me.naithantu.SlapHomebrew;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Team;

public class Extras {
	SlapHomebrew plugin;

	HashMap<String, Integer> rainbow = new HashMap<String, Integer>();
	List<String> hasJumped = new ArrayList<String>();
	Menus menus;
	List<String> ghosts = new ArrayList<String>();
	Team ghostTeam;

	public Extras(SlapHomebrew plugin) {
		this.plugin = plugin;
		menus  = new Menus(plugin);
		ghostTeam = Bukkit.getScoreboardManager().getNewScoreboard().registerNewTeam("ghosts");
		ghostTeam.setCanSeeFriendlyInvisibles(true);
	}

	public HashMap<String, Integer> getRainbow() {
		return rainbow;
	}

	public void setRainbow(HashMap<String, Integer> rainbow) {
		this.rainbow = rainbow;
	}
	
	public List<String> getHasJumped(){
		return hasJumped;
	}
	
	public void setHasJumped(List<String> hasJumped){
		this.hasJumped = hasJumped;
	}
	
	public Menus getMenus(){
		return menus;
	}
	
	public List<String> getGhosts(){
		return ghosts;
	}
	
	public void setGhosts(List<String> ghosts){
		this.ghosts = ghosts;
	}
	
	public Team getGhostTeam(){
		return ghostTeam;
	}
}
