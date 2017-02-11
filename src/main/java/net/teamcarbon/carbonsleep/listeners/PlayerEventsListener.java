package net.teamcarbon.carbonsleep.listeners;

import net.teamcarbon.carbonsleep.CarbonSleep;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;

public class PlayerEventsListener implements Listener {

	private CarbonSleep plugin;
	public PlayerEventsListener(CarbonSleep p) { plugin = p; }

	@EventHandler(ignoreCancelled = true)
	public void enterBed(PlayerBedEnterEvent e) {
		Player p = e.getPlayer();
		World w = e.getBed().getWorld();
		Location l = e.getBed().getLocation();
		if (plugin.worldEnabled(w)) {
			plugin.addSleeper(e.getPlayer());
			if (plugin.titleApi == null) {
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "title " + p.getName() + " times 0 20 20");
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void leaveBed(PlayerBedLeaveEvent e) {
		Player p = e.getPlayer();
		World w = e.getBed().getWorld();
		Location l = e.getBed().getLocation();
		if (plugin.worldEnabled(w)) {
			plugin.removeSleeper(e.getPlayer());
		}
	}

}
