package com.luffbox.smoothsleep.lib.actionbar;

import org.bukkit.entity.Player;

public class PaperActionHelper implements ActionBarHelper {
	@Override
	public void sendActionBar(Player player, String message) {
		player.sendActionBar(message);
	}
}
