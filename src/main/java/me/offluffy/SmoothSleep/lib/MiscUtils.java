package me.offluffy.SmoothSleep.lib;

public class MiscUtils {

	public static double remapValue(boolean clamp, double oldMin, double oldMax, double newMin, double newMax, double value) {
		if (clamp) {
			if (value >= oldMax) return newMax;
			if (value <= oldMin) return newMin;
		}
		return (((newMax - newMin) * (value - oldMin)) / (oldMax - oldMin)) + newMin;
	}

	public static double clamp(double val, double min, double max) { return Math.min(max, Math.max(min, val)); }
	public static int clamp(int val, int min, int max) { return Math.min(max, Math.max(min, val)); }

	public static int ticksTo24Hours(long ticks) {
		ticks += 6000;
		int hours = (int) ticks / 1000;
		return (hours >= 24 ? hours - 24 : hours);
	}
	public static int ticksTo12Hours(long ticks) {
		int hours = ticksTo24Hours(ticks);
		return hours > 12 ? hours - 12: hours;
	}
	public static int ticksToMinutes(long ticks) {
		return (int) ((ticks % 1000) / 16.66);
	}
	public static boolean ticksIsAM(long ticks) {
		return ticksTo24Hours(ticks) < 12;
	}

}
