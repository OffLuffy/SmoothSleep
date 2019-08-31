package com.luffbox.smoothsleep.lib;

import com.luffbox.lib.LuffSerializable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SleepRewardEffect extends LuffSerializable {

	private static final String DUR_PATH = "duration-ticks";
	private static final String AMP_PATH = "amplifier";
	private static final String REQ_PATH = "require-horus";
	private static final String AMB_PATH = "ambient";
	private static final String ICN_PATH = "show-icon";

	private PotionEffectType pet;
	private int duration;
	private int amplifier;
	private int requireHours;
	private boolean ambient;
	private boolean showIcon;
	private boolean fx = true; // Enable particles always

	public SleepRewardEffect(PotionEffectType type, ConfigurationSection sect) {
		pet = type;
		deserialize(sect);
	}

	public PotionEffectType getType() { return pet; }
	public int getDuration() { return duration; }
	public int getAmplifier() { return amplifier; }
	public int getRequireHours() { return requireHours; }
	public PotionEffect getEffect() { return new PotionEffect(pet, duration, amplifier, ambient, fx, showIcon); }

	protected ConfigurationSection serialize() {
		MemoryConfiguration sect = new MemoryConfiguration();
		sect.set(DUR_PATH, duration);
		sect.set(AMP_PATH, amplifier + 1);
		sect.set(REQ_PATH, requireHours);
		sect.set(AMB_PATH, ambient);
		sect.set(ICN_PATH, showIcon);
		return sect;
	}

	protected void deserialize(ConfigurationSection sect) {
		duration = sect.getInt(DUR_PATH, 0);
		amplifier = sect.getInt(AMP_PATH, 1) - 1;
		requireHours = sect.getInt(REQ_PATH, 0);
		ambient = sect.getBoolean(AMB_PATH, true);
		showIcon = sect.getBoolean(ICN_PATH, true);
	}
}
