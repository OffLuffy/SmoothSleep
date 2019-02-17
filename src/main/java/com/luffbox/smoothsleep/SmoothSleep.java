package com.luffbox.smoothsleep;

import com.luffbox.smoothsleep.commands.*;
import com.luffbox.smoothsleep.lib.LoggablePlugin;
import com.luffbox.smoothsleep.listeners.PlayerListeners;
import com.luffbox.smoothsleep.tasks.EveryTickTask;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

// TODO Better defaults for morning particles
// TODO Remove TestWakeParticles command

public final class SmoothSleep extends LoggablePlugin {

	public static final String PERM_IGNORE = "smoothsleep.ignore";
	public static final String PERM_NOTIFY = "smoothsleep.notify";

	public static final long SLEEP_TICKS_START = 12541L,
			SLEEP_TICKS_END = 23460L,
			SLEEP_TICKS_DURA = SLEEP_TICKS_END - SLEEP_TICKS_START + 2;
	public static final long TICKS_PER_DAY = 1728000,
			TICKS_PER_HOUR = 72000,
			TICKS_PER_MIN = 1200;

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
		logDebug("DataStore initialized");

		getServer().getPluginManager().registerEvents(new PlayerListeners(this), this);

		getServer().getPluginCommand("smoothsleepreload").setExecutor(new Reload(this));
		getServer().getPluginCommand("smoothsleeptoggle").setExecutor(new ToggleEnabled(this));
		getServer().getPluginCommand("smoothsleepmetrics").setExecutor(new ToggleMetrics(this));
		getServer().getPluginCommand("smoothsleepaddworld").setExecutor(new AddWorld(this));
		getServer().getPluginCommand("smoothsleepconfigureworld").setExecutor(new ConfigureWorld(this));

		everyTickTask = new EveryTickTask(this).runTaskTimer(this, 0L, 0L);
	}

	@Override
	public void onDisable() {
		everyTickTask.cancel();
		data.purgeData();
	}
}
