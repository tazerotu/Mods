package com.skillsmod.traits;

/**
 * The four upgrade tiers. A level gives only the highest tier it qualifies for.
 *
 * <pre>
 *   BIG        20, 40, 60, 80, 100
 *   MEDIUM     10, 30, 50, 70, 90
 *   SMALL      5, 15, 25 ... 95
 *   VERY_SMALL every other level
 * </pre>
 */
public enum UpgradeTier {
	VERY_SMALL, SMALL, MEDIUM, BIG;

	public static UpgradeTier at(int level) {
		if (level % 20 == 0) return BIG;
		if (level % 10 == 0) return MEDIUM;
		if (level % 5 == 0) return SMALL;
		return VERY_SMALL;
	}
}
