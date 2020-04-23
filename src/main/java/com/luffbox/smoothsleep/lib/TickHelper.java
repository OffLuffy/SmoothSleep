package com.luffbox.smoothsleep.lib;

import org.bukkit.GameRule;
import org.bukkit.World;

public class TickHelper {

	private final World w;
	private final ConfigHelper.WorldSettings ws;
	private final TickOptions options;
	private int randTickSpeed;

	public TickHelper(World world, ConfigHelper.WorldSettings settings, TickOptions options) {
		if (world == null) { throw new NullPointerException(); }
		this.w = world;
		this.ws = settings;
		this.options = options;
		Integer rts = world.getGameRuleValue(GameRule.RANDOM_TICK_SPEED);
		if (rts != null) { randTickSpeed = rts; }
	}

	public void tick(int ticks) {
		w.setTime(w.getTime() + ticks);
		if (options.weather) { w.setWeatherDuration(w.getWeatherDuration() - ticks); }
		if (options.randomTick) {
			int rts = Math.min(randTickSpeed * ticks, ws.getInt(ConfigHelper.WorldSettingKey.MAX_RAND_TICK));
			w.setGameRule(GameRule.RANDOM_TICK_SPEED, rts);
		}
	}

	public void reset() {
		w.setGameRule(GameRule.RANDOM_TICK_SPEED, randTickSpeed);
	}

}
