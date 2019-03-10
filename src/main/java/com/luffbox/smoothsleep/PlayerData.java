package com.luffbox.smoothsleep;

import com.luffbox.smoothsleep.lib.ConfigHelper;
import com.luffbox.smoothsleep.lib.MiscUtils;
import com.luffbox.smoothsleep.lib.PlayerTimers;
import com.luffbox.smoothsleep.lib.Purgeable;
import com.luffbox.smoothsleep.tasks.DeepSleepTask;
import com.luffbox.smoothsleep.tasks.WakeParticlesTask;
import org.apache.commons.lang.text.StrSubstitutor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import static com.luffbox.smoothsleep.lib.ConfigHelper.WorldSettingKey.*;

/**
 * Contains data about a Player that SmoothSleep will use later
 */
public class PlayerData implements Purgeable {

	private SmoothSleep pl;
	private PlayerTimers timers;
	private Player plr;
	private boolean ignorePerm = false;
	private BossBar bar;
	private BukkitTask deepSleepTask;
	private boolean woke = false;

	public PlayerData(SmoothSleep plugin, Player player) {
		SmoothSleep.logDebug("Initializing Player data for " + player.getName());
		pl = plugin;
		plr = player;
		timers = new PlayerTimers();
		update();
		if (plr.isSleeping()) { startDeepSleep(); }
	}

	public ConfigHelper.WorldSettings worldConf() {
		if (pl == null) SmoothSleep.logDebug("worldConf(): Plugin is null");
		else if (pl.data == null) SmoothSleep.logDebug("worldConf(): Plugin data is null");
		else if (pl.data.config == null) SmoothSleep.logDebug("worldConf(): Config is null");
		else if (pl.data.config.worlds == null) SmoothSleep.logDebug("worldConf(): World data cache is null");
		if (plr == null) SmoothSleep.logDebug("worldConf(): Player is null");
		return pl.data.config.worlds.get(plr.getWorld());
	}
	public WorldData worldData() { return pl.data.getWorldData(plr); }

	// Add anything that needs to be checked on join or world change here.
	// Do not call on sleep tick! (Will cause perm check every tick)
	public void update() {
		updateIgnorePerm();
		if (worldConf().getBoolean(BOSSBAR_ENABLED)) updateBossBar();
		updateActionBar();
		updateTitles();
	}

	public void updateUI() {
		updateBossBar();
		updateActionBar();
		updateTitles();
	}

	public void clearTitles() { plr.sendTitle(" ", " ", 0, 0, 0); }
	public void updateTitles() {
		if (!worldConf().getBoolean(TITLES_ENABLED)) return;
		if (!worldData().isNight() || !isSleeping()) {
			if (woke) plr.sendTitle(mrnTitle(), mrnSubtitle(), 0, worldConf().getInt(TITLE_STAY), worldConf().getInt(TITLE_FADE));
			woke = false;
			return;
		}
		plr.sendTitle(slpTitle(), slpSubtitle(), 0, worldConf().getInt(TITLE_STAY), worldConf().getInt(TITLE_FADE));
	}

	public void clearActionBar() { pl.data.actionBarHelper.sendActionBar(plr, " "); }
	public void updateActionBar() {
		if (!worldConf().getBoolean(ACTIONBAR_ENABLED)) { return; }
		if (!isSleeping() && !worldConf().getBoolean(ACTIONBAR_WAKERS)) { return; }
		if (!worldData().isNight() || worldData().getSleepers().isEmpty()) { return; }
		pl.data.actionBarHelper.sendActionBar(plr, actionBarTitle());
	}

	public void updateBossBar() {
		if (!worldConf().getBoolean(BOSSBAR_ENABLED)) { hideBossBar(); return; }
		if (!isSleeping() && !worldConf().getBoolean(BOSSBAR_WAKERS)) { hideBossBar(); return; }
		if (worldData().isNight() && !worldData().getSleepers().isEmpty()) {
			if (bar == null) { createBossBar(); }
			bar.setTitle(bossBarTitle());
			bar.setColor(worldConf().getBarColor(BOSSBAR_COLOR));
			bar.setProgress(worldData().getTimeRatio());
			showBossBar();
		} else { hideBossBar(); }
	}

	public void createBossBar() {
		if (bar == null) {
			bar = pl.getServer().createBossBar("", BarColor.BLUE, BarStyle.SOLID); // For less repetition, create then update
			bar.addPlayer(plr);
		}
	}

	public void showBossBar() { if (bar != null) bar.setVisible(true); }
	public void hideBossBar() { if (bar != null) bar.setVisible(false); }

	// Health and food is clamped to 20 to prevent IllegalArgumentException
	public void tickTimers(double ticks) {
		timers.incAll(ticks);
		if (isSleeping() || worldConf().getBoolean(FEED_AWAKE)) {
			if (!plr.hasPermission("smoothsleep.ignorefeed")) {
				while (timers.getFood() >= worldConf().getInt(FEED_TICKS)) {
					timers.decFood(worldConf().getInt(FEED_TICKS));
					int val = plr.getFoodLevel() + worldConf().getInt(FEED_AMOUNT);
					val = Math.max(Math.min(val, 20), 0);
					plr.setFoodLevel(val);
				}
			}
		}
		if (isSleeping() || worldConf().getBoolean(HEAL_AWAKE)) {
			if (!plr.hasPermission("smoothsleep.ignoreheal")) {
				while (timers.getHeal() >= worldConf().getInt(HEAL_TICKS)) {
					timers.decHeal(worldConf().getInt(HEAL_TICKS));
					double val = plr.getHealth() + worldConf().getInt(HEAL_AMOUNT);
					val = Math.max(Math.min(val, 20), 0);
					plr.setHealth(val);
				}
			}
		}
	}

	public Player getPlayer() { return plr; }

	public boolean isSleeping() { return plr.isSleeping(); }

	public PlayerTimers getTimers() { return timers; }

	// Checks if player has ignore perm or is otherwise ignoring sleepers
	public boolean isSleepingIgnored() {
		boolean ignore = hasIgnorePerm() || plr.isSleepingIgnored();
		if (!ignore && worldConf().getBoolean(IGNORE_VANISH) && pl.data.userHelper.isVanished(plr)) ignore = true;
		if (!ignore && worldConf().getBoolean(IGNORE_AFK) && pl.data.userHelper.isAfk(plr)) ignore = true;
		return ignore;
	}

	// Checks SmoothSleep's ignore permission
	private boolean hasIgnorePerm() { return ignorePerm; }

	// Only check this when player joins or changes world to minimize perm checks.
	public void updateIgnorePerm() { ignorePerm = plr.hasPermission(SmoothSleep.PERM_IGNORE); }

	public boolean deepSleepRunning() {
		if (deepSleepTask != null && deepSleepTask.isCancelled()) deepSleepTask = null;
		return deepSleepTask != null;
	}

	public void startDeepSleep() {
		SmoothSleep.logDebug("Starting deep sleep for " + getPlayer().getName());
		if (deepSleepRunning()) return;
		woke = false;
		DeepSleepTask dst = new DeepSleepTask(pl, plr);
		deepSleepTask =	dst.runTaskTimer(pl, 0L, 40L);
	}

	public void stopDeepSleep() {
		SmoothSleep.logDebug("Stopping deep sleep for " + getPlayer().getName());
		if (deepSleepTask == null) return;
		deepSleepTask.cancel();
		deepSleepTask = null;
		if (woke) { // Only run particles if woken by reaching morning. 'woke' should be false otherwise.
			WakeParticlesTask wpt = new WakeParticlesTask(pl, this);
			wpt.runTaskTimer(pl, 5, worldConf().getInt(PARTICLE_DELAY));
			if (worldConf().getSound(MORNING_SOUND) != null) {
				getPlayer().playSound(getPlayer().getLocation(), worldConf().getSound(MORNING_SOUND), 1.0f, 1.0f);
			}
		} else {
			clearTitles();
			clearActionBar();
		}
		if (worldConf().getBoolean(HEAL_NEG_STATUS)) {
			if ((int) timers.getSlpt() / 1000L >= worldConf().getInt(HOURS_NEG_STATUS)) {
				ConfigHelper.negativeEffects.forEach(pe -> plr.removePotionEffect(pe));
			}
		}
		if (worldConf().getBoolean(HEAL_POS_STATUS)) {
			if ((int) timers.getSlpt() / 1000L >= worldConf().getInt(HOURS_POS_STATUS)) {
				ConfigHelper.positiveEffects.forEach(pe -> plr.removePotionEffect(pe));
			}
		}
	}

	public void setWoke(boolean woke) { this.woke = woke; SmoothSleep.logDebug("Setting " + getPlayer().getName() + " as woke"); }

	@Override
	public void purgeData() {
		if (deepSleepTask != null) deepSleepTask.cancel();
		if (bar != null) {
			bar.removeAll();
			bar = null;
		}
	}

	// Some short-hand methods to assist with placeholder variables
	private StrSubstitutor strSub() {
		return MiscUtils.sub(plr.getWorld(), plr, worldData().getSleepers().size(), worldData().getWakers().size(),
				worldData().getTimescale(), (int) getTimers().getSlpt(), pl.data.userHelper.getNickname(plr));
	}
	private String slpTitle() { return MiscUtils.trans(strSub().replace(worldConf().getString(SLEEP_TITLE))); }
	private String slpSubtitle() { return MiscUtils.trans(strSub().replace(worldConf().getString(SLEEP_SUBTITLE))); }
	private String mrnTitle() { return MiscUtils.trans(strSub().replace(worldConf().getString(MORNING_TITLE))); }
	private String mrnSubtitle() { return MiscUtils.trans(strSub().replace(worldConf().getString(MORNING_SUBTITLE))); }
	private String actionBarTitle() { return MiscUtils.trans(strSub().replace(worldConf().getString(ACTIONBAR_TITLE))); }
	private String bossBarTitle() { return MiscUtils.trans(strSub().replace(worldConf().getString(BOSSBAR_TITLE))); }
}
