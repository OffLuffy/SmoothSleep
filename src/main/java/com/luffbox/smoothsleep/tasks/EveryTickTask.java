package com.luffbox.smoothsleep.tasks;

import com.luffbox.smoothsleep.data.WorldData;
import com.luffbox.smoothsleep.events.NightBeginEvent;
import com.luffbox.smoothsleep.events.NightEndEvent;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class EveryTickTask extends BukkitRunnable {

	WorldData worldData;
	public EveryTickTask(WorldData worldData) {
		this.worldData = worldData;
	}

	@Override
	public void run() {
		if (worldData.wasNight != worldData.isNight()) {
			worldData.wasNight = worldData.isNight();
			if (worldData.wasNight) {
				NightBeginEvent nbe = new NightBeginEvent(worldData);
				Bukkit.getPluginManager().callEvent(nbe);
			} else {
				NightEndEvent nee = new NightEndEvent(worldData);
				Bukkit.getPluginManager().callEvent(nee);
			}
		}
	}
}
