package com.skillsmod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Every trait (perk) of every skill, and the code that applies it.
 *
 * All the numbers live in the {@link Scaling} constants right below: change them to retune a trait.
 * A trait does nothing until its skill reaches {@code unlock}.
 */
public final class Traits {
	private Traits() {}

	// ---- Combat -------------------------------------------------------------------------------
	/** Bonus attack damage (flat). */
	public static final Scaling BRUTE_FORCE = new Scaling(5, 10, 0.5, 0.5, 3.0);
	/** Bonus armor points (flat). */
	public static final Scaling IRON_SKIN = new Scaling(10, 15, 1, 1, 5);

	// ---- Exploration (acrobatics merged in) ---------------------------------------------------
	/** Fraction of fall damage ignored (0.5 = half). */
	public static final Scaling FALL_TRAINING = new Scaling(5, 5, 0.05, 0.03, 0.50);
	/** Bonus movement speed (0.08 = +8%). */
	public static final Scaling LIGHT_FEET = new Scaling(10, 15, 0.02, 0.01, 0.08);

	// ---- Mining -------------------------------------------------------------------------------
	/** Haste: flat bonus to mining speed while holding a pickaxe (works on the right blocks only). */
	public static final Scaling MINING_HASTE = new Scaling(5, 10, 0.5, 0.5, 4.0);
	/** Chance that a block broken with a pickaxe costs no durability (Unbreaking-like). */
	public static final Scaling STURDY_PICKS = new Scaling(10, 10, 0.05, 0.05, 0.40);
	/** Vein Miner: max extra blocks per vein (hold sneak, ores only). */
	public static final Scaling VEIN_MINER = new Scaling(30, 20, 8, 8, 32);
	/** Prospector: chance of a second set of drops from an ore. */
	public static final Scaling PROSPECTOR = new Scaling(50, 10, 0.05, 0.03, 0.20);

	// ---- Woodcutting --------------------------------------------------------------------------
	/** Haste: flat bonus to mining speed while holding an axe. */
	public static final Scaling WOODCUTTING_HASTE = new Scaling(5, 10, 0.5, 0.5, 4.0);
	/** Chance that a block broken with an axe costs no durability. */
	public static final Scaling STURDY_AXES = new Scaling(10, 10, 0.05, 0.05, 0.40);
	/** Tree Feller: max extra logs per tree (hold sneak). */
	public static final Scaling TREE_FELLER = new Scaling(30, 20, 16, 16, 64);

	// ---- Farming ------------------------------------------------------------------------------
	/** Growth Aura, chance per second to bonemeal one crop around you. */
	public static final Scaling GROWTH_CHANCE = new Scaling(10, 10, 0.05, 0.05, 0.35);
	/** Growth Aura, radius in blocks. */
	public static final Scaling GROWTH_RADIUS = new Scaling(10, 15, 2, 1, 7);
	/** Double Harvest: chance of a second set of drops from a mature crop. */
	public static final Scaling DOUBLE_HARVEST = new Scaling(20, 10, 0.05, 0.05, 0.30);

	// ---- Fishing ------------------------------------------------------------------------------
	/** Treasure Hunter: bonus Luck, which improves fishing loot (also nudges other luck-based loot). */
	public static final Scaling TREASURE_HUNTER = new Scaling(10, 25, 1, 1, 3);
	/** Sea Legs: less slowdown when moving in water (1.0 would mean none). */
	public static final Scaling SEA_LEGS = new Scaling(5, 15, 0.1, 0.1, 0.5);

	// ==========================================================================================
	// Attribute based traits (applied by the server every few ticks)
	// ==========================================================================================

	/** Recomputes every attribute trait of this player. Cheap: only changed values are written. */
	public static void refreshAttributes(ServerPlayer p) {
		UUID id = p.getUUID();
		int combat = SkillData.level(id, SkillCategory.COMBAT);
		int explo = SkillData.level(id, SkillCategory.EXPLORATION);
		int mining = SkillData.level(id, SkillCategory.MINING);
		int wood = SkillData.level(id, SkillCategory.WOODCUTTING);
		int fishing = SkillData.level(id, SkillCategory.FISHING);

		ItemStack held = p.getMainHandItem();
		boolean pick = held.is(ItemTags.PICKAXES);
		boolean axe = held.is(ItemTags.AXES);

		set(p, Attributes.ATTACK_DAMAGE, "brute_force", BRUTE_FORCE.at(combat), Operation.ADD_VALUE);
		set(p, Attributes.ARMOR, "iron_skin", IRON_SKIN.at(combat), Operation.ADD_VALUE);

		set(p, Attributes.FALL_DAMAGE_MULTIPLIER, "fall_training", -FALL_TRAINING.at(explo), Operation.ADD_MULTIPLIED_TOTAL);
		set(p, Attributes.MOVEMENT_SPEED, "light_feet", LIGHT_FEET.at(explo), Operation.ADD_MULTIPLIED_BASE);

		set(p, Attributes.MINING_EFFICIENCY, "mining_haste", pick ? MINING_HASTE.at(mining) : 0, Operation.ADD_VALUE);
		set(p, Attributes.MINING_EFFICIENCY, "woodcutting_haste", axe ? WOODCUTTING_HASTE.at(wood) : 0, Operation.ADD_VALUE);

		set(p, Attributes.LUCK, "treasure_hunter", TREASURE_HUNTER.at(fishing), Operation.ADD_VALUE);
		set(p, Attributes.WATER_MOVEMENT_EFFICIENCY, "sea_legs", SEA_LEGS.at(fishing), Operation.ADD_VALUE);
	}

	private static void set(ServerPlayer p, Holder<Attribute> attribute, String name, double amount, Operation op) {
		AttributeInstance instance = p.getAttribute(attribute);
		if (instance == null) return;
		Identifier key = Identifier.fromNamespaceAndPath(SkillsMod.MOD_ID, name);
		AttributeModifier current = instance.getModifier(key);
		if (amount == 0) {
			if (current != null) instance.removeModifier(key);
		} else if (current == null || current.amount() != amount) {
			instance.addOrUpdateTransientModifier(new AttributeModifier(key, amount, op));
		}
	}

	// ==========================================================================================
	// Durability, bonus drops
	// ==========================================================================================

	/** Sturdy Picks / Sturdy Axes: after a block break, sometimes gives the durability point back. */
	public static void sturdyTool(ServerPlayer p, RandomSource random) {
		ItemStack tool = p.getMainHandItem();
		if (tool.isEmpty() || !tool.isDamageableItem() || tool.getDamageValue() <= 0) return;

		double chance = 0;
		if (tool.is(ItemTags.PICKAXES)) chance = STURDY_PICKS.at(SkillData.level(p.getUUID(), SkillCategory.MINING));
		else if (tool.is(ItemTags.AXES)) chance = STURDY_AXES.at(SkillData.level(p.getUUID(), SkillCategory.WOODCUTTING));

		if (chance > 0 && random.nextDouble() < chance) {
			tool.setDamageValue(tool.getDamageValue() - 1);
		}
	}

	/** Prospector / Double Harvest: rolls the chance, then drops the block's loot a second time. */
	public static void bonusDrops(ServerPlayer p, ServerLevel level, BlockPos pos, BlockState state,
								  BlockEntity blockEntity, double chance) {
		if (chance <= 0 || p.isCreative() || level.getRandom().nextDouble() >= chance) return;
		for (ItemStack drop : Block.getDrops(state, level, pos, blockEntity, p, p.getMainHandItem())) {
			Block.popResource(level, pos, drop);
		}
	}

	// ==========================================================================================
	// Growth Aura
	// ==========================================================================================

	/** Called once per second per player. May bonemeal one random crop around the player. */
	public static void growthAura(ServerPlayer p) {
		int farming = SkillData.level(p.getUUID(), SkillCategory.FARMING);
		double chance = GROWTH_CHANCE.at(farming);
		if (chance <= 0) return;

		ServerLevel level = p.level();
		RandomSource random = level.getRandom();
		if (random.nextDouble() >= chance) return;

		int r = (int) GROWTH_RADIUS.at(farming);
		BlockPos origin = p.blockPosition();
		List<BlockPos> candidates = new ArrayList<>();
		for (int dx = -r; dx <= r; dx++) {
			for (int dy = -2; dy <= 2; dy++) {
				for (int dz = -r; dz <= r; dz++) {
					BlockPos pos = origin.offset(dx, dy, dz);
					BlockState state = level.getBlockState(pos);
					if (state.is(BlockTags.CROPS)
							&& state.getBlock() instanceof BonemealableBlock b
							&& b.isValidBonemealTarget(level, pos, state, BonemealSource.INTERACTION)) {
						candidates.add(pos);
					}
				}
			}
		}
		if (candidates.isEmpty()) return;

		BlockPos target = candidates.get(random.nextInt(candidates.size()));
		BlockState state = level.getBlockState(target);
		BonemealableBlock block = (BonemealableBlock) state.getBlock();
		if (block.isBonemealSuccess(level, random, target, state, BonemealSource.INTERACTION)) {
			block.performBonemeal(level, random, target, state, BonemealSource.INTERACTION);
		}
		BoneMealItem.addGrowthParticles(level, target, 10);
	}
}
