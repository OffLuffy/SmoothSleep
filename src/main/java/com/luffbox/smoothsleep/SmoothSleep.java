package com.luffbox.smoothsleep;

import com.luffbox.smoothsleep.commands.*;
import com.luffbox.smoothsleep.lib.LoggablePlugin;
import com.luffbox.smoothsleep.lib.TabExecutor;
import com.luffbox.smoothsleep.listeners.NightListeners;
import com.luffbox.smoothsleep.listeners.PlayerListeners;
import com.luffbox.smoothsleep.tasks.EveryTickTask;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.scheduler.BukkitTask;

public final class SmoothSleep extends LoggablePlugin {

	public static final String PERM_IGNORE = "smoothsleep.ignore";
	public static final String PERM_NOTIFY = "smoothsleep.notify";

	public static final long SLEEP_TICKS_START = 12541L,
			SLEEP_TICKS_END = 23460L,
			SLEEP_TICKS_DURA = SLEEP_TICKS_END - SLEEP_TICKS_START;
	public static final long TICKS_PER_DAY = 1728000L,
			TICKS_PER_HOUR = 72000L,
			TICKS_PER_MIN = 1200L;

	public static String nmsver;
	public static boolean hasUpdate = false;

	public DataStore data;
	public static Metrics metrics;

	private BukkitTask everyTickTask;

	@Override
	public void onEnable() {
		resourceId = "32043";
		nmsver = Bukkit.getServer().getClass().getPackage().getName();
		nmsver = nmsver.substring(nmsver.lastIndexOf(".") + 1);
		hasUpdate = checkUpdate();

		metrics = new Metrics(this);
		data = new DataStore(this); // init() after assign so data variable isn't null
		data.init();

		getServer().getPluginManager().registerEvents(new PlayerListeners(this), this);
		getServer().getPluginManager().registerEvents(new NightListeners(this), this);

		registerCmd("smoothsleepreload", new Reload(this));
		registerCmd("smoothsleeptoggle", new ToggleEnabled(this));
		registerCmd("smoothsleepmetrics", new ToggleMetrics(this));
		registerCmd("smoothsleepaddworld", new AddWorld(this));
		registerCmd("smoothsleepconfigureworld", new ConfigureWorld(this));

		everyTickTask = new EveryTickTask(this).runTaskTimer(this, 0L, 0L);
	}

	@Override
	public void onDisable() {
		if (everyTickTask != null) everyTickTask.cancel();
		data.purgeData();
	}

	private void registerCmd(String cmd, TabExecutor exe) {
		PluginCommand pc = getServer().getPluginCommand(cmd);
		if (pc != null) {
			pc.setExecutor(exe);
			pc.setTabCompleter(exe);
		}
	}

	/**
	 * Sets the value to be used by SmoothSleep to calculate the new night speed. SmoothSleep will not make
	 * any attempt to change the time to maintain the speed specified; it'll only be used to determine the relative
	 * speed before applying adjustments during the night. To calculate ticks that pass each tick, SmoothSleep
	 * will multiply the base time speed by the night speed multiplier, then subtract the base speed from that to
	 * account for the time passing that tick. SmoothSleep will expect any plugin altering this value to be modifying
	 * the time during both the day and the night.
	 * @param speed The base time speed, where 1.0 is vanilla speed
	 * @return A boolean indicating if the base time was set successfully. This will return false if SmoothSleep's
	 * DataStore object has not been instantiated yet.
	 */
	public boolean setBaseTimeSpeed(double speed) {
		if (data == null) {
			return false;
		} else {
			data.baseTimeSpeed = speed;
			return true;
		}
	}

}
