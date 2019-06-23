package com.luffbox.smoothsleep.lib.particle;

import org.bukkit.Location;
import org.bukkit.Particle;

public interface ParticlePattern {
	void spawnParticle(Particle type, Location ref, double radius, double progress);
}
