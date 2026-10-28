package com.skillsmod;

/** The six skill categories shown in the Skills screen. */
public enum SkillCategory {
	COMBAT("combat", 0xFFE04B4B),
	EXPLORATION("exploration", 0xFF4BA3E0),
	MINING("mining", 0xFFA0A0A8),
	WOODCUTTING("woodcutting", 0xFFB5773A),
	FARMING("farming", 0xFF7BCB4A),
	FISHING("fishing", 0xFF3CC8C8);

	private final String id;
	private final int color;

	SkillCategory(String id, int color) {
		this.id = id;
		this.color = color;
	}

	public String translationKey() {
		return "skill." + SkillsMod.MOD_ID + "." + id;
	}

	/** ARGB colour used for the label and the filled part of the bar. */
	public int color() {
		return color;
	}
}
