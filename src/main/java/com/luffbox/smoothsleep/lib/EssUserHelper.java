package com.luffbox.smoothsleep.lib;

import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.User;
import com.luffbox.smoothsleep.SmoothSleep;
import org.bukkit.entity.Player;

public class EssUserHelper implements UserHelper {

	private IEssentials ess;
	public EssUserHelper(SmoothSleep pl) {
		ess = (IEssentials) pl.getServer().getPluginManager().getPlugin("Essentials");
		SmoothSleep.logInfo(pl.getDescription().getName() + ": Hooked to Essentials v" + ess.getDescription().getVersion());
	}

	@Override
	public String getNickname(Player p) { return getUser(p).getNickname(); }

	@Override
	public boolean isAfk(Player p) { return getUser(p).isAfk(); }

	@Override
	public boolean isVanished(Player p) { return getUser(p).isVanished(); }

	private User getUser(Player p) { return ess.getUser(p); }
}
