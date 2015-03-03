package me.naithantu.SlapHomebrew.Timing;

import me.naithantu.SlapHomebrew.Util.Log;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;

public class ModifiedRegisteredListener extends RegisteredListener {

	public ModifiedRegisteredListener(Listener listener, EventExecutor executor, EventPriority priority, Plugin plugin, boolean ignoreCancelled) {
		super(listener, executor, priority, plugin, ignoreCancelled);
	}
	
	@Override
	public void callEvent(Event event) throws EventException {
        if (event.isAsynchronous()) {
        	super.callEvent(event);
        } else {
        	long start = System.currentTimeMillis();
    		super.callEvent(event);
    		long passedTime = System.currentTimeMillis() - start;
    		if (passedTime > 50) {
    			Log.severe("Tick disrupted by: " + getPlugin().getName() + " | Event: " + event.getEventName() + " | Took: " + passedTime + " ms.");
    		}
        }
		
	}

}
