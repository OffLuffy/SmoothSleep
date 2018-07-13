package me.offluffy.SmoothSleep;

import me.offluffy.SmoothSleep.commands.SmoothSleepReload;
import me.offluffy.SmoothSleep.commands.SmoothSleepToggle;
import me.offluffy.SmoothSleep.lib.MiscUtils;
import me.offluffy.SmoothSleep.lib.ReflectionUtils;
import me.offluffy.SmoothSleep.listeners.PlayerEventsListener;
import org.apache.commons.lang.text.StrSubstitutor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

import static org.bukkit.ChatColor.*;

@SuppressWarnings("unused")
public class SmoothSleep extends JavaPlugin {
	private final long SLEEP_TICKS_START = 12541L,
			SLEEP_TICKS_END = 23458L,
			SLEEP_TICKS_DURA = SLEEP_TICKS_END - SLEEP_TICKS_START;
	private final long TICKS_PER_DAY = 1728000,
			TICKS_PER_HOUR = 72000,
			TICKS_PER_MIN = 1200;
	private HashMap<World, MultPair> nightMults = new HashMap<World, MultPair>();
	private Map<Player, Long> sleepers = new HashMap<Player, Long>();
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
					long newTime;
					int timescale;
					if (wc == 0 && getConfig().getBoolean(cp + "instant-day-if-all-sleeping", false)) {
						newTime = SLEEP_TICKS_END;
						timescale = (int) SLEEP_TICKS_DURA;
					} else {
						int minMult = nightMults.get(w).min, maxMult = nightMults.get(w).max;
						timescale = Math.round((float) MiscUtils.remapValue(true, 0, sc + wc, minMult, maxMult, sc));
						newTime = w.getTime() + timescale - 1;
						if (newTime > SLEEP_TICKS_END) newTime = SLEEP_TICKS_END;
					}
					for (Player p : sleepers.keySet()) {
						long sleepTime = sleepers.get(p);
						sleepTime += timescale;
						sleepers.put(p, sleepTime);
					}

					w.setTime(newTime);

					String title = "", subtitle = "";
					Sound snd = null;
					Map<Player, Long> tempSleepers = new HashMap<Player, Long>(sleepers);
					if (w.getTime() >= SLEEP_TICKS_END) {
						if (useTitles) {
							title = trans(getConfig().getString(cp + "morning-title", "&e{12H}:{MIN} {MER_UPPER}"));
							subtitle = trans(getConfig().getString(cp + "morning-subtitle", "&aRise and shine, {PLAYER}!"));
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
						title = trans(getConfig().getString(cp + "sleeping-title", "&b{12H}:{MIN} {MER_UPPER}"));
						subtitle = trans(getConfig().getString(cp + "sleeping-subtitle", "&a({SLEEPERS}/{PLAYERS} Sleeping &3({TIMESCALE}x speed)"));
					}

					for (Player p : tempSleepers.keySet()) {
						if (useTitles) {
							long worldTime = w.getTime();
							long timeLived = p.getTicksLived();
							long daysLived = timeLived / TICKS_PER_DAY;
							long hrsLived = (timeLived  % TICKS_PER_DAY) / TICKS_PER_HOUR;
							long minLived = (timeLived % TICKS_PER_DAY % TICKS_PER_HOUR) / TICKS_PER_MIN;
							Map<String, String> values = new HashMap<String, String>();

							values.put("12H",				MiscUtils.ticksTo12Hours(worldTime) + "");
							values.put("24H",				MiscUtils.ticksTo24Hours(worldTime) + "");
							values.put("MIN",				String.format("%02d", MiscUtils.ticksToMinutes(worldTime)));
							values.put("MER_UPPER",			MiscUtils.ticksIsAM(worldTime) ? "AM" : "PM");
							values.put("MER_LOWER",			MiscUtils.ticksIsAM(worldTime) ? "am" : "pm");
							values.put("SLEEPERS",			sc + "");
							values.put("WAKERS",			wc + "");
							values.put("TOTAL",				(sc+wc) + "");
							values.put("TIMESCALE",			timescale + "");
							values.put("USERNAME",			p.getName());
							values.put("DISPLAYNAME",		p.getDisplayName());
							values.put("DISPLAYNAME_STRIP",	stripColor(p.getDisplayName()));
							values.put("HOURS_SLEPT",		(tempSleepers.get(p) / 1000L) + "");
							values.put("LEVEL",				p.getLevel() + "");
							values.put("TIME_LIVED",		"{DAYS_LIVED}d, {REM_HOURS_LIVED}h, {REM_MINS_LIVED}m");
							values.put("DAYS_LIVED",		(timeLived / TICKS_PER_DAY) + "");
							values.put("REM_HOURS_LIVED",	((timeLived  % TICKS_PER_DAY) / TICKS_PER_HOUR) + "");
							values.put("REM_MINS_LIVED",	((timeLived % TICKS_PER_DAY % TICKS_PER_HOUR) / TICKS_PER_MIN) + "");
							values.put("TOTAL_HOURS_LIVED",	(p.getTicksLived() / TICKS_PER_HOUR) + "");
							values.put("TOTAL_MINS_LIVED",	(p.getTicksLived() / TICKS_PER_MIN) + "");
							values.put("WORLD",				w.getName());
							values.put("SERVER_IP",			Bukkit.getIp());
							values.put("SERVER_MOTD",		Bukkit.getMotd());
							values.put("SERVER_NAME",		Bukkit.getServerName());
							values.put("SERVER_MOTD_STRIP",	stripColor(Bukkit.getMotd()));
							values.put("SERVER_NAME_STRIP",	stripColor(Bukkit.getServerName()));
							StrSubstitutor sub = new StrSubstitutor(values, "{", "}");
							String ps = sub.replace(subtitle);
							String pt = sub.replace(title);
							p.sendTitle(pt, ps, 0, 20, 20);
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
					for (Player p : sleepers.keySet()) {
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
					if (!getConfig().contains(cp + "instant-day-if-all-sleeping", true)) {
						getConfig().set(cp + "instant-day-if-all-sleeping", false);
						changed = true;
					}
					if (!getConfig().contains(cp + "morning-title", true)) {
						getConfig().set(cp + "morning-title", "&e{12H}:{MIN} {MER_UPPER}");
						changed = true;
					}

					if (!getConfig().contains(cp + "sleeping-title", true)) {
						getConfig().set(cp + "sleeping-title", "&b{12H}:{MIN} {MER_UPPER}");
						changed = true;
					}

					if (!getConfig().contains(cp + "morning-subtitle", true)) {
						getConfig().set(cp + "morning-subtitle", "&aRise and shine, {USERNAME}!");
						changed = true;
					}

					if (!getConfig().contains(cp + "sleeping-subtitle", true)) {
						getConfig().set(cp + "sleeping-subtitle", "&a({SLEEPERS}/{TOTAL} Sleeping &3({TIMESCALE}x speed)");
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

					String mornSub = getConfig().getString(cp + "morning-subtitle", "&aRise and shine, {USERNAME}!");
					if (mornSub.contains("{PLAYER}")) {
						getLogger().warning(cp + "morning-subtitle: " + mornSub);
						getLogger().warning("The {PLAYER} placeholder is no longer used! I'll replace it with {USERNAME}.");
						getConfig().set(cp + "morning-subtitle", mornSub.replace("{PLAYER}", "{USERNAME}"));
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

	private String trans(String s) { return translateAlternateColorCodes('&', s); }

	public boolean worldEnabled(World w) { return nightMults.containsKey(w); }
	public void addSleeper(Player p, long time) { if (!sleepers.containsKey(p)) sleepers.put(p, 0L); }
	public void removeSleeper(Player p) { sleepers.remove(p); }
	private boolean isSleeping(Player p) { return p != null && sleepers.containsKey(p); }
	public int getSleeperCount(World w) {
		if (!worldEnabled(w)) return 0;
		int s = 0;
		for (Player p : sleepers.keySet()) { if (p.getWorld().equals(w)) { s++; } }
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