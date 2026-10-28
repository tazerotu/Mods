package com.skillsmod.traits;

import net.minecraft.server.level.ServerPlayer;

/** Starts abilities: checks the cooldown, applies the effect, starts the cooldown. */
public final class AbilityManager {
	private AbilityManager() {}

	/**
	 * Tries to start an ability for this player.
	 *
	 * @return true if it started (it was unlocked, off cooldown, and its effect ran)
	 */
	public static boolean tryStart(ServerPlayer player, Ability ability) {
		int now = player.level().getServer().getTickCount();
		if (!Cooldowns.isReady(player.getUUID(), ability, now)) return false;

		// TODO: check the ability is unlocked (the owning trait's hasPerk(level, perkId)).
		// TODO: apply the effect for ability.durationTicks() (status effect, attribute, flag...).
		// TODO: only start the cooldown if the effect really ran.
		Cooldowns.start(player.getUUID(), ability, now);
		return true;
	}

	/**
	 * The ability key was pressed: start every unlocked ability bound to that key.
	 * COMBAT only has Warlord. MOVEMENT starts Sprint Burst, Wind Runner and Marathoner together.
	 */
	public static void onKeyPressed(ServerPlayer player, Ability.Key key) {
		for (Ability a : Ability.values()) {
			if (a.key == key) tryStart(player, a);
		}
	}
}
