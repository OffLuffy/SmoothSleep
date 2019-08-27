package com.luffbox.smoothsleep.listeners;

import com.luffbox.lib.LuffListener;
import com.luffbox.smoothsleep.SmoothSleep;
import com.luffbox.smoothsleep.data.GlobalConfigKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

public class PlayerListeners extends LuffListener {

	public void unregister(Plugin plugin) {
		PlayerJoinEvent.getHandlerList().unregister(plugin);
		PlayerChangedWorldEvent.getHandlerList().unregister(plugin);
	}

	@EventHandler
	public void join(PlayerJoinEvent e) {
		if (!SmoothSleep.data.getGlobalConfig().getBoolean(GlobalConfigKey.ENABLED)) { return; }
		if (!SmoothSleep.data.isWorldEnabled(e.getPlayer().getWorld())) { return; }
		SmoothSleep.data.addPlayerData(e.getPlayer());
	}

	@EventHandler
	public void changeWorld(PlayerChangedWorldEvent e) {
		if (!SmoothSleep.data.getGlobalConfig().getBoolean(GlobalConfigKey.ENABLED)) { return; }
		Player p = e.getPlayer();
		boolean fromEnabled = SmoothSleep.data.isWorldEnabled(e.getFrom());
		boolean toEnabled = SmoothSleep.data.isWorldEnabled(p.getWorld());
		if (fromEnabled != toEnabled) {
			if (toEnabled) {
				SmoothSleep.data.addPlayerData(p);
			} else {
				SmoothSleep.data.removePlayerData(p);
			}
		}
	}

}
