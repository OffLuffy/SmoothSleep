package me.offluffy.SmoothSleep;

import me.offluffy.SmoothSleep.commands.*;
import me.offluffy.SmoothSleep.lib.*;
import me.offluffy.SmoothSleep.lib.ConfigHelper.*;
import me.offluffy.SmoothSleep.listeners.PlayerEventsListener;
import org.apache.commons.lang.text.StrSubstitutor;
import org.bstats.bukkit.Metrics;
import org.bukkit.*;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;

import java.util.*;
import java.util.Map.Entry;

import static me.offluffy.SmoothSleep.lib.ConfigHelper.WorldSettingKey.*;
import static org.bukkit.ChatColor.*;

@SuppressWarnings("unused")
public class SmoothSleep extends LoggablePlugin {
	private final String PERM_IGNORE = "smoothsleep.ignore";
	private final long SLEEP_TICKS_START = 12541L,
			SLEEP_TICKS_END = 23460L,
			SLEEP_TICKS_DURA = SLEEP_TICKS_END - SLEEP_TICKS_START + 2;
	private final long TICKS_PER_DAY = 1728000,
			TICKS_PER_HOUR = 72000,
			TICKS_PER_MIN = 1200;
	private final int HEAL_TIMER = 0, FOOD_TIMER = 1, PARTICLE_TIMER = 2;
	private Map<Player, Long> sleepers = new HashMap<>();
	private Map<Player, List<Long>> timers = new HashMap<>();
	public ConfigHelper conf;
	public boolean enabled = true, essEnabled = false;
	public UserHelper userHelper;

	public static Metrics metrics;

	public void onEnable() {

		Bukkit.getPluginManager().registerEvents(new PlayerEventsListener(this), this);
		Plugin ess = getServer().getPluginManager().getPlugin("Essentials");
		if (ess != null && ess.isEnabled()) { essEnabled = true; }
		if (essEnabled) { userHelper = new EssUserHelper(this); } else { userHelper = new StockUserHelper(); }

		getServer().getPluginCommand("smoothsleepreload").setExecutor(new Reload(this));
		getServer().getPluginCommand("smoothsleeptoggle").setExecutor(new ToggleEnabled(this));
		getServer().getPluginCommand("smoothsleepmetrics").setExecutor(new ToggleMetrics(this));
		getServer().getPluginCommand("smoothsleepaddworld").setExecutor(new AddWorld(this));
		getServer().getPluginCommand("smoothsleepconfigureworld").setExecutor(new ConfigureWorld(this));

		conf = new ConfigHelper(this);

		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {

			if (!enabled) { return; }

			if (conf.worlds.isEmpty()) { return; }
			for (Entry<World, WorldSettings> pair : conf.worlds.entrySet()) {
				World w = pair.getKey();
				WorldSettings ws = pair.getValue();// Process particle effects

				if (!timers.isEmpty()) {
					Particle particle = ws.getParticle(PARTICLE);
					if (particle != null) {
						double radius = ws.getDouble(PARTICLE_RADIUS);
						for (Entry<Player, List<Long>> timingsPair : timers.entrySet()) {
							Player p = timingsPair.getKey();
							try {
								if (timers.get(p).get(PARTICLE_TIMER) > 0) {
									Location l = p.getLocation();
									l.setX(l.getX() + ((Math.random() * radius * 2) - radius));
									l.setZ(l.getZ() + ((Math.random() * radius * 2) - radius));
									l.setY(l.getY() + (Math.random() * radius));
									w.spawnParticle(particle, l, 1);
									timers.get(p).set(PARTICLE_TIMER, timers.get(p).get(PARTICLE_TIMER) - 1);
								}
							} catch (Exception e) {
								logWarning("Failed to produce particle: " + e.getMessage());
								timers.get(p).set(PARTICLE_TIMER, 0L);
							}
						}
					}
				}

				// Now process world

				String cp = "worlds." + w.getName() + ".";
				if (w.getEnvironment() != Environment.NORMAL) { conf.worlds.remove(w); continue; }
				if (!isNight(w)) { continue; }

				int sc = getSleeperCount(w), wc = getWakerCount(w);

				if (sc == 0) { continue; } // Do nothing if no one is sleeping
				boolean useTitles = conf.getBoolean(w, "use-titles");
				long newTime;
				int timescale;
				if (wc == 0 && ws.getBoolean(INSTANT_DAY)) {
					newTime = SLEEP_TICKS_END;
					timescale = (int) SLEEP_TICKS_DURA;
				} else {
					int minMult = ws.getInt(MIN_NIGHT_MULT), maxMult = ws.getInt(MAX_NIGHT_MULT);
					double curve = ws.getDouble(SPEED_CURVE);
					double speedMult = calcSpeed(curve, ((double)sc / (double)(sc + wc)));
					timescale = Math.round((float) MiscUtils.remapValue(true, 0, 1, minMult, maxMult, speedMult));
					newTime = w.getTime() + timescale - 1;
					if (newTime > SLEEP_TICKS_END) newTime = SLEEP_TICKS_END;
				}
				for (Entry<Player, Long> sleepersPair : sleepers.entrySet()) {
					sleepers.put(sleepersPair.getKey(), sleepersPair.getValue() + timescale);
				}
				int ticksPerHealth = ws.getInt(HEALTH_TICKS);
				int ticksPerFood = ws.getInt(FOOD_TICKS);
				int healthAmount = ws.getInt(HEALTH_RESTORE);
				int foodAmount = ws.getInt(FOOD_RESTORE);
				for (Entry<Player, List<Long>> timingsPair : timers.entrySet()) {
					Player p = timingsPair.getKey();
					long healthTimer = timers.get(p).get(HEAL_TIMER);
					long foodTimer = timers.get(p).get(FOOD_TIMER);
					if (healthTimer + timescale > ticksPerHealth) {
						int mult = (int) (healthTimer + timescale) / ticksPerHealth;
						if (healthAmount != 0) {
							p.setHealth(MiscUtils.clamp(p.getHealth() + healthAmount * mult, 0.0, 20.0));
						}
						timers.get(p).set(HEAL_TIMER, (healthTimer + timescale) % ticksPerHealth);
					} else { timers.get(p).set(HEAL_TIMER, (healthTimer + timescale)); }
					if (foodTimer + timescale > ticksPerFood) {
						int mult = (int) (foodTimer + timescale) / ticksPerFood;
						if (foodAmount != 0) {
							p.setFoodLevel(MiscUtils.clamp(p.getFoodLevel() + foodAmount * mult, 0, 20));
						}
						timers.get(p).set(FOOD_TIMER, (foodTimer + timescale) % ticksPerFood);
					} else { timers.get(p).set(FOOD_TIMER, (foodTimer + timescale)); }
				}

				w.setTime(newTime);

				String title = "", subtitle = "";
				Sound snd = null;
				Map<Player, Long> tempSleepers = new HashMap<>(sleepers);

				if (w.getTime() >= SLEEP_TICKS_END) {
					if (useTitles) {
						title = trans(ws.getString(MORNING_TITLE));
						subtitle = trans(ws.getString(MORNING_SUBTITLE));
					}
					String sndName = ws.getString(MORNING_SOUND);
					if (!sndName.isEmpty()) {
						for (Sound s : Sound.values()) {
							if (s.name().equalsIgnoreCase(sndName)) {
								snd = s;
								break;
							}
						}
					}
					if (ws.getBoolean(CLEAR_WEATHER)) {
						w.setThunderDuration(0);
						w.setWeatherDuration(0);
						w.setThundering(false);
						w.setStorm(false);
					}


					for (Entry<Player, Long> sleepersPair : sleepers.entrySet()) {
						Player p = sleepersPair.getKey();
						for (PotionEffect pe : p.getActivePotionEffects()) {
							// Heal the player of negative status effects
							if (ConfigHelper.negativeEffects.contains(pe.getType()) && ws.getBoolean(HEAL_NEG_STATUS)) {
								p.removePotionEffect(pe.getType());
							}
							// Heal the player of positive status effects
							if (ws.getBoolean(HEAL_POS_STATUS) && ConfigHelper.positiveEffects.contains(pe.getType())) {
								p.removePotionEffect(pe.getType());
							}
						}

						// Spawn particles
						if (ws.getInt(PARTICLE_AMOUNT) > 0) {
							timers.get(p).set(PARTICLE_TIMER, (long) ws.getInt(PARTICLE_AMOUNT));
						}
					}

					sleepers.clear();
				} else if (useTitles) {
					title = trans(ws.getString(SLEEP_TITLE));
					subtitle = trans(ws.getString(SLEEP_SUBTITLE));
				}

				for (Entry<Player, Long> tempSleepersPair : tempSleepers.entrySet()) {
					Player p = tempSleepersPair.getKey();
					if (useTitles) {
						long worldTime = w.getTime();
						long timeLived = p.getTicksLived();
						long daysLived = timeLived / TICKS_PER_DAY;
						long hrsLived = (timeLived  % TICKS_PER_DAY) / TICKS_PER_HOUR;
						long minLived = (timeLived % TICKS_PER_DAY % TICKS_PER_HOUR) / TICKS_PER_MIN;
						Map<String, String> values = new HashMap<>();

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
						values.put("HOURS_SLEPT",		(tempSleepersPair.getValue() / 1000L) + "");
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
						values.put("NICKNAME",			userHelper.getNickname(p));
						values.put("NICKNAME_STRIP",	stripColor(userHelper.getNickname(p)));
						StrSubstitutor sub = new StrSubstitutor(values, "{", "}");
						String ps = sub.replace(subtitle);
						String pt = sub.replace(title);
						p.sendTitle(pt, ps, 0, ws.getInt(TITLE_STAY), ws.getInt(TITLE_FADE));
					}
					if (snd != null) {
						p.playSound(p.getLocation(), snd, 0.5f, 1.0f);
					}
				}
			}
		}, 0L, 0L);

		// Thanks to IAlIstannen @ Spigot for this idea of NMS sleepTick modification
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
			List<Player> remove = new ArrayList<>();
			if (!sleepers.isEmpty()) {
				for (Entry<Player, Long> sleepersPair : sleepers.entrySet()) {
					Player p = sleepersPair.getKey();
					if (!isNight(p.getWorld())) {
						remove.add(p);
						continue;
					}
					long wt = p.getWorld().getTime();
					try {
						Object nmsPlr = ReflectionUtils.invokeMethod(p, "getHandle");
						ReflectionUtils.setValue(nmsPlr, false, "sleepTicks", wt > SLEEP_TICKS_END ? 90 : 0);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				for (Player p : remove) { sleepers.remove(p); }
			}
		}, 0L, 30L);

		resourceId = "32043";
		checkUpdate();
	}

	private String trans(String s) { return s == null ? null : translateAlternateColorCodes('&', s); }

	public boolean worldEnabled(World w) { return conf.worlds.containsKey(w); }
	public boolean isNight(World w) {
		return w.getTime() >= SLEEP_TICKS_START && w.getTime() <= SLEEP_TICKS_END;
	}
	public void addSleeper(Player p, long time) {
		if (!sleepers.containsKey(p)) sleepers.put(p, 0L);
		if (!timers.containsKey(p)) timers.put(p, Arrays.asList(0L, 0L, 0L));
	}
	public void removeSleeper(Player p) {
		sleepers.remove(p);
		if (!timers.containsKey(p)) timers.put(p, Arrays.asList(0L, 0L, timers.get(p).get(PARTICLE_TIMER)));
	}
	private boolean isSleeping(Player p) { return p != null && sleepers.containsKey(p); }
	public int getSleeperCount(World w) {
		if (!worldEnabled(w)) return 0;
		int s = 0;
		for (Entry<Player, Long> sleepersPair : sleepers.entrySet()) { if (sleepersPair.getKey().getWorld().equals(w)) { s++; } }
		//for (Player p : sleepers.keySet()) { if (p.getWorld().equals(w)) { s++; } }
		return s;
	}
	public int getWakerCount(World w) {
		if (!worldEnabled(w)) return 0;
		int a = 0;
		for (Player p : w.getPlayers()) {
			if ((!isSleeping(p) && !p.isSleepingIgnored() && !p.hasPermission(PERM_IGNORE))
					&& (!userHelper.isAfk(p) || !conf.worlds.get(w).getBoolean(IGNORE_AFK))
					&& (!userHelper.isVanished(p) || !conf.worlds.get(w).getBoolean(IGNORE_VANISH)))
						a++;
		}
		return a;
	}

	/**
	 * Calculates the night speed multiplier based on the curve amount and percent (0-1) of players sleeping.
	 * @param curve A value between 0-1 (not inclusive) that determines the curve. Higher values will cause the returned
	 *              value to increase more rapidy with lower percents and slower with higher percents, lower values will
	 *              cause the returned value to increase more slowly with lower percents and rise rapidy with higher percents.
	 *              A value of 0.5 will be a linear function where the percent is returned. Clamped 0-1
	 * @param percent The percent of sleeping players. 0 will always return the minimum night speed, and 1 will always
	 *                return the max night speed. Clamped 0-1
	 * @return Returns a value between 0-1 indicating how fast night should proceed.
	 */
	// Thanks to math wizard theminerdude AKA Drathares @ NarniaMC for helping to discover and simplify this equation
	public double calcSpeed(double curve, double percent) {
		curve = curve > 1 ? 1 : curve < 0 ? 0 : curve;			// Clamp curve to 0-1.
		percent = percent > 1 ? 1 : percent < 0 ? 0 : percent;	// Clamp percent to 0-1;
		if (near(curve, 1)) return near(percent, 0) ? 0f : 1f;	// Filter out values too close to 1 -- any sleepers = max speed
		if (near(curve, 0)) return near(percent, 1) ? 1f : 0f;	// Filter out values too close to 0 -- all sleepers = max speed
		return (curve * percent) / (2 * curve * percent - curve - percent + 1);
	}

	private boolean near(double a, double b) { return Math.abs(a - b) < 0.0001f; }

	public class MultPair {
		public int min, max;
		public MultPair(int min, int max) { this.min = min; this.max = max; }
	}
}