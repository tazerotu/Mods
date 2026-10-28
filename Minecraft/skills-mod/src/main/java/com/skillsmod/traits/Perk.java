package com.skillsmod.traits;

/** A named upgrade (medium or big) of a trait, unlocked at {@code level}. */
public record Perk(int level, String id, String name, String description) {
	public UpgradeTier tier() {
		return UpgradeTier.at(level);
	}
}
