package com.luffbox.smoothsleep.data;

import com.luffbox.smoothsleep.SmoothSleep;
import com.luffbox.smoothsleep.lib.TickHelper;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class WorldData {

	private World world;
	private WorldConfig config;
	private double timescale = 1.0;
	private double tickRemain = 0.0;
	private TickHelper tickHelper;

	public boolean wasNight = isNight();

	public WorldData(SmoothSleep plugin, World world) {
		if (world == null) throw new NullPointerException();
		this.world = world;
		this.config = new WorldConfig(plugin, world);
		this.tickHelper = new TickHelper(this);
	}

	public World getWorld() { return world; }

	public WorldConfig getWorldConfig() { return config; }

	public void timestep() {
		long wtime = getTime();
		tickRemain += timescale;
		int ticks = (int) tickRemain - 1;
		boolean toMorning = wtime + ticks + 1 >= Constants.NIGHT_END;
		if (toMorning) { ticks = (int) (Constants.NIGHT_END - wtime); }

		tickHelper.tick(ticks); // Tick time, weather, randomTicks

		for (PlayerData pd : getPlayerData()) { // Tick player timers, process morning

		}
	}

	public long getTime() { return world.getTime(); }

	public boolean isNight() {
		return world != null
				&& world.getTime() >= Constants.NIGHT_START
				&& world.getTime() <= Constants.NIGHT_END;
	}

	public List<Player> getPlayers() { return world.getPlayers(); }
	public List<PlayerData> getPlayerData() {
		List<PlayerData> data = new ArrayList<>();
		for (Player p : world.getPlayers()) {
			PlayerData pd = SmoothSleep.data.getPlayerData(p);
			if (pd != null) { data.add(pd); }
		}
		return data;
	}

	public List<Player> getSleepers() {
		List<Player> sleepers = new ArrayList<>();
		for (Player p : world.getPlayers()) { if (p.isSleeping()) { sleepers.add(p); } }
		return sleepers;
	}

	public List<PlayerData> getSleeperData() {
		List<PlayerData> data = new ArrayList<>();
		for (Player p : world.getPlayers()) {
			if (!p.isSleeping()) continue;
			PlayerData pd = SmoothSleep.data.getPlayerData(p);
			if (pd != null) { data.add(pd); }
		}
		return data;
	}

	public boolean hasSleepers() {
		for (Player p : world.getPlayers()) { if (p.isSleeping()) { return true; } }
		return false;
	}

}
