package com.luffbox.smoothsleep.tasks;

import com.luffbox.smoothsleep.SmoothSleep;
import com.luffbox.smoothsleep.lib.ReflectUtil;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * A task to run less frequently and prevents players from being kicked out
 * of bed before it reaches morning time. Otherwise, the player will be
 * kicked out of bed after 5 seconds regardless of the time.
 */
public class DeepSleepTask extends BukkitRunnable {

	// Thanks to IAlIstannen @ Spigot for this idea of NMS sleepTick modification

	private SmoothSleep pl;
	private Player plr;
	public DeepSleepTask(SmoothSleep plugin, Player sleeper) {
		pl = plugin;
		plr = sleeper;
	}

	@Override
	public void run() {
		if (plr == null) { cancel(); return; }
		setSleepTicks(0);
	}

	@Override
	public void cancel() {
		setSleepTicks(100);
		super.cancel();
	}

	private void setSleepTicks(long ticks) {
		try {
			Object nmsPlr = ReflectUtil.invokeMethod(plr, "getHandle");
			ReflectUtil.setValue(nmsPlr, false, "sleepTicks", (int) ticks);
		} catch (Exception e) { e.printStackTrace(); }
	}
}
