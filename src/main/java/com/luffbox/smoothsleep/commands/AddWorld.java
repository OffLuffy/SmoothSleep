package com.luffbox.smoothsleep.commands;

import com.luffbox.smoothsleep.SmoothSleep;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class AddWorld implements CommandExecutor {

	private SmoothSleep pl;

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
}
