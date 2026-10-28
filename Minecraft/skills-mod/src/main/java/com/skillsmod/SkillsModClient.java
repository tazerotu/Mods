package com.skillsmod;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public class SkillsModClient implements ClientModInitializer {
	public static final String MOD_ID = SkillsMod.MOD_ID;

	private static final KeyMapping.Category CATEGORY =
			KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "main"));

	private static final KeyMapping OPEN_SKILLS = KeyMappingHelper.registerKeyMapping(
        	new KeyMapping(
              		"key." + MOD_ID + ".open",
              		InputConstants.KEY_K,
                	CATEGORY));

	@Override
	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (OPEN_SKILLS.consumeClick()) {
				// Only open from in-game (no other screen open)
				if (client.player != null && client.gui.screen() == null) {
					client.gui.setScreen(new SkillsScreen());
				}
			}
		});
	}
}
