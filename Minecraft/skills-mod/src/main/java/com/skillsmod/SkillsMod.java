package com.skillsmod;

import com.skillsmod.traits.TraitRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;

/** Common entrypoint: runs in singleplayer and on servers, and hooks gameplay to the skills. */
public class SkillsMod implements ModInitializer {
	public static final String MOD_ID = "skillsmod";

	@Override
	public void onInitialize() {
		TraitRegistry.init();
		PlayerBlockBreakEvents.AFTER.register(SkillEvents::onBlockBroken);
		ServerLivingEntityEvents.AFTER_DEATH.register(SkillEvents::onDeath);
		ServerTickEvents.END_SERVER_TICK.register(SkillEvents::onTick);
	}
}
