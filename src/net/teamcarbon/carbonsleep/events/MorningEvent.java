package net.teamcarbon.carbonsleep.events;

import net.teamcarbon.carbonlib.Misc.Messages;
import net.teamcarbon.carbonsleep.CarbonSleep;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Called when CarbonSleep has cycled the time to the start of a new day.
 * This will only be called if the time was accelerated when it hits the
 * last tick players can be in their beds.
 */
public class MorningEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	private World w;
	private List<Player> s = new ArrayList<>();

	public MorningEvent(World world, Player ... sleepers) {
		w = world;
		Collections.addAll(s, sleepers);
	}

	public MorningEvent(World world, List<Player> sleepers) {
		w = world;
		s = new ArrayList<>(sleepers);
	}

	public boolean isGreetingEnabled() {
		return CarbonSleep.inst().getConf().getBoolean("worlds." + w.getName() + ".morning-greeting-enabled", true);
	}
	public boolean onlyGreetSleepers() {
		return CarbonSleep.inst().getConf().getBoolean("worlds." + w.getName() + ".only-greet-sleepers", false);
	}
	public String getGreeting() {
		String g = CarbonSleep.inst().getConf().getString("worlds." + w.getName() + ".morning-greeting", "&bDawn of a new day has arrived!");
		g = Messages.Clr.trans(g);
		if (g.isEmpty()) return null;
		return g;
	}

	public World getWorld() { return w; }
	public List<Player> getSleepers() { return s; }

	public HandlerList getHandlers() { return handlers; }
	public static HandlerList getHandlerList() { return handlers; }
}
