package smsk.smoothscroll.mixin.CreativeScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;

import smsk.smoothscroll.Config;
import smsk.smoothscroll.SmoothSc;

@Mixin(value = HandledScreen.class, priority = 999)
public class HandledScreenMixin {

    Identifier backTex = Identifier.ofVanilla("textures/gui/container/creative_inventory/tab_items");
    boolean cutEnabled = false;
    DrawContext savedContext;
    int originalCursorY;

    @Inject(method = "render", at = @At("HEAD"))
    void render(DrawContext context, int mx, int my, float d, CallbackInfo ci) {
        savedContext = context;
        originalCursorY = my;
        if (Config.cfg.creativeScreenSpeed == 0 || SmoothSc.creativeSH == null) return;

        SmoothSc.creativeScreenScrollOffset = (float) (SmoothSc.creativeScreenScrollOffset * Math.pow(Config.cfg.creativeScreenSpeed, SmoothSc.getLastFrameDuration()));

        SmoothSc.creativeScreenScrollMixin = false;
        SmoothSc.creativeSH.scrollItems(((CreativeScreenHandlerAccessor) SmoothSc.creativeSH)
                .getPos(SmoothSc.creativeScreenPrevRow - SmoothSc.getCreativeScrollOffset() / 18));
        SmoothSc.creativeScreenScrollMixin = true;

        // this shares the y offset of the items in the creative inventory i want other mods to follow for better compatibility
		FabricLoader.getInstance().getObjectShare().put("smoothscroll:creative_screen/y_offset", SmoothSc.getCreativeDrawOffset());
		FabricLoader.getInstance().getObjectShare().put("smoothscroll:creative_screen/item_count", SmoothSc.creativeScreenItemCount);
    }

    @Inject(method = "render", at = @At(shift = At.Shift.AFTER, value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V"))
    void renderMid0(DrawContext context, int mx, int my, float d, CallbackInfo ci, @Local(ordinal = 1) LocalIntRef mouseY) {
        if (Config.cfg.creativeScreenSpeed == 0 || SmoothSc.creativeScreenItemCount <= 0 || SmoothSc.getCreativeScrollOffset() == 0) return;
        context.enableScissor(0, context.getScaledWindowHeight() / 2 - 50, context.getScaledWindowWidth(), context.getScaledWindowHeight() / 2 + 38);
        cutEnabled = true;
        if(originalCursorY >= savedContext.getScaledWindowHeight() / 2 - 51 && originalCursorY <= savedContext.getScaledWindowHeight() / 2 + 38)
            mouseY.set(my - SmoothSc.getCreativeDrawOffset());

        // the fix for instantly disappearing items on the opposite side of scrolling...
        // it gets the items that just left the slots and draws them in the correct
        // position
        var overUnder = SmoothSc.getCreativeScrollOffset() < 0 ? 9 * 5 : -9;
        var currRow = SmoothSc.creativeScreenPrevRow - SmoothSc.getCreativeScrollOffset() / 18;
        var fromIndex = currRow * 9 + overUnder;
        for(int i = fromIndex; i >= 0 && i < SmoothSc.creativeSH.itemList.size() && i < fromIndex + 9; i++) {
            context.drawItem(SmoothSc.creativeSH.itemList.get(i), 9 + i % 9 * 18, SmoothSc.getCreativeDrawOffset() + (SmoothSc.getCreativeScrollOffset() > 0 ? 0 : 18 * 6));
        }
    }

    @ModifyVariable(method = "drawSlot", at = @At(value = "STORE"), ordinal = 1)
    int drawItemY(int y) {
        SmoothSc.creativeScreenItemCount -= 1;
        if (SmoothSc.creativeScreenItemCount < 0) tryDisableMask(savedContext);
        if (Config.cfg.creativeScreenSpeed == 0 || SmoothSc.creativeScreenItemCount < 0) return (y);
        return (y + SmoothSc.getCreativeDrawOffset());
    }
    @ModifyVariable(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawSlot(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/screen/slot/Slot;)V", shift = At.Shift.AFTER), argsOnly = true, ordinal = 1)
    int revertMousePos(int mouseY) {
        if (Config.cfg.creativeScreenSpeed == 0 || SmoothSc.creativeScreenItemCount < 0) return originalCursorY;
        return mouseY;
    }

    @ModifyArgs(method = "drawSlotHighlight", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;fillGradient(Lnet/minecraft/client/render/RenderLayer;IIIIIII)V"))
    private static void drawHighlightY(Args args) {
        if (Config.cfg.creativeScreenSpeed == 0 || SmoothSc.creativeScreenItemCount < 0) return;

        args.set(2, (int) args.get(2) + SmoothSc.getCreativeDrawOffset());
        args.set(4, (int) args.get(4) + SmoothSc.getCreativeDrawOffset());
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawForeground(Lnet/minecraft/client/gui/DrawContext;II)V"))
    void renderMid1(DrawContext context, int mx, int my, float d, CallbackInfo ci, @Local(ordinal = 1) int mouseY) {
        tryDisableMask(context);
        mouseY = originalCursorY;
    }

    void tryDisableMask(DrawContext context){
        if (!cutEnabled) return;
        if (Config.cfg.enableMaskDebug)
            context.fill(-100, -100, context.getScaledWindowWidth(), context.getScaledWindowHeight(), ColorHelper.Argb.getArgb(50, 0, 255, 255));
        context.disableScissor();
        cutEnabled = false;
    }
    //@ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;isPointOverSlot(Lnet/minecraft/screen/slot/Slot;DD)Z"), index = 2)
    //double modifyCursorPosY(double my) {
    //    if(!cutEnabled || originalCursorY < savedContext.getScaledWindowHeight() / 2 - 51 || originalCursorY > savedContext.getScaledWindowHeight() / 2 + 38) return my;
    //    return originalCursorY - SmoothSc.getCreativeDrawOffset();
    //}
}