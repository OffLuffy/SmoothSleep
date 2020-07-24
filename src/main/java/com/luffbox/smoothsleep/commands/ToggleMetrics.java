package com.luffbox.smoothsleep.commands;

import com.luffbox.smoothsleep.SmoothSleep;
import com.luffbox.smoothsleep.lib.ConfigHelper.GlobalSettingKey;
import com.luffbox.smoothsleep.lib.TabExecutor;
import org.bstats.bukkit.Metrics;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ToggleMetrics implements TabExecutor {

	private final SmoothSleep pl;

	public ToggleMetrics(SmoothSleep plugin) { pl = plugin; }

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.hasPermission("smoothsleep.metrics")) {
			sender.sendMessage(ChatColor.RED + "You don't have permission to do this!");
		} else {
			boolean state = pl.data.config.getBoolean(GlobalSettingKey.ENABLE_STATS);
			pl.data.config.set(GlobalSettingKey.ENABLE_STATS, !state);
			pl.data.config.save();
			state = pl.data.config.getBoolean(GlobalSettingKey.ENABLE_STATS);
			SmoothSleep.metrics = state ? new Metrics(pl, SmoothSleep.STAT_ID) : null;
			sender.sendMessage(state ? (ChatColor.GREEN + "Enabled SmoothSleep Metrics") : (ChatColor.GOLD + "Disabled SmoothSleep Metrics"));
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		return null;
	}
}