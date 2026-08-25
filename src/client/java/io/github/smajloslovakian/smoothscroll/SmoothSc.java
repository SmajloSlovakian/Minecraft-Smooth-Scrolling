package io.github.smajloslovakian.smoothscroll;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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
	//public static boolean isCondensedInventoryLoaded;
	public static boolean isWindows = System.getProperty("os.name") == "Windows";


	@Override
	public void onInitializeClient() {
		isSmoothScrollingRefurbishedLoaded = FabricLoader.getInstance().isModLoaded("smoothscrollingrefurbished");
		//isCondensedInventoryLoaded = FabricLoader.getInstance().isModLoaded("condensed_creative");
		//FabricLoader.getInstance().getObjectShare().put("smoothscroll:creative_screen/y_offset", 0);
		//FabricLoader.getInstance().getObjectShare().put("smoothscroll:creative_screen/item_count", 0);

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
	public static void debugTextDraw(GuiGraphicsExtractor graphics, Object s, int x, int y) {
		graphics.text(mc.font, s + "", x, y, ARGB.color(255, 0, 255, 255), true);
	}

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