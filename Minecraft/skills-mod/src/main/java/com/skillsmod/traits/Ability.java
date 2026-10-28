package com.skillsmod.traits;

/** Every perk that has a cooldown, with the numbers from the design document. Times are in seconds. */
public enum Ability {
	// ---- Triggered with a key -----------------------------------------------------------------
	WARLORD(Key.COMBAT, 20, 180),
	SPRINT_BURST(Key.MOVEMENT, 10, 120),
	WIND_RUNNER(Key.MOVEMENT, 15, 180),
	MARATHONER(Key.MOVEMENT, 30, 240),

	// ---- Automatic (triggered by a condition) -------------------------------------------------
	CLEAVE(Key.AUTO, 0, 2),
	BATTLE_CRY(Key.AUTO, 3, 15),
	STONESKIN(Key.AUTO, 2, 7),
	SECOND_WIND(Key.AUTO, 5, 60),
	LAST_STAND(Key.AUTO, 0, 180),
	SKYDIVER(Key.AUTO, 0, 120),
	LAST_GASP(Key.AUTO, 3, 300),
	FIELD_REPAIR(Key.AUTO, 0, 120),
	HARVEST_MOON(Key.AUTO, 0, 600),
	LEGENDARY_CATCH(Key.AUTO, 0, 1800);

	/** How the ability is started. COMBAT and MOVEMENT are the two ability keys. */
	public enum Key { AUTO, COMBAT, MOVEMENT }

	public final Key key;
	public final int durationSeconds;
	public final int cooldownSeconds;

	Ability(Key key, int durationSeconds, int cooldownSeconds) {
		this.key = key;
		this.durationSeconds = durationSeconds;
		this.cooldownSeconds = cooldownSeconds;
	}

	public int cooldownTicks() {
		return cooldownSeconds * 20;
	}

	public int durationTicks() {
		return durationSeconds * 20;
	}
}
