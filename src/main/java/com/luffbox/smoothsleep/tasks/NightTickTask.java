package com.luffbox.smoothsleep.tasks;

import com.luffbox.lib.ReflectUtil;
import com.luffbox.smoothsleep.data.PlayerData;
import com.luffbox.smoothsleep.data.WorldData;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class NightTickTask extends BukkitRunnable {

	private int deepSleepCount = 0;

	WorldData worldData;
	public NightTickTask(WorldData worldData) {
		this.worldData = worldData;
	}

	@Override
	public void run() {
		if (worldData == null || !worldData.isNight()) { cancel(); return; }
		if (deepSleepCount >= 50) {
			setSleepTicks(0);
			deepSleepCount = 0;
		} else { deepSleepCount++; }
	}

	private void setSleepTicks(int ticks) {
		for (PlayerData plr : worldData.getSleeperData()) {
			try {
				Object nmsPlr = ReflectUtil.invokeMethod(plr, "getHandle");
				ReflectUtil.setValue(nmsPlr, false, "sleepTicks", ticks);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
