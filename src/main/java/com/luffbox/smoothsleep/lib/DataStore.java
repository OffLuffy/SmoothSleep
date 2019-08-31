package com.luffbox.smoothsleep.lib;

import com.luffbox.lib.ConfigHelper;
import com.luffbox.smoothsleep.SmoothSleep;
import com.luffbox.smoothsleep.data.*;
import com.luffbox.smoothsleep.listeners.EntityListeners;
import com.luffbox.smoothsleep.listeners.PlayerListeners;
import com.luffbox.smoothsleep.listeners.TimeListeners;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DataStore {

	private SmoothSleep plugin;
	private LangHelper lang;
	private boolean enabled;
	private static Metrics metrics;
	private ConfigHelper conf;
	private GlobalConfig globalConf;
	private Map<World, WorldData> worldData = new HashMap<>();
	private Map<Player, PlayerData> playerData = new HashMap<>();

	public DataStore(SmoothSleep plugin) {
		this.plugin = plugin;
		loadData();
	}

	public void loadData() {
		conf = new ConfigHelper(plugin);
		globalConf = new GlobalConfig();
		String language = globalConf.getString(GlobalConfigKey.LANGUAGE);
		lang = new LangHelper(plugin, language);
		updateConfigGlobals();

		if (!globalConf.getBoolean(GlobalConfigKey.ENABLED)) { return; } // Stop here if disabled in config

		Set<String> worlds = conf.getConfSection("worlds").getKeys(false);
		for (String w : worlds) {
			World world = plugin.getServer().getWorld(w);
			Map<String, String> valueMap = new HashMap<>();
			valueMap.put("world", w);
			if (world == null) {
				SmoothSleep.logSevere(lang.translate(LangKey.WORLD_CONF_LOAD_FAIL, valueMap));
				continue;
			}
			valueMap.put("world", world.getName());
			if (world.getEnvironment() != World.Environment.NORMAL) {
				SmoothSleep.logSevere(lang.translate(LangKey.INVALID_WORLD_TYPE, valueMap));
				continue;
			}

			WorldData wd = new WorldData(plugin, world);
			updateConfigWorld(wd);
			worldData.put(world, wd);

			SmoothSleep.logDebug("Loaded configuration for world:" + world.getName());

			for (Player p : world.getPlayers()) { addPlayerData(p); }
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

		// Register via LuffPlugin so I can unregister easily later
		plugin.registerEvents(new PlayerListeners());
		plugin.registerEvents(new EntityListeners());
		plugin.registerEvents(new TimeListeners());

		// TODO Initialize tasks if already night and already sleepers

	}

	public void purgeData() {
		plugin.unregisterAllEvents();
		worldData.clear();
		metrics = null;
	}

	public void updateConfigGlobals() {
		// Manual moves
		if (conf.contains("worlds")) {
			SmoothSleep.logDebug("Old world settings moving to '" + WorldConfig.WORLDS_SECT + "'");
			if (!conf.contains(WorldConfig.WORLDS_SECT)) {
				conf.set(WorldConfig.WORLDS_SECT, conf.getConfSection("worlds"));
			} else {
				for (String world : conf.getConfSection("worlds").getKeys(false)) {
					if (!conf.contains(WorldConfig.WORLDS_SECT + "." + world)) {
						conf.set(WorldConfig.WORLDS_SECT + "." + world, conf.getConfSection("worlds."+ world));
					}
				}
			}
			conf.set("worlds", null);
		}
		// Remove unused keys/sections, move any keys that were renamed
		for (Map.Entry<String, GlobalConfigKey> moveEntry : Constants.movedGlobalSettings.entrySet()) {
			if (conf.contains(moveEntry.getKey())) {
				if (moveEntry.getValue() == null) { // Delete keys not being used anymore
					SmoothSleep.logDebug("Removing unused config key: " + moveEntry.getKey());
					conf.set(moveEntry.getKey(), null);
					continue;
				}
				SmoothSleep.logDebug("Config key '" + moveEntry.getKey() + "' has moved to '" + moveEntry.getValue().key + "'");
				if (!globalConf.contains(moveEntry.getValue())) { // Move renamed keys if it doesn't exist
					Object val = conf.getConfig().get(moveEntry.getKey()); // Cache current value
					globalConf.set(moveEntry.getValue(), val); // Set current value to new path
				}
				conf.set(moveEntry.getKey(), null); // Remove old path
			}
		}
		// Add any non-existing new keys
		for (GlobalConfigKey gck : GlobalConfigKey.values()) { // Add new keys
			if (!conf.getConfig().contains(gck.key)) {
				SmoothSleep.logDebug("New key added to config: " + gck.key);
				globalConf.setToDefault(gck);
			}
		}
	}

	public void updateConfigWorld(WorldData worldData) {
		World world = worldData.getWorld();
		WorldConfig wconf = worldData.getWorldConfig();
		// Remove unused keys/sections, move any keys that were renamed
		for (Map.Entry<String, WorldConfigKey> moveEntry : Constants.movedWorldSettings.entrySet()) {
			String fromPath = wconf.path() + "." + moveEntry.getKey();
			String toPath = wconf.path(moveEntry.getValue());
			if (conf.contains(fromPath)) {
				if (moveEntry.getValue() == null) {
					SmoothSleep.logDebug("Removing unused config key: " + fromPath);
					conf.set(fromPath, null);
					continue;
				}
				SmoothSleep.logDebug("Config key '" + fromPath + "' has moved to '" + toPath + "'");
				if (!conf.contains(toPath)) {
					Object val = conf.getConfig().get(fromPath);
					wconf.set(moveEntry.getValue(), val);
				}
				conf.set(fromPath, null);
			}
		}
		// Add any non-existing new keys
		for (WorldConfigKey wck : WorldConfigKey.values()) { // Add new keys
			if (!conf.getConfig().contains(wck.key)) {
				SmoothSleep.logDebug("New key added to config: " + wck.key);
				wconf.setToDefault(wck);
			}
		}
	}

	public ConfigHelper getConfigHelper() { return conf; }

	public GlobalConfig getGlobalConfig() { return globalConf; }

	public WorldData getWorldData(World world) { return worldData.get(world); }

	public WorldConfig getWorldConfig(World world) { return worldData.containsKey(world) ? worldData.get(world).getWorldConfig() : null; }

	public boolean isWorldEnabled(World world) { return worldData.containsKey(world); }

	public void addPlayerData(Player player) { playerData.put(player, new PlayerData(player)); }

	public void removePlayerData(Player player) { if (player != null) { playerData.remove(player); } }

	public PlayerData getPlayerData(Player player) { return playerData.get(player); }

}
