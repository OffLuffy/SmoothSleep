package com.luffbox.smoothsleep.lib.hooks;

import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.User;
import com.luffbox.smoothsleep.SmoothSleep;
import org.bukkit.entity.Player;

public class EssUserHelper implements UserHelper {

	private SmoothSleep pl;
	private IEssentials ess;

	public EssUserHelper(SmoothSleep plugin) {
		this.pl = plugin;
		ess = (IEssentials) plugin.getServer().getPluginManager().getPlugin("Essentials");
		if (ess == null) { throw new NullPointerException(); }
		SmoothSleep.logInfo("Hooked to Essentials v" + ess.getDescription().getVersion());
	}

	@Override
	public String getNickname(Player p) { return getUser(p).getNickname(); }

	@Override
	public boolean isAfk(Player p) { return getUser(p).isAfk(); }

	@Override
	public boolean isVanished(Player p) { return getUser(p).isVanished(); }

	private User getUser(Player p) { return ess.getUser(p); }
}
