package net.teamcarbon.carbonsleep.lib.TitleUtils;

import net.minecraft.server.v1_11_R1.*;
import net.teamcarbon.carbonsleep.lib.FormatUtils.FormattedMessage;
import net.teamcarbon.carbonsleep.lib.MiscUtils;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.lang.reflect.Field;

@SuppressWarnings("unused")
public class TitleHelper implements Listener {
	private static final int[] DEF_TIMES = {10, 40, 10};

	// Title API
	public static void sendTitle(Player player, int[] timings, String title, String subtitle) {
		sendRawTitle(player, timings == null ? DEF_TIMES : timings, jsonWrap(title), jsonWrap(subtitle));
	}
	public static void sendTitle(Player player, int[] timings, FormattedMessage title, FormattedMessage subtitle) {
		sendRawTitle(player, timings == null ? DEF_TIMES : timings, title.toJSONString(), subtitle.toJSONString());
	}
	private static void sendRawTitle(Player player, int[] timings, String title, String subtitle) {
		PacketPlayOutTitle subtitlePacket = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, IChatBaseComponent.ChatSerializer.a(MiscUtils.repVars(title, player)));
		PacketPlayOutTitle titlePacket = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, IChatBaseComponent.ChatSerializer.a(MiscUtils.repVars(subtitle, player)));
		int[] ft = fixTimings(timings);
		sendPacket(player, new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TIMES, null, ft[0], ft[1], ft[2]));
		sendPacket(player, titlePacket, subtitlePacket);
	}
	private static int[] fixTimings(int[] timings) {
		int[] newTimings = new int[DEF_TIMES.length];
		for (int i = 0; i < DEF_TIMES.length; i++)
			newTimings[i] = i > timings.length-1 ? DEF_TIMES[i] : timings[i];
		return newTimings;
	}

	// Action Bar API
	public static void sendActionBar(Player player, String message) {
		sendRawActionBar(player, jsonWrap(message));
	}
	public static void sendActionBar(Player player, FormattedMessage message) {
		sendRawActionBar(player, message.toJSONString());
	}
	public static void sendRawActionBar(Player player, String message){
		IChatBaseComponent actionMessage = IChatBaseComponent.ChatSerializer.a(MiscUtils.repVars(ChatColor.translateAlternateColorCodes('&', message), player));
		sendPacket(player, new PacketPlayOutChat(actionMessage, (byte) 2));
	}

	// Tab Title/Footer API
	public static void sendTabTitle(Player player, String header, String footer) {
		sendRawTabTitle(player, jsonWrap(header), jsonWrap(footer));
	}
	public static void sendTabTitle(Player player, FormattedMessage header, FormattedMessage footer) {
		sendRawTabTitle(player, header.toJSONString(), footer.toJSONString());
	}
	public static void sendRawTabTitle(Player player, String header, String footer) {
		IChatBaseComponent tabTitle = IChatBaseComponent.ChatSerializer.a(MiscUtils.repVars(header, player));
		IChatBaseComponent tabFoot = IChatBaseComponent.ChatSerializer.a(MiscUtils.repVars(footer, player));
		PacketPlayOutPlayerListHeaderFooter headerPacket = new PacketPlayOutPlayerListHeaderFooter(tabTitle);
		try {
			Field field = headerPacket.getClass().getDeclaredField("b");
			field.setAccessible(true);
			field.set(headerPacket, tabFoot);
		} catch (Exception ignored) {}
		sendPacket(player, headerPacket);
	}

	// Private Methods
	private static void sendPacket(Player pl, Packet... packets) { for (Packet p : packets) ((CraftPlayer) pl).getHandle().playerConnection.sendPacket(p); }
	private static String jsonWrap(String msg) { return "{\"text\": \"" + (msg == null ? "" : msg) + "\"}"; }
}