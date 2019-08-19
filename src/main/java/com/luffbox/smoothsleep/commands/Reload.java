package com.luffbox.smoothsleep.commands;

import com.luffbox.smoothsleep.SmoothSleep;
import com.luffbox.smoothsleep.lib.ConfigHelper;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.List;

public class Reload implements TabExecutor {

	private SmoothSleep pl;

	public Reload(SmoothSleep plugin) { pl = plugin; }

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.hasPermission("smoothsleep.reload")) {
			sender.sendMessage(ChatColor.RED + "You don't have permission to do this!");
		} else {
			ConfigHelper.firstRun = false;
			pl.data.reload();
			sender.sendMessage(ChatColor.GREEN + "Reloaded SmoothSleep");
			if (!pl.data.isPluginEnabled()) {
				sender.sendMessage(ChatColor.GOLD + "SmoothSleep is disabled. To enable, use " + ChatColor.YELLOW + "/sstoggle");
			}
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		return null;
	}
}
