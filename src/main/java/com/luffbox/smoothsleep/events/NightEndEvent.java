package com.luffbox.smoothsleep.events;

import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class NightEndEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private World w;
	public NightEndEvent(World world) {
		w = world;
	}
	public World getWorld() { return w; }
	public HandlerList getHandlers() { return handlers; }
	public static HandlerList getHandlerList() { return handlers; }
}
