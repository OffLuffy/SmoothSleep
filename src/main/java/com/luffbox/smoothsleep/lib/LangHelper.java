package com.luffbox.smoothsleep.lib;

import com.luffbox.lib.LuffI18n;
import org.bukkit.plugin.Plugin;

import java.util.Map;

/**
 * Extension of the language file wrapper that allows using {@link LangKey} enums
 */
public class LangHelper extends LuffI18n {
	public LangHelper(Plugin plugin, String lang) { super(plugin, lang); }
	public String translate(LangKey key) { return super.translate(key.key); }
	public String translate(LangKey key, Map<String, String> valueMap) { return super.translate(key.key, valueMap); }
}
