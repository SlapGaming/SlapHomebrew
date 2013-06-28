package me.naithantu.SlapHomebrew.Runnables;

import java.util.ArrayList;

import me.naithantu.SlapHomebrew.ApplyChecker;
import me.naithantu.SlapHomebrew.Storage.XMLParser;
import me.naithantu.SlapHomebrew.Storage.YamlStorage;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

public class ApplyGathererTask extends BukkitRunnable{

	private ApplyChecker plugin;
	private YamlStorage applyThreadStorage;
	private FileConfiguration applyThreadConfig;
	private boolean firstRun = true;
	
	
	public ApplyGathererTask(ApplyChecker plugin, FileConfiguration applyThreadConfig, YamlStorage applyThreadStorage) {
		this.plugin = plugin;
		this.applyThreadConfig = applyThreadConfig;
		this.applyThreadStorage = applyThreadStorage;
	}
	
    @Override
    public void run() {
    	checkChanges();
    }    
    
    public void checkChanges(){
    	ArrayList<Integer> threadNrs = XMLParser.getThreads();
    	if (threadNrs == null) {
    		//Forums down
    		plugin.forumsDown();
    		return;
    	}
    	boolean changed = false;
    	for (int threadNr : threadNrs) {
			Object storedThread = applyThreadConfig.getBoolean(String.valueOf(threadNr)); //Find the thread in YAML file
			if (storedThread == null || (firstRun && !(boolean)storedThread)) { //If not found create a new one
				Object[] applyThread = XMLParser.createApplyThread(threadNr);
				if (applyThread != null) { //If new thread is not null
					boolean done = false;
					if ((boolean)applyThread[3] == false) { //If not dealt with
						if ((boolean)applyThread[2]) { //If correctly filled in
							boolean userFound = plugin.findUser((String)applyThread[1]);
							if (userFound) {
								done = true;
							} else {
								plugin.warnMods(new String[]{"Username doesn't exist.", (String)applyThread[1], String.valueOf(applyThread[0])});
							}
						} else {
							plugin.warnMods(new String[]{"Not correctly filled in.", (String)applyThread[1], String.valueOf(applyThread[0])});
						}
					} else {
						done = true;
					}
					applyThreadConfig.set(String.valueOf(threadNr), done);
					changed = true;
				}
			}
    	}
    	if (changed){
    		applyThreadStorage.saveConfig();
    	}
    	if (!firstRun) {
    		plugin.warnModsFailedThreads();
    	}
    	firstRun = false;
    }
    

}
