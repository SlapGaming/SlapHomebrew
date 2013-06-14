package me.naithantu.SlapHomebrew;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Extras {
	SlapHomebrew plugin;

	HashMap<String, Integer> rainbow = new HashMap<String, Integer>();
	List<String> hasJumped = new ArrayList<String>();
	Menus menus;


	public Extras(SlapHomebrew plugin) {
		this.plugin = plugin;
		menus  = new Menus(plugin);
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
	
	
}
