package com.luffbox.smoothsleep.tasks;

import com.luffbox.smoothsleep.PlayerData;
import com.luffbox.smoothsleep.SmoothSleep;
import com.luffbox.smoothsleep.WorldData;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Set;

/**
 * This task only runs during the night while players are sleeping.
 * It handles stepping ticks and cancels itself when it's done.
 */
public class SleepTickTask extends BukkitRunnable {

	private SmoothSleep pl;
	private WorldData wd;
	private int counter;

	public SleepTickTask(SmoothSleep plugin, WorldData worldData) {
		pl = plugin;
		wd = worldData;
		counter = 0;
	}

	@Override
	public void run() {
		Set<Player> sleepers = wd.getSleepers();
		wd.updateTimescale();
		boolean isNight = wd.isNight();
		if (isNight) { wd.timestep(); }
		SmoothSleep.logDebug("===== SleepTickTask#run() ======");
		SmoothSleep.logDebug("| - isNight = " + isNight);
		SmoothSleep.logDebug("| - sleeper count = " + sleepers.size());
		sleepers.forEach(plr -> {
			PlayerData pd = pl.data.getPlayerData(plr);
			if (pd != null) {
				SmoothSleep.logDebug("| - Sleeper: " + pd.getPlayer().getName());
				SmoothSleep.logDebug("| --- is sleeping = " + pd.isSleeping());
				if (isNight) {
					pd.updateUI();
					if (counter > 50) {
						pd.setSleepTicks(0);
						counter = 0;
					}
					counter++;
				} else {
					pd.wake();
				}
			} else {
				SmoothSleep.logDebug("| - Sleeper's PlayerData is null!");
			}
		});
		if (!isNight || sleepers.isEmpty()) { wd.stopSleepTick(); }
	}
}
