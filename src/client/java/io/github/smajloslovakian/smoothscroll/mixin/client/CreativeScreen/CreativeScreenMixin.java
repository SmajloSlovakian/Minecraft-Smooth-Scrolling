package io.github.smajloslovakian.smoothscroll.mixin.client.CreativeScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.CreativeModeTab;
import io.github.smajloslovakian.smoothscroll.SmoothSc;
import io.github.smajloslovakian.smoothscroll.cfg.SmScCfg;

@Mixin(CreativeModeInventoryScreen.class)
public class CreativeScreenMixin {

    @Shadow
    private static CreativeModeTab selectedTab;

    @Inject(method = "selectTab", at = @At("TAIL"))
    private void setSelectedTabT(CreativeModeTab group, CallbackInfo ci) {
        SmoothSc.creativeScreenScrollOffset = 0;
    }

    @Inject(method = "renderBg", at = @At(value = "INVOKE", shift = Shift.AFTER, target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIII)V"))
    private void drawBackground(GuiGraphics context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        if (SmoothSc.getCreativeScrollOffset() == 0 || SmScCfg.creativeScreenSmoothness == 0 || SmoothSc.creativeSH == null) return;

        if (FabricLoader.getInstance().getObjectShare().get("flow:is_caching_screen") instanceof Boolean isCaching
                && isCaching)
            return;
        
        SmoothSc.creativeScreenScrollOffset = (float) ((SmoothSc.creativeScreenScrollOffset)
                * Math.pow(SmScCfg.creativeScreenSmoothness, SmoothSc.getLastFrameDuration()));

        SmoothSc.creativeScreenScrollMixin = false;
        SmoothSc.creativeSH.scrollTo(((CreativeScreenHandlerAccessor) SmoothSc.creativeSH)
                .getPos(SmoothSc.creativeScreenPrevRow - SmoothSc.getCreativeScrollOffset() / 18));
        SmoothSc.creativeScreenScrollMixin = true;

        int posx = Math.round(context.guiWidth() / 2f) - 90;
        int posy = context.guiHeight() / 2 - 51;
        int width = 162;
        int height = 90;
        int u = 8;
        int v = 17;


        //context.drawText(SmoothSc.mc.textRenderer, mouseX + " - " + mouseY, 10, 10, ColorHelper.getArgb(255, 0, 255, 255), true);
        //context.fill(0, 0, 1920, 1080, ColorHelper.getArgb(50, 255, 128, 0));
        context.enableScissor(posx, posy + 1, posx + width, posy + height - 1);
        context.blit(RenderPipelines.GUI_TEXTURED, selectedTab.getBackgroundTexture(), posx, 
            posy + SmoothSc.getCreativeDrawOffset(),
                u, v, width, height, 256, 256);
        context.blit(RenderPipelines.GUI_TEXTURED, selectedTab.getBackgroundTexture(), posx,
            (int) (posy + SmoothSc.getCreativeDrawOffset() - height * Math.signum(SmoothSc.getCreativeScrollOffset())),
                u, v, width, height, 256, 256);

        if (SmScCfg.enableMaskDebug)
            context.fill(-100, -100, context.guiWidth(), context.guiHeight(), ARGB.color(50, 255, 255, 0));
        
        context.disableScissor();
    }
}
