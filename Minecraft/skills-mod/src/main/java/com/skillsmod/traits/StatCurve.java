package com.skillsmod.traits;

/**
 * The main stat of a trait. Only small and very small levels add to it:
 * each very small level adds 0.5% of the cap, each small level adds 6% of the cap,
 * so the stat reaches exactly the cap at level 100.
 */
public record StatCurve(double cap) {
	public static final double VERY_SMALL_SHARE = 0.005;
	public static final double SMALL_SHARE = 0.06;
	public static final int MAX_LEVEL = 100;

	/** The stat value at this skill level (ignores the trait's unlock level, see {@link Trait#stat}). */
	public double at(int level) {
		double total = 0;
		for (int l = 1; l <= Math.min(level, MAX_LEVEL); l++) {
			switch (UpgradeTier.at(l)) {
				case VERY_SMALL -> total += cap * VERY_SMALL_SHARE;
				case SMALL -> total += cap * SMALL_SHARE;
				default -> { } // medium and big levels give a perk, not stat
			}
		}
		return total;
	}
}
