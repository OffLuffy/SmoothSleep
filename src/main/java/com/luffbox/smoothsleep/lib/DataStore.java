package com.luffbox.smoothsleep.lib;

import com.luffbox.lib.ConfigHelper;
import com.luffbox.smoothsleep.SmoothSleep;
import com.luffbox.smoothsleep.data.GlobalConfig;
import com.luffbox.smoothsleep.data.GlobalConfigKey;
import com.luffbox.smoothsleep.data.WorldConfig;
import com.luffbox.smoothsleep.data.WorldData;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DataStore {

	private SmoothSleep plugin;
	public static Metrics metrics;
	private ConfigHelper conf;
	private GlobalConfig globalConf;
	private Map<World, WorldData> worldData = new HashMap<>();

	public DataStore(SmoothSleep plugin) {
		this.plugin = plugin;
		loadData();
	}

	public void loadData() {
		this.conf = new ConfigHelper(plugin);
		this.globalConf = new GlobalConfig(plugin);
		Set<String> worlds = conf.getConfSection("worlds").getKeys(false);
		for (String w : worlds) {
			World world = plugin.getServer().getWorld(w);
			if (world == null) {
				SmoothSleep.logSevere("Failed to load configuration for world: " + w);
				SmoothSleep.logSevere("Make sure the world exists and has the correct name in the config.yml");
				continue;
			}
			if (world.getEnvironment() != World.Environment.NORMAL) {
				SmoothSleep.logWarning("Cannot enable SmoothSleep world: " + world.getName());
				SmoothSleep.logWarning("SmoothSleep only works in normal worlds (not nether, end, etc)");
				continue;
			}
			worldData.put(world, new WorldData(plugin, world));
			SmoothSleep.logDebug("Loaded configuration for world:" + world.getName());
		}
		if (globalConf.getBoolean(GlobalConfigKey.ENABLE_STATS)) {
			metrics = new Metrics(plugin);
			String out = "Other";
			String ver = Bukkit.getVersion().toLowerCase();
			if (ver.contains("paper")) {
				out = "Paper";
			} else if (ver.contains("spigot")) {
				out = "Spigot";
			} else if (ver.contains("bukkit")) {
				out = "Bukkit";
			} else if (ver.contains("bungeecord")) {
				out = "BungeeCord";
			} else if (ver.contains("waterfall")) {
				out = "Waterfall";
			}
			final String o = out;
			metrics.addCustomChart(new Metrics.SimplePie("server_software", () -> o));
			SmoothSleep.logDebug("Metrics initialized. Server software: " + o);
		}

		// TODO Sanity check config values

	}

	public void purgeData() {
		worldData.clear();
		metrics = null;
	}

	public ConfigHelper getConfigHelper() { return conf; }

	public GlobalConfig getGlobalConfig() { return globalConf; }

	public WorldData getWorldData(World w) { return worldData.get(w); }

	public WorldConfig getWorldConfig(World w) { return worldData.containsKey(w) ? worldData.get(w).getWorldConfig() : null; }

}
