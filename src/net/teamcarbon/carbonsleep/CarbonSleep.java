package net.teamcarbon.carbonsleep;

import net.teamcarbon.carbonlib.CarbonPlugin;
import net.teamcarbon.carbonlib.Misc.MiscUtils;
import net.teamcarbon.carbonlib.Misc.NumUtils;
import net.teamcarbon.carbonsleep.events.MorningEvent;
import net.teamcarbon.carbonsleep.listeners.*;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("unused")
public class CarbonSleep extends CarbonPlugin {

	private static final long SLEEP_TICKS_START = 12541L,
			SLEEP_TICKS_END = 23458L,
			SLEEP_TICKS_DURA = SLEEP_TICKS_END - SLEEP_TICKS_START;
	private static HashMap<World, Integer> nightTimes = new HashMap<>();

	public void enablePlugin() {

        pm().registerEvents(new PlayerEventsListener(), this);
        pm().registerEvents(new MorningEventListener(), this);

		List<String> disabledWorlds = getConf().getStringList("disabled-worlds");
		for (World w : server().getWorlds()) {
			if (w.getEnvironment() == Environment.NORMAL && !MiscUtils.eq(w.getName(), disabledWorlds)) {
				String confPath = "worlds." + w.getName() + ".";
				nightTimes.put(w, getConf().getInt(confPath + "min-night-duration-seconds", 15));
			}
		}

		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				if (nightTimes.isEmpty()) return;
				for (World w : nightTimes.keySet()) {
                    if (w.getEnvironment() != Environment.NORMAL) { nightTimes.remove(w); continue; }
                    if (w.getTime() < SLEEP_TICKS_START || w.getTime() > SLEEP_TICKS_END) { continue; }

                    // Calculate some math for time and stuff
                    long duraTicks = nightTimes.get(w) * 20; // Number of ticks for night to last with 100% speed
                    long ticksPerTick = SLEEP_TICKS_DURA / duraTicks - 1; // Ticks to pass per tick at 100% speed

                    // Count players, track awake/asleep, ignored or not.
                    int asleep = 0, awake = 0, ignoreAsleep = 0;
                    List<Player> sleepers = new ArrayList<Player>();
                    for (Player p : w.getPlayers()) {
                        if (p.isSleeping()) sleepers.add(p);
                    	if (p.isSleepingIgnored()) { if (p.isSleeping()) ignoreAsleep++; }
                    	else { if (p.isSleeping()) asleep++; else awake++; }
                    }
                    int totalAsleep = asleep + ignoreAsleep;

                    if (totalAsleep == 0) continue; // Continue, no one is sleeping

                    // Scale the 100% night speed down by the ratio of awake/asleep players
                    ticksPerTick = NumUtils.remapValue(true, 0, ticksPerTick, 0, awake, totalAsleep);

                    long newTime = w.getTime() + ticksPerTick;
                    if (newTime > SLEEP_TICKS_END) newTime = SLEEP_TICKS_END;

                    w.setTime(newTime);

                    if (newTime >= SLEEP_TICKS_END) { pm().callEvent(new MorningEvent(w, sleepers)); }

				}
			}
		}, 0L, 1L);
	}

	public static boolean worldEnabled(World w) { return nightTimes.containsKey(w); }

    public static int getSleeperCount(World w) {
        int s = 0;
        for (Player p : w.getPlayers()) { if (p.isSleeping()) s++; }
        return s;
    }

    public static int getWakerCount(World w) {
        int a = 0;
        for (Player p : w.getPlayers()) { if (p.isSleepingIgnored() || !p.isSleeping()) a++; }
        return a;
    }

}