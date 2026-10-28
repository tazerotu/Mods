package com.skillsmod.traits;

import com.skillsmod.SkillCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * EXAMPLE trait showing the pattern: the perk list is filled in from the design document, the hook
 * bodies are the part to write. Copy this file for every other trait.
 */
public final class SturdyPicks extends Trait {

	public SturdyPicks() {
		super("sturdy_picks", SkillCategory.MINING, 0.10, 1, List.of(
				new Perk(10, "anvil_know_how", "Anvil Know-How", "Anvil repairs on pickaxes cost 1 fewer level, never below 1"),
				new Perk(20, "last_gasp", "Last Gasp", "A breaking pickaxe survives at 1 durability for 3 seconds; cooldown 300 s; 3 uses per pickaxe"),
				new Perk(30, "quick_repair", "Quick Repair", "Rare +1 durability on ore"),
				new Perk(40, "field_repair", "Field Repair", "Use the repair material to restore durability; cooldown 120 s"),
				new Perk(50, "keen_mender", "Keen Mender", "Mending repairs 25% more per XP orb"),
				new Perk(60, "deep_tempering", "Deep Tempering", "Below Y=0 the no-durability chance is 50% higher"),
				new Perk(70, "pick_fighter", "Pick Fighter", "Attacking with a pickaxe costs less durability"),
				new Perk(80, "obsidian_resolve", "Obsidian Resolve", "Obsidian and ancient debris cost no durability"),
				new Perk(90, "frugal_fixer", "Frugal Fixer", "Grid-repairing two pickaxes gives a 20% bonus"),
				new Perk(100, "masterwork", "Masterwork", "Acts as one extra level of Unbreaking")));
	}

	@Override
	public void onBlockBroken(ServerPlayer player, ServerLevel world, BlockPos pos, BlockState state, int level) {
		// TODO: only when the held item is a pickaxe.
		// TODO: chance = stat(level), times 1.5 if hasPerk(level, "deep_tempering") and pos.getY() < 0.
		// TODO: skip durability entirely for obsidian / ancient debris if hasPerk(level, "obsidian_resolve").
		// TODO: on a successful roll, give the durability point back (see Traits.sturdyTool for the current approach).
	}
}
