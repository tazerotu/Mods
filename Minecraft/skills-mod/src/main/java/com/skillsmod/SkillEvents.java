package com.skillsmod;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Reacts to what the player does: awards xp and triggers the traits. Runs on the server. */
public final class SkillEvents {
	private SkillEvents() {}

	/** Attribute traits (haste, speed...) are refreshed this often, in ticks. */
	private static final int ATTRIBUTE_INTERVAL = 5;
	/** Growth Aura, exploration and fishing are checked this often, in ticks. */
	private static final int SLOW_INTERVAL = 20;
	/** Moving further than this in one check is a teleport, not exploring. */
	private static final double TELEPORT_DISTANCE = 100;

	/** Things we remember about a player between ticks. */
	private static final class Tracker {
		Vec3 lastPos;
		ResourceKey<Level> lastDimension;
		double distanceCarry;
		final Set<String> biomes = new HashSet<>();
		int fishCaught = -1;
	}

	private static final Map<UUID, Tracker> TRACKERS = new HashMap<>();

	// ==========================================================================================
	// Breaking blocks: mining, woodcutting, farming
	// ==========================================================================================

	public static void onBlockBroken(Level world, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity) {
		if (!(player instanceof ServerPlayer sp) || !(world instanceof ServerLevel level)) return;
		UUID id = sp.getUUID();
		ItemStack tool = sp.getMainHandItem();
		float hardness = state.getDestroySpeed(level, pos);
		boolean correctTool = sp.hasCorrectToolForDrops(state);

		// xp
		SkillXp.Gain gain = SkillXp.forBlock(state, correctTool, hardness);
		if (gain != null) SkillData.addXp(id, gain.skill(), gain.xp());

		// Sturdy Picks / Sturdy Axes (blocks that cost no durability are left alone)
		if (hardness > 0) Traits.sturdyTool(sp, level.getRandom());

		// Prospector: extra ore drops
		if (SkillXp.isOre(state) && correctTool && tool.is(ItemTags.PICKAXES)) {
			double chance = Traits.PROSPECTOR.at(SkillData.level(id, SkillCategory.MINING));
			Traits.bonusDrops(sp, level, pos, state, blockEntity, chance);
		}

		// Double Harvest: extra crop drops (only when the crop gave farming xp, i.e. was mature)
		if (gain != null && gain.skill() == SkillCategory.FARMING) {
			double chance = Traits.DOUBLE_HARVEST.at(SkillData.level(id, SkillCategory.FARMING));
			Traits.bonusDrops(sp, level, pos, state, blockEntity, chance);
		}

		// Vein Miner / Tree Feller: hold sneak
		if (!VeinMiner.isBreaking() && sp.isShiftKeyDown()) {
			if (SkillXp.isOre(state) && tool.is(ItemTags.PICKAXES)) {
				int max = (int) Traits.VEIN_MINER.at(SkillData.level(id, SkillCategory.MINING));
				Block ore = state.getBlock();
				if (max > 0) VeinMiner.breakConnected(sp, pos, max, s -> s.is(ore));
			} else if (state.is(BlockTags.LOGS) && tool.is(ItemTags.AXES)) {
				int max = (int) Traits.TREE_FELLER.at(SkillData.level(id, SkillCategory.WOODCUTTING));
				if (max > 0) VeinMiner.breakConnected(sp, pos, max, s -> s.is(BlockTags.LOGS));
			}
		}
	}

	// ==========================================================================================
	// Kills: combat
	// ==========================================================================================

	public static void onDeath(LivingEntity entity, DamageSource source) {
		if (entity instanceof Player) return;
		if (source.getEntity() instanceof ServerPlayer killer) {
			SkillData.addXp(killer.getUUID(), SkillCategory.COMBAT, SkillXp.forKill(entity));
		}
	}

	// ==========================================================================================
	// Tick: attributes, Growth Aura, exploration, fishing
	// ==========================================================================================

	public static void onTick(MinecraftServer server) {
		int tick = server.getTickCount();
		boolean attributes = tick % ATTRIBUTE_INTERVAL == 0;
		boolean slow = tick % SLOW_INTERVAL == 0;
		if (!attributes && !slow) return;

		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			if (attributes) Traits.refreshAttributes(player);
			if (slow && !player.isSpectator()) {
				Traits.growthAura(player);
				Tracker tracker = TRACKERS.computeIfAbsent(player.getUUID(), id -> new Tracker());
				trackExploration(player, tracker);
				trackFishing(player, tracker);
			}
		}
	}

	private static void trackExploration(ServerPlayer player, Tracker t) {
		UUID id = player.getUUID();
		ServerLevel level = player.level();

		// Distance travelled (ignoring teleports and dimension changes)
		Vec3 pos = player.position();
		if (t.lastPos != null && level.dimension().equals(t.lastDimension)) {
			double dx = pos.x - t.lastPos.x;
			double dz = pos.z - t.lastPos.z;
			double distance = Math.sqrt(dx * dx + dz * dz);
			if (distance < TELEPORT_DISTANCE) t.distanceCarry += distance;
		}
		t.lastPos = pos;
		t.lastDimension = level.dimension();

		int xp = SkillXp.forDistance(t.distanceCarry);
		if (xp > 0) {
			SkillData.addXp(id, SkillCategory.EXPLORATION, xp);
			t.distanceCarry -= xp * SkillXp.BLOCKS_PER_XP / SkillXp.XP_MULTIPLIER;
		}

		// New biomes
		level.getBiome(player.blockPosition()).unwrapKey().ifPresent(key -> {
			String biome = key.identifier().toString();
			if (t.biomes.add(biome)) {
				SkillData.addXp(id, SkillCategory.EXPLORATION, SkillXp.forNewBiome(biome));
			}
		});
	}

	/** Fishing has no event in Fabric API, so we watch the player's "fish caught" statistic. */
	private static void trackFishing(ServerPlayer player, Tracker t) {
		int caught = player.getStats().getValue(Stats.CUSTOM.get(Stats.FISH_CAUGHT));
		if (t.fishCaught >= 0 && caught > t.fishCaught) {
			SkillData.addXp(player.getUUID(), SkillCategory.FISHING, (long) (caught - t.fishCaught) * SkillXp.forFish());
		}
		t.fishCaught = caught;
	}
}
