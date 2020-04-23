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
 * enabled. It's only used to trigger night start and end events
 * @see NightStartEvent
 * @see NightEndEvent
 */
public class EveryTickTask extends BukkitRunnable {

	private final SmoothSleep pl;
	private final Map<World, Boolean> night = new HashMap<>();

	public EveryTickTask(SmoothSleep plugin) { pl = plugin; }

	@Override
	public void run() {
		if (!pl.isEnabled()) { return; }
		for (Map.Entry<World, WorldData> w : pl.data.getWorldData().entrySet()) {
			World world = w.getKey();
			WorldData wd = w.getValue();
			if (!night.containsKey(world)) { night.put(world, wd.isNight()); }
			if (night.get(world) != wd.isNight()) {
				night.put(world, wd.isNight());
				if (wd.isNight()) {
					NightStartEvent nse = new NightStartEvent(world, wd);
					pl.getServer().getPluginManager().callEvent(nse);
				} else {
					NightEndEvent nee = new NightEndEvent(world, wd);
					pl.getServer().getPluginManager().callEvent(nee);
				}
			}
		}
	}
}
