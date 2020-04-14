package com.luffbox.smoothsleep.commands;

import com.luffbox.smoothsleep.SmoothSleep;
import com.luffbox.smoothsleep.lib.TabExecutor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AddWorld implements TabExecutor {

	private final SmoothSleep pl;

	public AddWorld(SmoothSleep plugin) { pl = plugin; }

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.hasPermission("smoothsleep.addworld")) {
			sender.sendMessage(ChatColor.RED + "You don't have permission to do this!");
		} else {
			if (args.length < 1) {
				sender.sendMessage(ChatColor.RED + "This requires a world: /" + label + " <world>");
				return true;
			}
			World w = Bukkit.getWorld(args[0]);
			if (w == null) {
				sender.sendMessage(ChatColor.RED + "That world couldn't be found!");
				return true;
			}
			if (pl.data.config.contains("worlds." + w.getName())) {
				sender.sendMessage(ChatColor.AQUA + "That world already exists in the config!");
				sender.sendMessage(ChatColor.AQUA + "To configure your world, use " + ChatColor.GREEN + "/ssconf");
				return true;
			}
			pl.data.config.set("worlds." + w.getName(), pl.data.config.getDefaultConfSection("worlds.world"));
			pl.data.config.save();
			sender.sendMessage(ChatColor.AQUA + "Default values added for world: " + ChatColor.GREEN + w.getName());
			sender.sendMessage(ChatColor.AQUA + "To configure your world, use " + ChatColor.GREEN + "/ssconf");
			sender.sendMessage(ChatColor.AQUA + "This won't take effect until you use " + ChatColor.GREEN + "/ssreload");
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> opts = new ArrayList<>();
		if (args.length == 1) {
			for (World w : pl.getServer().getWorlds()) {
				if (!pl.data.worldEnabled(w)) {
					String worldName = w.getName().toLowerCase(Locale.ENGLISH);
					if (worldName.startsWith(args[0].toLowerCase())) {
						opts.add(w.getName());
					}
				}
			}
		}
		return opts;
	}
}
