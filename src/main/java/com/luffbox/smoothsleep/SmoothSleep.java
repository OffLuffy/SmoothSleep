package com.luffbox.smoothsleep;

import com.luffbox.lib.LuffPlugin;
import com.luffbox.smoothsleep.lib.DataStore;
import org.bstats.bukkit.Metrics;

public final class SmoothSleep extends LuffPlugin {

	// DataStore will contain config and world data
	public static DataStore data;

	public static boolean hasUpdate = false;

	@Override
	public void onEnable() {
		// Plugin startup logic
		resourceId = "32043";
		loadData();
	}

	@Override
	public void onDisable() {
		// Plugin shutdown logic
		purgeData();
	}

	public void loadData() {
		hasUpdate = checkUpdate();
		data = new DataStore(this);
	}

	public void purgeData() {
		data.purgeData();
	}
}
