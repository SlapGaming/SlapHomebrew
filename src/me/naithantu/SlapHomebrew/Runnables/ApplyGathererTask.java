package me.naithantu.SlapHomebrew.Runnables;

import java.util.ArrayList;

import me.naithantu.SlapHomebrew.ApplyChecker;
import me.naithantu.SlapHomebrew.Storage.XMLParser;
import me.naithantu.SlapHomebrew.Storage.YamlStorage;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

public class ApplyGathererTask extends BukkitRunnable {

	private ApplyChecker applyChecker;
	private YamlStorage applyThreadStorage;
	private FileConfiguration applyThreadConfig;
	private boolean firstRun = true;

	public ApplyGathererTask(ApplyChecker applyChecker, FileConfiguration applyThreadConfig, YamlStorage applyThreadStorage) {
		this.applyChecker = applyChecker;
		this.applyThreadConfig = applyThreadConfig;
		this.applyThreadStorage = applyThreadStorage;
	}

	@Override
	public void run() {
		checkChanges();
	}

	public void checkChanges() {
		ArrayList<Integer> threadNrs = XMLParser.getThreads();
		if (threadNrs == null) {
			//Forums down
			applyChecker.forumsDown();
			return;
		}
		boolean changed = false;
		for (int threadNr : threadNrs) {
			Boolean storedThread = applyThreadConfig.getBoolean(String.valueOf(threadNr)); //Find the thread in YAML file
			if (storedThread == null || (firstRun && !storedThread)) { //If not found create a new one
				Object[] applyThread = XMLParser.createApplyThread(threadNr);
				if (applyThread != null) { //If new thread is not null
					boolean done = false;
					if ((Boolean) applyThread[3] == false) { //If not dealt with
						if ((Boolean) applyThread[2]) { //If correctly filled in
							boolean userFound = applyChecker.findUser((String) applyThread[1]);
							if (userFound) {
								done = true;
							} else {
								applyChecker.warnMods(new String[] { "Username doesn't exist.", (String) applyThread[1], String.valueOf(applyThread[0]) });
							}
						} else {
							applyChecker.warnMods(new String[] { "Not correctly filled in.", (String) applyThread[1], String.valueOf(applyThread[0]) });
						}
					} else {
						done = true;
					}
					applyThreadConfig.set(String.valueOf(threadNr), done);
					changed = true;
				}
			}
		}
		if (changed) {
			applyThreadStorage.saveConfig();
		}
		if (!firstRun) {
			applyChecker.warnModsFailedThreads();
		}
		firstRun = false;
	}

}