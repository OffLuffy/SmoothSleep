package com.luffbox.smoothsleep.tasks;

import com.luffbox.smoothsleep.SmoothSleep;
import com.luffbox.smoothsleep.WorldData;
import com.luffbox.smoothsleep.events.NightEndEvent;
import com.luffbox.smoothsleep.events.NightStartEvent;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

/**
 * The task that will run on every tick as long as the plugin is
 * enabled. It will only collect relevant information and store
 * it in the DataStore to be handled by other tasks later.
 * @see com.luffbox.smoothsleep.DataStore
 */
public class EveryTickTask extends BukkitRunnable {

	private SmoothSleep pl;
	public EveryTickTask(SmoothSleep plugin) { pl = plugin; }

	private Map<World, Boolean> night = new HashMap<>();

	@Override
	public void run() {
		if (!pl.isEnabled()) {
			cancel();
			return;
		}
		for (Map.Entry<World, WorldData> w : pl.data.getWorldData().entrySet()) {
			World world = w.getKey();
			WorldData wd = w.getValue();
			if (!night.containsKey(world)) { night.put(world, wd.isNight()); }
			if (night.get(world) != wd.isNight()) {
				night.put(world, wd.isNight());
				if (wd.isNight()) {
					NightStartEvent nse = new NightStartEvent(world);
					pl.getServer().getPluginManager().callEvent(nse);
				} else {
					NightEndEvent nee = new NightEndEvent(world);
					pl.getServer().getPluginManager().callEvent(nee);
				}
			}
		}
	}
}
