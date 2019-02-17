package com.luffbox.smoothsleep.tasks;

import com.luffbox.smoothsleep.PlayerData;
import com.luffbox.smoothsleep.SmoothSleep;
import com.luffbox.smoothsleep.WorldData;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Set;

/**
 * The task where the majority of the heavy lifting is done. Using the
 * data collected in the EveryTickTask, this task will only be running
 * if a player is sleeping. This lets me keep the task that always runs
 * relatively light weight, minimizing the resource impact of the plugin.
 * @see EveryTickTask
 */
public class SleepTickTask extends BukkitRunnable {

	private SmoothSleep pl;
	private WorldData wd;
//	private boolean tickWeather;
	public SleepTickTask(SmoothSleep plugin, WorldData worldData) {
		pl = plugin;
		wd = worldData;
//		tickWeather = wd.getSettings().getBoolean(ConfigHelper.WorldSettingKey.ADVANCE_WEATHER);
	}

	@Override
	public void run() {
		// TODO Tick player timers
		wd.updateTimescale();
		if (wd.isNight()) { wd.timestep(); }
//		if (wd.hasAnyWeather() && tickWeather) { wd.timestepWeather(); }
		if (!wd.isNight()) { // Not night or weather, cancel everything
			Set<Player> sleepers = wd.getSleepers();
			wd.getSleepers().forEach(plr -> {
				PlayerData pd = pl.data.getPlayerData(plr);
				if (pd != null) {
					pd.setWoke(true);
					pd.stopDeepSleep();
				} // Cancelling deep sleep task should remove them from bed
			});
			cancel();
		}
		if (wd.getSleepers().size() <= 0) { cancel(); } else {
			wd.timestepTimers(wd.getTimescale()); wd.tickUI();
		}
	}

	@Override
	public void cancel() {
		super.cancel();
		PostSleepTickTask pst = new PostSleepTickTask(pl, wd);
		pst.runTaskLater(pl, 1);
	}
}
