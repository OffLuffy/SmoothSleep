package com.luffbox.smoothsleep.lib.particle;

import org.bukkit.Location;
import org.bukkit.Particle;

public class SpiralPattern implements ParticlePattern {
	@Override
	public void spawnParticle(Particle type, Location ref, double radius, double progress) {
		int amount = 20;
		double inc = (2 * Math.PI) / amount;
		for (int i = 0; i < amount; i++) {
			double a = inc * i + progress * 2;
			ref.getWorld().spawnParticle(type,
					ref.getX() + (radius * Math.cos(a)),
					ref.getY() + (2.0 / amount * i),
					ref.getZ() + (radius * Math.sin(a)),
					1);
			ref.getWorld().spawnParticle(type,
					ref.getX() + (-radius * Math.cos(a)),
					ref.getY() + (2.0 / amount * i),
					ref.getZ() + (-radius * Math.sin(a)),
					1);
		}
	}
}
