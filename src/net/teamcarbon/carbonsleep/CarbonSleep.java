package net.teamcarbon.carbonsleep;

import net.milkbowl.vault.permission.Permission;
import net.teamcarbon.carbonsleep.commands.CarbonSleepReload;
import net.teamcarbon.carbonsleep.events.MorningEvent;
import net.teamcarbon.carbonsleep.lib.FormatUtils.FormattedMessage;
import net.teamcarbon.carbonsleep.lib.MiscUtils;
import net.teamcarbon.carbonsleep.lib.ReflectionUtils;
import net.teamcarbon.carbonsleep.lib.TitleUtils.TitleHelper;
import net.teamcarbon.carbonsleep.listeners.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("unused")
public class CarbonSleep extends JavaPlugin {
	private static final long SLEEP_TICKS_START = 12541L,
			SLEEP_TICKS_END = 23458L,
			SLEEP_TICKS_DURA = SLEEP_TICKS_END - SLEEP_TICKS_START;
	private static HashMap<World, Integer> nightTimes = new HashMap<>();
	private static List<Player> sleepers = new ArrayList<>();
	private static CarbonSleep inst;
	private Permission perm;
	public void onEnable() {
		if (!setupPerm()) {
			log(ChatColor.RED + "Cannot find Vault! Disabling plugin...");
			pm().disablePlugin(this);
		}
		inst = this;
		pm().registerEvents(new PlayerEventsListener(), this);
		getServer().getPluginCommand("carbonsleepreload").setExecutor(new CarbonSleepReload());

		saveDefaultConfig();
		reload();

		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
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

					FormattedMessage title, subtitle;
					if (newTime >= SLEEP_TICKS_END) {
						pm().callEvent(new MorningEvent(w, sleepers));
						title = new FormattedMessage(MiscUtils.ticksToTime(w.getTime())).color(ChatColor.YELLOW);
						subtitle = new FormattedMessage("Good morning!").color(ChatColor.GREEN);
						w.setThundering(false);
						w.setStorm(false);
					} else {
						title = new FormattedMessage(MiscUtils.ticksToTime(w.getTime())).color(ChatColor.AQUA);
						subtitle = new FormattedMessage(sc + "/" + (sc+wc) + " Sleepers").color(ChatColor.GREEN);
					}

					for (Player p : sleepers) { TitleHelper.sendTitle(p, new int[] {0, 40, 10}, title, subtitle); }

				}
			}
		}, 0L, 0L);
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
	public static void reload() {
		inst().reloadConfig();
		nightTimes.clear();
		List<String> disabledWorlds = inst().getConfig().getStringList("disabled-worlds");
		for (World w : Bukkit.getWorlds()) {
			if (w.getEnvironment() == Environment.NORMAL && !MiscUtils.eq(w.getName(), disabledWorlds)) {
				String confPath = "worlds." + w.getName() + ".";
				nightTimes.put(w, inst().getConfig().getInt(confPath + "min-night-duration-seconds", 15));
			}
		}
	}
	public static boolean perm(CommandSender sender, String perm) { return inst.perm.has(sender, perm); }
	public static PluginManager pm() { return Bukkit.getPluginManager(); }
	public static CarbonSleep inst() { return inst; }
	public static void log(String msg) { inst().getServer().getConsoleSender().sendMessage(msg); }
	public static boolean worldEnabled(World w) { return nightTimes.containsKey(w); }
	public static void addSleeper(Player p) { if (!sleepers.contains(p)) sleepers.add(p); }
	public static void removeSleeper(Player p) { if (sleepers.contains(p)) sleepers.remove(p); }
	public static boolean isSleeping(Player p) { return p != null && sleepers.contains(p); }
	public static int getSleeperCount(World w) {
		if (!worldEnabled(w)) return 0;
		int s = 0;
		for (Player p : sleepers) { if (p.getWorld().equals(w)) { s++; } }
		return s;
	}
	public static int getWakerCount(World w) {
		if (!worldEnabled(w)) return 0;
		int a = 0;
		for (Player p : w.getPlayers()) { if (!isSleeping(p) && !p.isSleepingIgnored()) a++; }
		return a;
	}
	private boolean setupPerm() {
		RegisteredServiceProvider<Permission> pp = Bukkit.getServicesManager().getRegistration(Permission.class);
		if (pp != null) perm = pp.getProvider();
		return perm != null;
	}
}