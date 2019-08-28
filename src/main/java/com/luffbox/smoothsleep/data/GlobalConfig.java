package com.luffbox.smoothsleep.data;

import com.luffbox.lib.ConfigHelper;
import com.luffbox.smoothsleep.SmoothSleep;
import org.bukkit.configuration.ConfigurationSection;

public class GlobalConfig {

	private ConfigHelper conf() { return SmoothSleep.data.getConfigHelper(); }

	public boolean contains(GlobalConfigKey key) { return conf().contains(key.key); }

	public void set(GlobalConfigKey key, Object value) { conf().set(key.key, value); }
	public void setToDefault(GlobalConfigKey key) { conf().setToDefault(key.key); }

	public int getInt(GlobalConfigKey key) { return conf().getInt(key.key); }
	public int getDefaultInt(GlobalConfigKey key) { return conf().getDefaultInt(key.key); }

	public boolean getBoolean(GlobalConfigKey key) { return conf().getBoolean(key.key); }
	public boolean getDefaultBoolean(GlobalConfigKey key) { return conf().getDefaultBoolean(key.key); }

	public String getString(GlobalConfigKey key) { return conf().getString(key.key); }
	public String getDefaultString(GlobalConfigKey key) { return conf().getDefaultString(key.key); }

	public double getDouble(GlobalConfigKey key) { return conf().getDouble(key.key); }
	public double getDefaultDouble(GlobalConfigKey key) { return conf().getDefaultDouble(key.key); }

	public ConfigurationSection getConfSection(GlobalConfigKey key) { return conf().getConfSection(key.key); }
	public ConfigurationSection getDefaultConfSection(GlobalConfigKey key) { return conf().getDefaultConfSection(key.key); }
}
