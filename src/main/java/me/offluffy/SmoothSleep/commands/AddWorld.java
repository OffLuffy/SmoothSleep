package me.offluffy.SmoothSleep.commands;

import me.offluffy.SmoothSleep.SmoothSleep;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.MemoryConfiguration;

public class AddWorld implements CommandExecutor {

	private SmoothSleep plugin;

	public AddWorld(SmoothSleep p) { plugin = p; }

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
			if (plugin.conf.contains("worlds." + w.getName())) {
				sender.sendMessage(ChatColor.AQUA + "That world already exists in the config!");
				sender.sendMessage(ChatColor.AQUA + "To configure your world, use " + ChatColor.GREEN + "/ssconf");
				return true;
			}
			plugin.conf.set("worlds." + w.getName(), plugin.conf.getDefaultConfigurationSection("worlds.world"));
			plugin.conf.save();
			sender.sendMessage(ChatColor.AQUA + "Default values added for world: " + ChatColor.GREEN + w.getName());
			sender.sendMessage(ChatColor.AQUA + "To configure your world, use " + ChatColor.GREEN + "/ssconf");
			sender.sendMessage(ChatColor.AQUA + "This won't take effect until you use " + ChatColor.GREEN + "/ssreload");
		}
		return true;
	}
}
