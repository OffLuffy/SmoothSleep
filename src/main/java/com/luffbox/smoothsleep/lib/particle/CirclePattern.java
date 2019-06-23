package com.luffbox.smoothsleep.lib.particle;

import org.bukkit.Location;
import org.bukkit.Particle;

public class CirclePattern implements ParticlePattern {
	@Override
	public void spawnParticle(Particle type, Location ref, double radius, double progress) {
		int amount = 10;
		double inc = (2 * Math.PI) / amount;
		for (int i = 0; i < amount; i++) {
			double a = inc * i + progress * 2;
			ref.getWorld().spawnParticle(type,
					ref.getX() + (radius * Math.cos(a)),
					ref.getY() + 0.1,
					ref.getZ() + (radius * Math.sin(a)),
					1);
		}
	}
}
