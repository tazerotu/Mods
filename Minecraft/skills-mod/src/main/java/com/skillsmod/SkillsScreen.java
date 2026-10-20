package com.skillsmod;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/** Window listing every skill category with its experience bar. */
public class SkillsScreen extends Screen {
	private static final int PANEL_W = 280;
	private static final int PADDING = 12;
	private static final int TITLE_H = 28;
	private static final int ROW_H = 30;
	private static final int BAR_H = 8;
	private static final int FOOTER_H = 34;

	private static final int PANEL_BG = 0xE0101010;
	private static final int PANEL_BORDER = 0xFF555555;
	private static final int BAR_BG = 0xFF3A3A3A;
	private static final int BAR_BORDER = 0xFF000000;
	private static final int TEXT = 0xFFFFFFFF;
	private static final int TEXT_DIM = 0xFFAAAAAA;

	public SkillsScreen() {
		super(Component.translatable("screen." + SkillsModClient.MOD_ID + ".title"));
	}

	private int panelHeight() {
		return TITLE_H + SkillCategory.values().length * ROW_H + FOOTER_H;
	}

	private int panelLeft() {
		return (this.width - PANEL_W) / 2;
	}

	private int panelTop() {
		return (this.height - panelHeight()) / 2;
	}

	@Override
	protected void init() {
		int buttonW = 100;
		int x = panelLeft() + (PANEL_W - buttonW) / 2;
		int y = panelTop() + panelHeight() - FOOTER_H + 7;
		this.addRenderableWidget(
				Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
						.bounds(x, y, buttonW, 20)
						.build());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		// Draws the dimmed background and the widgets (the Done button).
		super.extractRenderState(graphics, mouseX, mouseY, delta);

		int left = panelLeft();
		int top = panelTop();
		int right = left + PANEL_W;
		int bottom = top + panelHeight();

		// Panel background + 1px border
		graphics.fill(left - 1, top - 1, right + 1, bottom + 1, PANEL_BORDER);
		graphics.fill(left, top, right, bottom, PANEL_BG);

		// Title
		String title = this.title.getString();
		graphics.text(this.font, title, left + (PANEL_W - this.font.width(title)) / 2, top + 10, TEXT, true);

		int barX = left + PADDING;
		int barW = PANEL_W - PADDING * 2;

		SkillCategory[] categories = SkillCategory.values();
		for (int i = 0; i < categories.length; i++) {
			SkillCategory category = categories[i];
			SkillProgress progress = SkillData.get(category);

			int rowTop = top + TITLE_H + i * ROW_H;
			int barY = rowTop + this.font.lineHeight + 3;

			// Above the bar, left: category name + level
			String name = Component.translatable(category.translationKey()).getString();
			String level = Component.translatable("screen." + SkillsModClient.MOD_ID + ".level", progress.level()).getString();
			graphics.text(this.font, name, barX, rowTop, category.color(), true);
			graphics.text(this.font, level, barX + this.font.width(name) + 6, rowTop, TEXT_DIM, true);

			// Above the bar, right: xp possessed / xp needed to level up
			String xp = Component.translatable("screen." + SkillsModClient.MOD_ID + ".xp", progress.xp(), progress.xpNeeded()).getString();
			graphics.text(this.font, xp, barX + barW - this.font.width(xp), rowTop, TEXT, true);

			// The bar itself
			graphics.fill(barX - 1, barY - 1, barX + barW + 1, barY + BAR_H + 1, BAR_BORDER);
			graphics.fill(barX, barY, barX + barW, barY + BAR_H, BAR_BG);
			int filled = (int) (barW * progress.fraction());
			if (filled > 0) {
				graphics.fill(barX, barY, barX + filled, barY + BAR_H, category.color());
			}
		}
	}
}
