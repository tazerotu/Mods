package com.skillsmod;

/**
 * Placeholder data source. For now it returns fixed demo values so the bars are visible.
 * Replace the body of {@link #get(SkillCategory)} with real, saved player data later.
 */
public final class SkillData {
	private SkillData() {}

	public static SkillProgress get(SkillCategory category) {
		return switch (category) {
			case COMBAT -> new SkillProgress(5, 340, 500);
			case EXPLORATION -> new SkillProgress(2, 45, 150);
			case MINING -> new SkillProgress(8, 910, 1000);
			case WOODCUTTING -> new SkillProgress(3, 120, 300);
			case FARMING -> new SkillProgress(1, 0, 100);
			case FISHING -> new SkillProgress(4, 205, 400);
		};
	}
}
