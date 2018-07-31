package me.offluffy.SmoothSleep.commands;

import me.offluffy.SmoothSleep.SmoothSleep;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ToggleEnabled implements CommandExecutor {

	private SmoothSleep plugin;
	public ToggleEnabled(SmoothSleep p) { plugin = p; }

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.hasPermission("smoothsleep.reload")) {
			sender.sendMessage(ChatColor.RED + "You don't have permission to do this!");
		} else {
			plugin.enabled = !plugin.enabled;
			sender.sendMessage(plugin.enabled ? (ChatColor.GREEN + "Enabled SmoothSleep") : (ChatColor.GOLD + "Temporarily Disabled SmoothSleep"));
			if (!plugin.enabled) {
				sender.sendMessage(ChatColor.GOLD + "This isn't persistent, SmoothSleep will re-enable when the server restarts or reloads");
			}
		}
		return true;
	}
}
