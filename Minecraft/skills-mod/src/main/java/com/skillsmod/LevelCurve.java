package com.skillsmod;

/**
 * The level curve. Edit the three constants below to retune every skill at once.
 *
 * <pre>
 *   xpToNext(level) = round(BASE_XP * GROWTH ^ (level - 1))
 * </pre>
 *
 * With the defaults (50 / 1.05 / 100):
 * <pre>
 *   level   1 ->     50 xp        level  50 ->    546 xp
 *   level  10 ->     78 xp        level  75 ->  1 849 xp
 *   level  25 ->    161 xp        level  99 ->  5 964 xp
 *   total xp to reach level 100: about 124 000
 * </pre>
 *
 * Tips: a higher GROWTH makes late levels much slower (1.07 is very grindy, 1.03 is relaxed).
 * A higher BASE_XP slows every level by the same proportion.
 */
public final class LevelCurve {
	private LevelCurve() {}

	/** Highest reachable level. */
	public static final int MAX_LEVEL = 100;
	/** XP needed to go from level 1 to level 2. */
	public static final double BASE_XP = 50;
	/** Each level needs this many times more xp than the previous one. */
	public static final double GROWTH = 1.05;

	/** XP needed to go from {@code level} to {@code level + 1}. */
	public static int xpToNext(int level) {
		return (int) Math.round(BASE_XP * Math.pow(GROWTH, Math.max(0, level - 1)));
	}
}
