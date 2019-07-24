package com.luffbox.smoothsleep.tasks;

import com.luffbox.smoothsleep.PlayerData;
import com.luffbox.smoothsleep.SmoothSleep;
import com.luffbox.smoothsleep.lib.particle.*;
import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitRunnable;

import static com.luffbox.smoothsleep.lib.ConfigHelper.WorldSettingKey.*;

/**
 * The task responsible for spawning particles around a player when
 * they wake up. Each time the task runs, it will produce one particle.
 * How many particles have been spawned and how many will be spawned
 * will be stored in the PlayerTimers in their PlayerData object.
 * @see com.luffbox.smoothsleep.PlayerData
 */
public class WakeParticlesTask extends BukkitRunnable {

	private SmoothSleep pl;
	private PlayerData pd;
	private int complete = 0, target;
	private double radius;
	private Particle type;
	private ParticlePatternType pattType;
	private ParticlePattern patt;
	public WakeParticlesTask(SmoothSleep plugin, PlayerData plrData) {
		pl = plugin;
		pd = plrData;
		target = pd.worldConf().getInt(PARTICLE_AMOUNT);
		type = pd.worldConf().getParticle(PARTICLE_TYPE);
		radius = pd.worldConf().getDouble(PARTICLE_RADIUS);
		pattType = pd.worldConf().getPatternType(PARTICLE_PATTERN);
		switch (pattType) {
			case RANDOM: patt = new RandomPattern(); break;
			case CIRCLE: patt = new CirclePattern(); break;
			case SPIRAL: patt = new SpiralPattern(); break;
		}
	}

	@Override
	public void run() {
		if (pd == null || pd.worldConf() == null) { cancel(); return; }
		if (!pd.worldConf().getBoolean(PARTICLE_ENABLED)) { cancel(); return; }
		patt.spawnParticle(type, pd.getPlayer().getLocation(), radius, (double) complete / (double) target);
		if (++complete >= target) {
			cancel();
		}
	}
}
