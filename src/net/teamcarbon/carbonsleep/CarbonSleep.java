package net.teamcarbon.carbonsleep;

import net.md_5.bungee.api.chat.TextComponent;
import net.teamcarbon.carbonsleep.commands.CarbonSleepReload;
import net.teamcarbon.carbonsleep.lib.MiscUtils;
import net.teamcarbon.carbonsleep.lib.ReflectionUtils;
import net.teamcarbon.carbonsleep.listeners.*;
import org.bukkit.Bukkit;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("unused")
public class CarbonSleep extends JavaPlugin {
	private final long SLEEP_TICKS_START = 12541L,
			SLEEP_TICKS_END = 23458L,
			SLEEP_TICKS_DURA = SLEEP_TICKS_END - SLEEP_TICKS_START;
	private HashMap<World, Integer> nightTimes = new HashMap<>();
	private List<Player> sleepers = new ArrayList<>();
	public void onEnable() {
		pm().registerEvents(new PlayerEventsListener(this), this);
		getServer().getPluginCommand("carbonsleepreload").setExecutor(new CarbonSleepReload(this));

		saveDefaultConfig();
		reload();

		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			@SuppressWarnings("deprecation")
			public void run() {
				if (nightTimes.isEmpty()) return;
				for (World w : nightTimes.keySet()) {
					if (w.getEnvironment() != Environment.NORMAL) { nightTimes.remove(w); continue; }
					if (w.getTime() < SLEEP_TICKS_START || w.getTime() > SLEEP_TICKS_END) { continue; }

					int sc = getSleeperCount(w), wc = getWakerCount(w);

					if (sc == 0) { continue; }

					// Calculate some math for time and stuff
					long duraTicks = nightTimes.get(w) * 20; // Number of ticks for night to last with 100% speed
					long ticksPerTick = SLEEP_TICKS_DURA / duraTicks - 1; // Ticks to pass per tick at 100% speed
					double newtt = MiscUtils.remapValue(true, 0, (sc+wc), 0, ticksPerTick, sc);
					long newTime = w.getTime() + Math.round(newtt);
					if (newTime > SLEEP_TICKS_END) newTime = SLEEP_TICKS_END;
					w.setTime(newTime);

					//FormattedMessage title, subtitle;
					TextComponent title, subtitle;
					if (newTime >= SLEEP_TICKS_END) {
						title = new TextComponent(MiscUtils.ticksToTime(w.getTime()));
						title.setColor(ChatColor.YELLOW);
						subtitle = new TextComponent(getConfig().getString("worlds." + w.getName() + ".morning-subtitle", "Rise and shine, {PLAYER}!"));
						subtitle.setColor(ChatColor.GREEN);
						w.setThundering(false);
						w.setStorm(false);
					} else {
						title = new TextComponent(MiscUtils.ticksToTime(w.getTime()));
						title.setColor(ChatColor.AQUA);
						subtitle = new TextComponent(sc + "/" + (sc+wc) + " Sleepers");
						subtitle.setColor(ChatColor.GREEN);
					}

					for (Player p : sleepers) {
						if (subtitle != null) {
							subtitle.setText(subtitle.getText().replace("{PLAYER}", p.getName()));
						}
						p.sendTitle(title.toLegacyText(), subtitle.toLegacyText());
					}

				}
			}
		}, 0L, 0L);

		// Thanks to IAlIstannen at the Spigot forums for this idea of NMS sleepTick modification
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				if (!sleepers.isEmpty()) {
					for (Player p : sleepers) {
						long wt = p.getWorld().getTime();
						try {
							Object nmsPlr = ReflectionUtils.invokeMethod(p, "getHandle", new Class[0]);
							ReflectionUtils.setValue(nmsPlr, false, "sleepTicks", wt > SLEEP_TICKS_END ? 100 : 0);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}, 0L, 10L);
	}
	public void reload() {
		reloadConfig();
		nightTimes.clear();
		boolean saveConf = false;
		for (World w : Bukkit.getWorlds()) {
			if (w.getEnvironment() == Environment.NORMAL) {
				if (getConfig().contains("worlds." + w.getName())) {

					// Will add config changes here if they'll need to be added from and older config.
					if (!getConfig().contains("worlds." + w.getName() + ".morning-subtitle")) {
						getConfig().set("worlds." + w.getName() + ".morning-subtitle", "Rise and shine, {PLAYER}!");
						saveConf = true;
					}

					// Cache min. durations (just easier to iterate over later I suppose)
					nightTimes.put(w, getConfig().getInt("worlds." + w.getName() + ".min-night-duration-seconds", 10));
				}
			}
		}
		if (saveConf) saveConfig();
	}

	private PluginManager pm() { return Bukkit.getPluginManager(); }

	public boolean worldEnabled(World w) { return nightTimes.containsKey(w); }
	public void addSleeper(Player p) { if (!sleepers.contains(p)) sleepers.add(p); }
	public void removeSleeper(Player p) { if (sleepers.contains(p)) sleepers.remove(p); }
	private boolean isSleeping(Player p) { return p != null && sleepers.contains(p); }
	public int getSleeperCount(World w) {
		if (!worldEnabled(w)) return 0;
		int s = 0;
		for (Player p : sleepers) { if (p.getWorld().equals(w)) { s++; } }
		return s;
	}
	public int getWakerCount(World w) {
		if (!worldEnabled(w)) return 0;
		int a = 0;
		for (Player p : w.getPlayers()) { if (!isSleeping(p) && !p.isSleepingIgnored()) a++; }
		return a;
	}
}