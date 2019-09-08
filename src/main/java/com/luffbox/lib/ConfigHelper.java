package com.luffbox.lib;

import org.bukkit.*;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

/**
 * Config wrapper that assists with handling a plugin's config file.
 * This class's primary use is to retrieve a default value from the
 * built-in config.yml resource if a getter fails to fetch a value
 * from the current config.<br>
 * There are also additional methods to resolve several of Bukkit's
 * enum classes from String values in the config. All enum getters
 * will ignore capitalization, spaces, and underscores (including
 * PotionEffectType, which isn't exactly an Enum, but close enough)<br>
 * Enum getters do not provide 'getDefault' methods explicitly, but
 * since they use {@link #getString(String)} internally, they will
 * still fetch the default config value if it isn't found.
 */
public class ConfigHelper {

	private Plugin plugin;

	/**
	 * Construct a ConfigHelper to assist with handling a plugin's config
	 * @param plugin The plugin to fetch the config from
	 */
	public ConfigHelper(Plugin plugin) {
		this.plugin = plugin;
		reload();
	}

	private Configuration conf() { return plugin.getConfig(); }

	/**
	 * Saves the default config if a config doesn't exist and reloads config
	 * to ensure the latest version of the config is loaded.
	 * @see Plugin#saveDefaultConfig()
	 * @see Plugin#reloadConfig()
	 */
	public void reload() {
		plugin.saveDefaultConfig();
		plugin.reloadConfig();
	}

	/**
	 * Saves the config via {@link Plugin#saveConfig()}
	 */
	public void save() { plugin.saveConfig(); }

	/**
	 * Gets the {@link Configuration} from {@link Plugin#getConfig()}
	 * @return A {@link Configuration} object using {@link Plugin#getConfig()}
	 */
	public Configuration getConfig() { return plugin.getConfig(); }

	/**
	 * Gets the {@link Configuration} from {@link Configuration#getDefaults()}
	 * @return A {@link Configuration} object using {@link Configuration#getDefaults()}
	 */
	public Configuration getDefaults() { return plugin.getConfig().getDefaults(); }

	/**
	 * Checks if this {@link ConfigurationSection} contains the given path.
	 * @param path The path to check
	 * @return A boolean indicating if the given path was found
	 */
	public boolean contains(String path) { return conf().contains(path); }

	/**
	 * Sets the given path in the config to the value found at the same path in the default config.
	 * @param path The path to set back to its default value
	 */
	public void setToDefault(String path) { set(path, getDefaults().get(path)); }

	/**
	 * Sets the value of the given path in the config
	 * @param path The path to set the value for
	 * @param value The value to set the path to in the config
	 */
	public void set(String path, Object value) { conf().set(path, value); }

	/**
	 * Sets the value of the given path in the config to the serialized value provided.
	 * This will set the given path to a {@link ConfigurationSection} representing the value given.
	 * @param path The path to set the value for
	 * @param value The {@link LuffSerializable} object to set to this path
	 */
	public void setSerializable(String path, LuffSerializable value) { conf().set(path, value.serialize()); }

	/**
	 * Retrieves an int value from the config
	 * @param path The path to retrieve the value from
	 * @return An int value found at the given path, or the value at the same path in the default config.
	 * @see #getDefaultInt(String)
	 */
	public int getInt(String path) { return conf().getInt(path, getDefaultInt(path)); }

	/**
	 * Retrieves an int value from the default config
	 * @param path The path to retrieve the value from
	 * @return An int value found at the given path if found, otherwise returns 0
	 */
	public int getDefaultInt(String path) { return getDefaults() == null ? 0 : getDefaults().getInt(path); }

	/**
	 * Retrieves a boolean value from the config
	 * @param path The path to retrieve the value from
	 * @return A boolean value found at the given path, or the value at the same path in the default config.
	 * @see #getDefaultBoolean(String)
	 */
	public boolean getBoolean(String path) { return conf().getBoolean(path, getDefaultBoolean(path)); }

	/**
	 * Retrieves a boolean value from the default config
	 * @param path The path to retrieve the value from
	 * @return A boolean value found at the given path if found, otherwise returns false
	 */
	public boolean getDefaultBoolean(String path) { return getDefaults() != null && getDefaults().getBoolean(path); }

	/**
	 * Retrieves a String value from the config
	 * @param path The path to retrieve the value from
	 * @return A String value found at the given path, or the value at the same path in the default config.
	 * @see #getDefaultString(String)
	 */
	public String getString(String path) { return conf().getString(path, getDefaultString(path)); }

	/**
	 * Retrieves a String value from the default config
	 * @param path The path to retrieve the value from
	 * @return A String value found at the given path if found, otherwise returns an empty String
	 */
	public String getDefaultString(String path) { return getDefaults() == null ? "" : getDefaults().getString(path); }

	/**
	 * Retrieves a double value from the config
	 * @param path The path to retrieve the value from
	 * @return A double value found at the given path, or the value at the same path in the default config.
	 * @see #getDefaultDouble(String)
	 */
	public double getDouble(String path) { return conf().getDouble(path, getDefaultDouble(path)); }

	/**
	 * Retrieves a double value from the default config
	 * @param path The path to retrieve the value from
	 * @return A double value found at the given path if found, otherwise returns 0.0
	 */
	public double getDefaultDouble(String path) { return getDefaults() == null ? 0.0 : getDefaults().getDouble(path); }

	/**
	 * Retrieves an ItemStack value from the config
	 * @param path The path to retrieve the value from
	 * @return An ItemStack value found at the given path, or the value at the same path in the default config.
	 * @see #getDefaultItemStack(String)
	 */
	public ItemStack getItemStack(String path) { return conf().getItemStack(path, getDefaultItemStack(path)); }

	/**
	 * Retrieves an ItemStack value from the default config
	 * @param path The path to retrieve the value from
	 * @return An ItemStack value found at the given path if found, otherwise returns an ItemStack with one {@link Material#AIR}
	 */
	public ItemStack getDefaultItemStack(String path) { return getDefaults() == null ? new ItemStack(Material.AIR) : getDefaults().getItemStack(path); }

	/**
	 * Retrieves a {@link ConfigurationSection} at the given key in the config
	 * @param path The path to a section in the config
	 * @return A {@link ConfigurationSection} representing the specified section of the config. If not found,
	 * it will then return {@link ConfigHelper#getDefaultConfSection(String)} instead.
	 */
	public ConfigurationSection getConfSection(String path) {
		if (conf().contains(path))
			return conf().getConfigurationSection(path);
		else return getDefaultConfSection(path);
	}

	/**
	 * Retrieves a {@link ConfigurationSection} at the given key in the default config
	 * @param path The path to a section in the default config
	 * @return A {@link ConfigurationSection} representing the specified section of the default config.
	 * If it can't be found, then an empty {@link MemoryConfiguration} is returned instead.
	 */
	public ConfigurationSection getDefaultConfSection(String path) {
		if (getDefaults().contains(path))
			return getDefaults().getConfigurationSection(path);
		return new MemoryConfiguration();
	}

	/**
	 * Attempts to resolve a {@link Sound} from the String specified at the given config key
	 * @param path The path to the String value in the config
	 * @return A {@link Sound} if the String matches one, null otherwise
	 */
	public Sound getSound(String path) { return getEnum(path, Sound.values()); }

	/**
	 * Attempts to resolve a List of {@link Sound} objects from the String specified at the given config key
	 * @param path The path to the String List value in the config
	 * @return A List of {@link Sound} objects or null. If the path in the config doesn't exist or isn't a
	 * list, null is returned. An empty List can be returned if the list in the config is empty or if none
	 * of the String values in the list can be resolved to a valid {@link Sound} name.
	 */
	public List<Sound> getSoundList(String path) { return getEnumList(path, Sound.values()); }

	/**
	 * Attempts to resolve a {@link Particle} from the String specified at the given config key
	 * @param path The path to the String value in the config
	 * @return A {@link Particle} if the String matches one, null otherwise
	 */
	public Particle getParticle(String path) { return getEnum(path, Particle.values()); }

	/**
	 * Attempts to resolve a List of {@link Particle} objects from the String specified at the given config key
	 * @param path The path to the String List value in the config
	 * @return A List of {@link Particle} objects or null. If the path in the config doesn't exist or isn't a
	 * list, null is returned. An empty List can be returned if the list in the config is empty or if none
	 * of the String values in the list can be resolved to a valid {@link Particle} name.
	 */
	public List<Particle> getParticleList(String path) { return getEnumList(path, Particle.values()); }

	/**
	 * Attempts to resolve a {@link Material} from the String specified at the given config key
	 * @param path The path to the String value in the config
	 * @return A {@link Material} if the String matches one, null otherwise
	 */
	public Material getMaterial(String path) { return getEnum(path, Material.values()); }

	/**
	 * Attempts to resolve a List of {@link Material} objects from the String specified at the given config key
	 * @param path The path to the String List value in the config
	 * @return A List of {@link Material} objects or null. If the path in the config doesn't exist or isn't a
	 * list, null is returned. An empty List can be returned if the list in the config is empty or if none
	 * of the String values in the list can be resolved to a valid {@link Material} name.
	 */
	public List<Material> getMaterialList(String path) { return getEnumList(path, Material.values()); }

	/**
	 * Attempts to resolve a {@link EntityType} from the String specified at the given config key
	 * @param path The path to the String value in the config
	 * @return A {@link EntityType} if the String matches one, null otherwise
	 */
	public EntityType getEntityType(String path) { return getEnum(path, EntityType.values()); }

	/**
	 * Attempts to resolve a List of {@link EntityType} objects from the String specified at the given config key
	 * @param path The path to the String List value in the config
	 * @return A List of {@link EntityType} objects or null. If the path in the config doesn't exist or isn't a
	 * list, null is returned. An empty List can be returned if the list in the config is empty or if none
	 * of the String values in the list can be resolved to a valid {@link EntityType} name.
	 */
	public List<EntityType> getEntityTypeList(String path) { return getEnumList(path, EntityType.values()); }

	/**
	 * Attempts to resolve a {@link DyeColor} from the String specified at the given config key
	 * @param path The path to the String value in the config
	 * @return A {@link DyeColor} if the String matches one, null otherwise
	 */
	public DyeColor getDyeColor(String path) { return getEnum(path, DyeColor.values()); }

	/**
	 * Attempts to resolve a List of {@link DyeColor} objects from the String specified at the given config key
	 * @param path The path to the String List value in the config
	 * @return A List of {@link DyeColor} objects or null. If the path in the config doesn't exist or isn't a
	 * list, null is returned. An empty List can be returned if the list in the config is empty or if none
	 * of the String values in the list can be resolved to a valid {@link DyeColor} name.
	 */
	public List<DyeColor> getDyeColorList(String path) { return getEnumList(path, DyeColor.values()); }

	/**
	 * Attempts to resolve a {@link ChatColor} from the String specified at the given config key
	 * @param path The path to the String value in the config
	 * @return A {@link ChatColor} if the String matches one, null otherwise
	 */
	public ChatColor getChatColor(String path) { return getEnum(path, ChatColor.values()); }

	/**
	 * Attempts to resolve a List of {@link ChatColor} objects from the String specified at the given config key
	 * @param path The path to the String List value in the config
	 * @return A List of {@link ChatColor} objects or null. If the path in the config doesn't exist or isn't a
	 * list, null is returned. An empty List can be returned if the list in the config is empty or if none
	 * of the String values in the list can be resolved to a valid {@link ChatColor} name.
	 */
	public List<ChatColor> getChatColorList(String path) { return getEnumList(path, ChatColor.values()); }

	/**
	 * Attempts to resolve a {@link PotionEffectType} from the String specified at the given config key
	 * @param path The path to the String value in the config
	 * @return A {@link PotionEffectType} if the String matches one, null otherwise
	 */
	public PotionEffectType getPotionEffectType(String path) {
		return resolvePotionEffectType(getString(path));
	}

	/**
	 * Attempts to resolve a List of {@link PotionEffectType} objects from the String specified at the given config key
	 * @param path The path to the String List value in the config
	 * @return A List of {@link PotionEffectType} objects or null. If the path in the config doesn't exist or isn't a
	 * list, null is returned. An empty List can be returned if the list in the config is empty or if none
	 * of the String values in the list can be resolved to a valid {@link PotionEffectType} name.
	 */
	public List<PotionEffectType> getPotionEFfectTypeList(String path) {
		if (!contains(path) || !getConfig().isList(path)) return null;
		List<PotionEffectType> petList = new ArrayList<>();
		List<String> stringList = getConfig().getStringList(path);
		for (String petName : stringList) {
			PotionEffectType pet = resolvePotionEffectType(petName);
			if (pet != null) petList.add(pet);
		}
		return petList;
	}

	public <T extends Enum> T getEnum(String path, T[] enums) {
		return resolveEnum(getString(path), enums);
	}

	public <T extends Enum> List<T> getEnumList(String path, T[] enums) {
		if (!contains(path) || !getConfig().isList(path)) return null;
		List<T> enumList = new ArrayList<>();
		List<String> stringList = getConfig().getStringList(path);
		for (String enumName : stringList) {
			T res = resolveEnum(enumName, enums);
			if (res != null) enumList.add(res);
		}
		return enumList;
	}

	public static <T extends Enum> T resolveEnum(String enumName, T[] enums) {
		String p = prepString(enumName);
		for (T x : enums) { if (p.equals(prepString(x.name()))) return x; }
		return null;
	}

	public static PotionEffectType resolvePotionEffectType(String typeName) {
		String p = prepString(typeName);
		for (PotionEffectType pet : PotionEffectType.values()) { if (p.equals(prepString(pet.getName()))) return pet; }
		return null;
	}

	public static String prepString(String in) {
		return in.toLowerCase().replace("_", "").replace(" ", "");
	}

}
