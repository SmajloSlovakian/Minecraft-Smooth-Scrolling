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

import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import smsk.smoothscroll.cfg.SmScCfg;

public class SmoothSc implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("Smooth Scrolling");
	public static final MinecraftClient mc = MinecraftClient.getInstance();

	public static SmScCfg cfg;
    public static boolean isSmoothScrollingRefurbishedLoaded;
    public static boolean isCondensedInventoryLoaded;

	public static boolean scissorMatrixEnabled = false;
	public static float creativeScreenScrollOffset = 0;
	public static int creativeScreenItemCount = 0;
	public static CreativeScreenHandler creativeSH;
	public static boolean creativeScreenScrollMixin = true;
	public static int creativeScreenPrevRow = 0;

	public static int hotbarRollover = 0;

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
		return mc.getLastFrameDuration();
	}
	public static void debugTextDraw(DrawContext context, Object s, int x, int y) {
		context.drawText(mc.textRenderer, s + "", x, y, ColorHelper.Argb.getArgb(255, 0, 255, 255), true);
	}
	public static void matrixedScissor(DrawContext context, int x1, int y1, int x2, int y2) {

		context.getMatrices().push();
		context.getMatrices().translate(x1, y1, 0);
		context.getMatrices().scale(1, 1, 1);

		scissorMatrixEnabled = true;
		context.enableScissor(0, 0, x2 - x1, y2 - y1);
		scissorMatrixEnabled = false;

		context.getMatrices().pop();
	}

	static float scaleForMaskDebug = 2f;
	public static void debugMaskMatrix(DrawContext context, int mx, int my) {
		
		debugTextDraw(context, mx + ", " + my, 10, 10);
		context.fill(0, 0, 1, 1, ColorHelper.Argb.getArgb(255, 0, 255, 255));

		debugMaskRect(context, ColorHelper.Argb.getArgb(255, 255, 0, 0), 20, 20);

		context.getMatrices().push();
		context.getMatrices().scale(scaleForMaskDebug, scaleForMaskDebug, 1);
		context.getMatrices().translate(mx, my, 0);

		scissorMatrixEnabled = true;
		debugMaskRect(context, ColorHelper.Argb.getArgb(255, 0, 255, 0), 20, 20);
		scissorMatrixEnabled = false;

		context.getMatrices().pop();
		context.getMatrices().push();
		context.getMatrices().translate(mx, my, 0);
		context.getMatrices().scale(scaleForMaskDebug, scaleForMaskDebug, 1);

		scissorMatrixEnabled = true;
		debugMaskRect(context, ColorHelper.Argb.getArgb(255, 0, 0, 255), 20, 20);
		scissorMatrixEnabled = false;

		context.getMatrices().pop();
	}

	public static void debugMaskRect(DrawContext context, int col, int w, int h) {
		int acol = ColorHelper.Argb.lerp(0.75f, col, ColorHelper.Argb.getArgb(0, ColorHelper.Argb.getRed(col), ColorHelper.Argb.getGreen(col), ColorHelper.Argb.getBlue(col)));
		
		context.fill(-1, -1, 0, 0, col);
		context.fill(w, h, w+1, h+1, col);

		context.enableScissor(0, 0, w, h);

		context.fill(-1000, -1000, 999, -999, col);
		context.fill(-1000, 999, 1000, 1000, col);

		context.fill(-1000, -1000, -999, 999, col);
		context.fill(999, -1000, 1000, 1000, col);

		context.fill(-1000, -1000, 1000, 1000, acol);

		context.disableScissor();
	}

	public static Inventory getDelegatingInventory(ScreenHandler handler) {
		return DelegatingInventory.itemStackBased(creativeSH.itemList::get);
	}
    public static int getCreativeDrawOffset() {
        return Math.round(SmoothSc.creativeScreenScrollOffset) - Math.round(SmoothSc.creativeScreenScrollOffset) / 18 * 18;
    }
    public static int getCreativeScrollOffset() {
        return Math.round(SmoothSc.creativeScreenScrollOffset);
    }
	
	public static void drawHotbarRolloverMirror(DrawContext context, Identifier texture, int x, int hotbarWidth, int offset, int y, int width, int height) {
		//context.drawGuiTexture(RenderLayer::getGuiTextured, texture, x + hotbarWidth + offset, y, width, height);
		context.drawTexture(texture, x + hotbarWidth + offset, y, 0, 22, width, height);
	}
	public static Vector3f getMatrixTranslate(DrawContext context) {
		return context.getMatrices().peek().getPositionMatrix().getTranslation(new Vector3f(0,0,0));
	}
}