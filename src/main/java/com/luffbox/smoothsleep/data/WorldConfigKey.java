package com.luffbox.smoothsleep.data;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public enum WorldConfigKey {
	MIN_NIGHT_MULT("min-night-speed-mult", double.class),
	MAX_NIGHT_MULT("max-night-speed-mult", double.class),
	SPEED_CURVE("night-speed-curve", double.class),
	ACCEL_RAND_TICK("accelerate-random-tick", boolean.class),
	MAX_RAND_TICK("max-random-tick", int.class),
	ACCEL_WEATHER("accelerate-weather", boolean.class),
	MORNING_SOUND("morning-sound", String.class),
	CLEAR_WEATHER("clear-weather-when-morning", boolean.class),
	INSTANT_DAY("instant-day-if-all-sleeping", boolean.class),
	IGNORE_AFK("essentials-settings.ignore-afk", boolean.class),
	IGNORE_VANISH("essentials-settings.ignore-vanish", boolean.class),
	HEAL_VILLAGERS("heal-slept-villagers", boolean.class),
	PHANTOM_RESET_RATIO("phantoms.reset-all-ratio", double.class),
	PHANTOM_SPAWN_CHANCE("phantoms.spawn-success-chance", double.class),

	HEAL_AMOUNT("replenish-settings.heal-amount", int.class),
	HEAL_TICKS("replenish-settings.ticks-per-heal", int.class),
	HEAL_AWAKE("replenish-settings.heal-awake", boolean.class),
	FEED_AMOUNT("replenish-settings.feed-amount", int.class),
	FEED_TICKS("replenish-settings.ticks-per-feed", int.class),
	FEED_AWAKE("replenish-settings.feed-awake", boolean.class),
	HEAL_NEG_STATUS("replenish-settings.heal-negative-statuses", boolean.class),
	HEAL_POS_STATUS("replenish-settings.heal-positive-statuses", boolean.class),
	HOURS_NEG_STATUS("replenish-settings.hours-to-heal-negative", int.class),
	HOURS_POS_STATUS("replenish-settings.hours-to-heal-positive", int.class),

	REWARD_EFFECT_ENABLED("sleep-rewards.potion-effects.enabled", boolean.class),
	REWARD_EFFECT_SLEEP_HOURS("sleep-rewards.potion-effects.required-hours-sleep", int.class),
	REWARD_EFFECT_PARTICLES("sleep-rewards.potion-effects.show-effect-particles", boolean.class),
	REWARD_EFFECT_LIST("sleep-rewards.potion-effects.effects", ConfigurationSection.class),

	PARTICLE_ENABLED("morning-particle-options.enabled", boolean.class),
	PARTICLE_TYPE("morning-particle-options.particle", String.class),
	PARTICLE_AMOUNT("morning-particle-options.amount", int.class),
	PARTICLE_RADIUS("morning-particle-options.radius", double.class),
	PARTICLE_DELAY("morning-particle-options.delay-ticks", int.class),
	PARTICLE_PATTERN("morning-particle-options.pattern", String.class),

	TITLES_ENABLED("titles.enabled", boolean.class),
	SLEEP_TITLE("titles.sleep-title", String.class),
	SLEEP_SUBTITLE("titles.sleep-subtitle", String.class),
	MORNING_TITLE("titles.morning-title", String.class),
	MORNING_SUBTITLE("titles.morning-subtitle", String.class),
	TITLE_STAY("titles.stay-ticks", int.class),
	TITLE_FADE("titles.fade-ticks", int.class),

	ACTIONBAR_ENABLED("action-bar.enabled", boolean.class),
	ACTIONBAR_WAKERS("action-bar.show-if-awake", boolean.class),
	ACTIONBAR_TITLE("action-bar.title", String.class),

	BOSSBAR_ENABLED("boss-bar.enabled", boolean.class),
	BOSSBAR_WAKERS("boss-bar.show-if-awake", boolean.class),
	BOSSBAR_COLOR("boss-bar.color", String.class),
	BOSSBAR_TITLE("boss-bar.title", String.class),
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
