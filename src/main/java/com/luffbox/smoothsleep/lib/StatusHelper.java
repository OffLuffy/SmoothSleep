package com.luffbox.smoothsleep.lib;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import static org.bukkit.potion.PotionEffectType.*;

import java.util.HashSet;
import java.util.Set;

public class StatusHelper {
	public static final Set<PotionEffectType> GOOD = new HashSet<PotionEffectType>() {{
		add(ABSORPTION);
		add(FIRE_RESISTANCE);
		add(FAST_DIGGING);
		add(HEALTH_BOOST);
		add(HEAL);
		add(INVISIBILITY);
		add(JUMP);
		add(LUCK);
		add(NIGHT_VISION);
		add(REGENERATION);
		add(DAMAGE_RESISTANCE);
		add(SATURATION);
		add(SLOW_FALLING);
		add(SPEED);
		add(INCREASE_DAMAGE);
		add(WATER_BREATHING);
	}};

	public static final Set<PotionEffectType> BAD = new HashSet<PotionEffectType>() {{
		add(UNLUCK);
		add(BLINDNESS);
		add(POISON);
		add(HUNGER);
		add(HARM);
		add(LEVITATION);
		add(SLOW_DIGGING);
		add(CONFUSION);
		add(SLOW);
		add(WITHER);
		add(WEAKNESS);
	}};

	public static void healGood(Player player) {
		for (PotionEffect pe : player.getActivePotionEffects()) {
			if (GOOD.contains(pe.getType())) {
				player.removePotionEffect(pe.getType());
			}
		}
	}

	public static void healBad(Player player) {
		for (PotionEffect pe : player.getActivePotionEffects()) {
			if (BAD.contains(pe.getType())) {
				player.removePotionEffect(pe.getType());
			}
		}
	}
}
