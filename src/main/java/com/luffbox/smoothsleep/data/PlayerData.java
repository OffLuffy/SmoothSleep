package com.luffbox.smoothsleep.data;

import com.luffbox.lib.ReflectUtil;
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

	public PlayerTimer getTimers() { return timers; }

	private void setSleepTicks(int ticks) {
		try {
			Object nmsPlr = ReflectUtil.invokeMethod(player, "getHandle");
			ReflectUtil.setValue(nmsPlr, false, "sleepTicks", ticks);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
