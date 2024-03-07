package smsk.smoothscroll.mixin.CreativeScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import smsk.smoothscroll.Config;
import smsk.smoothscroll.SmoothSc;

@Mixin(HandledScreen.class)
public class HandledScreenMixin {

    Identifier backtex = new Identifier("textures/gui/container/creative_inventory/tab_items");
    boolean cutenabled = false;
    float lFDBuffer;

    @Inject(method = "render", at = @At("HEAD"))
    void render(DrawContext context, int mx, int my, float d, CallbackInfo ci) {
        if (Config.cfg.creativeScreenSpeed == 0 || SmoothSc.creativeSH == null) return;

        lFDBuffer += SmoothSc.mc.getLastFrameDuration();
        var a = SmoothSc.creativeScreenScrollOffset;
        SmoothSc.creativeScreenScrollOffset = (int) Math.round(SmoothSc.creativeScreenScrollOffset * Math.pow(Config.cfg.creativeScreenSpeed, lFDBuffer));
        if (a != SmoothSc.creativeScreenScrollOffset || SmoothSc.creativeScreenScrollOffset == 0) lFDBuffer = 0;

        SmoothSc.creativeScreenScrollMixin = false;
        SmoothSc.creativeSH.scrollItems(((CreativeScreenHandlerAccessor) SmoothSc.creativeSH)
                .getPos(SmoothSc.creativeScreenPredRow - SmoothSc.creativeScreenScrollOffset / 18));
        SmoothSc.creativeScreenScrollMixin = true;
    }

    @ModifyArg(method = "drawSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawItem(Lnet/minecraft/item/ItemStack;III)V"), index = 2)
    int drawItemY(int y) {
        if (Config.cfg.creativeScreenSpeed == 0 || SmoothSc.creativeScreenItemCount <= 0) return (y);
        SmoothSc.creativeScreenItemCount -= 1;
        return (y + SmoothSc.creativeScreenScrollOffset - SmoothSc.creativeScreenScrollOffset / 18 * 18);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawSlot(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/screen/slot/Slot;)V"))
    void renderMid0(DrawContext context, int mx, int my, float d, CallbackInfo ci) {
        if (Config.cfg.creativeScreenSpeed == 0 || SmoothSc.creativeScreenItemCount <= 0 || SmoothSc.creativeScreenScrollOffset == 0) return;
        context.enableScissor(0, context.getScaledWindowHeight() / 2 - 50, context.getScaledWindowWidth(), context.getScaledWindowHeight() / 2 + 38);
        cutenabled = true;
    }

    @Inject(method = "render", at = @At(shift = Shift.AFTER, value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawSlot(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/screen/slot/Slot;)V"))
    void renderMid1(DrawContext context, int mx, int my, float d, CallbackInfo ci) {
        if (!cutenabled) return;
        if (Config.cfg.enableMaskDebug)
            context.fill(-100, -100, context.getScaledWindowWidth(), context.getScaledWindowHeight(), ColorHelper.Argb.getArgb(50, 0, 255, 255));
        context.disableScissor();
        cutenabled = false;
    }
}
