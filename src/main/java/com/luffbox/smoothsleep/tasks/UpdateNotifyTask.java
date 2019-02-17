package com.luffbox.smoothsleep.tasks;

import com.luffbox.smoothsleep.SmoothSleep;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class UpdateNotifyTask extends BukkitRunnable {

	private SmoothSleep pl;
	private Player plr;
	public UpdateNotifyTask(SmoothSleep plugin, Player player) {
		pl = plugin;
		plr = player;
	}

	@Override
	public void run() {
		plr.sendMessage(ChatColor.AQUA + "Update available for SmoothSleep!");
		plr.sendMessage(ChatColor.AQUA + "https://www.spigotmc.org/resources/32043/");
	}
}
