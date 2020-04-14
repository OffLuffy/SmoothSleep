package com.luffbox.smoothsleep.lib.hooks;

import com.luffbox.smoothsleep.SmoothSleep;
import com.luffbox.smoothsleep.lib.MiscUtils;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class DefPlaceholderHelper implements PlaceholderHelper {

	private final SmoothSleep pl;
	public DefPlaceholderHelper(SmoothSleep plugin) {
		this.pl = plugin;
	}

	@Override
	public String replace(String template, World w, Player p, int sc, int wc, double timescale, long ticksSlept, String nickname) {
		return MiscUtils.sub(w, p, sc, wc, timescale, ticksSlept, nickname).replace(template);
	}
}
