package net.teamcarbon.carbonsleep;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import net.teamcarbon.carbonlib.CarbonPlugin;
import net.teamcarbon.carbonlib.Misc.CarbonException;
import net.teamcarbon.carbonlib.Misc.MiscUtils;
import net.teamcarbon.carbonlib.Misc.NumUtils;
import net.teamcarbon.carbonsleep.events.MorningEvent;
import net.teamcarbon.carbonsleep.listeners.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("unused")
public class CarbonSleep extends CarbonPlugin {

	private static final long SLEEP_TICKS_START = 12541L,
			SLEEP_TICKS_END = 23458L,
			SLEEP_TICKS_DURA = SLEEP_TICKS_END - SLEEP_TICKS_START;
	private static HashMap<World, Integer> nightTimes = new HashMap<>();
	private static List<Player> sleepers = new ArrayList<>();
	private static CarbonSleep inst;
	private static Plugin protoLib; // ProtocolLib

	public String getDebugPath() { return "enable-debug-messages"; }

	public void enablePlugin() {

		inst = (CarbonSleep) getPlugin();
		protoLib = MiscUtils.checkPlugin("ProtocolLib", true) ? pm().getPlugin("ProtocolManager") : null;

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
					List<Player> sleepers = new ArrayList<>();
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

	public void disablePlugin() { }

	public static ProtocolManager protoMgr() { return ((com.comphenix.protocol.ProtocolLibrary) protoLib).getProtocolManager(); }

	public static CarbonSleep inst() { return inst; }

	public static boolean worldEnabled(World w) { return nightTimes.containsKey(w); }

	public static int getSleeperCount(World w) { return sleepers.size(); }

	public static boolean isSleeping(Player p) { return p != null && sleepers.contains(p); }

	public static int getWakerCount(World w) {
		int a = 0;
		for (Player p : w.getPlayers()) { if (p.isSleepingIgnored() || !p.isSleeping()) a++; }
		return a;
	}

	public static void playSleepAnim(Player sleeper, Location bedLoc) {
		if (!sleepers.contains(sleeper)) sleepers.add(sleeper);
		if (bedLoc == null) bedLoc = sleeper.getLocation();
		final PacketContainer bedPacket = CarbonSleep.protoMgr().createPacket(PacketType.Play.Server.BED, false);
		bedPacket.getEntityModifier(sleeper.getWorld()).write(0, sleeper);
		bedPacket.getBlockPositionModifier().write(0, new BlockPosition(bedLoc.getBlockX(), bedLoc.getBlockY(), bedLoc.getBlockZ()));
		broadcastNearby(sleeper, bedPacket);
	}

	public static void stopSleepAnim(Player sleeper) {
		if (sleepers.contains(sleeper)) sleepers.remove(sleeper);
		final PacketContainer animation = CarbonSleep.protoMgr().createPacket(PacketType.Play.Server.ANIMATION, false);
		animation.getEntityModifier(sleeper.getWorld()).write(0, sleeper);
		animation.getIntegers().write(1, 2);
		broadcastNearby(sleeper, animation);
	}

	private static void broadcastNearby(Player asleep, PacketContainer bedPacket) {
		for (Player observer : CarbonSleep.protoMgr().getEntityTrackers(asleep)) {
			try {
				CarbonSleep.protoMgr().sendServerPacket(observer, bedPacket);
			} catch (InvocationTargetException e) {
				throw new CarbonException(inst, new RuntimeException("Cannot send packet.", e));
			}
		}
	}

}