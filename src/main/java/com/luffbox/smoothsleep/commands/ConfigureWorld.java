package com.luffbox.smoothsleep.commands;

import com.luffbox.smoothsleep.SmoothSleep;
import com.luffbox.smoothsleep.lib.ConfigHelper;
import com.luffbox.smoothsleep.lib.ConfigHelper.WorldSettingKey;
import com.luffbox.smoothsleep.lib.TabExecutor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.bukkit.ChatColor.*;

public class ConfigureWorld implements TabExecutor {

	private final SmoothSleep pl;

	public ConfigureWorld(SmoothSleep plugin) { pl = plugin; }

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.hasPermission("smoothsleep.configure")) {
			sender.sendMessage(RED + "You don't have permission to do this!");
			return true;
		} else {

			if (args.length < 1) { // No arguments. Print some help text
				sender.sendMessage(RED + "A World is required");
				sendHelp(sender, label);
				return true;
			}

			World w = Bukkit.getWorld(args[0]);
			if (w == null) { // The world specified wasn't found. Print some more help text
				sender.sendMessage(RED + "That world couldn't be found!");
				sendHelp(sender, label);
				return true;
			}

			if (args.length < 2) { // A setting name wasn't provided, print available settings.
				sender.sendMessage(RED + "A setting is required");
				sendHelp(sender, label);
				return true;
			}

			WorldSettingKey key = null;
			try { key = WorldSettingKey.valueOf(args[1].toUpperCase(Locale.ENGLISH)); } catch (Exception ignore) {}
			if (key == null) {
				sender.sendMessage(RED + "That setting couldn't be found!");
				sendHelp(sender, label);
				return true;
			}

			String settingName = key.name().toLowerCase();
			String currentVal = "";
			String type = "Text";
			if (key.type == String.class) {
				currentVal = pl.data.config.getString(w, key);
			} else if (key.type == int.class) {
				currentVal = String.format("%d", pl.data.config.getInt(w, key));
				type = "Whole Number";
			} else if (key.type == double.class) {
				currentVal = String.format("%.2f", pl.data.config.getDouble(w, key));
				type = "Number/Decimal";
			} else if (key.type == boolean.class) {
				currentVal = pl.data.config.getBoolean(w, key) ? "true" : "false";
				type = "True/False";
			}
			if (args.length < 3) { // No value provided. Print out current setting value
				sender.sendMessage(GREEN + settingName + GRAY + " (" + type + ") " + "= " + AQUA + currentVal);
				switch (key) { // Give colored preview of these Strings
					case SLEEP_TITLE:
					case MORNING_TITLE:
					case SLEEP_SUBTITLE:
					case MORNING_SUBTITLE:
						sender.sendMessage(DARK_GRAY + "Colored: " + RESET + translateAlternateColorCodes('&', currentVal));
					default: return true;
				}
			}

			if (key.type == String.class) {
				StringBuilder sb = new StringBuilder();
				for (int i = 2; i < args.length; i++) {
					if (sb.length() > 0) { sb.append(" "); }
					sb.append(args[i]);
				}
				String value = sb.toString().substring(0, sb.length());
				switch (key) { // Special checks for these setting keys
					case MORNING_SOUND:
						if (!ConfigHelper.isValidSound(value)) {
							sender.sendMessage(RED + "'" + value + "' does not appear to be a valid sound name!");
							sender.sendMessage(GRAY + "Valid sounds: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html");
							return true;
						}
						break;
					case PARTICLE_TYPE:
						if (!ConfigHelper.isValidParticle(value)) {
							sender.sendMessage(RED + "'" + value + "' does not appear to be a valid particle name!");
							sender.sendMessage(GRAY + "Valid particles: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Particle.html");
							return true;
						}
						break;
					case BOSSBAR_COLOR:
						if (!ConfigHelper.isValidBarColor(value)) {
							sender.sendMessage(RED + "'" + value + "' does not appear to be a valid boss bar color!");
							sender.sendMessage(GRAY + "Valid colors: " + ConfigHelper.validBarColors());
							return true;
						}
						break;
					case BOSSBAR_STYLE:
						if (!ConfigHelper.isValidBarStyle(value)) {
							sender.sendMessage(RED + "'" + value + "' does not appear to be a valid boss bar style!");
							sender.sendMessage(GRAY + "Valid styles: " + ConfigHelper.validBarStyles());
							return true;
						}
						break;
					case PARTICLE_PATTERN:
						if (!ConfigHelper.isValidPattern(value)) {
							sender.sendMessage(RED + "'" + value + "' does not appear to be a valid particle pattern type!");
							sender.sendMessage(GRAY + "Valid patterns: " + ConfigHelper.validParticlePatternTypes());
							return true;
						}
				}
				pl.data.config.set(w, key, value);
				pl.saveConfig();
				sender.sendMessage(GREEN + settingName + DARK_AQUA + " changed from " + AQUA
						+ currentVal + DARK_AQUA + " to " + AQUA + value);
				return true;
			} else if (key.type == int.class) {
				try {
					int value = Integer.parseInt(args[2]);
					pl.data.config.set(w, key, value);
					pl.data.config.save();
					sender.sendMessage(GREEN + settingName + DARK_AQUA + " changed from " + AQUA
							+ currentVal + DARK_AQUA + " to " + AQUA + value);
					return true;
				} catch (Exception e) {
					sender.sendMessage(RED + "Invalid value. " + settingName + " must be a " + type + " value.") ;
					return true;
				}
			} else if (key.type == double.class) {
				try {
					double value = Double.parseDouble(args[2]);
					pl.data.config.set(w, key, value);
					pl.data.config.save();
					sender.sendMessage(GREEN + settingName + DARK_AQUA + " changed from " + AQUA
							+ currentVal + DARK_AQUA + " to " + AQUA + String.format("%.2f", value));
					return true;
				} catch (Exception e) {
					sender.sendMessage(RED + "Invalid value. " + settingName + " must be a " + type + " value.") ;
					return true;
				}
			} else if (key.type == boolean.class) {
				boolean value;
				switch (args[2].toLowerCase()) {
					case "true":
					case "t":
					case "1":
						value = true;
						break;
					case "false":
					case "f":
					case "0":
						value = false;
						break;
					default:
						sender.sendMessage(RED + "Invalid value. " + settingName + " must be a " + type + " value.") ;
						return true;
				}
				pl.data.config.set(w, key, value);
				pl.data.config.save();
				sender.sendMessage(GREEN + settingName + DARK_AQUA + " changed from " + AQUA
						+ currentVal + DARK_AQUA + " to " + AQUA + (value ? "true" : "false"));
				return true;
			} else return true;
		}
	}

	private void sendHelp(CommandSender sender, String label) {
		final String SEP = DARK_GRAY + " / " + GRAY;
		sender.sendMessage(DARK_RED + "Usage: " + GRAY + "/" + label + " <world> <setting> [value]");
		StringBuilder sb = new StringBuilder(DARK_RED + "Settings: " + GRAY);
		for (WorldSettingKey key : WorldSettingKey.values()) {
			if (sb.length() + key.name().length() + SEP.length() >= 230) {
				sender.sendMessage(GRAY + sb.toString().substring(0, sb.length() - SEP.length()));
				sb = new StringBuilder();
			} else { sb.append(key.name().toLowerCase(Locale.ENGLISH)).append(SEP); }
		}
		if (sb.length() > 0) { sender.sendMessage(GRAY + sb.toString().substring(0, sb.length() - SEP.length())); }
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> opts = new ArrayList<>();
		if (args.length == 1) {
			for (World w : pl.data.getWorldData().keySet()) {
				String worldName = w.getName().toLowerCase(Locale.ENGLISH);
				if (worldName.startsWith(args[0].toLowerCase())) {
					opts.add(w.getName());
				}
			}
		} else if (args.length == 2) {
			for (WorldSettingKey key : WorldSettingKey.values()) {
				String keyName = key.name().toLowerCase(Locale.ENGLISH);
				if (keyName.startsWith(args[1].toLowerCase())) {
					opts.add(keyName);
				}
			}
		}
		return opts;
	}
}
