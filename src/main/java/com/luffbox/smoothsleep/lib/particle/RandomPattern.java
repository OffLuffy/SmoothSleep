package com.luffbox.smoothsleep.lib.particle;

import org.bukkit.Location;
import org.bukkit.Particle;

// Guess this one turned out a bit crazy looking, but I thought a random sphere pattern would look better than a cube
// Credit: https://karthikkaranth.me/blog/generating-random-points-in-a-sphere/
public class RandomPattern implements ParticlePattern {
	@Override
	public void spawnParticle(Particle type, Location ref, double radius, double progress) {
		for (int i = 0; i < 3; i++) {
			double theta = Math.random() * 2.0 * Math.PI;
			double phi = Math.acos(2.0 * Math.random() - 1.0);
			double sinTheta = Math.sin(theta);
			double cosTheta = Math.cos(theta);
			double sinPhi = Math.sin(phi);
			double cosPhi = Math.cos(phi);
			double r = Math.cbrt(radius * 3);
			double x = r * sinPhi * cosTheta;
			double y = (r * sinPhi * sinTheta) + 1.5;
			double z = r * cosPhi;
			ref.getWorld().spawnParticle(type, ref.getX() + x, ref.getY() + y, ref.getZ() + z, 1);
		}
	}
}
