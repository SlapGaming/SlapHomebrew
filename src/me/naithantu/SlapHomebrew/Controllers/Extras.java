package me.naithantu.SlapHomebrew.Controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Team;

public class Extras extends AbstractController {
	HashSet<String> pvpWorld = new HashSet<String>();
	HashSet<String> pvpTimer = new HashSet<String>();
	
	HashMap<String, Integer> rainbow = new HashMap<String, Integer>();
	HashMap<String, HomeMenu> homeMenus = new HashMap<String, HomeMenu>();
	List<String> hasJumped = new ArrayList<String>();
	Menus menus;
	HashSet<String> ghosts = new HashSet<String>();
	Team ghostTeam;

	public Extras() {
		menus  = new Menus();
		ghostTeam = Bukkit.getScoreboardManager().getNewScoreboard().registerNewTeam("ghosts");
		ghostTeam.setCanSeeFriendlyInvisibles(true);
	}
	
	public HashSet<String> getPvpWorld(){
		return pvpWorld;
	}
	
	public HashSet<String> getPvpTimer(){
		return pvpTimer;
	}

	public HashMap<String, Integer> getRainbow() {
		return rainbow;
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
	
	public HashSet<String> getGhosts() {
		return ghosts;
	}
	
	public Team getGhostTeam(){
		return ghostTeam;
	}
	
	public HashMap<String, HomeMenu> getHomeMenus() {
		return homeMenus;
	}
	
	@Override
	public void shutdown() {
		//Not needed
	}
}
