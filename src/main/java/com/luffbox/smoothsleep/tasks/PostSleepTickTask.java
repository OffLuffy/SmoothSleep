package com.luffbox.smoothsleep.tasks;

import com.luffbox.smoothsleep.PlayerData;
import com.luffbox.smoothsleep.SmoothSleep;
import com.luffbox.smoothsleep.WorldData;
import org.bukkit.scheduler.BukkitRunnable;

// This task is intended to run a single time after the sleep tick task ends in order to update UI elements
public class PostSleepTickTask extends BukkitRunnable {

	private SmoothSleep pl;
	private WorldData wd;
	public PostSleepTickTask(SmoothSleep plugin, WorldData worldData) {
		pl = plugin;
		wd = worldData;
	}
	@Override
	public void run() {
		//wd.tickUI();
		wd.getPlayers().forEach(plr -> {
			PlayerData pd = pl.data.getPlayerData(plr);
			if (pd != null) { pd.clearActionBar(); pd.hideBossBar(); }
		});
		cancel(); // To make sure it only runs once
	}
}
