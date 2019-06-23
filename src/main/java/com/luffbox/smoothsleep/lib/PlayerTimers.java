package com.luffbox.smoothsleep.lib;

public class PlayerTimers {

	public enum TimerType {
		/**
		 * Represents a food timer type. It's used to determine when to feed the player while sleeping.
		 */
		FOOD,
		/**
		 * Represents a heal timer type. It's used to determine when to heal the player while sleeping.
		 */
		HEAL,
		/**
		 * Represents a slept timer type. It's the ticks a player slept (including fast-forwarded ticks)
		 */
		SLPT
	}

	private double foodTimer = 0L, healTimer = 0L, slptTimer = 0L;

	public void incAll(double amount) { incFood(amount); incHeal(amount); incSlpt(amount); }
	public void incFood(double amount) { foodTimer += amount; }
	public void incHeal(double amount) { healTimer += amount; }
	public void incSlpt(double amount) { slptTimer += amount; }

	public void decAll(double amount) { decFood(amount); decHeal(amount); decSlpt(amount); }
	public void decFood(double amount) { foodTimer -= amount; }
	public void decHeal(double amount) { healTimer -= amount; }
	public void decSlpt(double amount) { slptTimer -= amount; }

	public void setAll(double amount) { setFood(amount); setHeal(amount); setSlpt(amount); }
	public void setFood(double amount) { foodTimer = amount; }
	public void setHeal(double amount) { healTimer = amount; }
	public void setSlpt(double amount) { slptTimer = amount; }

	public double getFood() { return foodTimer; }
	public double getHeal() { return healTimer; }
	public double getSlpt() { return slptTimer; }

	public void resetAll() { resetFood(); resetHeal(); resetSlpt(); }
	public void resetFood() { foodTimer = 0L; }
	public void resetHeal() { healTimer = 0L; }
	public void resetSlpt() { slptTimer = 0L; }

}
