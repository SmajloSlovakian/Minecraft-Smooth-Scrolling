package smsk.smoothscroll;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen.CreativeScreenHandler;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smsk.smoothscroll.compat.CondensedInventoryCompat;

public class SmoothSc implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("Smooth Scrolling");
	public static final MinecraftClient mc = MinecraftClient.getInstance();

	public static Config cfg;
    public static boolean isSmoothScrollingRefurbishedLoaded;
    public static boolean isCondensedInventoryLoaded;
	public static double scissorScaleFactor; // used for non-scaling-dependant masking mainly for chat with chat text size changed

	public static float creativeScreenScrollOffset = 0;
	public static float creativeScreenTargetPos = 0;
	public static float creativeScreenCurrentPos = 0;
	public static int creativeScreenItemCount = 0;
	public static CreativeScreenHandler creativeSH;
	public static boolean creativeScreenScrollMixin = true;
	public static int creativeScreenPrevRow = 0;

	public static int hotbarRollover = 0;

	@Override
	public void onInitializeClient() {
        isSmoothScrollingRefurbishedLoaded = FabricLoader.getInstance().isModLoaded("smoothscrollingrefurbished");
        isCondensedInventoryLoaded = FabricLoader.getInstance().isModLoaded("condensed_creative");
		updateConfig();
		FabricLoader.getInstance().getObjectShare().put("smoothscroll:creative_screen/y_offset", 0);
		FabricLoader.getInstance().getObjectShare().put("smoothscroll:creative_screen/item_count", 0);
	}

	public static void print(Object s) {
		LOGGER.info(s + "");
	}
	public static void updateConfig() {
		cfg = new Config();
	}
	public static int clamp(int val, int min, int max) {
		return Math.max(min, Math.min(max, val));
	}
	public static float getLastFrameDuration() {
		return mc.getRenderTickCounter().getLastFrameDuration();
	}
	public static void debugTextDraw(DrawContext context, Object s, int x, int y) {
		context.drawText(mc.textRenderer, s + "", x, y, ColorHelper.Argb.getArgb(255, 0, 255, 255), true);
	}

	public static Inventory getDelegatingInventory(ScreenHandler handler) {
		return isCondensedInventoryLoaded
				? CondensedInventoryCompat.of(handler)
				: DelegatingInventory.itemStackBased(creativeSH.itemList::get);
	}
    public static int getCreativeDrawOffset() {
        return Math.round(SmoothSc.creativeScreenScrollOffset) - Math.round(SmoothSc.creativeScreenScrollOffset) / 18 * 18;
    }
    public static int getCreativeScrollOffset() {
        return Math.round(SmoothSc.creativeScreenScrollOffset);
    }
	
	public static void drawHotbarRolloverMirror(DrawContext context, Identifier texture, int x, int hotbarWidth, int offset, int y, int width, int height) {
		context.drawGuiTexture(texture, x + hotbarWidth + offset, y, width, height);
	}
}