package smsk.smoothscroll;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen.CreativeScreenHandler;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.text.OrderedText;
import net.minecraft.util.math.ColorHelper.Argb;

import org.joml.Matrix4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmoothSc implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("Smooth Scrolling");
	public static final MinecraftClient mc = MinecraftClient.getInstance();

	public static Config cfg;
	public static int creativeScreenScrollOffset = 0;
	public static float creativeScreenTargetPos = 0;
	public static float creativeScreenCurrentPos = 0;
	public static int creativeScreenItemCount = 0;
	public static CreativeScreenHandler creativeSH;
	public static boolean creativeScreenScrollMixin = true;
	public static int creativeScreenPredRow = 0;
	public static int hotbarRollover = 0;

	@Override
	public void onInitialize() {
		updateConfig();
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
	public static int unmodifiedShadowedText(DrawContext drawContext, TextRenderer textRenderer, OrderedText text, int x, int y, int color) {
        // this is a workaround because immediately fast scissor doesn't work for text
        var a = textRenderer.draw(text, x, y, color, true,
                drawContext.getMatrices().peek().getPositionMatrix(), drawContext.getVertexConsumers(),
                TextRenderer.TextLayerType.NORMAL, 0, 15728880);
        drawContext.draw();
        return (a);
    }
	public static void unmodifiedFill(DrawContext drawContext, int x1, int y1, int x2, int y2, int color) {
        // this is a workaround because immediately fast scissor doesn't work for fill
        int z = 0;
        var layer = RenderLayer.getGui();

        Matrix4f matrix4f = drawContext.getMatrices().peek().getPositionMatrix();
        int i;
        if (x1 < x2) {
            i = x1;
            x1 = x2;
            x2 = i;
        }

        if (y1 < y2) {
            i = y1;
            y1 = y2;
            y2 = i;
        }

        float f = (float)Argb.getAlpha(color) / 255.0F;
        float g = (float)Argb.getRed(color) / 255.0F;
        float h = (float)Argb.getGreen(color) / 255.0F;
        float j = (float)Argb.getBlue(color) / 255.0F;
        VertexConsumer vertexConsumer = drawContext.getVertexConsumers().getBuffer(layer);
        vertexConsumer.vertex(matrix4f, (float)x1, (float)y1, (float)z).color(g, h, j, f).next();
        vertexConsumer.vertex(matrix4f, (float)x1, (float)y2, (float)z).color(g, h, j, f).next();
        vertexConsumer.vertex(matrix4f, (float)x2, (float)y2, (float)z).color(g, h, j, f).next();
        vertexConsumer.vertex(matrix4f, (float)x2, (float)y1, (float)z).color(g, h, j, f).next();
        drawContext.draw();
    }
}
