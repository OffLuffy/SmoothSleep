package com.luffbox.smoothsleep.lib.hooks;

import com.luffbox.smoothsleep.SmoothSleep;
import com.luffbox.smoothsleep.lib.MiscUtils;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class PlaceholderAPIHelper implements PlaceholderHelper {

	private SmoothSleep pl;
	private PlaceholderAPIPlugin papi;

	public PlaceholderAPIHelper(SmoothSleep plugin) {
		this.pl = plugin;
		papi = (PlaceholderAPIPlugin) plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI");
		if (papi == null) { throw new NullPointerException(); }
		SmoothSleep.logInfo("Hooked to PlaceholderAPI v" + papi.getDescription().getVersion());
	}

	@Override
	public String replace(String template, World w, Player p, int sc, int wc, double timescale, long ticksSlept, String nickname) {
		String out = MiscUtils.sub(w, p, sc, wc, timescale, ticksSlept, nickname).replace(template);
		return PlaceholderAPI.setPlaceholders(p, out);
	}
}
