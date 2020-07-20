package com.luffbox.smoothsleep.lib.hooks;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.luffbox.smoothsleep.SmoothSleep;
import org.bukkit.entity.Player;

public class CmiUserHelper implements UserHelper {

	private final SmoothSleep pl;
	private final CMI cmi;

	public CmiUserHelper(SmoothSleep plugin) {
		this.pl = plugin;
		cmi = CMI.getInstance();
		if (cmi == null) { throw new NullPointerException(); }
		SmoothSleep.logInfo(String.format("Hooked to %s v%s", cmi.getDescription().getName(), cmi.getDescription().getVersion()));
	}

	@Override
	public String getNickname(Player p) { return getUser(p).getNickName(); }

	@Override
	public boolean isAfk(Player p) { return getUser(p).isAfk(); }

	@Override
	public boolean isVanished(Player p) { return getUser(p).isVanished(); }

	private CMIUser getUser(Player p) { return cmi.getPlayerManager().getUser(p); }
}
