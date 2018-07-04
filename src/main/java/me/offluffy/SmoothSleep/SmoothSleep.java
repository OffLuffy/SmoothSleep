package me.offluffy.SmoothSleep;

import me.offluffy.SmoothSleep.commands.SmoothSleepReload;
import me.offluffy.SmoothSleep.commands.SmoothSleepToggle;
import me.offluffy.SmoothSleep.lib.MiscUtils;
import me.offluffy.SmoothSleep.lib.ReflectionUtils;
import me.offluffy.SmoothSleep.listeners.PlayerEventsListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("unused")
public class SmoothSleep extends JavaPlugin {
	private final long SLEEP_TICKS_START = 12541L,
			SLEEP_TICKS_END = 23458L,
			SLEEP_TICKS_DURA = SLEEP_TICKS_END - SLEEP_TICKS_START;
	private HashMap<World, MultPair> nightMults = new HashMap<World, MultPair>();
	private List<Player> sleepers = new ArrayList<Player>();
	public boolean enabled = true;
	public void onEnable() {

		Bukkit.getPluginManager().registerEvents(new PlayerEventsListener(this), this);
		getServer().getPluginCommand("smoothsleepreload").setExecutor(new SmoothSleepReload(this));
		getServer().getPluginCommand("smoothsleeptoggle").setExecutor(new SmoothSleepToggle(this));

		saveDefaultConfig();
		reload();

		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				if (!enabled) return;
				if (nightMults.isEmpty()) return;
				for (World w : nightMults.keySet()) {
					String cp = "worlds." + w.getName() + ".";
					if (w.getEnvironment() != Environment.NORMAL) { nightMults.remove(w); continue; }
					if (w.getTime() < SLEEP_TICKS_START || w.getTime() > SLEEP_TICKS_END) { continue; }

					int sc = getSleeperCount(w), wc = getWakerCount(w);

					if (sc == 0) { continue; } // Do nothing if no one is sleeping

					boolean useTitles = getConfig().getBoolean(cp + "use-titles", true);
					int minMult = nightMults.get(w).min, maxMult = nightMults.get(w).max;
					int timescale = Math.round((float) MiscUtils.remapValue(true, 0, sc+wc, minMult, maxMult, sc));
					w.setTime(w.getTime() + timescale - 1);

					String title = "", subtitle = "";
					Sound snd = null;
					List<Player> tempSleepers = new ArrayList<Player>(sleepers);
					if (w.getTime() >= SLEEP_TICKS_END) {
						if (useTitles) {
							title = ChatColor.YELLOW + MiscUtils.ticksToTime(w.getTime());
							subtitle = trans(getConfig().getString(cp + "morning-subtitle", "Rise and shine, {PLAYER}!"));
						}
						String sndName = getConfig().getString(cp + "morning-sound", "ENTITY_PLAYER_LEVELUP");
						if (!sndName.isEmpty()) {
							for (Sound s : Sound.values()) {
								if (s.name().equalsIgnoreCase(sndName)) {
									snd = s;
									break;
								}
							}
						}
						if (getConfig().getBoolean(cp + "clear-weather-when-morning", true)) {
							w.setThundering(false);
							w.setStorm(false);
						}
						sleepers.clear();
					} else if (useTitles) {
						title = ChatColor.AQUA + MiscUtils.ticksToTime(w.getTime());
						subtitle = ChatColor.GREEN + (sc + "/" + (sc+wc) + " Sleepers")
								+ ChatColor.DARK_AQUA + " (" + timescale + "x speed)";
					}

					for (Player p : tempSleepers) {
						if (useTitles) {
							String ps = subtitle.replace("{PLAYER}", p.getName())
									.replace("{WORLD}", w.getName());
							p.sendTitle(title, ps, 0, 20, 20);
						}
						if (snd != null) {
							p.playSound(p.getLocation(), snd, 0.5f, 1.0f);
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
		nightMults.clear();
		for (World w : Bukkit.getWorlds()) {
			String cp = "worlds." + w.getName() + ".";
			if (w.getEnvironment() == Environment.NORMAL) {
				if (getConfig().contains("worlds." + w.getName())) {

					boolean changed = false;

					// Will add config changes here if they'll need to be added from and older config.
					if (!getConfig().contains(cp + "morning-subtitle", true)) {
						getConfig().set(cp + "morning-subtitle", "Rise and shine, {PLAYER}!");
						changed = true;
					}
					if (!getConfig().contains(cp + "morning-sound", true)) {
						getConfig().set(cp + "morning-sound", "ENTITY_PLAYER_LEVELUP");
						changed = true;
					}
					if (!getConfig().contains(cp + "use-titles", true)) {
						getConfig().set(cp + "use-titles", true);
						changed = true;
					}
					if (!getConfig().contains(cp + "min-night-speed-mult", true)) {
						getConfig().set(cp + "min-night-speed-mult", 5);
						changed = true;
					}
					if (!getConfig().contains(cp + "max-night-speed-mult", true)) {
						getConfig().set(cp + "max-night-speed-mult", 20);
						changed = true;
					}
					if (!getConfig().contains(cp + "clear-weather-when-morning", true)) {
						getConfig().set(cp + "clear-weather-when-morning", true);
						changed = true;
					}

					// Some sanity checks to make sure that config values are valid
					if (getConfig().getInt("worlds." + w.getName() + ".min-night-speed-mult", 10) < 1) {
						getConfig().set(cp + "min-night-speed-mult", 1); // Must be >0
						changed = true;
					}
					if (getConfig().getInt(cp + "max-night-speed-mult", 50) < 1) {
						// Perhaps I should check that max > min, but if it's switched, it'll likely just mean
						// that fewer players sleeping will make night pass faster than more players sleeping,
						// which might be something that some strange person would want *shrug*
						getConfig().set(cp + "max-night-speed-mult", 1); // Must be >0
						changed = true;
					}
					String sndName = getConfig().getString(cp + "morning-sound", "ENTITY_PLAYER_LEVELUP");
					if (!sndName.isEmpty()) {
						boolean sndFound = false;
						for (Sound snd : Sound.values()) {
							if (snd.name().equalsIgnoreCase(sndName)) {
								sndFound = true;
								break;
							}
						}
						if (!sndFound) {
							getLogger().warning(cp + "morning-sound: '" + sndName + "' does not appear to be a valid sound name!");
							getLogger().warning("For a list of valid sounds, refer to https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html");
						}
					}


					if (changed) { saveConfig(); }

					int minMult = getConfig().getInt("worlds." + w.getName() + ".min-night-speed-mult", 10);
					int maxMult = getConfig().getInt("worlds." + w.getName() + ".max-night-speed-mult", 50);
					MultPair mult = new MultPair(minMult, maxMult);
					nightMults.put(w, mult);
				}
			}
		}
	}

	private String trans(String s) { return ChatColor.translateAlternateColorCodes('&', s); }

	public boolean worldEnabled(World w) { return nightMults.containsKey(w); }
	public void addSleeper(Player p) { if (!sleepers.contains(p)) sleepers.add(p); }
	public void removeSleeper(Player p) { sleepers.remove(p); }
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

	public class MultPair {
		public int min, max;
		public MultPair(int min, int max) { this.min = min; this.max = max; }
	}
}