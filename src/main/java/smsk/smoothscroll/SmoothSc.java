package smsk.smoothscroll;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen.CreativeScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmoothSc implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("Smooth Scrolling");
	public static final MinecraftClient mc = MinecraftClient.getInstance();

	public static Config cfg;
    public static boolean isImmediatelyFastLoaded;

	public static float creativeScreenScrollOffset = 0;
	public static float creativeScreenTargetPos = 0;
	public static float creativeScreenCurrentPos = 0;
	public static int creativeScreenItemCount = 0;
	public static CreativeScreenHandler creativeSH;
	public static boolean creativeScreenScrollMixin = true;
	public static int creativeScreenPrevRow = 0;

	public static int hotbarRollover = 0;

	@Override
	public void onInitialize() { // TODO znovu prejsť na float pri offsetoch a tak... 
		updateConfig();
        isImmediatelyFastLoaded = FabricLoader.getInstance().isModLoaded("immediatelyfast");
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
	public static void drawHotbarRolloverMirror(DrawContext context, Identifier texture, int x, int hotbarWidth, int offset, int y, int width, int height) {
		context.drawTexture(texture, x + hotbarWidth + offset, y, 0, 22, width, height);
	}
	public static float getLastFrameDuration() {
		return mc.getLastFrameDuration();
	}
	public static void debugTextDraw(DrawContext context, Object s, int x, int y) {
		context.drawText(mc.textRenderer, s + "", x, y, ColorHelper.Argb.getArgb(255, 0, 255, 255), true);
	}
    public static int getCreativeDrawOffset() {
        return Math.round(SmoothSc.creativeScreenScrollOffset) - Math.round(SmoothSc.creativeScreenScrollOffset) / 18 * 18;
    }
    public static int getCreativeScrollOffset() {
        return Math.round(SmoothSc.creativeScreenScrollOffset);
    }
}