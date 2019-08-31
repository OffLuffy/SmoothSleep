package com.luffbox.lib;

import org.bukkit.configuration.ConfigurationSection;

public abstract class LuffSerializable {
	protected abstract ConfigurationSection serialize();
	abstract protected void deserialize(ConfigurationSection sect);
}
