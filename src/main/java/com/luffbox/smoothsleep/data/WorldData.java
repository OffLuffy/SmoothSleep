package com.luffbox.smoothsleep.data;

import com.luffbox.smoothsleep.SmoothSleep;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class WorldData {

	private World world;
	private WorldConfig config;
	private double timescale = 1.0;
	private double tickRemain = 0.0;

	public boolean wasNight = isNight();

	public WorldData(SmoothSleep plugin, World world) {
		if (world == null) throw new NullPointerException();
		this.world = world;
		this.config = new WorldConfig(plugin, world);
	}

	public World getWorld() { return world; }

	public WorldConfig getWorldConfig() { return config; }

	public long getTime() { return world.getTime(); }

	public boolean isNight() {
		return world != null
				&& world.getTime() >= Constants.NIGHT_START
				&& world.getTime() <= Constants.NIGHT_END;
	}

	public List<Player> getPlayers() { return world.getPlayers(); }

	public List<Player> getSleepers() {
		List<Player> sleepers = new ArrayList<>();
		world.getPlayers().forEach(plr -> { if (plr.isSleeping()) { sleepers.add(plr); } });
		return sleepers;
	}

	public boolean hasSleepers() {
		for (Player plr : world.getPlayers()) { if (plr.isSleeping()) { return true; } }
		return false;
	}

}
