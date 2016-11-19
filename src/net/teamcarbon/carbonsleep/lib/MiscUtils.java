package net.teamcarbon.carbonsleep.lib;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

public class MiscUtils {

	public static double remapValue(boolean clamp, double oldMin, double oldMax, double newMin, double newMax, double value) {
		if (clamp) {
			if (value >= oldMax) return newMax;
			if (value <= oldMin) return newMin;
		}
		return (((newMax - newMin) * (value - oldMin)) / (oldMax - oldMin)) + newMin;
	}

	/**
	 * Replaces supported variables with appropriate values
	 * @param msg The message containing variables to be replaced
	 * @param pl Player whose data to use for variable replacements
	 * @return Returns a String with all variables in the original message replaced
	 */
	public static String repVars(String msg, Player pl) {
		if (msg == null || msg.isEmpty()) return "";
		msg = ChatColor.translateAlternateColorCodes('&', msg);
		if (pl == null) return msg;

		// User Identity
		if (msg.contains("{NAME}")) msg = msg.replace("{NAME}", pl.getName());
		if (msg.contains("{DISPNAME}")) msg = msg.replace("{DISPNAME}", pl.getDisplayName());
		if (msg.contains("{UUID}")) msg = msg.replace("{UUID}", pl.getUniqueId().toString());
		if (msg.contains("{IP}")){
			String ip = pl.getAddress() == null ? "" : pl.getAddress().toString();
			if (!ip.isEmpty()) {
				if (ip.contains("/")) ip = ip.replace("/", ""); // Remove leading slash
				if (ip.contains(":")) ip = ip.substring(0, ip.indexOf(":")); // Remove port and port delimiter
			}
			msg = msg.replace("{IP}", ip);
		}

		// User Stats
		if (msg.contains("{HEALTH}")) msg = msg.replace("{HEALTH}", String.format(Locale.ENGLISH, "%.0f", pl.getHealth()));
		if (msg.contains("{MAXHEALTH}")) msg = msg.replace("{MAXHEALTH}", String.format(Locale.ENGLISH, "%.0f", pl.getMaxHealth()));
		if (msg.contains("{LEVEL}")) msg = msg.replace("{LEVEL}", String.format(Locale.ENGLISH, "%d", pl.getLevel()));
		if (msg.contains("{DAYSLIVED}")) msg = msg.replace("{DAYSLIVED}", String.format(Locale.ENGLISH, "%d", pl.getTicksLived() / 24000));

		// Server Info
		if (msg.contains("{TIME}")) msg = msg.replace("{TIME}", ticksToTime(pl.getWorld().getTime()));

		// Location Info
		if (msg.contains("{WORLD}")) msg = msg.replace("{WORLD}", pl.getWorld().getName());
		if (msg.contains("{X}")) msg = msg.replace("{X}", String.format(Locale.ENGLISH, "%d", pl.getLocation().getBlockX()));
		if (msg.contains("{Y}")) msg = msg.replace("{Y}", String.format(Locale.ENGLISH, "%d", pl.getLocation().getBlockY()));
		if (msg.contains("{Z}")) msg = msg.replace("{Z}", String.format(Locale.ENGLISH, "%d", pl.getLocation().getBlockZ()));
		if (msg.contains("{PITCH}")) msg = msg.replace("{PITCH}", String.format(Locale.ENGLISH, "%.0f", pl.getLocation().getPitch()));
		if (msg.contains("{YAW}")) msg = msg.replace("{YAW}", String.format(Locale.ENGLISH, "%.0f", pl.getLocation().getYaw()));

		// Directional Info
		if (msg.contains("{FACING}")) msg = msg.replace("{FACING}", getCardinalDir(pl, false, false, false));
		if (msg.contains("{FACING_SHORT}")) msg = msg.replace("{FACING_SHORT}", getCardinalDir(pl, true, false, false));
		if (msg.contains("{FACING_SEC}")) msg = msg.replace("{FACING_SEC}", getCardinalDir(pl, false, false, true));
		if (msg.contains("{FACING_SHORT_SEC}")) msg = msg.replace("{FACING_SHORT_SEC}", getCardinalDir(pl, true, false, true));
		if (msg.contains("{COMPASS}")) msg = msg.replace("{COMPASS}", getCardinalDir(pl, false, true, false));
		if (msg.contains("{COMPASS_SHORT}")) msg = msg.replace("{COMPASS_SHORT}", getCardinalDir(pl, true, true, false));
		if (msg.contains("{COMPASS_SEC}")) msg = msg.replace("{COMPASS_SEC}", getCardinalDir(pl, false, true, true));
		if (msg.contains("{COMPASS_SHORT_SEC}")) msg = msg.replace("{COMPASS_SHORT_SEC}", getCardinalDir(pl, true, true, true));

		return msg;
	}

	public static String ticksToTime(long ticks) {
		ticks += 6000; // Offset 0 ticks to = 6AM
		int hours = (int)(ticks / 1000), minutes = (int)((ticks % 1000) / 16.66);
		return (hours > 12 ? hours > 24 ? hours - 24 : hours-12 : hours) + ":"
				+ (minutes < 10 ? "0" : "") + minutes + (hours >= 12 && hours < 24 ? " PM" : " AM");
	}
	/**
	 * Checks a String query against a list of Strings
	 * @param query The string to check
	 * @param matches The list of Strings to check the query against
	 * @return Returns true if the query matches any String from matches (case-insensitive)
	 */
	public static boolean eq(String query, String ... matches) {
		query = query.replace("_", "").replace("-", "");
		for (String s : matches) {
			s = s.replace("_", "").replace("-", "");
			if (query.equalsIgnoreCase(s))
				return true;
		}
		return false;
	}
	/**
	 * Checks a String query against a list of Strings
	 * @param query The string to check
	 * @param matches The list of Strings to check the query against
	 * @return Returns true if the query matches any String from matches (case-insensitive)
	 */
	public static boolean eq(String query, List<String> matches) { return eq(query, matches.toArray(new String[matches.size()])); }

	/**
	 * Converts the Player's angle to a string. i.e., North, South, etc.
	 * @param pl The Player to base the angle on
	 * @param acro Whether or not to return an acronym instead (NE instead of North-East, etc)
	 * @param compDir Whether or not to only show compass directions (false will also return Up and Down or U and D acronyms)
	 * @param secondary Whether or not to include secondary inter-cardinal directions (NNE, SSW, WSW, etc)
	 * @return Returns the string value of the angle in terms of cardinal directions
	 */
	public static String getCardinalDir(Player pl, boolean acro, boolean compDir, boolean secondary) {
		String[] prim = {"South", "South-West", "West", "North-West", "North", "North-East", "East", "South-East"};
		String[] scnd = {"South", "South South-West", "South-West", "West South-West", "West", "West North-West",
				"North-West", "North North-West", "North", "North North-East", "North-East", "East North-East",
				"East", "East South-East", "South-East", "South South-East"};
		double rot = pl.getLocation().getYaw(), pit = pl.getLocation().getPitch(), rotInt = secondary ? 22.5 : 45;
		if (rot < 0) rot += 360;
		if (!compDir && (pit > 45 || pit < -45)) { return (pit < -45) ? (acro ? "U" : "Up") : (acro ? "D" : "Down"); }
		String dir = secondary ? scnd[(int)((rot+(rotInt/2))/rotInt)%16] : prim[(int)((rot+(rotInt/2))/rotInt)%8];
		return acro ? dir.replaceAll("orth|ast|outh|est| |-", "") : dir; // .replaceAll acronyms the direction
	}

}
