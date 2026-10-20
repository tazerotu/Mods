package com.skillsmod;

/**
 * Progress of one skill.
 *
 * @param level    current level
 * @param xp       experience already possessed in this level
 * @param xpNeeded experience needed to reach the next level
 */
public record SkillProgress(int level, int xp, int xpNeeded) {
	public float fraction() {
		if (xpNeeded <= 0) return 0f;
		return Math.max(0f, Math.min(1f, (float) xp / xpNeeded));
	}
}
