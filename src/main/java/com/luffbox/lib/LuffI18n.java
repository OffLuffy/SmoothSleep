package com.luffbox.lib;

import org.apache.commons.lang.text.StrSubstitutor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.Map;

/**
 * A wrapper for language configuration files. Using this to fetch String keys from a
 * language file will also parse color codes using the '&' character and can optionally
 * replace keys using {@link StrSubstitutor} if you pass in a {@link Map<String, String>} object.
 */
public class LuffI18n {

	private final Plugin plugin;
	private final String lang;
	private final File file;
	private FileConfiguration conf;

	/**
	 * Constructs a language file wrapper.
	 * @param plugin This {@link Plugin} will be used to locate the 'lang' folder (placed within the plugin's data folder)
	 * @param lang The name of the language file to load (.yml extension not required)
	 * @see Plugin#getDataFolder()
	 */
	public LuffI18n(Plugin plugin, String lang) {
		this.plugin = plugin;
		if (lang.toLowerCase().endsWith(".yml")) { lang = lang.substring(0, lang.toLowerCase().indexOf(".yml")); }
		this.lang = lang;
		this.file = new File(plugin.getDataFolder() + File.separator + "lang", lang + ".yml");
		reloadLangFile();
	}

	/**
	 * Retrieves the translated value of the specified message. Parses '&' color codes.
	 * @param key The key of the message to translate
	 * @return String with the specified message translated and color parsed,
	 * or and empty String if the message key doesn't exist or is empty.
	 */
	public String translate(String key) { return translate(key, null); }

	/**
	 * Retrieves the translated value of the specified message. Parses '&' color codes and
	 * substitues valueMap keys for valueMap values if provided.
	 * @param key The key of the message to translate
	 * @param valueMap Map indicating String keys to be replaced with the corresponding value
	 * @return String with the specified message translated, color parsed, and substituted
	 * or and empty String if the message key doesn't exist or is empty.
	 */
	public String translate(String key, Map<String, String> valueMap) {
		if (conf != null && conf.contains(key)) {
			String msg = conf.getString(key);
			if (msg == null || msg.isEmpty()) return "";
			msg = ChatColor.translateAlternateColorCodes('&', msg);
			if (valueMap != null && !valueMap.isEmpty()) {
				StrSubstitutor sub = new StrSubstitutor(valueMap);
				msg = sub.replace(msg);
			}
			return msg;
		} else return "";
	}

	/**
	 * Loads the language file from disk.
	 */
	public void reloadLangFile() {
		if (!file.getParentFile().exists()) { file.mkdirs(); }
		conf = new YamlConfiguration();
		try {
			conf.load(file);
		} catch (Exception e) {
			Bukkit.getLogger().warning("[" + plugin.getName() + "] Failed to load language file: " + lang + ".yml");
		}
	}

	/**
	 * Saves the language file to disk
	 */
	public void saveLangFile() {
		if (!file.getParentFile().exists()) { file.mkdirs(); }
		try {
			conf.save(file);
		} catch (Exception e) {
			Bukkit.getLogger().warning("[" + plugin.getName() + "] Failed to save language file: " + lang + ".yml");
		}
	}

}
