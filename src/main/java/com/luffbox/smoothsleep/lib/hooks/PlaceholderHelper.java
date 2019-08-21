package com.luffbox.smoothsleep.lib.hooks;

import org.bukkit.World;
import org.bukkit.entity.Player;

public interface PlaceholderHelper {

	String replace(String template, World w, Player p, int sc, int wc, double timescale, long ticksSlept, String nickname);

}
