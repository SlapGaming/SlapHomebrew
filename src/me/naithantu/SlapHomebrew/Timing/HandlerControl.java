package me.naithantu.SlapHomebrew.Timing;

import me.naithantu.SlapHomebrew.Util.Util;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.RegisteredListener;

import java.lang.reflect.Field;

public class HandlerControl {

	public HandlerControl() {
		Util.runLater(new Runnable() {
			
			@Override
			public void run() {
				Field f = null;
				for (Field r : RegisteredListener.class.getDeclaredFields()) {
					if (r.getName().startsWith("executor")) {
						r.setAccessible(true);
						f = r;
					}
				}
				if (f == null) {
					System.out.println("Failed to get Field.");
					return;
				}
				for (HandlerList handlerList : HandlerList.getHandlerLists()) {
					RegisteredListener[] listeners = handlerList.getRegisteredListeners();
					for (int x = 0; x < listeners.length; x++) {
						RegisteredListener currentListener = listeners[x];
						try {
							EventExecutor e = (EventExecutor) f.get(currentListener);
							listeners[x] = new ModifiedRegisteredListener(
									currentListener.getListener(),
									e,
									currentListener.getPriority(),
									currentListener.getPlugin(),
									currentListener.isIgnoringCancelled()
							);
						} catch (IllegalAccessException e) {
							System.out.println("Failed to change Listener for " + currentListener.getPlugin().getName());
						}
					}
				}
			}
		}, 30);
	}
	
	

}
