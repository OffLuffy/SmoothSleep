package com.luffbox.smoothsleep;

import com.luffbox.smoothsleep.lib.ConfigHelper;
import com.luffbox.smoothsleep.lib.Purgeable;
import com.luffbox.smoothsleep.lib.actionbar.ActionBarHelper;
import com.luffbox.smoothsleep.lib.actionbar.NmsActionBarHelper;
import com.luffbox.smoothsleep.lib.actionbar.PaperActionHelper;
import com.luffbox.smoothsleep.lib.actionbar.SpigotActionBarHelper;
import com.luffbox.smoothsleep.lib.hooks.*;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

public class DataStore implements Purgeable {

	private final SmoothSleep pl;
	private final Map<World, WorldData> worldData = new HashMap<>();
	private final Map<Player, PlayerData> playerData = new HashMap<>();

	private boolean pluginEnabled = true;

	public ConfigHelper config;
	public UserHelper userHelper;
	public ActionBarHelper actionBarHelper;
	public PlaceholderHelper placeholders;
	public double baseTimeSpeed = 1.0;

	public DataStore(SmoothSleep plugin) {
		pl = plugin;
		config = new ConfigHelper(pl);

		Plugin ess = pl.getServer().getPluginManager().getPlugin("Essentials");
		if (ess != null && ess.isEnabled()) { userHelper = new EssUserHelper(pl); }
		else { userHelper = new DefUserHelper(); }

		Plugin papi = pl.getServer().getPluginManager().getPlugin("PlaceholderAPI");
		if (papi != null && papi.isEnabled()) {
			placeholders = new PlaceholderAPIHelper(pl);
		} else { placeholders = new DefPlaceholderHelper(pl); }

		try { // See if the Paper method exists
			Player.class.getMethod("sendActionBar", String.class);
			SmoothSleep.logDebug("Detected Paper, using Player#sendActionBar() to send action bar");
			actionBarHelper = new PaperActionHelper();
		} catch (Exception e1) {
			try { // See if the Spigot method exists
				Player.class.getMethod("spigot");
				SmoothSleep.logDebug("Detected Spigot, using Player#spigot()#sendMessage() to send action bar");
				actionBarHelper = new SpigotActionBarHelper();
			} catch (Exception e2) { // Resort to NMS
				SmoothSleep.logDebug("Not using Paper or Spigot, resorting to NMS method to send action bar");
				actionBarHelper = new NmsActionBarHelper();
			}
		}
		SmoothSleep.logDebug("DataStore initialized");
	}

	public void init() {
		if (config == null) { SmoothSleep.logDebug("DataStore#init() - Config null"); }
		if (config.worlds == null || config.worlds.isEmpty()) { SmoothSleep.logDebug("DataStore#init() - No worlds in config"); }
		config.worlds.forEach((w, ws) -> {
			WorldData wd = new WorldData(pl, w, ws);
			worldData.put(w, wd);
			wd.getPlayers().forEach(plr -> playerData.put(plr, new PlayerData(pl, plr)));
		});
	}

	public void reload() {
		purgeData();
		config.reload();
		init();
	}

	public boolean worldEnabled(World w) { return config.worlds.containsKey(w); }

	public PlayerData addPlayer(Player plr) {
		PlayerData pd;
		if (!playerData.containsKey(plr)) {
			pd = new PlayerData(pl, plr);
			playerData.put(plr, pd);
		} else { pd = playerData.get(plr); }
		return pd;
	}

	public void removePlayer(Player plr) {
		PlayerData pd = playerData.remove(plr);
		if (pd != null) pd.purgeData();
	}

	public boolean isPluginEnabled() { return pluginEnabled; }
	public void setPluginEnabled(boolean enabled) {
		pluginEnabled = enabled;
		pl.getServer().getScheduler().runTaskLater(pl, () -> {
			for (WorldData wd : worldData.values()) {
				for (PlayerData pd : wd.getPlayerData()) {
					pd.clearActionBar();
					pd.clearTitles();
					pd.hideBossBar();
				}
			}
		}, 1L);
	}

	public Map<World, WorldData> getWorldData() { return new HashMap<>(worldData); }
	public WorldData getWorldData(World w) { return w == null ? null : worldData.get(w); }
	public WorldData getWorldData(Player p) { return p == null ? null : worldData.get(p.getWorld()); }
	public PlayerData getPlayerData(Player p) { return p == null ? null : playerData.get(p); }

	@Override
	public void purgeData() {
		if (!worldData.isEmpty()) {
			worldData.values().forEach(WorldData::purgeData);
			worldData.clear();
		}
		if (!playerData.isEmpty()) {
			playerData.values().forEach(PlayerData::purgeData);
			playerData.clear();
		}
	}
}
