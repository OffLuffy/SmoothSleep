package com.luffbox.smoothsleep.data;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public enum WorldConfigKey {
	MIN_NIGHT_SPEED("min-night-speed", double.class),
	MAX_NIGHT_SPEED("max-night-speed", double.class),
	SPEED_CURVE("speed-curve", double.class),

	INSTANT_DAY_ENABLED("instant-day.enabled", boolean.class),
	INSTANT_DAY_RATIO("instant-day.ratio", double.class),

	TICK_RAND_TICK("tick-settings.random-tick", boolean.class),
	RAND_TICK_MAX("tick-settings.random-tick-limit", int.class),
	TICK_WEATHER("tick-settings.weather", boolean.class),

	CLEAR_WEATHER("morning-settings.clear-weather", boolean.class),
	HEAL_VILLAGERS("morning-settings.heal-sleeping-villagers", boolean.class),

	PHANTOM_RESET_RATIO("phantoms.reset-all-ratio", double.class),
	PHANTOM_SPAWN_CHANCE("phantoms.spawn-success-chance", double.class),
	;

	public final String key;
	public final Class type;
	WorldConfigKey(String key, Class type) { this.key = key; this.type = type; }

	public static List<WorldConfigKey> valuesByType(Class type) {
		List<WorldConfigKey> keys = new ArrayList<>();
		for (WorldConfigKey key : values()) { if (key.type == type) { keys.add(key); } }
		return keys;
	}

	@Override
	public String toString() { return key; }

}
