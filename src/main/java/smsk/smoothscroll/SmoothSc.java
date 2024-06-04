package smsk.smoothscroll;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen.CreativeScreenHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmoothSc implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("Smooth Scrolling");
	public static final MinecraftClient mc = MinecraftClient.getInstance();

	public static Config cfg;
    public static boolean isImmediatelyFastLoaded;

	public static int creativeScreenScrollOffset = 0;
	public static float creativeScreenTargetPos = 0;
	public static float creativeScreenCurrentPos = 0;
	public static int creativeScreenItemCount = 0;
	public static CreativeScreenHandler creativeSH;
	public static boolean creativeScreenScrollMixin = true;
	public static int creativeScreenPrevRow = 0;

	public static int hotbarRollover = 0;

	@Override
	public void onInitialize() {
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
		return (Math.max(min, Math.min(max, val)));
	}
}
