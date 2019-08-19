package com.luffbox.smoothsleep;

import com.luffbox.smoothsleep.lib.ConfigHelper;
import com.luffbox.smoothsleep.lib.MiscUtils;
import com.luffbox.smoothsleep.lib.Purgeable;
import com.luffbox.smoothsleep.lib.TickHelper;
import com.luffbox.smoothsleep.tasks.SleepTickTask;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;

/**
 * Contains data about a World that SmoothSleep will use later.
 */
public class WorldData implements Purgeable {

	private SmoothSleep pl;

	private World w;
	private ConfigHelper.WorldSettings ws;
	private TickHelper tickHelper;
	private BukkitTask sleepTickTask;
	private double timescale = 0.0, timeTickRemain;

	public WorldData(SmoothSleep plugin, World world, ConfigHelper.WorldSettings settings) {
		pl = plugin;
		w = world;
		ws = settings;
	}

	public World getWorld() { return w; }

	public Set<Player> getPlayers() { return new HashSet<>(w.getPlayers()); }

	public Set<Player> getSleepers() {
		Set<Player> sleepers = new HashSet<>();
		w.getPlayers().forEach(plr -> {
			if (plr.isSleeping()) sleepers.add(plr);
		});
		return sleepers;
	}

	public boolean hasSleepers() {
		for (Player plr : w.getPlayers()) { if (plr.isSleeping()) { return true; } }
		return false;
	}

	public Set<Player> getWakers() {
		Set<Player> wakers = new HashSet<>();
		w.getPlayers().forEach(plr -> {
			if (!plr.isSleeping()) {
				PlayerData pd = pl.data.getPlayerData(plr);
				if (pd == null || !pd.isSleepingIgnored()) wakers.add(plr);
			}
		});
		return wakers;
	}

	public double getSleepRatio() {
		double s = getSleepers().size(); // Sleepers count
		double a = getWakers().size() + s; // Wakers + Sleepers count
		if (a <= 1 && s >= 1) return 1.0; // Only player is sleeping
		if (a <= 1 || s <= 1) return 0.0; // No one sleeping or online
		return s - 1 / a - 1; // -1 on both = first player sleeping causes min night mult
	}

	public long getTime() { return w.getTime(); }

	public double getTimeRatio() {
		long current = getTime();
		if (current > SmoothSleep.SLEEP_TICKS_END) { return 1.0; }
		if (current < SmoothSleep.SLEEP_TICKS_START) { return 0.0; }
		return MiscUtils.remapValue(true, 0, SmoothSleep.SLEEP_TICKS_DURA, 0.0, 1.0, current - SmoothSleep.SLEEP_TICKS_START);
	}

	public double getTimescale() { return timescale; }

	public void updateTimescale() {
		if (ws.getBoolean(ConfigHelper.WorldSettingKey.INSTANT_DAY) && getWakers().size() <= 0) {
			timescale = SmoothSleep.SLEEP_TICKS_END - getTime();
		} else {
			double crv = getSettings().getDouble(ConfigHelper.WorldSettingKey.SPEED_CURVE);
			double mns = ws.getDouble(ConfigHelper.WorldSettingKey.MIN_NIGHT_MULT);
			double xns = ws.getDouble(ConfigHelper.WorldSettingKey.MAX_NIGHT_MULT);
			double ratio = getSleepRatio();
			timescale = MiscUtils.remapValue(true, 0.0, 1.0, mns, xns, MiscUtils.calcSpeed(crv, getSleepRatio()));
		}
	}

	public void timestep() {
		timeTickRemain += timescale;
		int a = (int) timeTickRemain - 1;
		tickHelper.tick(a, SmoothSleep.SLEEP_TICKS_END);
		timeTickRemain %= 1;
		timestepTimers(timescale);
	}

	public boolean isNight() { return getTime() > SmoothSleep.SLEEP_TICKS_START && getTime() <= SmoothSleep.SLEEP_TICKS_END; }

	public void timestepTimers(double timescale) {
		getPlayers().forEach(plr -> {
			PlayerData pd = pl.data.getPlayerData(plr);
			if (pd != null) {
				pd.tickTimers(timescale);
			}
		});
	}

	public boolean hasAnyWeather() { return w.isThundering() || w.hasStorm(); }

	public void clearWeather() {
		w.setThundering(false);
		w.setStorm(false);
	}

	public ConfigHelper.WorldSettings getSettings() { return ws; }

	private boolean sleepTickRunning() {
		if (sleepTickTask != null && sleepTickTask.isCancelled()) sleepTickTask = null;
		return sleepTickTask != null;
	}

	public void startSleepTick() {
		if (!pl.isEnabled()) return;
		if (sleepTickRunning()) return;
		SmoothSleep.logDebug("#startSleepTick()");
		tickHelper = new TickHelper(w, ws, ws.getTickOptions());
		SleepTickTask stt = new SleepTickTask(pl, this);
		sleepTickTask = stt.runTaskTimer(pl, 0L, 0L);
	}

	public void stopSleepTick() {
		SmoothSleep.logDebug("#stopSleepTick()");
		getPlayers().forEach(plr -> {
			PlayerData pd = pl.data.getPlayerData(plr);
			if (pd != null) { pd.hideBossBar(); }
		});
		if (sleepTickRunning()) { sleepTickTask.cancel(); }
		sleepTickTask = null;
		if (tickHelper != null) tickHelper.reset();
	}

	@Override
	public void purgeData() {}
}
