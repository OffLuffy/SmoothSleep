package com.luffbox.smoothsleep.data;

import com.luffbox.smoothsleep.lib.PlayerTimer;
import org.bukkit.entity.Player;

public class PlayerData {

	private Player player;
	private PlayerTimer timers;
	private boolean ignorePerm = false;

	public PlayerData(Player player) {
		this.player = player;
		timers = new PlayerTimer();
	}

}
