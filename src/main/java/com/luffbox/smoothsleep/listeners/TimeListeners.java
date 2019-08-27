package com.luffbox.smoothsleep.listeners;

import com.luffbox.lib.LuffListener;
import com.luffbox.smoothsleep.SmoothSleep;
import com.luffbox.smoothsleep.data.GlobalConfigKey;
import com.luffbox.smoothsleep.events.NightBeginEvent;
import com.luffbox.smoothsleep.events.NightEndEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.Plugin;

public class TimeListeners extends LuffListener {

	@Override
	public void unregister(Plugin plugin) {
		NightBeginEvent.getHandlerList().unregister(plugin);
		NightEndEvent.getHandlerList().unregister(plugin);
	}

	@EventHandler
	public void nightBegin(NightBeginEvent e) {
		if (!SmoothSleep.data.getGlobalConfig().getBoolean(GlobalConfigKey.ENABLED)) { return; }
	}

	@EventHandler
	public void nightEnd(NightEndEvent e) {
		if (!SmoothSleep.data.getGlobalConfig().getBoolean(GlobalConfigKey.ENABLED)) { return; }
	}

}
