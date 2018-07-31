package me.offluffy.SmoothSleep.commands;

import me.offluffy.SmoothSleep.SmoothSleep;
import me.offluffy.SmoothSleep.lib.ConfigHelper.GlobalSettingKey;
import org.bstats.bukkit.Metrics;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ToggleMetrics implements CommandExecutor {

	private SmoothSleep plugin;
	public ToggleMetrics(SmoothSleep p) { plugin = p; }

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.hasPermission("smoothsleep.metrics")) {
			sender.sendMessage(ChatColor.RED + "You don't have permission to do this!");
		} else {
			boolean state = plugin.conf.getBoolean(GlobalSettingKey.ENABLE_STATS);
			plugin.conf.set(GlobalSettingKey.ENABLE_STATS, !state);
			plugin.conf.save();
			state = plugin.conf.getBoolean(GlobalSettingKey.ENABLE_STATS);
			SmoothSleep.metrics = state ? new Metrics(plugin) : null;
			sender.sendMessage(state ? (ChatColor.GREEN + "Enabled SmoothSleep Metrics") : (ChatColor.GOLD + "Disabled SmoothSleep Metrics"));
		}
		return true;
	}
}