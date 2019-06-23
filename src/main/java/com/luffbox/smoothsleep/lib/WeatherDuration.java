package com.luffbox.smoothsleep.lib;

import org.bukkit.World;

public class WeatherDuration {

	public World w;

	public WeatherDuration(World w) { this.w = w; }

	public int getStormDuration() { return w.getWeatherDuration(); }
	public int getThunderDuration() { return w.getThunderDuration(); }
	public void decStormDuration(int amount) { w.setWeatherDuration(Math.max(0, getStormDuration() - amount)); }
	public void decThunderDuration(int amount) { w.setThunderDuration(Math.max(0, getThunderDuration() - amount)); }

}
