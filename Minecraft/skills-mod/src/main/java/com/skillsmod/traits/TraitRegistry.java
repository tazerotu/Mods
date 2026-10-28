package com.skillsmod.traits;

import com.skillsmod.SkillData;
import com.skillsmod.SkillCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Holds every trait and passes game events to them. Call {@link #init()} once at startup. */
public final class TraitRegistry {
	private TraitRegistry() {}

	private static final List<Trait> TRAITS = new ArrayList<>();

	public static void register(Trait trait) {
		TRAITS.add(trait);
	}

	public static List<Trait> all() {
		return List.copyOf(TRAITS);
	}

	public static void init() {
		// Combat
		// TODO register(new BruteForce());  register(new IronSkin());
		// Exploration
		// TODO register(new FallTraining());  register(new LightFeet());
		// Mining
		// TODO register(new MiningHaste());
		register(new SturdyPicks());
		// TODO register(new VeinMiner());  register(new Prospector());
		// Woodcutting
		// TODO register(new WoodcuttingHaste());  register(new SturdyAxes());  register(new TreeFeller());
		// Farming
		// TODO register(new GrowthAura());  register(new DoubleHarvest());
		// Fishing
		// TODO register(new TreasureHunter());  register(new SeaLegs());
	}

	// ---- Dispatch: SkillEvents calls these instead of calling traits one by one ----------------

	public static void tick(ServerPlayer player) {
		UUID id = player.getUUID();
		for (Trait t : TRAITS) t.onTick(player, SkillData.level(id, t.skill()));
	}

	public static void blockBroken(ServerPlayer player, ServerLevel world, BlockPos pos, BlockState state) {
		UUID id = player.getUUID();
		for (Trait t : TRAITS) t.onBlockBroken(player, world, pos, state, SkillData.level(id, t.skill()));
	}

	public static void kill(ServerPlayer player, LivingEntity victim) {
		UUID id = player.getUUID();
		for (Trait t : TRAITS) t.onKill(player, victim, SkillData.level(id, t.skill()));
	}

	/** Called by the server when a player presses an ability key. */
	public static void abilityKey(ServerPlayer player, Ability.Key key) {
		UUID id = player.getUUID();
		for (Trait t : TRAITS) t.onAbilityKey(player, key, SkillData.level(id, t.skill()));
	}
}
