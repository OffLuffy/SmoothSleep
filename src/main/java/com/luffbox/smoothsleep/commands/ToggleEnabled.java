package com.luffbox.smoothsleep.commands;

import com.luffbox.smoothsleep.SmoothSleep;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ToggleEnabled implements CommandExecutor {

	private SmoothSleep pl;

	public ToggleEnabled(SmoothSleep plugin) { pl = plugin; }

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.hasPermission("smoothsleep.reload")) {
			sender.sendMessage(ChatColor.RED + "You don't have permission to do this!");
		} else {
			pl.data.setPluginEnabled(!pl.data.isPluginEnabled());
			sender.sendMessage(pl.data.isPluginEnabled() ? (ChatColor.GREEN + "Enabled SmoothSleep") : (ChatColor.GOLD + "Temporarily Disabled SmoothSleep"));
			if (!pl.data.isPluginEnabled()) {
				sender.sendMessage(ChatColor.GOLD + "This isn't persistent, SmoothSleep will re-enable when the server restarts or reloads");
			}
		}
		return true;
	}
}
