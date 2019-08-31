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

	public boolean contains(WorldConfigKey key) { return conf().contains(path(key)); }

	public void set(String path, Object value) { conf().set(path() + "." + path, value); }
	public void set(WorldConfigKey key, Object value) { conf().set(path(key), value); }
	public void setToDefault(WorldConfigKey key) { conf().setToDefault(path(key)); }

	public int getInt(String path) { return conf().getConfig().getInt(path() + "." + path, conf().getDefaultInt(DWP + path)); }
	public int getInt(WorldConfigKey key) { return getInt(key.key); }
	public int getDefaultInt(WorldConfigKey key) { return conf().getDefaultInt(DWP + key.key); }
	
	public boolean getBoolean(String path) { return conf().getConfig().getBoolean(path() + "." + path, conf().getDefaultBoolean(DWP + path)); }
	public boolean getBoolean(WorldConfigKey key) { return getBoolean(key.key); }
	public boolean getDefaultBoolean(WorldConfigKey key) { return conf().getDefaultBoolean(DWP + key.key); }
	
	public String getString(String path) { return conf().getConfig().getString(path() + "." + path, conf().getDefaultString(DWP + path)); }
	public String getString(WorldConfigKey key) { return getString(key.key); }
	public String getDefaultString(WorldConfigKey key) { return conf().getDefaultString(DWP + key.key); }
	
	public double getDouble(String path) { return conf().getConfig().getDouble(path() + "." + path, conf().getDefaultDouble(DWP + path)); }
	public double getDouble(WorldConfigKey key) { return getDouble(key.key); }
	public double getDefaultDouble(WorldConfigKey key) { return conf().getDefaultDouble(DWP + key.key); }
	
	public ItemStack getItemStack(String path) { return conf().getConfig().getItemStack(path() + "." + path, conf().getDefaultItemStack(DWP + path)); }
	public ItemStack getItemStack(WorldConfigKey key) { return getItemStack(key.key); }
	public ItemStack getDefaultItemStack(WorldConfigKey key) { return conf().getDefaultItemStack(DWP + key.key); }
	
	public ConfigurationSection getConfSection(WorldConfigKey key) { return getConfSection(key.key); }
	public ConfigurationSection getConfSection(String path) {
		return (conf().getConfig().contains(path() + "." + path)) ? conf().getConfig().getConfigurationSection(path() + "." + path) : conf().getDefaultConfSection(DWP + path);
	}
	public ConfigurationSection getDefaultConfSection(WorldConfigKey key) { return conf().getDefaultConfSection(DWP + key.key); }
	
	public Sound getSound(WorldConfigKey key) { return getSound(key.key); }
	public Sound getSound(String path) { return conf().getSound(path() + "." + path); }
	
	public Particle getParticle(WorldConfigKey key) { return getParticle(key.key); }
	public Particle getParticle(String path) { return conf().getParticle(path() + "." + path); }

	public Set<SleepRewardEffect> getSleepRewardEffects() {
		Set<SleepRewardEffect> effects = new HashSet<>();
		// TODO Get sleep reward effects
		//ConfigurationSection sect = getConfSection();
		return effects;
	}

}
