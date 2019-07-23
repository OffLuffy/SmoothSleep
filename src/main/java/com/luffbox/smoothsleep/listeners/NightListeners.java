package com.luffbox.smoothsleep.listeners;

import com.luffbox.smoothsleep.SmoothSleep;
import com.luffbox.smoothsleep.events.NightStartEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NightListeners implements Listener {

	private SmoothSleep p;

	public NightListeners(SmoothSleep plugin) { p = plugin; }

	@EventHandler
	public void onNight(NightStartEvent e) {
		if (e.getWorld().getPlayers().isEmpty()) return;
		SmoothSleep.logDebug("Updating perms for players in world: " + e.getWorld().getName());
		for (Player pl : e.getWorld().getPlayers()) {
			p.data.getPlayerData(pl).updateIgnorePerm();
		}
	}
}
