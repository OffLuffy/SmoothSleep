package net.teamcarbon.carbonsleep.listeners;

import net.teamcarbon.carbonlib.Misc.Messages;
import net.teamcarbon.carbonsleep.CarbonSleep;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;

public class PlayerEventsListener implements Listener {

	@EventHandler
	public void enterBed(PlayerBedEnterEvent e) {
		World w = e.getBed().getWorld();
		if (CarbonSleep.worldEnabled(w)) {
			if (CarbonSleep.getConf().getBoolean("worlds." + w.getName() + ".enable-sleeper-updates", true)) {
				int sleepers = CarbonSleep.getSleeperCount(w) + 1;
				int wakers = CarbonSleep.getWakerCount(w) - (e.getPlayer().isSleeping() ? 0 : 1);
				for (Player p : w.getPlayers()) {
					p.sendMessage(Messages.Clr.trans("&a" + sleepers + "/" + wakers + " &bplayers sleeping! &7(Night speeds up if more players sleep!)"));
				}
			}
		}
	}

}
