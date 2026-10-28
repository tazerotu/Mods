package com.skillsmod.traits;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Remembers when each ability is ready again, per player. In memory only (not saved yet). */
public final class Cooldowns {
	private Cooldowns() {}

	private static final Map<UUID, Map<Ability, Integer>> READY_AT = new HashMap<>();

	/** {@code nowTick} is the server tick count. */
	public static boolean isReady(UUID player, Ability ability, int nowTick) {
		return remaining(player, ability, nowTick) == 0;
	}

	public static int remaining(UUID player, Ability ability, int nowTick) {
		Map<Ability, Integer> map = READY_AT.get(player);
		Integer readyAt = map == null ? null : map.get(ability);
		return readyAt == null ? 0 : Math.max(0, readyAt - nowTick);
	}

	public static void start(UUID player, Ability ability, int nowTick) {
		READY_AT.computeIfAbsent(player, id -> new EnumMap<>(Ability.class))
				.put(ability, nowTick + ability.cooldownTicks());
	}
}
