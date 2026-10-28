package com.skillsmod;

/**
 * How a trait value grows with the skill level: it starts at {@code unlock}, improves by
 * {@code perStep} every {@code interval} levels (not every level), and stops at {@code cap}.
 *
 * <pre>
 *   value = min(cap, base + perStep * floor((level - unlock) / interval))
 * </pre>
 */
public record Scaling(int unlock, int interval, double base, double perStep, double cap) {

	/** The value at the given skill level, or 0 while the trait is still locked. */
	public double at(int level) {
		if (level < unlock) return 0;
		int steps = (level - unlock) / Math.max(1, interval);
		return Math.min(cap, base + perStep * steps);
	}

	public boolean unlocked(int level) {
		return level >= unlock;
	}
}
