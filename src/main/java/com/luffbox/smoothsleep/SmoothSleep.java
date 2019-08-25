package com.luffbox.smoothsleep;

import com.luffbox.lib.ConfigHelper;
import com.luffbox.lib.LuffPlugin;
import com.luffbox.smoothsleep.data.GlobalConfig;
import com.luffbox.smoothsleep.data.WorldData;
import org.bstats.bukkit.Metrics;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class SmoothSleep extends LuffPlugin {

	private ConfigHelper conf;
	private GlobalConfig globalConf;
	private Map<World, WorldData> worldData = new HashMap<>();

	public static boolean hasUpdate = false;
	public static Metrics metrics;

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
		metrics = new Metrics(this);
		conf = new ConfigHelper(this);
		globalConf = new GlobalConfig(this);
		Set<String> worlds = conf.getConfSection("worlds").getKeys(false);
		worlds.forEach(w -> {
			World world = getServer().getWorld(w);
			try {
				worldData.put(world, new WorldData(this, world));
			} catch (NullPointerException e) {
				// TODO Log null world in config
			}
		});
	}

	public void purgeData() {
		metrics = null;
		globalConf = null;
		worldData.clear();
	}

	public ConfigHelper getConfigHelper() { return conf; }
	public GlobalConfig getGlobalConfig() { return globalConf; }
	public WorldData getWorldData(World w) { return worldData.get(w); }
}
