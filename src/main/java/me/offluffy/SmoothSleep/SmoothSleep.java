package me.offluffy.SmoothSleep;

import me.offluffy.SmoothSleep.commands.SmoothSleepReload;
import me.offluffy.SmoothSleep.commands.SmoothSleepToggle;
import me.offluffy.SmoothSleep.lib.*;
import me.offluffy.SmoothSleep.lib.ConfigHelper.*;
import me.offluffy.SmoothSleep.listeners.PlayerEventsListener;
import org.apache.commons.lang.text.StrSubstitutor;
import org.bukkit.*;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;

import java.util.*;

import static me.offluffy.SmoothSleep.lib.ConfigHelper.SettingKey.*;
import static org.bukkit.ChatColor.*;

@SuppressWarnings("unused")
public class SmoothSleep extends JavaPlugin {
	private final long SLEEP_TICKS_START = 12541L,
			SLEEP_TICKS_END = 23458L,
			SLEEP_TICKS_DURA = SLEEP_TICKS_END - SLEEP_TICKS_START;
	private final long TICKS_PER_DAY = 1728000,
			TICKS_PER_HOUR = 72000,
			TICKS_PER_MIN = 1200;
	private final int HEAL_TIMER = 0, FOOD_TIMER = 1, PARTICLE_TIMER = 2;
	private Map<Player, Long> sleepers = new HashMap<Player, Long>();
	private Map<Player, List<Long>> timers = new HashMap<Player, List<Long>>();
	public ConfigHelper conf;
	public boolean enabled = true, essEnabled = false;
	public UserHelper userHelper;
	public void onEnable() {

		Bukkit.getPluginManager().registerEvents(new PlayerEventsListener(this), this);
		Plugin ess = getServer().getPluginManager().getPlugin("Essentials");
		if (ess != null && ess.isEnabled()) { essEnabled = true; }
		if (essEnabled) { userHelper = new EssUserHelper(this); } else { userHelper = new StockUserHelper(); }

		getServer().getPluginCommand("smoothsleepreload").setExecutor(new SmoothSleepReload(this));
		getServer().getPluginCommand("smoothsleeptoggle").setExecutor(new SmoothSleepToggle(this));

		conf = new ConfigHelper(this);

		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				if (!enabled) { return; }
				if (conf.worlds.isEmpty()) { return; }
				for (World w : conf.worlds.keySet()) {
					WorldSettings ws = conf.worlds.get(w);

					// Process particle effects

					Particle particle = ws.getParticle(PARTICLE);
					if (particle != null) {
							double radius = ws.getDouble(PARTICLE_RADIUS);
							for (Player p : timers.keySet()) {
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
									getLogger().warning("Failed to produce particle: " + e.getMessage());
									timers.get(p).set(PARTICLE_TIMER, 0L);
								}
							}
					}

					// Now process world

					String cp = "worlds." + w.getName() + ".";
					if (w.getEnvironment() != Environment.NORMAL) { conf.worlds.remove(w); continue; }
					if (w.getTime() < SLEEP_TICKS_START || w.getTime() > SLEEP_TICKS_END) { continue; }

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
						timescale = Math.round((float) MiscUtils.remapValue(true, 0, sc + wc, minMult, maxMult, sc));
						newTime = w.getTime() + timescale - 1;
						if (newTime > SLEEP_TICKS_END) newTime = SLEEP_TICKS_END;
					}
					for (Player p : sleepers.keySet()) {
						sleepers.put(p, sleepers.get(p) + timescale);
					}
					int ticksPerHealth = ws.getInt(HEALTH_TICKS);
					int ticksPerFood = ws.getInt(FOOD_TICKS);
					int healthAmount = ws.getInt(HEALTH_RESTORE);
					int foodAmount = ws.getInt(FOOD_RESTORE);
					for (Player p : timers.keySet()) {
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
					Map<Player, Long> tempSleepers = new HashMap<Player, Long>(sleepers);

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


						for (Player p : sleepers.keySet()) {

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
//								Particle particle = ws.getParticle(PARTICLE);
//								if (particle != null) {
//									double radius = ws.getDouble(PARTICLE_RADIUS);
//									int amount = ws.getInt(PARTICLE_AMOUNT);
//									for (int i = 0; i < amount; i++) {
//										Location l = p.getLocation();
//										l.setX(l.getX() + ((Math.random() * radius) - (radius / 2)));
//										l.setZ(l.getZ() + ((Math.random() * radius) - (radius / 2)));
//										l.setY(l.getY() + (Math.random() * radius));
//										w.spawnParticle(particle, l, 1);
//									}
//								}
							}
						}


						sleepers.clear();
					} else if (useTitles) {
						title = trans(ws.getString(SLEEP_TITLE));
						subtitle = trans(ws.getString(SLEEP_SUBTITLE));
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

	private String trans(String s) { return s == null ? null : translateAlternateColorCodes('&', s); }

	public boolean worldEnabled(World w) { return conf.worlds.containsKey(w); }
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
		for (Player p : sleepers.keySet()) { if (p.getWorld().equals(w)) { s++; } }
		return s;
	}
	public int getWakerCount(World w) {
		if (!worldEnabled(w)) return 0;
		int a = 0;
		for (Player p : w.getPlayers()) {
			if ((!isSleeping(p) && !p.isSleepingIgnored())
					&& (!userHelper.isAfk(p) || !conf.worlds.get(w).getBoolean(IGNORE_AFK))
					&& (!userHelper.isVanished(p) || !conf.worlds.get(w).getBoolean(IGNORE_VANISH)))
						a++;
		}
		return a;
	}

	public class MultPair {
		public int min, max;
		public MultPair(int min, int max) { this.min = min; this.max = max; }
	}
}