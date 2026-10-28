package com.skillsmod;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Holds every player's level and xp per skill, and handles level-ups.
 *
 * Data lives in memory only for now (it is lost when the game closes) and is shared between the
 * server thread (which awards xp) and the client thread (which draws the bars), so every method is
 * synchronized. The client reads it directly, which works in singleplayer / when hosting.
 */
public final class SkillData {
	private SkillData() {}

	private static final class Entry {
		int level = 1;
		long xp = 0;
	}

	private static final Map<UUID, Map<SkillCategory, Entry>> DATA = new HashMap<>();

	private static Entry entry(UUID player, SkillCategory category) {
		return DATA.computeIfAbsent(player, id -> new EnumMap<>(SkillCategory.class))
				.computeIfAbsent(category, c -> new Entry());
	}

	/** What the Skills screen draws for one skill. */
	public static synchronized SkillProgress get(UUID player, SkillCategory category) {
		Entry e = entry(player, category);
		int needed = LevelCurve.xpToNext(e.level);
		if (e.level >= LevelCurve.MAX_LEVEL) {
			return new SkillProgress(e.level, needed, needed); // full bar at max level
		}
		return new SkillProgress(e.level, (int) e.xp, needed);
	}

	public static synchronized int level(UUID player, SkillCategory category) {
		return entry(player, category).level;
	}

	/**
	 * Adds xp and levels up as many times as needed.
	 *
	 * @return how many levels were gained (0 most of the time)
	 */
	public static synchronized int addXp(UUID player, SkillCategory category, long amount) {
		if (amount <= 0) return 0;
		Entry e = entry(player, category);
		if (e.level >= LevelCurve.MAX_LEVEL) return 0;

		e.xp += amount;
		int gained = 0;
		while (e.level < LevelCurve.MAX_LEVEL && e.xp >= LevelCurve.xpToNext(e.level)) {
			e.xp -= LevelCurve.xpToNext(e.level);
			e.level++;
			gained++;
		}
		if (e.level >= LevelCurve.MAX_LEVEL) e.xp = 0;
		return gained;
	}
}
