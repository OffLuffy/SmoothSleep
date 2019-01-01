package me.offluffy.SmoothSleep.listeners;

import me.offluffy.SmoothSleep.SmoothSleep;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerEventsListener implements Listener {

	private SmoothSleep plugin;
	public PlayerEventsListener(SmoothSleep p) { plugin = p; }

	@EventHandler(ignoreCancelled = true)
	public void enterBed(PlayerBedEnterEvent e) {
		if (!plugin.enabled) return;
		Player p = e.getPlayer();
		World w = e.getBed().getWorld();
		if (plugin.worldEnabled(w) && plugin.isNight(w)) { plugin.addSleeper(p, w.getTime()); }
	}

	@EventHandler(ignoreCancelled = true)
	public void leaveBed(PlayerBedLeaveEvent e) {
		if (!plugin.enabled) return;
		Player p = e.getPlayer();
		World w = e.getBed().getWorld();
		if (plugin.worldEnabled(w) && plugin.isNight(w)) { plugin.removeSleeper(p); }
	}

	@EventHandler(ignoreCancelled = true)
	public void playerJoin(PlayerJoinEvent e) {
		if (!plugin.enabled) return;
		Player p = e.getPlayer();
		World w = p.getWorld();
		if (plugin.worldEnabled(w) && plugin.isNight(w) && p.hasPermission(plugin.PERM_IGNORE)) { plugin.addSleepIgnored(w, p); }
	}
}
