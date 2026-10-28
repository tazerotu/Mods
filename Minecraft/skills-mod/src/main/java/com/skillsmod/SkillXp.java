package com.skillsmod;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.Set;

/**
 * How much xp each action is worth. Harder actions give more:
 * rarer ores, tougher mobs, rarer crops and biomes.
 *
 * Change {@link #XP_MULTIPLIER} to scale everything at once, or any number below for one action.
 */
public final class SkillXp {
	private SkillXp() {}

	/** Global multiplier applied to every xp gain (2.0 = twice as fast). */
	public static final double XP_MULTIPLIER = 1.0;

	/** An xp reward and the skill that receives it. */
	public record Gain(SkillCategory skill, int xp) {}

	// ---- Mining: by ore rarity ----------------------------------------------------------------
	private static final Map<Block, Integer> ORE_XP = Map.ofEntries(
			Map.entry(Blocks.COAL_ORE, 5), Map.entry(Blocks.DEEPSLATE_COAL_ORE, 6),
			Map.entry(Blocks.COPPER_ORE, 6), Map.entry(Blocks.DEEPSLATE_COPPER_ORE, 7),
			Map.entry(Blocks.IRON_ORE, 8), Map.entry(Blocks.DEEPSLATE_IRON_ORE, 9),
			Map.entry(Blocks.NETHER_QUARTZ_ORE, 8),
			Map.entry(Blocks.REDSTONE_ORE, 9), Map.entry(Blocks.DEEPSLATE_REDSTONE_ORE, 10),
			Map.entry(Blocks.LAPIS_ORE, 10), Map.entry(Blocks.DEEPSLATE_LAPIS_ORE, 11),
			Map.entry(Blocks.GOLD_ORE, 12), Map.entry(Blocks.DEEPSLATE_GOLD_ORE, 13),
			Map.entry(Blocks.NETHER_GOLD_ORE, 6),
			Map.entry(Blocks.DIAMOND_ORE, 25), Map.entry(Blocks.DEEPSLATE_DIAMOND_ORE, 28),
			Map.entry(Blocks.EMERALD_ORE, 30), Map.entry(Blocks.DEEPSLATE_EMERALD_ORE, 33),
			Map.entry(Blocks.ANCIENT_DEBRIS, 60),
			Map.entry(Blocks.OBSIDIAN, 15));
	/** Ores from other mods (anything in the ores tag that is not listed above). */
	private static final int UNKNOWN_ORE_XP = 8;
	/** Plain stone-like blocks: xp = hardness / this, at least 1, at most MAX_STONE_XP. */
	private static final double STONE_HARDNESS_DIVISOR = 1.5;
	private static final int MAX_STONE_XP = 5;

	// ---- Woodcutting --------------------------------------------------------------------------
	private static final int LOG_XP = 3;
	private static final int NETHER_STEM_XP = 4;
	private static final Set<Block> NETHER_STEMS = Set.of(
			Blocks.CRIMSON_STEM, Blocks.WARPED_STEM, Blocks.CRIMSON_HYPHAE, Blocks.WARPED_HYPHAE);

	// ---- Farming: only fully grown crops give xp ----------------------------------------------
	private static final Map<Block, Integer> CROP_XP = Map.of(
			Blocks.WHEAT, 4, Blocks.CARROTS, 4, Blocks.POTATOES, 4,
			Blocks.BEETROOTS, 5, Blocks.TORCHFLOWER_CROP, 10);
	private static final int OTHER_CROP_XP = 4;
	private static final int NETHER_WART_XP = 6;
	private static final int COCOA_XP = 5;
	private static final int MELON_PUMPKIN_XP = 3;

	// ---- Combat: from the victim's toughness --------------------------------------------------
	/** xp = max health * this + attack damage. */
	private static final double HEALTH_FACTOR = 0.5;
	/** Animals and other non-hostile mobs give this fraction of the xp. */
	private static final double PASSIVE_FACTOR = 0.5;
	/** Baby mobs give this fraction of the xp. */
	private static final double BABY_FACTOR = 0.5;
	private static final int MAX_MOB_XP = 250;

	// ---- Exploration --------------------------------------------------------------------------
	/** Blocks walked (horizontally) for 1 xp. */
	public static final double BLOCKS_PER_XP = 50;
	/** xp for entering a biome for the first time. */
	public static final int BIOME_XP = 40;
	/** xp for first entering one of the rarer biomes. */
	public static final int RARE_BIOME_XP = 100;
	public static final Set<String> RARE_BIOMES = Set.of(
			"minecraft:mushroom_fields", "minecraft:ice_spikes", "minecraft:deep_dark",
			"minecraft:eroded_badlands", "minecraft:cherry_grove", "minecraft:bamboo_jungle",
			"minecraft:pale_garden", "minecraft:the_end", "minecraft:soul_sand_valley",
			"minecraft:basalt_deltas");

	// ---- Fishing ------------------------------------------------------------------------------
	/** xp per catch. */
	public static final int FISH_XP = 20;

	/** True for ores (used by Vein Miner and Prospector). Obsidian gives xp but is not an ore. */
	public static boolean isOre(BlockState state) {
		Block block = state.getBlock();
		return block != Blocks.OBSIDIAN && (ORE_XP.containsKey(block) || state.is(BlockTags.ORES));
	}

	private static int scaled(double xp) {
		return Math.max(1, (int) Math.round(xp * XP_MULTIPLIER));
	}

	/**
	 * The reward for breaking this block, or {@code null} if it gives no xp.
	 *
	 * @param correctTool whether the player used a tool that makes the block drop (mining only)
	 * @param hardness    the block's hardness (used for stone-like blocks)
	 */
	public static Gain forBlock(BlockState state, boolean correctTool, float hardness) {
		Block block = state.getBlock();

		// Mining
		Integer ore = ORE_XP.get(block);
		if (ore != null) return correctTool ? new Gain(SkillCategory.MINING, scaled(ore)) : null;
		if (state.is(BlockTags.ORES)) return correctTool ? new Gain(SkillCategory.MINING, scaled(UNKNOWN_ORE_XP)) : null;
		if (state.is(BlockTags.BASE_STONE_OVERWORLD) || state.is(BlockTags.BASE_STONE_NETHER)) {
			if (!correctTool) return null;
			int xp = (int) Math.min(MAX_STONE_XP, Math.max(1, Math.round(hardness / STONE_HARDNESS_DIVISOR)));
			return new Gain(SkillCategory.MINING, scaled(xp));
		}

		// Woodcutting
		if (state.is(BlockTags.LOGS)) {
			return new Gain(SkillCategory.WOODCUTTING, scaled(NETHER_STEMS.contains(block) ? NETHER_STEM_XP : LOG_XP));
		}

		// Farming (mature crops only)
		if (block instanceof CropBlock crop) {
			if (!crop.isMaxAge(state)) return null;
			return new Gain(SkillCategory.FARMING, scaled(CROP_XP.getOrDefault(block, OTHER_CROP_XP)));
		}
		if (block instanceof NetherWartBlock) {
			return state.getValue(NetherWartBlock.AGE) == NetherWartBlock.MAX_AGE
					? new Gain(SkillCategory.FARMING, scaled(NETHER_WART_XP)) : null;
		}
		if (block instanceof CocoaBlock) {
			return state.getValue(CocoaBlock.AGE) == CocoaBlock.MAX_AGE
					? new Gain(SkillCategory.FARMING, scaled(COCOA_XP)) : null;
		}
		if (block == Blocks.MELON || block == Blocks.PUMPKIN) {
			return new Gain(SkillCategory.FARMING, scaled(MELON_PUMPKIN_XP));
		}
		return null;
	}

	/** Combat xp for killing this mob: tougher and more dangerous mobs give more. */
	public static int forKill(LivingEntity victim) {
		double xp = victim.getMaxHealth() * HEALTH_FACTOR;
		if (victim.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
			xp += victim.getAttributeValue(Attributes.ATTACK_DAMAGE);
		}
		if (!(victim instanceof Enemy)) xp *= PASSIVE_FACTOR;
		if (victim.isBaby()) xp *= BABY_FACTOR;
		return Math.min(MAX_MOB_XP, scaled(xp));
	}

	public static int forNewBiome(String biomeId) {
		return scaled(RARE_BIOMES.contains(biomeId) ? RARE_BIOME_XP : BIOME_XP);
	}

	public static int forDistance(double blocks) {
		return (int) Math.floor(blocks / BLOCKS_PER_XP * XP_MULTIPLIER);
	}

	public static int forFish() {
		return scaled(FISH_XP);
	}
}
