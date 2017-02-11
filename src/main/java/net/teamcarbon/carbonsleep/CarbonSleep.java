package net.teamcarbon.carbonsleep;

import net.teamcarbon.carbonsleep.commands.CarbonSleepReload;
import net.teamcarbon.carbonsleep.lib.MiscUtils;
import net.teamcarbon.carbonsleep.lib.ReflectionUtils;
import net.teamcarbon.carbonsleep.listeners.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("unused")
public class CarbonSleep extends JavaPlugin {
	public static boolean titleApi = false;
	private final long SLEEP_TICKS_START = 12541L,
			SLEEP_TICKS_END = 23458L,
			SLEEP_TICKS_DURA = SLEEP_TICKS_END - SLEEP_TICKS_START;
	private HashMap<World, Integer> nightTimes = new HashMap<>();
	private List<Player> sleepers = new ArrayList<>();
	public void onEnable() {
		titleApi = pm().isPluginEnabled("TitleAPI");

		pm().registerEvents(new PlayerEventsListener(this), this);
		getServer().getPluginCommand("carbonsleepreload").setExecutor(new CarbonSleepReload(this));

		saveDefaultConfig();
		reload();

		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			@SuppressWarnings("deprecation")
			public void run() {
				if (nightTimes.isEmpty()) return;
				for (World w : nightTimes.keySet()) {
					String cp = "worlds." + w.getName() + ".";
					if (w.getEnvironment() != Environment.NORMAL) { nightTimes.remove(w); continue; }
					if (w.getTime() < SLEEP_TICKS_START || w.getTime() > SLEEP_TICKS_END) { continue; }

					int sc = getSleeperCount(w), wc = getWakerCount(w);

					if (sc == 0) { continue; }

					// Calculate some math for time and stuff
					double vDuraTicks = 12000; // Ticks night lasts at vanilla speed
					double fDuraTicks = nightTimes.get(w) * 20; // Number of ticks for night to last with 100% speed
					double cDuraTicks = MiscUtils.remapValue(true, 0, (sc+wc), vDuraTicks, fDuraTicks, sc); // Calculated number of ticks for night to last.

					double ticksPerTick = SLEEP_TICKS_DURA / cDuraTicks - 1; // Ticks to pass per tick
					long newTime = w.getTime() + Math.round(ticksPerTick);
					if (newTime > SLEEP_TICKS_END) newTime = SLEEP_TICKS_END;
					w.setTime(newTime);

					boolean useTitles = getConfig().getBoolean(cp + "use-titles", true);

					//FormattedMessage title, subtitle;
					String title = "", subtitle = "";
					List<Player> tempSleepers = new ArrayList<>(sleepers);
					if (newTime >= SLEEP_TICKS_END) {
						if (useTitles) {
							title = ChatColor.YELLOW + MiscUtils.ticksToTime(w.getTime());
							subtitle = trans(getConfig().getString(cp + "morning-subtitle", "Rise and shine, {PLAYER}!"));
						}
						w.setThundering(false);
						w.setStorm(false);
						sleepers.clear();
					} else if (useTitles) {
						title = ChatColor.AQUA + MiscUtils.ticksToTime(w.getTime());
						subtitle = ChatColor.GREEN + (sc + "/" + (sc+wc) + " Sleepers");
					}

					if (useTitles) {
						for (Player p : tempSleepers) {
							String ps = subtitle.replace("{PLAYER}", p.getName());
							if (titleApi) {
								com.connorlinfoot.titleapi.TitleAPI.sendTitle(p, 0, 20, 20, title, ps);
							} else { p.sendTitle(title, ps); }
						}
					}
				}
			}
		}, 0L, 0L);

		// Thanks to IAlIstannen at the Spigot forums for this idea of NMS sleepTick modification
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				if (!sleepers.isEmpty()) {
					for (Player p : sleepers) {
						long wt = p.getWorld().getTime();
						try {
							Object nmsPlr = ReflectionUtils.invokeMethod(p, "getHandle");
							ReflectionUtils.setValue(nmsPlr, false, "sleepTicks", wt > SLEEP_TICKS_END ? 100 : 0);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}, 0L, 30L);
	}
	public void reload() {
		reloadConfig();
		nightTimes.clear();
		for (World w : Bukkit.getWorlds()) {
			String cp = "worlds." + w.getName() + ".";
			if (w.getEnvironment() == Environment.NORMAL) {
				if (getConfig().contains("worlds." + w.getName())) {

					// Will add config changes here if they'll need to be added from and older config.
					if (!getConfig().contains(cp + "morning-subtitle", true)) {
						getConfig().set(cp + "morning-subtitle", "Rise and shine, {PLAYER}!");
						saveConfig();
					}
					if (!getConfig().contains(cp + "use-titles", true)) {
						getConfig().set(cp + "use-titles", true);
						saveConfig();
					}

					// Cache min. durations (just easier to iterate over later I suppose)
					nightTimes.put(w, getConfig().getInt("worlds." + w.getName() + ".min-night-duration-seconds", 10));
				}
			}
		}
	}

	private PluginManager pm() { return Bukkit.getPluginManager(); }
	private String trans(String s) { return ChatColor.translateAlternateColorCodes('&', s); }

	public boolean worldEnabled(World w) { return nightTimes.containsKey(w); }
	public void addSleeper(Player p) { if (!sleepers.contains(p)) sleepers.add(p); }
	public void removeSleeper(Player p) { if (sleepers.contains(p)) sleepers.remove(p); }
	private boolean isSleeping(Player p) { return p != null && sleepers.contains(p); }
	public int getSleeperCount(World w) {
		if (!worldEnabled(w)) return 0;
		int s = 0;
		for (Player p : sleepers) { if (p.getWorld().equals(w)) { s++; } }
		return s;
	}
	public int getWakerCount(World w) {
		if (!worldEnabled(w)) return 0;
		int a = 0;
		for (Player p : w.getPlayers()) { if (!isSleeping(p) && !p.isSleepingIgnored()) a++; }
		return a;
	}
}