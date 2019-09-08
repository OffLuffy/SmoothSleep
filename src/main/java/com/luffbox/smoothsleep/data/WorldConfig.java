package com.luffbox.smoothsleep.data;

import com.luffbox.lib.ConfigHelper;
import com.luffbox.smoothsleep.SmoothSleep;
import com.luffbox.smoothsleep.lib.SleepRewardEffect;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

import static com.luffbox.smoothsleep.data.WorldConfigKey.*;

/**
 * Class for fetching config values specifically for World settings. This
 * differs from the {@link ConfigHelper} in that if a given path isn't found
 * in the World's section, it'll first try to fall back on the default-world
 * settings, then falls back on the default config's default world setting.
 */
public class WorldConfig {

	public static final String WORLDS_SECT = "world-settings";
	private static final String DWP = WORLDS_SECT + ".default-settings."; // Default world path

	private World world;

	public WorldConfig(World world) {
		this.world = world;
	}
	
	private ConfigHelper conf() { return SmoothSleep.data.getConfigHelper(); }

	public String path() { return WORLDS_SECT + "." + world.getName(); }
	public String path(WorldConfigKey key) { return path() + "." + key.key; }

	// TODO Create option-specific getters and setters

	public double getMinSpeedMult() { return getDouble(MIN_NIGHT_SPEED); }
	public double getMaxSpeedMult() { return getDouble(MAX_NIGHT_SPEED); }
	public double getSpeedCurve() { return getDouble(SPEED_CURVE); }
	public boolean isInstantDayEnabled() { return getBoolean(INSTANT_DAY_ENABLED); }
	public double getInstanDayRatio() { return getDouble(INSTANT_DAY_RATIO); }
	public boolean isTickingWeather() { return getBoolean(TICK_WEATHER); }
	public boolean isTickingRandom() { return getBoolean(TICK_RAND_TICK); }
	public int getRandomTickLimit() { return getInt(RAND_TICK_MAX); }
	public double getPhantomResetRatio() { return getDouble(PHANTOM_RESET_RATIO); }
	public double getPhantomSpawnChance() { return getDouble(PHANTOM_SPAWN_CHANCE); }
	public boolean isClearingWeather() { return getBoolean(CLEAR_WEATHER); }
	public boolean isHealingVillagers() { return getBoolean(HEAL_VILLAGERS); }

	// ====================================================================================

	public boolean contains(WorldConfigKey key) { return conf().contains(path(key)); }

	public void set(String path, Object value) { conf().set(path() + "." + path, value); }
	public void set(WorldConfigKey key, Object value) { conf().set(path(key), value); }
	public void setToDefault(WorldConfigKey key) { conf().setToDefault(path(key)); }

	public int getInt(String path) { return conf().getConfig().getInt(path() + "." + path, conf().getInt(DWP + path)); }
	public int getInt(WorldConfigKey key) { return getInt(key.key); }
	public int getDefaultInt(WorldConfigKey key) { return conf().getInt(DWP + key.key); }
	
	public boolean getBoolean(String path) { return conf().getConfig().getBoolean(path() + "." + path, conf().getBoolean(DWP + path)); }
	public boolean getBoolean(WorldConfigKey key) { return getBoolean(key.key); }
	public boolean getDefaultBoolean(WorldConfigKey key) { return conf().getBoolean(DWP + key.key); }
	
	public String getString(String path) { return conf().getConfig().getString(path() + "." + path, conf().getString(DWP + path)); }
	public String getString(WorldConfigKey key) { return getString(key.key); }
	public String getDefaultString(WorldConfigKey key) { return conf().getString(DWP + key.key); }
	
	public double getDouble(String path) { return conf().getConfig().getDouble(path() + "." + path, conf().getDouble(DWP + path)); }
	public double getDouble(WorldConfigKey key) { return getDouble(key.key); }
	public double getDefaultDouble(WorldConfigKey key) { return conf().getDouble(DWP + key.key); }
	
	public ItemStack getItemStack(String path) { return conf().getConfig().getItemStack(path() + "." + path, conf().getItemStack(DWP + path)); }
	public ItemStack getItemStack(WorldConfigKey key) { return getItemStack(key.key); }
	public ItemStack getDefaultItemStack(WorldConfigKey key) { return conf().getItemStack(DWP + key.key); }

	public ConfigurationSection getConfSection(String path) {
		return (conf().getConfig().contains(path() + "." + path)) ? conf().getConfig().getConfigurationSection(path() + "." + path) : conf().getConfSection(DWP + path);
	}
	public ConfigurationSection getConfSection(WorldConfigKey key) { return getConfSection(key.key); }
	public ConfigurationSection getDefaultConfSection(WorldConfigKey key) { return conf().getConfSection(DWP + key.key); }

	public Sound getSound(String path) {
		String sndName = conf().getConfig().getString(path() + "." + path, conf().getString(DWP + path));
		return ConfigHelper.resolveEnum(sndName, Sound.values());
	}
	public Sound getSound(WorldConfigKey key) { return getSound(key.key); }

	public Particle getParticle(String path) {
		String prtName = conf().getConfig().getString(path() + "." + path, conf().getString(DWP + path));
		return ConfigHelper.resolveEnum(prtName, Particle.values());
	}
	public Particle getParticle(WorldConfigKey key) { return getParticle(key.key); }

	public Set<SleepRewardEffect> getSleepRewardEffects() {
		Set<SleepRewardEffect> effects = new HashSet<>();
		// TODO Get sleep reward effects
		//ConfigurationSection sect = getConfSection();
		return effects;
	}

}
