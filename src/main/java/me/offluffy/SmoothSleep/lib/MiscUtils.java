package me.offluffy.SmoothSleep.lib;

import me.offluffy.SmoothSleep.SmoothSleep;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class MiscUtils {

	public static double remapValue(boolean clamp, double oldMin, double oldMax, double newMin, double newMax, double value) {
		if (clamp) {
			if (value >= oldMax) return newMax;
			if (value <= oldMin) return newMin;
		}
		return (((newMax - newMin) * (value - oldMin)) / (oldMax - oldMin)) + newMin;
	}

	public static double clamp(double val, double min, double max) { return Math.min(max, Math.max(min, val)); }
	public static int clamp(int val, int min, int max) { return Math.min(max, Math.max(min, val)); }

	public static int ticksTo24Hours(long ticks) {
		ticks += 6000;
		int hours = (int) ticks / 1000;
		return (hours >= 24 ? hours - 24 : hours);
	}
	public static int ticksTo12Hours(long ticks) {
		int hours = ticksTo24Hours(ticks);
		return hours > 12 ? hours - 12 : hours == 0 ? 12 : hours;
	}
	public static int ticksToMinutes(long ticks) { return (int) ((ticks % 1000) / 16.66); }
	public static boolean ticksIsAM(long ticks) { return ticksTo24Hours(ticks) < 12; }


	public static void sendActionBar(SmoothSleep pl, Player player, String message) {
		if (!player.isOnline()) { return; }

		String nmsver = SmoothSleep.nmsver;

		try {
			Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + nmsver + ".entity.CraftPlayer");
			Object craftPlayer = craftPlayerClass.cast(player);
			Object packet;
			Class<?> packetPlayOutChatClass = Class.forName("net.minecraft.server." + nmsver + ".PacketPlayOutChat");
			Class<?> packetClass = Class.forName("net.minecraft.server." + nmsver + ".Packet");
			Class<?> chatComponentTextClass = Class.forName("net.minecraft.server." + nmsver + ".ChatComponentText");
			Class<?> iChatBaseComponentClass = Class.forName("net.minecraft.server." + nmsver + ".IChatBaseComponent");
			try {
				Class<?> chatMessageTypeClass = Class.forName("net.minecraft.server." + nmsver + ".ChatMessageType");
				Object[] chatMessageTypes = chatMessageTypeClass.getEnumConstants();
				Object chatMessageType = null;
				for (Object obj : chatMessageTypes) { if (obj.toString().equals("GAME_INFO")) { chatMessageType = obj; } }
				Object chatCompontentText = chatComponentTextClass.getConstructor(new Class<?>[]{String.class}).newInstance(message);
				packet = packetPlayOutChatClass.getConstructor(new Class<?>[]{iChatBaseComponentClass, chatMessageTypeClass}).newInstance(chatCompontentText, chatMessageType);
			} catch (ClassNotFoundException cnfe) {
				Object chatCompontentText = chatComponentTextClass.getConstructor(new Class<?>[]{String.class}).newInstance(message);
				packet = packetPlayOutChatClass.getConstructor(new Class<?>[]{iChatBaseComponentClass, byte.class}).newInstance(chatCompontentText, (byte) 2);
			}
			Method craftPlayerHandleMethod = craftPlayerClass.getDeclaredMethod("getHandle");
			Object craftPlayerHandle = craftPlayerHandleMethod.invoke(craftPlayer);
			Field playerConnectionField = craftPlayerHandle.getClass().getDeclaredField("playerConnection");
			Object playerConnection = playerConnectionField.get(craftPlayerHandle);
			Method sendPacketMethod = playerConnection.getClass().getDeclaredMethod("sendPacket", packetClass);
			sendPacketMethod.invoke(playerConnection, packet);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void sendActionBar(SmoothSleep pl, final Player player, final String message, long duration) {
		sendActionBar(pl, player, message);

		if (duration >= 0) {
			new BukkitRunnable() { public void run() { sendActionBar(pl, player, ""); } }.runTaskLater(pl, duration + 1);
		}

		// Re-sends the messages every 2 seconds so it doesn't go away from the player's screen.
		while (duration > 40) {
			duration -= 40;
			new BukkitRunnable() { public void run() { sendActionBar(pl, player, message); } }.runTaskLater(pl, duration);
		}
	}

	public static void sendActionBarToAllPlayers(SmoothSleep pl, String message, int duration) {
		Bukkit.getOnlinePlayers().forEach((p)->sendActionBar(pl, p, message, duration));
	}

}
