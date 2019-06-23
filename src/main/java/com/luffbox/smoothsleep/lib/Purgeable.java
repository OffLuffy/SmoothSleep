package com.luffbox.smoothsleep.lib;

/**
 * Represents an object which is storing data that can be purged,
 * designed to be used to flush data before reloading data.
 */
public interface Purgeable {
	void purgeData();
}
