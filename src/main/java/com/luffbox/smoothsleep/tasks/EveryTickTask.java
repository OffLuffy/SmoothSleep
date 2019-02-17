package com.luffbox.smoothsleep.tasks;

import com.luffbox.smoothsleep.SmoothSleep;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * The task that will run on every tick as long as the plugin is
 * enabled. It will only collect relevant information and store
 * it in the DataStore to be handled by other tasks later.
 * @see com.luffbox.smoothsleep.DataStore
 */
public class EveryTickTask extends BukkitRunnable {

	private SmoothSleep pl;
	public EveryTickTask(SmoothSleep plugin) { pl = plugin; }

	@Override
	public void run() {
		if (!pl.isEnabled()) { cancel(); return; }
	}
}
