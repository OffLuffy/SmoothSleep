package com.luffbox.smoothsleep.events;

import com.luffbox.smoothsleep.data.WorldData;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.ArrayList;
import java.util.List;

public class NightEndEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private final WorldData worldData;
	private final List<Player> fullySlept;
	public NightEndEvent(WorldData worldData, List<Player> fullySlept) {
		this.worldData = worldData;
		this.fullySlept = fullySlept;
	}
	public World getWorld() { return worldData.getWorld(); }
	public WorldData getWorldData() { return worldData; }
	public List<Player> getFullySleptPlayers() { return new ArrayList<>(fullySlept); }
	public HandlerList getHandlers() { return handlers; }
	public static HandlerList getHandlerList() { return handlers; }
}
