package com.luffbox.smoothsleep.listeners;

import com.luffbox.smoothsleep.PlayerData;
import com.luffbox.smoothsleep.SmoothSleep;
import com.luffbox.smoothsleep.events.NightEndEvent;
import com.luffbox.smoothsleep.events.NightStartEvent;
import com.luffbox.smoothsleep.lib.ConfigHelper;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NightListeners implements Listener {

	private SmoothSleep pl;

	public NightListeners(SmoothSleep plugin) { pl = plugin; }

	@EventHandler
	public void onNightStart(NightStartEvent e) {
		if (!pl.data.worldEnabled(e.getWorld())) return;
		e.getWorldData().resetFinishedSleeping();
		if (e.getWorldData().getPlayers().isEmpty()) return;
		SmoothSleep.logDebug("Checking sleep-ignore for players in world: " + e.getWorld().getName());
		for (Player pl : e.getWorldData().getPlayers()) {
			PlayerData pd = this.pl.data.getPlayerData(pl);
			if (pd != null) pd.updateIgnorePerm();
		}
	}

	@EventHandler
	public void onNightEnd(NightEndEvent e) {
		if (e.getWorldData().getPlayers().isEmpty()) return;
		if (e.getWorldData().getSettings().getBoolean(ConfigHelper.WorldSettingKey.HEAL_VILLAGERS)) {
			for (LivingEntity le : e.getWorld().getLivingEntities()) {
				if (le instanceof Villager) {
					AttributeInstance attr = le.getAttribute(Attribute.GENERIC_MAX_HEALTH);
					if (attr != null) {
						le.setHealth(attr.getValue());
						e.getWorld().spawnParticle(Particle.HEART, le.getLocation(), 1);
					}
				}
			}
		}
	}
}
