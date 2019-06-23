package com.luffbox.smoothsleep.lib.actionbar;

import com.luffbox.smoothsleep.lib.MiscUtils;
import org.bukkit.entity.Player;

public class NmsActionBarHelper implements ActionBarHelper {
	@Override
	public void sendActionBar(Player player, String message) {
		MiscUtils.nmsActionBar(player, message);
	}
}
