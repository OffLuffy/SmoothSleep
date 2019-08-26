package com.luffbox.smoothsleep.events;

import com.luffbox.smoothsleep.data.WorldData;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class NightBeginEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private final WorldData worldData;
	public NightBeginEvent(WorldData worldData) {
		this.worldData = worldData;
	}
	public World getWorld() { return worldData.getWorld(); }
	public WorldData getWorldData() { return worldData; }
	public HandlerList getHandlers() { return handlers; }
	public static HandlerList getHandlerList() { return handlers; }
}
