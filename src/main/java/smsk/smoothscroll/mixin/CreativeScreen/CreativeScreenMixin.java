package smsk.smoothscroll.mixin.CreativeScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.math.ColorHelper;
import smsk.smoothscroll.SmoothSc;
import smsk.smoothscroll.cfg.SmScCfg;

@Mixin(CreativeInventoryScreen.class)
public class CreativeScreenMixin {

    @Shadow
    private static ItemGroup selectedTab;

    @Inject(method = "setSelectedTab", at = @At("TAIL"))
    private void setSelectedTabT(ItemGroup group, CallbackInfo ci) {
        SmoothSc.creativeScreenScrollOffset = 0;
    }

    @Inject(method = "drawBackground", at = @At(value = "INVOKE", shift = Shift.AFTER, target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIFFIIII)V"))
    private void drawBackground(DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        if (SmoothSc.getCreativeScrollOffset() == 0 || SmScCfg.creativeScreenSmoothness == 0 || SmoothSc.creativeSH == null) return;

        if (FabricLoader.getInstance().getObjectShare().get("flow:is_caching_screen") instanceof Boolean isCaching
                && isCaching)
            return;
        
        SmoothSc.creativeScreenScrollOffset = (float) ((SmoothSc.creativeScreenScrollOffset)
                * Math.pow(SmScCfg.creativeScreenSmoothness, SmoothSc.getLastFrameDuration()));

        SmoothSc.creativeScreenScrollMixin = false;
        SmoothSc.creativeSH.scrollItems(((CreativeScreenHandlerAccessor) SmoothSc.creativeSH)
                .getPos(SmoothSc.creativeScreenPrevRow - SmoothSc.getCreativeScrollOffset() / 18));
        SmoothSc.creativeScreenScrollMixin = true;

        int posx = Math.round(context.getScaledWindowWidth() / 2f) - 90;
        int posy = context.getScaledWindowHeight() / 2 - 51;
        int width = 162;
        int height = 90;
        int u = 8;
        int v = 17;


        //context.drawText(SmoothSc.mc.textRenderer, mouseX + " - " + mouseY, 10, 10, ColorHelper.getArgb(255, 0, 255, 255), true);
        //context.fill(0, 0, 1920, 1080, ColorHelper.getArgb(50, 255, 128, 0));
        context.enableScissor(posx, posy + 1, posx + width, posy + height - 1);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, selectedTab.getTexture(), posx, 
            posy + SmoothSc.getCreativeDrawOffset(),
                u, v, width, height, 256, 256);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, selectedTab.getTexture(), posx,
            (int) (posy + SmoothSc.getCreativeDrawOffset() - height * Math.signum(SmoothSc.getCreativeScrollOffset())),
                u, v, width, height, 256, 256);

        if (SmScCfg.enableMaskDebug)
            context.fill(-100, -100, context.getScaledWindowWidth(), context.getScaledWindowHeight(), ColorHelper.getArgb(50, 255, 255, 0));
        
        context.disableScissor();
    }
}
