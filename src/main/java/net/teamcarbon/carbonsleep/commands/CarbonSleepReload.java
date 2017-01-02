package net.teamcarbon.carbonsleep.commands;

import net.teamcarbon.carbonsleep.CarbonSleep;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CarbonSleepReload implements CommandExecutor {

	private CarbonSleep plugin;
	public CarbonSleepReload(CarbonSleep p) { plugin = p; }

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.hasPermission("carbonsleep.reload")) {
			sender.sendMessage(ChatColor.RED + "You don't have permission to do this!");
		} else {
			plugin.reload();
			sender.sendMessage(ChatColor.GREEN + "Reloaded CarbonSleep");
		}
		return true;
	}
}
