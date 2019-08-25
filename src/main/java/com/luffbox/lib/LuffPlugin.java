package com.luffbox.lib;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.URL;
import java.util.Scanner;

public class LuffPlugin extends JavaPlugin {

	private static final String[] SVP = {"major", "minor", "patch"};

	private static LuffPlugin inst;

	protected String resourceId = null;
	public static String nmsver;

	public LuffPlugin() {
		inst = this;
		nmsver = getServer().getClass().getPackage().getName();
		nmsver = nmsver.substring(nmsver.lastIndexOf(".") + 1);
	}

	public enum LogLevel {
		INFO("info"),
		DEBUG("debug"),
		WARN("warning"),
		SEVERE("severe");
		private String levelName;
		LogLevel(String name) { levelName = name; }
		public String getLevelName() { return levelName; }
	}

	protected boolean checkUpdate() {
		if (resourceId == null) { logDebug("No resource ID specified, skipping update check."); return false; }
		try {
			URL url = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + resourceId);
			Scanner s = new Scanner(url.openStream());
			StringBuilder out = new StringBuilder();
			while (s.hasNext()) { out.append(s.next()); }
			String newVer = out.toString();
			if (!getVersion().equals(newVer)) {
				logWarning("Installed version: " + getVersion() + ", current version: " + newVer);
				boolean showDownload = true;
				try {
					// Try to check semantic version; if it fails, just show the download link
					String[] newVerParts = newVer.split(".");
					String[] oldVerParts = getVersion().split(".");
					for (int i = 0; i < 3; i++) { // 0 = major, 1 = minor, 2 = patch (SemVer)
						int lv = Integer.parseInt(newVerParts[i]); // Latest release on Spigot's site
						int cv = Integer.parseInt(oldVerParts[i]); // Current version being used
						if (lv < cv) {
							logWarning("Using a version that isn't released yet.");
							showDownload = false;
							break;
						} else if (lv > cv) {
							logWarning("A new " + SVP[i] + " version is available.");
							break;
						}
						// If lv == cv, continue to next iteration
					}
				} catch (Exception ignore) {}
				if (showDownload) {
					logWarning("Download the current version at https://www.spigotmc.org/resources/" + resourceId + "/");
				}
				return true;
			}
		} catch (Exception ignore) {}
		return false;
	}

	/**
	 * Checks if a given level of logging is enabled in the config
	 * @param level The {@link LogLevel} to check
	 * @param def The default value to assume if not specified in the config
	 * @return If specified in the config, returns true if the log level is enabled, false if not. If not on config, returns 'def'
	 */
	public static boolean logLevelEnabled(LogLevel level, boolean def) { return inst.getConfig().getBoolean("logging-settings.log-" + level.getLevelName(), def); }

	/**
	 * Short-hand method of getting the name of the plugin from the plugin description file
	 * @return Returns the name of the plugin
	 */
	public static String getPluginName() { return inst.getDescription().getName(); }

	/**
	 * Short-hand method of getting the version of the plugin from the plugin description file
	 * @return Returns the version string of the plugin
	 */
	public static String getVersion() { return inst.getDescription().getVersion(); }

	private static void log(LogLevel level, boolean def, ChatColor clr, String msg) {
		if (level.equals(LogLevel.SEVERE) || logLevelEnabled(level, def)) {
			Bukkit.getConsoleSender().sendMessage(clr + "[" + getPluginName() + " : " + level.name() + "] " + msg);
		}
	}

	/**
	 * Logs a message to the console without any prefix or log level. Ignores enabled or disabled log levels
	 * @param msg The message to print to console
	 */
	public static void logCustom(String msg) { Bukkit.getConsoleSender().sendMessage(msg); }

	/**
	 * Logs an informational message to the console if {@link LogLevel#INFO} is enabled
	 * @param msg The message to print to console
	 */
	public static void logInfo(String msg) { log(LogLevel.INFO, true, ChatColor.AQUA, msg); }

	/**
	 * Logs an debug message to the console if {@link LogLevel#DEBUG} is enabled
	 * @param msg The message to print to console
	 */
	public static void logDebug(String msg) { log(LogLevel.DEBUG, false, ChatColor.LIGHT_PURPLE, msg); }

	/**
	 * Logs an warning message to the console if {@link LogLevel#WARN} is enabled
	 * @param msg The message to print to console
	 */
	public static void logWarning(String msg) { log(LogLevel.WARN, true, ChatColor.GOLD, msg); }

	/**
	 * Logs an severe message to the console, severe messages are always printed regardless if enabled
	 * @param msg The message to print to console
	 */
	public static void logSevere(String msg) { log(LogLevel.SEVERE, true, ChatColor.RED ,msg); }

}
