package smsk.smoothscroll;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen.CreativeScreenHandler;
import net.minecraft.util.math.MathHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmoothSc implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("smoothscroll");
    public static final MinecraftClient mc = MinecraftClient.getInstance();

	public static int creativeScreenOffsetY=0;
	public static float creativeScreenTargetPos=0;
	public static float creativeScreenCurrentPos=0;
	public static int creativeScreenItemCount=0;
	public static CreativeScreenHandler creativeSH;
	public static boolean creativeScreenScrollMixin=true;
	public static int creativeScreenPredRow=0;
	public static int hotbarRollover=0;
	public static Config cfg;
	public static int chatOffsetY=0;

	@Override
	public void onInitialize() {
		updateConfig();
	}
	public static void print(Object s){
		LOGGER.info(s+"");
	}
    public static float creativeScreenGetPos(int row,CreativeScreenHandler that){
        return MathHelper.clamp((float)row / ((float)MathHelper.ceilDiv(SmoothSc.creativeSH.itemList.size(), 9) - 5), 0.0F, 1.0F);
    }
	public static void updateConfig(){
		cfg=new Config();
	}
}
