package com.luffbox.smoothsleep.lib.actionbar;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

@SuppressWarnings("deprecation")
public class SpigotActionBarHelper implements ActionBarHelper {
	@Override
	public void sendActionBar(Player player, String message) {
		player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
	}
}
