package net.teamcarbon.carbonsleep.listeners;

import net.teamcarbon.carbonsleep.events.MorningEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MorningEventListener implements Listener {

	@EventHandler
	public void morning(MorningEvent e) {
		boolean greet = e.isGreetingEnabled();
		for (Player p : e.getWorld().getPlayers()) {
			if (greet && (!e.onlyGreetSleepers() || e.getSleepers().contains(p))) {
				p.sendMessage(e.getGreeting());
			}
		}
	}

}
