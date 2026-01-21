package io.github.smajloslovakian.smoothscroll;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen.ItemPickerMenu;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import io.github.smajloslovakian.smoothscroll.SmoothSc;
import io.github.smajloslovakian.smoothscroll.cfg.SmScCfg;

public class SmoothSc implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("Smooth Scrolling");
	public static final Minecraft mc = Minecraft.getInstance();

	public static SmScCfg cfg;
	public static boolean isSmoothScrollingRefurbishedLoaded;
	public static boolean isCondensedInventoryLoaded;

	public static float creativeScreenScrollOffset = 0;
	public static int creativeScreenItemCount = 0;
	public static ItemPickerMenu creativeSH;
	public static boolean creativeScreenScrollMixin = true;
	public static int creativeScreenPrevRow = 0;

	public static int hotbarRollover = 0;
	public static boolean preciseScissor = false;


	@Override
	public void onInitializeClient() {
		isSmoothScrollingRefurbishedLoaded = FabricLoader.getInstance().isModLoaded("smoothscrollingrefurbished");
		isCondensedInventoryLoaded = FabricLoader.getInstance().isModLoaded("condensed_creative");
		FabricLoader.getInstance().getObjectShare().put("smoothscroll:creative_screen/y_offset", 0);
		FabricLoader.getInstance().getObjectShare().put("smoothscroll:creative_screen/item_count", 0);

		cfg = new SmScCfg();
	}

	public static void print(Object s) {
		LOGGER.info("" + s);
	}
	public static void printt(Object... s) {
		joinPrint(", ", s);
	}

	public static void joinPrint(String join, Object... s) {
		String a = "";
		for (Object object : s) {
			a += object + join;
		}
		LOGGER.info(a);
	}
	public static void readConfig() {
		cfg.loadAndSave();
	}
	public static int clamp(int val, int min, int max) {
		return Math.max(min, Math.min(max, val));
	}
	public static float getLastFrameDuration() {
		return mc.getDeltaTracker().getGameTimeDeltaTicks();
	}
	public static void debugTextDraw(GuiGraphics context, Object s, int x, int y) {
		context.drawString(mc.font, s + "", x, y, ARGB.color(255, 0, 255, 255), true);
	}

	public static Container getDelegatingInventory(AbstractContainerMenu handler) {
		return DelegatingInventory.itemStackBased(creativeSH.items::get); // TODO redo condensed creative inventory compatibility
		/*return isCondensedInventoryLoaded
				? CondensedInventoryCompat.of(handler)
				: DelegatingInventory.itemStackBased(creativeSH.items::get);*/
	}
	public static int getCreativeDrawOffset() {
        return Math.round(SmoothSc.creativeScreenScrollOffset) - Math.round(SmoothSc.creativeScreenScrollOffset) / 18 * 18;
	}
	public static int getCreativeScrollOffset() {
		return Math.round(SmoothSc.creativeScreenScrollOffset);
	}

	public static void drawHotbarRolloverMirror(GuiGraphics context, Identifier texture, int x, int hotbarWidth, int offset, int y, int width, int height) {
		context.blitSprite(RenderPipelines.GUI_TEXTURED, texture, x + hotbarWidth + offset, y, width, height);
	}
	/*
	public static Vector3f getMatrixTranslate(DrawContext context) {
		return context.getMatrices().peek().getPositionMatrix().getTranslation(new Vector3f(0,0,0));
	}/* */
	public static UVPair vec2fAdd(UVPair a, UVPair b) {
		return new UVPair(a.u() + b.u(), a.v() + b.v());
	}
	public static UVPair vec2fSub(UVPair a, UVPair b) {
		return new UVPair(a.u() - b.u(), a.v() - b.v());
	}
	public static UVPair vec2fMul(UVPair a, float b) {
		return new UVPair(a.u() * b, a.v() * b);
	}
}