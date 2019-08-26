package com.luffbox.smoothsleep.tasks;

import com.luffbox.smoothsleep.data.WorldData;
import org.bukkit.scheduler.BukkitRunnable;

public class NightTickTask extends BukkitRunnable {

	WorldData worldData;
	public NightTickTask(WorldData worldData) {
		this.worldData = worldData;
	}

	@Override
	public void run() {
		if (worldData == null || !worldData.isNight()) { cancel(); return; }

	}
}
