package com.luffbox.smoothsleep.data;

import java.util.ArrayList;
import java.util.List;

public enum GlobalConfigKey {
	ENABLED("enabled", boolean.class),
	ENABLE_STATS("enable-stats", boolean.class),
	UPDATE_NOTIFY("update-notify-login", boolean.class),
	LOG_DEBUG("logging-settings.log-debug", boolean.class),
	LOG_INFO("logging-settings.log-info", boolean.class),
	LOG_WARNING("logging-settings.log-warning", boolean.class),
	;

	public final String key;
	public final Class type;
	GlobalConfigKey(String key, Class type) { this.key = key; this.type = type; }

	public static List<GlobalConfigKey> valuesByType(Class type) {
		List<GlobalConfigKey> keys = new ArrayList<>();
		for (GlobalConfigKey key : values()) { if (key.type == type) { keys.add(key); } }
		return keys;
	}

	@Override
	public String toString() { return key; }
}
