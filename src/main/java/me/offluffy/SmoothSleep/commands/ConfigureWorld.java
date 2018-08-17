package me.offluffy.SmoothSleep.commands;

import me.offluffy.SmoothSleep.SmoothSleep;
import me.offluffy.SmoothSleep.lib.ConfigHelper.WorldSettingKey;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Locale;

public class ConfigureWorld implements CommandExecutor {

	private SmoothSleep plugin;

	public ConfigureWorld(SmoothSleep p) { plugin = p; }

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.hasPermission("smoothsleep.configure")) {
			sender.sendMessage(ChatColor.RED + "You don't have permission to do this!");
			return true;
		} else {

			if (args.length < 1) { // No arguments. Print some help text
				sender.sendMessage(ChatColor.RED + "A World is required");
				sendHelp(sender, label);
				return true;
			}

			World w = Bukkit.getWorld(args[0]);
			if (w == null) { // The world specified wasn't found. Print some more help text
				sender.sendMessage(ChatColor.RED + "That world couldn't be found!");
				sendHelp(sender, label);
				return true;
			}

			if (args.length < 2) { // A setting name wasn't provided, print available settings.
				sender.sendMessage(ChatColor.RED + "A setting is required");
				sendHelp(sender, label);
				return true;
			}

			WorldSettingKey key = null;
			try { key = WorldSettingKey.valueOf(args[1].toUpperCase(Locale.ENGLISH)); } catch (Exception ignore) {}
			if (key == null) {
				sender.sendMessage(ChatColor.RED + "That setting couldn't be found!");
				sendHelp(sender, label);
				return true;
			}

			String settingName = key.name().toLowerCase();
			String currentVal = "";
			String type = "Text";
			if (key.type == String.class) {
				currentVal = plugin.conf.getString(w, key);
			} else if (key.type == int.class) {
				currentVal = String.format("%d", plugin.conf.getInt(w, key));
				type = "Whole Number";
			} else if (key.type == double.class) {
				currentVal = String.format("%f", plugin.conf.getDouble(w, key));
				type = "Number/Decimal";
			} else if (key.type == boolean.class) {
				currentVal = plugin.conf.getBoolean(w, key) ? "true" : "false";
				type = "True/False";
			}
			if (args.length < 3) { // No value provided. Print out current setting value
				sender.sendMessage(ChatColor.GREEN + settingName + ChatColor.GRAY + " (" + type + ") " + "= " + ChatColor.AQUA + currentVal);
				switch (key) { // Give colored preview of these Strings
					case SLEEP_TITLE:
					case MORNING_TITLE:
					case SLEEP_SUBTITLE:
					case MORNING_SUBTITLE:
						sender.sendMessage(ChatColor.DARK_GRAY + "Colored: " + ChatColor.RESET
								+ ChatColor.translateAlternateColorCodes('&', currentVal));
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
						boolean sndFound = false;
						for (Sound snd : Sound.values()) {
							if (snd.name().equalsIgnoreCase(value)) { sndFound = true; value = snd.name(); break; }
						}
						if (!sndFound) {
							sender.sendMessage(ChatColor.RED + "'" + value + "' does not appear to be a valid sound name!");
							sender.sendMessage(ChatColor.GRAY + "Valid sounds: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html");
							return true;
						}
						break;
					case PARTICLE:
						boolean partFound = false;
						for (Particle p : Particle.values()) {
							if (p.name().equalsIgnoreCase(value)) { partFound = true; value = p.name(); break; }
						}
						if (!partFound) {
							sender.sendMessage(ChatColor.RED + "'" + value + "' does not appear to be a valid particle name!");
							sender.sendMessage(ChatColor.GRAY + "Valid particles: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Particle.html");
							return true;
						}
						break;
				}
				plugin.conf.set(w, key, value);
				plugin.saveConfig();
				sender.sendMessage(ChatColor.GREEN + settingName + ChatColor.DARK_AQUA + " changed from " + ChatColor.AQUA
						+ currentVal + ChatColor.DARK_AQUA + " to " + ChatColor.AQUA + value);
				return true;
			} else if (key.type == int.class) {
				try {
					int value = Integer.valueOf(args[2]);
					plugin.conf.set(w, key, value);
					plugin.conf.save();
					sender.sendMessage(ChatColor.GREEN + settingName + ChatColor.DARK_AQUA + " changed from " + ChatColor.AQUA
							+ currentVal + ChatColor.DARK_AQUA + " to " + ChatColor.AQUA + value);
					return true;
				} catch (Exception e) {
					sender.sendMessage(ChatColor.RED + "Invalid value. " + settingName + " must be a " + type + " value.") ;
					return true;
				}
			} else if (key.type == double.class) {
				try {
					double value = Double.valueOf(args[2]);
					plugin.conf.set(w, key, value);
					plugin.conf.save();
					sender.sendMessage(ChatColor.GREEN + settingName + ChatColor.DARK_AQUA + " changed from " + ChatColor.AQUA
							+ currentVal + ChatColor.DARK_AQUA + " to " + ChatColor.AQUA + value);
					return true;
				} catch (Exception e) {
					sender.sendMessage(ChatColor.RED + "Invalid value. " + settingName + " must be a " + type + " value.") ;
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
						sender.sendMessage(ChatColor.RED + "Invalid value. " + settingName + " must be a " + type + " value.") ;
						return true;
				}
				plugin.conf.set(w, key, value);
				plugin.conf.save();
				sender.sendMessage(ChatColor.GREEN + settingName + ChatColor.DARK_AQUA + " changed from " + ChatColor.AQUA
						+ currentVal + ChatColor.DARK_AQUA + " to " + ChatColor.AQUA + (value ? "true" : "false"));
				return true;
			} else return true;
		}
	}

	private void sendHelp(CommandSender sender, String label) {
		final String SEP = ChatColor.DARK_GRAY + " / " + ChatColor.GRAY;
		sender.sendMessage(ChatColor.DARK_RED + "Usage: " + ChatColor.GRAY + "/" + label + " <world> <setting> [value]");
		StringBuilder sb = new StringBuilder(ChatColor.DARK_RED + "Settings: " + ChatColor.GRAY);
		for (WorldSettingKey key : WorldSettingKey.values()) {
			if (sb.length() + key.name().length() + SEP.length() >= 230) {
				sender.sendMessage(ChatColor.GRAY + sb.toString().substring(0, sb.length() - SEP.length()));
				sb = new StringBuilder();
			} else {
				sb.append(key.name().toLowerCase(Locale.ENGLISH)).append(SEP);
			}
		}
		if (sb.length() > 0) {
			sender.sendMessage(ChatColor.GRAY + sb.toString().substring(0, sb.length() - SEP.length()));
		}
	}
}
