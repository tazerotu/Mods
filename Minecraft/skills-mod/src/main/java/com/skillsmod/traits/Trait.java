package com.skillsmod.traits;

import com.skillsmod.SkillCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Base class of every trait. A trait knows its skill, its main stat, its perks, and reacts to game
 * events through the hooks below. Override only the hooks the trait needs; the rest do nothing.
 *
 * The central dispatcher ({@link TraitRegistry}) calls the hooks with the player's current level in
 * the trait's skill, so a trait never has to look the level up itself.
 */
public abstract class Trait {
	private final String id;
	private final SkillCategory skill;
	private final StatCurve stat;
	private final int unlockLevel;
	private final List<Perk> perks;

	/**
	 * @param statCap     the main stat at level 100
	 * @param unlockLevel 1 for normal traits, 20 for ability traits (Vein Miner, Tree Feller,
	 *                    Prospector, Double Harvest, Growth Aura)
	 */
	protected Trait(String id, SkillCategory skill, double statCap, int unlockLevel, List<Perk> perks) {
		this.id = id;
		this.skill = skill;
		this.stat = new StatCurve(statCap);
		this.unlockLevel = unlockLevel;
		this.perks = List.copyOf(perks);
	}

	public String id() { return id; }
	public SkillCategory skill() { return skill; }
	public List<Perk> perks() { return perks; }

	public boolean unlocked(int level) {
		return level >= unlockLevel;
	}

	/** The main stat at this level, or 0 while the trait is locked (earned stat is held until unlock). */
	public double stat(int level) {
		return unlocked(level) ? stat.at(level) : 0;
	}

	/** True if the perk with this id is unlocked at this level. */
	public boolean hasPerk(int level, String perkId) {
		if (!unlocked(level)) return false;
		for (Perk p : perks) {
			if (p.id().equals(perkId)) return p.level() <= level;
		}
		return false;
	}

	// ---- Hooks (all optional) -----------------------------------------------------------------

	/** Every few ticks per player. Use it for attribute modifiers and passive effects. */
	public void onTick(ServerPlayer player, int level) {}

	/** After the player broke a block. */
	public void onBlockBroken(ServerPlayer player, ServerLevel world, BlockPos pos, BlockState state, int level) {}

	/** After the player killed a mob. */
	public void onKill(ServerPlayer player, LivingEntity victim, int level) {}

	/** When the player pressed the ability key this trait's ability is bound to. */
	public void onAbilityKey(ServerPlayer player, Ability.Key key, int level) {}

	// TODO more hooks when a trait needs them, for example:
	//   onDamaged(player, source, amount, level)   (Iron Skin, Stoneskin, Battle Cry, Last Stand)
	//   onFall(player, distance, level)            (Fall Training)
	//   onFished(player, level)                    (Treasure Hunter)
}
