package com.luffbox.smoothsleep.lib;
import com.luffbox.smoothsleep.SmoothSleep;
import com.luffbox.smoothsleep.lib.particle.ParticlePatternType;
import org.bstats.bukkit.Metrics;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.bukkit.potion.PotionEffectType.*;
import static com.luffbox.smoothsleep.lib.ConfigHelper.WorldSettingKey.*;
import static org.bukkit.Particle.*;

/**
 * This class mimics FileConfiguration, but all get methods will automatically
 * fetch the default config value instead of having to specify the default value
 * every time I try to fetch a setting. This isn't meant to entirely replace
 * the built in config object as it mostly only includes what I need.
 */
@SuppressWarnings("Duplicates")
public class ConfigHelper {

	private SmoothSleep ss;
	private ConfigHelper conf;
	public Map<World, WorldSettings> worlds = new HashMap<>();

	public static Set<PotionEffectType> negativeEffects = Stream.of(BLINDNESS, CONFUSION, HARM, HUNGER, LEVITATION,
			POISON, SLOW, SLOW_DIGGING, UNLUCK, WEAKNESS, WITHER, UNLUCK).collect(Collectors.toSet());
	public static Set<PotionEffectType> positiveEffects = Stream.of(ABSORPTION, DAMAGE_RESISTANCE, FAST_DIGGING,
			FIRE_RESISTANCE, GLOWING, HEAL, HEALTH_BOOST, INCREASE_DAMAGE, INVISIBILITY, JUMP, NIGHT_VISION,
			REGENERATION, SATURATION, SPEED, WATER_BREATHING, LUCK).collect(Collectors.toSet());
	public static Set<Particle> requiresData = Stream.of(REDSTONE, BLOCK_CRACK, BLOCK_DUST, FALLING_DUST, ITEM_CRACK,
			SPELL_MOB, SPELL_MOB_AMBIENT).collect(Collectors.toSet());

	// If a config option within the world settings has been moved, adding it here should
	// copy the value from the old key into it's new position and remove the old key.
	// If it's moved more than once, include both, earlier first.
	// LinkedHashMap in order to iterate over map in order
	private final Map<String, WorldSettingKey> worldMoved = new LinkedHashMap<String, WorldSettingKey>() {
		{
			//  FROM									TO
			put("action-bar",							ACTIONBAR_TITLE);
			put("use-action-bar",						ACTIONBAR_ENABLED);
			put("use-titles",							TITLES_ENABLED);
			put("sleeping-title",						SLEEP_TITLE);
			put("sleeping-subtitle",					SLEEP_SUBTITLE);
			put("morning-title",						MORNING_TITLE);
			put("morning-subtitle",						MORNING_SUBTITLE);
			put("title-stay-ticks",						TITLE_STAY);
			put("title-fade-ticks",						TITLE_FADE);
			put("replenish-settings.ticks-per-food",	FEED_TICKS);
			put("replenish-settings.ticks-per-health",	HEAL_TICKS);
			put("replenish-settings.food-amount",		FEED_AMOUNT);
			put("replenish-settings.health-amount",		HEAL_AMOUNT);
		}
	};

	// A list of keys supporting placeholder text
	private final Set<WorldSettingKey> placeholderKeys = new HashSet<WorldSettingKey>() {
		{
			add(SLEEP_TITLE);
			add(SLEEP_SUBTITLE);
			add(MORNING_TITLE);
			add(MORNING_SUBTITLE);
			add(ACTIONBAR_TITLE);
			add(BOSSBAR_TITLE);
		}
	};

	public ConfigHelper(SmoothSleep ss) {
		this.ss = ss;
		this.conf = this;
		ss.saveDefaultConfig();
		reload();
	}

	public enum GlobalSettingKey {
		ENABLE_STATS("enable-stats", boolean.class),
		UPDATE_NOTIFY("update-notify-login", boolean.class),
		LOG_DEBUG("logging-settings.log-debug", boolean.class),
		LOG_INFO("logging-settings.log-info", boolean.class),
		LOG_WARNING("logging-settings.log-warning", boolean.class),
		;

		public final String key;
		public final Class type;
		GlobalSettingKey(String key, Class type) { this.key = key; this.type = type; }

		public static List<GlobalSettingKey> valuesByType(Class type) {
			List<GlobalSettingKey> keys = new ArrayList<>();
			for (GlobalSettingKey key : values()) { if (key.type == type) { keys.add(key); } }
			return keys;
		}

		@Override
		public String toString() { return key; }
	}

	/**
	 * This enum represents every world option available and allows me to more easily update
	 * old configs with new world settings even if the server is using worlds other than 'world'.
	 * This only affects per-world settings, but currently I only have per-world settings.<br />
	 * The {@link #key} is the setting path within a world's configuration section.
	 * The {@link #type} variable makes it so that I can check if a key will return the proper
	 * type before attempting to read from the config. It also allows me to iterate over the
	 * enum values in order to automatically update old configs.
	 */
	public enum WorldSettingKey {
		MIN_NIGHT_MULT("min-night-speed-mult", double.class),
		MAX_NIGHT_MULT("max-night-speed-mult", double.class),
		SPEED_CURVE("night-speed-curve", double.class),
		MORNING_SOUND("morning-sound", String.class),
		CLEAR_WEATHER("clear-weather-when-morning", boolean.class),
//		ADVANCE_WEATHER("advance-weather-time", boolean.class),
		INSTANT_DAY("instant-day-if-all-sleeping", boolean.class),
		IGNORE_AFK("essentials-settings.ignore-afk", boolean.class),
		IGNORE_VANISH("essentials-settings.ignore-vanish", boolean.class),

		HEAL_AMOUNT("replenish-settings.heal-amount", int.class),
		HEAL_TICKS("replenish-settings.ticks-per-heal", int.class),
		HEAL_AWAKE("replenish-settings.heal-awake", boolean.class),
		FEED_AMOUNT("replenish-settings.feed-amount", int.class),
		FEED_TICKS("replenish-settings.ticks-per-feed", int.class),
		FEED_AWAKE("replenish-settings.feed-awake", boolean.class),
		HEAL_NEG_STATUS("replenish-settings.heal-negative-statuses", boolean.class),
		HEAL_POS_STATUS("replenish-settings.heal-positive-statuses", boolean.class),
		HOURS_NEG_STATUS("replenish-settings.hours-to-heal-negative", int.class),
		HOURS_POS_STATUS("replenish-settings.hours-to-heal-positive", int.class),

		PARTICLE_ENABLED("morning-particle-options.enabled", boolean.class),
		PARTICLE_TYPE("morning-particle-options.particle", String.class),
		PARTICLE_AMOUNT("morning-particle-options.amount", int.class),
		PARTICLE_RADIUS("morning-particle-options.radius", double.class),
		PARTICLE_DELAY("morning-particle-options.delay-ticks", int.class),
		PARTICLE_PATTERN("morning-particle-options.pattern", String.class),

		TITLES_ENABLED("titles.enabled", boolean.class),
		SLEEP_TITLE("titles.sleep-title", String.class),
		SLEEP_SUBTITLE("titles.sleep-subtitle", String.class),
		MORNING_TITLE("titles.morning-title", String.class),
		MORNING_SUBTITLE("titles.morning-subtitle", String.class),
		TITLE_STAY("titles.stay-ticks", int.class),
		TITLE_FADE("titles.fade-ticks", int.class),

		ACTIONBAR_ENABLED("action-bar.enabled", boolean.class),
		ACTIONBAR_WAKERS("action-bar.show-if-awake", boolean.class),
//		ACTIONBAR_DAYSTORM("action-bar.day-storm-visible", boolean.class),
		ACTIONBAR_TITLE("action-bar.title", String.class),

		BOSSBAR_ENABLED("boss-bar.enabled", boolean.class),
		BOSSBAR_WAKERS("boss-bar.show-if-awake", boolean.class),
//		BOSSBAR_DAYSTORM("boss-bar.day-storm-visible", boolean.class),
//		BOSSBAR_TRACK_TYPE("boss-bar.track", String.class),
		BOSSBAR_COLOR("boss-bar.color", String.class),
		BOSSBAR_TITLE("boss-bar.title", String.class),
		;

		public final String key;
		public final Class type;
		WorldSettingKey(String key, Class type) { this.key = key; this.type = type; }

		public static List<WorldSettingKey> valuesByType(Class type) {
			List<WorldSettingKey> keys = new ArrayList<>();
			for (WorldSettingKey key : values()) { if (key.type == type) { keys.add(key); } }
			return keys;
		}

		@Override
		public String toString() { return key; }
	}

	/**
	 * Wrapper for per-world settings. Each getter will return
	 * the default value if no value is present.
	 * If the type of the WorldSettingKey does not match the value
	 * requested: 0, false, or an empty string will be returned.
	 */
	public class WorldSettings {
		private World w;

		public WorldSettings(World w) { this.w = w; }

		public int getInt(WorldSettingKey setting) { return setting.type == int.class ? conf.getInt(w, setting.key) : 0; }
		public double getDouble(WorldSettingKey setting) { return setting.type == double.class ? conf.getDouble(w, setting.key) : 0.0; }
		public boolean getBoolean(WorldSettingKey setting) { return setting.type == boolean.class && conf.getBoolean(w, setting); }
		public String getString(WorldSettingKey setting) { return setting.type == String.class ? conf.getString(w, setting) : ""; }
		public Sound getSound(WorldSettingKey setting) { return setting.type == String.class ? conf.getSound(w, setting) : null; }
		public Particle getParticle(WorldSettingKey setting) { return setting.type == String.class ? conf.getParticle(w, setting) : null; }
		public BarColor getBarColor(WorldSettingKey setting) { return setting.type == String.class ? conf.getBarColor(w, setting) : null; }
//		public BarTrackType getTrackType(WorldSettingKey setting) { return setting.type == String.class ? conf.getTrackType(w, setting) : null; }
		public ParticlePatternType getPatternType(WorldSettingKey setting) { return setting.type == String.class ? conf.getPatternType(w, setting) : null; }

		public boolean contains(WorldSettingKey setting) { return conf.contains(w, setting); }

		public void set(WorldSettingKey setting, Object value) { conf.set(w, setting.key, value); }
	}

	public int getInt(String path) { return ss.getConfig().getInt(path, ss.getConfig().getDefaults().getInt(path)); }
	public int getInt(GlobalSettingKey key) { return getInt(key.key); }
	public int getInt(World w, String path) { return ss.getConfig().getInt(path(w) + "." + path, getDefaultInt(path)); }
	public int getInt(World w, WorldSettingKey key) { return getInt(w, key.key); }
	public int getDefaultInt(String path) { return ss.getConfig().getDefaults().getInt("worlds.world." + path); }
	public int getDefaultInt(GlobalSettingKey key) { return getDefaultInt(key.key); }
	public int getDefaultInt(WorldSettingKey key) { return getDefaultInt(key.key); }

	public boolean getBoolean(String path) { return ss.getConfig().getBoolean(path, ss.getConfig().getDefaults().getBoolean(path)); }
	public boolean getBoolean(GlobalSettingKey key) { return getBoolean(key.key); }
	public boolean getBoolean(World w, String path) { return ss.getConfig().getBoolean(path(w) + "." + path, getDefaultBoolean(path)); }
	public boolean getBoolean(World w, WorldSettingKey key) { return getBoolean(w, key.key); }
	public boolean getDefaultBoolean(String path) { return ss.getConfig().getDefaults().getBoolean("worlds.world." + path); }
	public boolean getDefaultBoolean(GlobalSettingKey key) { return getDefaultBoolean(key.key); }
	public boolean getDefaultBoolean(WorldSettingKey key) { return getDefaultBoolean(key.key); }

	public String getString(String path) { return ss.getConfig().getString(path, ss.getConfig().getDefaults().getString(path)); }
	public String getString(GlobalSettingKey key) { return getString(key.key); }
	public String getString(World w, String path) { return ss.getConfig().getString(path(w) + "." + path, getDefaultString(path)); }
	public String getString(World w, WorldSettingKey key) { return getString(w, key.key); }
	public String getDefaultString(String path) { return ss.getConfig().getDefaults().getString("worlds.world." + path); }
	public String getDefaultString(GlobalSettingKey key) { return getDefaultString(key.key); }
	public String getDefaultString(WorldSettingKey key) { return getDefaultString(key.key); }

	public double getDouble(String path) { return ss.getConfig().getDouble(path, ss.getConfig().getDefaults().getDouble(path)); }
	public double getDouble(GlobalSettingKey key) { return getDouble(key.key); }
	public double getDouble(World w, String path) { return ss.getConfig().getDouble(path(w) + "." + path, getDefaultDouble(path)); }
	public double getDouble(World w, WorldSettingKey key) { return getDouble(w, key.key); }
	public double getDefaultDouble(String path) { return ss.getConfig().getDefaults().getDouble("worlds.world." + path); }
	public double getDefaultDouble(GlobalSettingKey key) { return getDefaultDouble(key.key); }
	public double getDefaultDouble(WorldSettingKey key) { return getDefaultDouble(key.key); }

	public Sound getSound(String path) {
		String p = getString(path);
		for (Sound sound : Sound.values()) { if (sound.name().equalsIgnoreCase(p)) return sound; }
		return null;
	}
	public Sound getSound(World w, String path) { return getSound(path(w) + "." + path); }
	public Sound getSound(World w, WorldSettingKey key) { return getSound(w, key.key); }

	public Particle getParticle(String path) {
		String p = getString(path);
		for (Particle particle : Particle.values()) { if (particle.name().equalsIgnoreCase(p)) return particle; }
		return null;
	}
	public Particle getParticle(World w, String path) { return getParticle(path(w) + "." + path); }
	public Particle getParticle(World w, WorldSettingKey key) { return getParticle(w, key.key); }

	public BarColor getBarColor(String path) {
		String p = getString(path);
		for (BarColor color : BarColor.values()) { if (color.name().equalsIgnoreCase(p)) return color; }
		return null;
	}
	public BarColor getBarColor(World w, String path) { return getBarColor(path(w) + "." + path); }
	public BarColor getBarColor(World w, WorldSettingKey key) { return getBarColor(w, key.key); }

//	public BarTrackType getTrackType(String path) {
//		String p = getString(path);
//		for (BarTrackType type : BarTrackType.values()) { if (type.name().equalsIgnoreCase(p)) return type; }
//		return null;
//	}
//	public BarTrackType getTrackType(World w, String path) { return getTrackType(path(w) + "." + path); }
//	public BarTrackType getTrackType(World w, WorldSettingKey key) { return getTrackType(w, key.key); }

	public ParticlePatternType getPatternType(String path) {
		String p = getString(path);
		for (ParticlePatternType type : ParticlePatternType.values()) { if (type.name().equalsIgnoreCase(p)) return type; }
		return null;
	}
	public ParticlePatternType getPatternType(World w, String path) { return getPatternType(path(w) + "." + path); }
	public ParticlePatternType getPatternType(World w, WorldSettingKey key) { return getPatternType(w, key.key); }

	public ConfigurationSection getDefaultConfigurationSection(String path) {
		if (ss.getConfig().getDefaults().contains(path))
			return ss.getConfig().getDefaults().getConfigurationSection(path);
		return new MemoryConfiguration();
	}
	public ConfigurationSection getConfigurationSection(String path) {
		if (ss.getConfig().contains(path))
			return ss.getConfig().getConfigurationSection(path);
		else return getDefaultConfigurationSection(path);
	}

	public boolean isSection(String path) { return ss.getConfig().isConfigurationSection(path); }
	public boolean isSection(GlobalSettingKey key) { return isSection(key.key); }
	public boolean isSection(World w, String path) { return isSection(path(w) + "." + path); }
	public boolean isSection(World w, WorldSettingKey key) { return isSection(w, key.key); }

	public boolean contains(String path) { return ss.getConfig().contains(path, true); }
	public boolean contains(GlobalSettingKey key) { return contains(key.key); }
	public boolean contains(World w, String path) { return contains(path(w) + "." + path); }
	public boolean contains(World w, WorldSettingKey key) { return contains(w, key.key); }

	public void set(String path, Object value) { ss.getConfig().set(path, value); }
	public void set(GlobalSettingKey key, Object value) { set(key.key, value); }
	public void set(World w, String path, Object value) { set(path(w) + "." + path, value); }
	public void set(World w, WorldSettingKey key, Object value) { set(w, key.key, value); }

	public void save() { ss.saveConfig(); }

	public void reload() {
		worlds.clear();

		ss.saveDefaultConfig();
		ss.reloadConfig();

		if (getBoolean(GlobalSettingKey.ENABLE_STATS)) { SmoothSleep.metrics = new Metrics(ss); } else { SmoothSleep.metrics = null; }

		boolean changed = false;

		for (GlobalSettingKey key : GlobalSettingKey.values()) {
			if (!contains(key)) {
				if (key.type == int.class) set(key, getDefaultInt(key));
				if (key.type == boolean.class) set(key, getDefaultBoolean(key));
				if (key.type == double.class) set(key, getDefaultDouble(key));
				if (key.type == String.class) set(key, getDefaultString(key));
				changed = true;
			}
		}

		boolean foundTypo = false;
		for (World w : Bukkit.getWorlds()) {
			SmoothSleep.logDebug("Checking config for world: " + w.getName());
			if (w.getEnvironment() == World.Environment.NORMAL) {
				if (contains(path(w))) {
					WorldSettings ws = new WorldSettings(w);
					worlds.put(w, ws);

					// Quick fix to get rid of that pesky 'ignore-vanished' that was never used from the config
					if (contains(w, "essentials-settings.ignore-vanished")) {
						set(w, "essentials-settings.ignore-vanished", null);
						if (!foundTypo) {
							SmoothSleep.logDebug("Sorry, I see your config still contains my typo 'ignore-vanished'. I'll just sweep that under the rug now...");
							foundTypo = true;
						}
						changed = true;
					}

					// Attempt to upgrade the config so users don't need to regenerate it
					SmoothSleep.logDebug("Checking for outdated config keys for world: " + w.getName());
					for (Map.Entry<String, WorldSettingKey> entry : worldMoved.entrySet()) {
						String from = entry.getKey();
						WorldSettingKey to = entry.getValue();
						if (!contains(w, from) || isSection(w, from)) { continue; }
						SmoothSleep.logDebug("-- Moving key: " + from + " --> " + to.key);
						if (to.type.equals(double.class)) {
							double val = getDouble(w, from);
							set(w, to, val);
						} else if (to.type.equals(int.class)) {
							int val = getInt(w, from);
							set(w, to, val);
						} else if (to.type.equals(boolean.class)) {
							boolean val = getBoolean(w, from);
							set(w, to, val);
						} else if (to.type.equals(String.class)) {
							String val = getString(w, from);
							set(w, to, val);
						}
						// Now check if the old key still exists and that it's not a config section.
						// If it's a config section, probably means we're still using the key, so can't delete it.
						if (contains(w, from)) { if (!isSection(w, from)) { set(w, from, null); } }
						changed = true;
					}

					// This for-loop adds in any new config changes for older configs
					for (WorldSettingKey key : WorldSettingKey.values()) {
						if (!ws.contains(key)) {
							if (key.type == int.class) ws.set(key, getDefaultInt(key));
							if (key.type == boolean.class) ws.set(key, getDefaultBoolean(key));
							if (key.type == double.class) ws.set(key, getDefaultDouble(key));
							if (key.type == String.class) ws.set(key, getDefaultString(key));
							changed = true;
						}
					}

					// Some sanity checks to make sure that config values are valid

					// Changed {PLAYER} to {USERNAME}, make sure old configs are updated
					for (WorldSettingKey wsk : placeholderKeys) {
						if (ws.getString(wsk).contains("{PLAYER}")) {
							SmoothSleep.logWarning(path(w, wsk) + ": Using the old placeholder {PLAYER}, updating it to {USERNAME}");
							ws.set(wsk, ws.getString(wsk).replace("{PLAYER}", "{USERNAME}"));
							changed = true;
						}
					}

					// TODO Check these sanity checks for mults now that they're doubles (< 0 instead of < 1)

					// Make sure the min and max multipliers are greather than 0, otherwise night may last forever or go backwards
					if (ws.getDouble(MIN_NIGHT_MULT) < 0.01) {
						ws.set(MIN_NIGHT_MULT, 0.01);
						changed = true;
					}

					// Perhaps I should check that max > min, but if it's switched, it'll likely just mean
					// that fewer players sleeping will make night pass faster than more players sleeping,
					// which might be something that some strange person would want *shrug* (not tested)
					if (ws.getDouble(MAX_NIGHT_MULT) < 0.01) {
						ws.set(MAX_NIGHT_MULT, 0.01);
						changed = true;
					}

					// Make sure the particle radius is positive. Probably won't break anything but just to be safe
					if (ws.getDouble(PARTICLE_RADIUS) < 0.0) {
						ws.set(PARTICLE_RADIUS, Math.abs(ws.getDouble(PARTICLE_RADIUS)));
						changed = true;
					}

					// Putting a max value on particle radius to prevent senseless radius values. Don't want to affect the whole world
					if (ws.getDouble(PARTICLE_RADIUS) > 20.0) {
						SmoothSleep.logWarning(path(w, PARTICLE_RADIUS) + ": '" + ws.getDouble(PARTICLE_RADIUS) + "' can't be greater than 20!");
						SmoothSleep.logWarning("(For sanity's sake and possibly performance; Not much reason to want particles all over the world)");
						ws.set(PARTICLE_RADIUS, 20.0);
						changed = true;
					}

					// Speed curve can't be less than 0. Shouldn't even be 0, but we'll let it slide as a special case
					if (ws.getDouble(SPEED_CURVE) < 0.0) {
						SmoothSleep.logWarning(path(w, SPEED_CURVE) + ": '" + ws.getDouble(SPEED_CURVE) + "' can't be less than 0!");
						ws.set(SPEED_CURVE, 0);
						changed = true;
					}

					// Speed curve can't be greater than 1, same as above
					if (ws.getDouble(SPEED_CURVE) > 1.0) {
						SmoothSleep.logWarning(path(w, SPEED_CURVE) + ": '" + ws.getDouble(SPEED_CURVE) + "' can't be greater than 1!");
						ws.set(SPEED_CURVE, 1);
						changed = true;
					}

					String barClrName = ws.getString(BOSSBAR_COLOR);
					if (barClrName.isEmpty() || !isValidBarColor(barClrName)) {
						SmoothSleep.logWarning(path(w, BOSSBAR_COLOR) + ": '" + barClrName + "' does not appear to be a valid bar color!");
						SmoothSleep.logWarning("Valid colors: BLUE, GREEN, PINK, PURPLE, RED, WHITE, YELLOW");
						ws.set(BOSSBAR_COLOR, "BLUE");
						changed = true;
					}

//					String barTrkType = ws.getString(BOSSBAR_TRACK_TYPE);
//					if (barTrkType.isEmpty() || !isValidBarTrackType(barTrkType)) {
//						SmoothSleep.logWarning(path(w, BOSSBAR_TRACK_TYPE) + ": '" + barTrkType + "' does not appear to be a valid bar track type!");
//						SmoothSleep.logWarning("Valid types: SLEEPERS, TIME");
//						ws.set(BOSSBAR_TRACK_TYPE, "TIME");
//						changed = true;
//					}

					// Check for errors. This won't save the config, but just warn if an invalid value is used

					String sndName = ws.getString(MORNING_SOUND);
					if (!sndName.isEmpty() && !isValidSound(sndName)) {
						SmoothSleep.logWarning(path(w, MORNING_SOUND) + ": '" + sndName + "' does not appear to be a valid sound name!");
						SmoothSleep.logWarning("For a list of valid sounds, refer to https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html");
					}

					String pattName = ws.getString(PARTICLE_PATTERN);
					if (!pattName.isEmpty() && !isValidPattern(pattName)) {
						SmoothSleep.logWarning("'" + pattName + "' does not appear to be a valid particle pattern type!");
						SmoothSleep.logWarning("Valid patterns: RANDOM, CIRCLE, SPIRAL");
					}

					String partName = ws.getString(PARTICLE_TYPE);
					if (!partName.isEmpty() && !isValidParticle(partName)) {
						SmoothSleep.logWarning("'" + partName + "' does not appear to be a valid particle name!");
						SmoothSleep.logWarning("For a list of valid particles, refer to https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Particle.html");
					}

					if (ws.getParticle(PARTICLE_TYPE) != null) {
						Particle prt = ws.getParticle(PARTICLE_TYPE);
						if (requiresData.contains(prt)) {
							SmoothSleep.logWarning(path(w, PARTICLE_TYPE) + ": '" + partName + "' is a valid particle, but does not work in this version");
							SmoothSleep.logWarning("Particles that require extra data cannot currently be configured, and won't work without it.");
						}
					}
				} else { SmoothSleep.logDebug("World not enabled: " + w.getName() + " (not found in config)"); }
			} else if (contains(path(w))) {
				SmoothSleep.logWarning("World is not a normal environment type (world: " + w.getName() + ", environment: " + w.getEnvironment().name().toLowerCase(Locale.ENGLISH) + ")");
			}
		}

		if (changed) save();
	}

	public static String path(World w) { return "worlds." + w.getName(); }
	public static String path(World w, WorldSettingKey key) { return path(w) + "." + key.key; }

	public static boolean isValidSound(String name) {
		for (Sound snd : Sound.values()) { if (snd.name().equalsIgnoreCase(name)) { return true; } } return false;
	}

	public static boolean isValidParticle(String name) {
		for (Particle prt : Particle.values()) { if (prt.name().equalsIgnoreCase(name)) { return true; } } return false;
	}

	public static boolean isValidBarColor(String name) {
		for (BarColor clr : BarColor.values()) { if (clr.name().equalsIgnoreCase(name)) { return true; } } return false;
	}

//	public static boolean isValidBarTrackType(String name) {
//		for (BarTrackType trk : BarTrackType.values()) { if (trk.name().equalsIgnoreCase(name)) { return true; } } return false;
//	}

	public static boolean isValidPattern(String name) {
		for (ParticlePatternType ppt : ParticlePatternType.values()) { if (ppt.name().equalsIgnoreCase(name)) { return true; } } return false;
	}

}
