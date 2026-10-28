package com.skillsmod;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/** Breaks a connected group of blocks (an ore vein or a tree) for Vein Miner and Tree Feller. */
public final class VeinMiner {
	private VeinMiner() {}

	/** True while we are breaking the extra blocks, so they do not trigger another vein. */
	private static boolean breaking = false;

	public static boolean isBreaking() {
		return breaking;
	}

	/**
	 * Breaks up to {@code maxBlocks} blocks touching {@code origin} (diagonals included) that match.
	 * The blocks are broken as if the player mined them: the tool loses durability, loot drops, and
	 * xp is awarded, but no new vein is started.
	 */
	public static void breakConnected(ServerPlayer player, BlockPos origin, int maxBlocks, Predicate<BlockState> matches) {
		Level level = player.level();
		List<BlockPos> found = new ArrayList<>();
		Set<BlockPos> seen = new HashSet<>();
		Deque<BlockPos> queue = new ArrayDeque<>();
		seen.add(origin);
		queue.add(origin);

		while (!queue.isEmpty() && found.size() < maxBlocks) {
			BlockPos current = queue.poll();
			for (int dx = -1; dx <= 1; dx++) {
				for (int dy = -1; dy <= 1; dy++) {
					for (int dz = -1; dz <= 1; dz++) {
						BlockPos next = current.offset(dx, dy, dz);
						if (found.size() >= maxBlocks || !seen.add(next)) continue;
						if (matches.test(level.getBlockState(next))) {
							found.add(next);
							queue.add(next);
						}
					}
				}
			}
		}

		breaking = true;
		try {
			for (BlockPos pos : found) {
				if (player.getMainHandItem().isEmpty()) break; // the tool broke
				player.gameMode.destroyBlock(pos);
			}
		} finally {
			breaking = false;
		}
	}
}
