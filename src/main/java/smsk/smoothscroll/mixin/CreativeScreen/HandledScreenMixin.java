package smsk.smoothscroll.mixin.CreativeScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import smsk.smoothscroll.Config;
import smsk.smoothscroll.SmoothSc;

@Mixin(HandledScreen.class)
public class HandledScreenMixin {

    Identifier backTex = new Identifier("textures/gui/container/creative_inventory/tab_items");
    boolean cutEnabled = false;
    float lFDBuffer;
    DrawContext savedContext;

    @Inject(method = "render", at = @At("HEAD"))
    void render(DrawContext context, int mx, int my, float d, CallbackInfo ci) {
        if (Config.cfg.creativeScreenSpeed == 0 || SmoothSc.creativeSH == null) return;
        savedContext = context;

        lFDBuffer += SmoothSc.mc.getLastFrameDuration();
        var a = SmoothSc.creativeScreenScrollOffset;
        SmoothSc.creativeScreenScrollOffset = (int) Math.round(SmoothSc.creativeScreenScrollOffset * Math.pow(Config.cfg.creativeScreenSpeed, lFDBuffer));
        if (a != SmoothSc.creativeScreenScrollOffset || SmoothSc.creativeScreenScrollOffset == 0) lFDBuffer = 0;
        // this shares the y offset of the items in the creative inventory i want other mods to follow for better compatibility
		FabricLoader.getInstance().getObjectShare().put("smoothsc:creative_screen/y_offset", SmoothSc.creativeScreenScrollOffset - SmoothSc.creativeScreenScrollOffset / 18 * 18);
		FabricLoader.getInstance().getObjectShare().put("smoothsc:creative_screen/item_count", SmoothSc.creativeScreenItemCount);

        SmoothSc.creativeScreenScrollMixin = false;
        SmoothSc.creativeSH.scrollItems(((CreativeScreenHandlerAccessor) SmoothSc.creativeSH)
                .getPos(SmoothSc.creativeScreenPrevRow - SmoothSc.creativeScreenScrollOffset / 18));
        SmoothSc.creativeScreenScrollMixin = true;
    }

    @Inject(method = "render", at = @At(shift = At.Shift.AFTER, value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V"))
    void renderMid0(DrawContext context, int mx, int my, float d, CallbackInfo ci) {
        if (Config.cfg.creativeScreenSpeed == 0 || SmoothSc.creativeScreenItemCount <= 0 || SmoothSc.creativeScreenScrollOffset == 0) return;
        context.enableScissor(0, context.getScaledWindowHeight() / 2 - 50, context.getScaledWindowWidth(), context.getScaledWindowHeight() / 2 + 38);
        cutEnabled = true;

        // the fix for instantly disappearing items on the opposite side of scrolling...
        // it gets the items that just left the slots and draws them in the correct
        // position
        var overUnder = SmoothSc.creativeScreenScrollOffset < 0 ? 9 * 5 : -9;
        var currRow = SmoothSc.creativeScreenPrevRow - SmoothSc.creativeScreenScrollOffset / 18;
        var fromIndex = currRow * 9 + overUnder;
        for(int i = fromIndex; i >= 0 && i < SmoothSc.creativeSH.itemList.size() && i < fromIndex + 9; i++) {
            context.drawItem(SmoothSc.creativeSH.itemList.get(i), 9 + i % 9 * 18, SmoothSc.creativeScreenScrollOffset - SmoothSc.creativeScreenScrollOffset / 18 * 18 + (SmoothSc.creativeScreenScrollOffset > 0 ? 0 : 18 * 6));
        }
    }

    @ModifyVariable(method = "drawSlot", at = @At(value = "STORE"), ordinal = 1)
    int drawItemY(int y) {
        SmoothSc.creativeScreenItemCount -= 1;
        if (SmoothSc.creativeScreenItemCount < 0) tryDisableMask(savedContext);
        if (Config.cfg.creativeScreenSpeed == 0 || SmoothSc.creativeScreenItemCount < 0) return (y);
        return (y + SmoothSc.creativeScreenScrollOffset - SmoothSc.creativeScreenScrollOffset / 18 * 18);
    }

    @ModifyVariable(method = "render", at = @At(value = "STORE"),ordinal = 6)
    int drawHighlightY(int y) {
        if (Config.cfg.creativeScreenSpeed == 0 || SmoothSc.creativeScreenItemCount < 0) return (y);
        return (y + SmoothSc.creativeScreenScrollOffset - SmoothSc.creativeScreenScrollOffset / 18 * 18);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawForeground(Lnet/minecraft/client/gui/DrawContext;II)V"))
    void renderMid1(DrawContext context, int mx, int my, float d, CallbackInfo ci) {
        tryDisableMask(context);
    }

    void tryDisableMask(DrawContext context){
        if (!cutEnabled) return;
        if (Config.cfg.enableMaskDebug)
            context.fill(-100, -100, context.getScaledWindowWidth(), context.getScaledWindowHeight(), ColorHelper.Argb.getArgb(50, 0, 255, 255));
        context.disableScissor();
        cutEnabled = false;
    }
}