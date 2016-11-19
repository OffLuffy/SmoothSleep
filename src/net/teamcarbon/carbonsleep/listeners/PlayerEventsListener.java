package net.teamcarbon.carbonsleep.listeners;

import net.teamcarbon.carbonlib.Misc.Messages;
import net.teamcarbon.carbonsleep.CarbonSleep;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class PlayerEventsListener implements Listener {

	@EventHandler(ignoreCancelled = true)
	public void enterBed(PlayerBedEnterEvent e) {
		World w = e.getBed().getWorld();
		Location l = e.getBed().getLocation();
		if (CarbonSleep.worldEnabled(w)) {
			e.setCancelled(true);
			CarbonSleep.playSleepAnim(e.getPlayer(), l);
			if (CarbonSleep.inst().getConf().getBoolean("worlds." + w.getName() + ".enable-sleeper-updates", true)) {
				int sleepers = CarbonSleep.getSleeperCount(w) + 1;
				int wakers = CarbonSleep.getWakerCount(w) - (e.getPlayer().isSleeping() ? 0 : 1);
				for (Player p : w.getPlayers()) {
					p.sendMessage(Messages.Clr.trans("&a" + sleepers + "/" + wakers + " &bplayers sleeping! &7(Night speeds up if more players sleep!)"));
				}
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void leaveBed(PlayerBedLeaveEvent e) {
		World w = e.getBed().getWorld();
		Location l = e.getBed().getLocation();
		if (CarbonSleep.worldEnabled(w) && CarbonSleep.isSleeping(e.getPlayer())) {
			CarbonSleep.stopSleepAnim(e.getPlayer());
			if (CarbonSleep.inst().getConf().getBoolean("worlds." + w.getName() + ".enable-sleeper-updates", true)) {
				int sleepers = CarbonSleep.getSleeperCount(w) - 1;
				int wakers = CarbonSleep.getWakerCount(w) - (e.getPlayer().isSleeping() ? 0 : 1);
				for (Player p : w.getPlayers()) {
					p.sendMessage(Messages.Clr.trans("&a" + sleepers + "/" + wakers + " &bplayers sleeping! &7(Night speeds up if more players sleep!)"));
				}
			}
		}
	}

	/*@EventHandler(ignoreCancelled = true)
	public void sneak(PlayerToggleSneakEvent e) { // Sneaking will cancel sleeping
		World w = e.getPlayer().getWorld();
		if (CarbonSleep.worldEnabled(w) && CarbonSleep.isSleeping(e.getPlayer())) {
			CarbonSleep.stopSleepAnim(e.getPlayer());
			if (CarbonSleep.inst().getConf().getBoolean("worlds." + w.getName() + ".enable-sleeper-updates", true)) {
				int sleepers = CarbonSleep.getSleeperCount(w) - 1;
				int wakers = CarbonSleep.getWakerCount(w) - (e.getPlayer().isSleeping() ? 0 : 1);
				for (Player p : w.getPlayers()) {
					p.sendMessage(Messages.Clr.trans("&a" + sleepers + "/" + wakers + " &bplayers sleeping! &7(Night speeds up if more players sleep!)"));
				}
			}
		}
	}*/

}
