package com.luffbox.smoothsleep.lib;

import com.luffbox.smoothsleep.data.WorldConfig;
import com.luffbox.smoothsleep.data.WorldConfigKey;
import com.luffbox.smoothsleep.data.WorldData;
import org.bukkit.GameRule;
import org.bukkit.World;

public class TickHelper {

	private World w;
	private WorldData wd;
	private WorldConfig ws;
	private int randTickSpeed;

	public boolean tickWeather, tickRandom;

	public TickHelper(WorldData worldData) {
		if (worldData == null) { throw new NullPointerException(); }
		this.wd = worldData;
		this.w = worldData.getWorld();
		this.ws = worldData.getWorldConfig();
		tickWeather = ws.getBoolean(WorldConfigKey.ACCEL_WEATHER);
		tickRandom = ws.getBoolean(WorldConfigKey.ACCEL_RAND_TICK);
		Integer rts = w.getGameRuleValue(GameRule.RANDOM_TICK_SPEED);
		if (rts != null) { randTickSpeed = rts; }
	}

	public void tick(int ticks) {
		w.setTime(w.getTime() + ticks);
		if (tickWeather) { w.setWeatherDuration(w.getWeatherDuration() - ticks); }
		if (tickRandom) {
			int rts = Math.min(randTickSpeed * ticks, ws.getInt(WorldConfigKey.MAX_RAND_TICK));
			w.setGameRule(GameRule.RANDOM_TICK_SPEED, rts);
		}
	}

	public void reset() {
		w.setGameRule(GameRule.RANDOM_TICK_SPEED, randTickSpeed);
	}
}
