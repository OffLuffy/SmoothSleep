package com.luffbox.lib;

import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public abstract class LuffListener implements Listener {
	
	public void register(Plugin plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	public abstract void unregister(Plugin plugin);
	
}
