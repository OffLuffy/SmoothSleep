package com.luffbox.smoothsleep.events;

import com.luffbox.smoothsleep.WorldData;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class NightStartEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private World w;
	private WorldData wd;
	public NightStartEvent(World world, WorldData data) {
		w = world;
		wd = data;
	}
	public World getWorld() { return w; }
	public WorldData getWorldData() { return wd; }
	public HandlerList getHandlers() { return handlers; }
	public static HandlerList getHandlerList() { return handlers; }
}
