package me.offluffy.SmoothSleep.lib;

public class MiscUtils {

	public static double remapValue(boolean clamp, double oldMin, double oldMax, double newMin, double newMax, double value) {
		if (clamp) {
			if (value >= oldMax) return newMax;
			if (value <= oldMin) return newMin;
		}
		return (((newMax - newMin) * (value - oldMin)) / (oldMax - oldMin)) + newMin;
	}

	public static String ticksToTime(long ticks) {
		ticks += 6000; // Offset 0 ticks to = 6AM
		int hours = (int)(ticks / 1000), minutes = (int)((ticks % 1000) / 16.66);
		return (hours > 12 ? hours > 24 ? hours - 24 : hours-12 : hours) + ":"
				+ (minutes < 10 ? "0" : "") + minutes + (hours >= 12 && hours < 24 ? " PM" : " AM");
	}

}
