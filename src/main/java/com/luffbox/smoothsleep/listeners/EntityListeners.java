package com.luffbox.smoothsleep.listeners;

import com.destroystokyo.paper.event.entity.PhantomPreSpawnEvent;
import com.luffbox.lib.LuffListener;
import com.luffbox.smoothsleep.SmoothSleep;
import com.luffbox.smoothsleep.data.GlobalConfigKey;
import com.luffbox.smoothsleep.data.WorldConfigKey;
import com.luffbox.smoothsleep.data.WorldData;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.Plugin;

public class EntityListeners extends LuffListener {

	@Override
	public void unregister(Plugin plugin) {
		PhantomPreSpawnEvent.getHandlerList().unregister(plugin);
	}

	@EventHandler
	public void phantomSpawn(PhantomPreSpawnEvent e) {
		if (!SmoothSleep.data.getGlobalConfig().getBoolean(GlobalConfigKey.ENABLED)) { return; }
		Entity ent = e.getSpawningEntity();
		if (ent == null) { return; }
		World w = ent.getWorld();
		WorldData wd = SmoothSleep.data.getWorldData(w);
		if (wd == null) { return; }
		double chance = wd.getWorldConfig().getDouble(WorldConfigKey.PHANTOM_RESET_RATIO);
		chance = Math.min(Math.max(chance, 0.0), 1.0);
		if (chance == 1.0) { return; }
		if (chance == 0.0) { e.setCancelled(true); return; }
		double rand = Math.random();
		if (rand > chance) { e.setCancelled(true); }
	}
}
