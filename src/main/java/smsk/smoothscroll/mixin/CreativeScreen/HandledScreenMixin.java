package smsk.smoothscroll.mixin.CreativeScreen;

import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalDoubleRef;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import smsk.smoothscroll.SmoothSc;
import smsk.smoothscroll.cfg.SmScCfg;

@Mixin(value = HandledScreen.class, priority = 999)
public abstract class HandledScreenMixin<T extends ScreenHandler> {

    @Shadow @Final protected T handler;

    @Shadow protected abstract void drawSlot(DrawContext context, Slot slot);

    @Unique private final Identifier backTex = Identifier.ofVanilla("textures/gui/container/creative_inventory/tab_items");
    @Unique private boolean cutEnabled = false;
    @Unique private int originalCursorY;
    @Unique private boolean drawingOverdrawnSlot = false;

    @Inject(method = "render", at = @At("HEAD"))
    private void render(DrawContext context, int mx, int my, float d, CallbackInfo ci) {
        this.originalCursorY = my;
        if (SmScCfg.creativeScreenSpeed == 0 || SmoothSc.creativeSH == null || SmoothSc.getCreativeScrollOffset() == 0) return;

        SmoothSc.creativeScreenScrollOffset = (float) (SmoothSc.creativeScreenScrollOffset * Math.pow(SmScCfg.creativeScreenSpeed, SmoothSc.getLastFrameDuration()));

        SmoothSc.creativeScreenScrollMixin = false;
        SmoothSc.creativeSH.scrollItems(((CreativeScreenHandlerAccessor) SmoothSc.creativeSH)
                .getPos(SmoothSc.creativeScreenPrevRow - SmoothSc.getCreativeScrollOffset() / 18));
        SmoothSc.creativeScreenScrollMixin = true;

        // this shares the y offset of the items in the creative inventory i want other mods to follow for better compatibility
		FabricLoader.getInstance().getObjectShare().put("smoothscroll:creative_screen/y_offset", SmoothSc.getCreativeDrawOffset());
		FabricLoader.getInstance().getObjectShare().put("smoothscroll:creative_screen/item_count", SmoothSc.creativeScreenItemCount);
    }

    @Inject(method = "render", at = @At(shift = At.Shift.AFTER, value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V"))
    private void renderMid0(DrawContext context, int mx, int my, float d, CallbackInfo ci, @Local(ordinal = 1, argsOnly = true) LocalIntRef mouseY) {
        //SmoothSc.print("lallalala");
        if (SmScCfg.creativeScreenSpeed == 0 || SmoothSc.creativeScreenItemCount <= 0 || SmoothSc.getCreativeScrollOffset() == 0) return;
        //SmoothSc.print("popopopo");
        context.enableScissor(0, context.getScaledWindowHeight() / 2 - 50, context.getScaledWindowWidth(), context.getScaledWindowHeight() / 2 + 38);
        context.getMatrices().push();
        context.getMatrices().translate(0, SmoothSc.getCreativeDrawOffset(), 0);
        cutEnabled = true;
        if(originalCursorY >= context.getScaledWindowHeight() / 2 - 51 && originalCursorY <= context.getScaledWindowHeight() / 2 + 38)
            mouseY.set(my - SmoothSc.getCreativeDrawOffset());

        // the fix for instantly disappearing items on the opposite side of scrolling...
        // it gets the items that just left the slots and draws them in the correct
        // position
        var overUnder = SmoothSc.getCreativeScrollOffset() < 0 ? 9 * 5 : -9;
        var currRow = SmoothSc.creativeScreenPrevRow - SmoothSc.getCreativeScrollOffset() / 18;
        var fromIndex = currRow * 9 + overUnder;
        for(int i = fromIndex; i >= 0 && i < SmoothSc.creativeSH.itemList.size() && i < fromIndex + 9; i++) {
            var tempSlot = new Slot(SmoothSc.getDelegatingInventory(this.handler), i, 9 + i % 9 * 18, SmoothSc.getCreativeScrollOffset() > 0 ? 0 : 18 * 6);

            this.drawSlotOverridden(context, tempSlot);
        }
    }

    @Unique
    private void drawSlotOverridden(DrawContext context, Slot slot) {
        this.drawingOverdrawnSlot = true;
        this.drawSlot(context, slot);
        this.drawingOverdrawnSlot = false;
    }

    @ModifyVariable(method = "drawSlot", at = @At(value = "STORE"), ordinal = 1)
    private int drawItemY(int y, @Local(argsOnly = true) DrawContext context) {
        if(drawingOverdrawnSlot) return y;
        SmoothSc.creativeScreenItemCount -= 1;
        if (SmoothSc.creativeScreenItemCount < 0) tryDisableMask(context);
        if (SmScCfg.creativeScreenSpeed == 0 || SmoothSc.creativeScreenItemCount < 0) return y;
        return y ;//+ SmoothSc.getCreativeDrawOffset();
    }

    @ModifyVariable(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawSlot(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/screen/slot/Slot;)V", shift = At.Shift.AFTER), argsOnly = true, ordinal = 1)
    private int revertMousePos(int mouseY) {
        if (SmScCfg.creativeScreenSpeed == 0 || SmoothSc.creativeScreenItemCount < 0) return originalCursorY;
        return mouseY;
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawForeground(Lnet/minecraft/client/gui/DrawContext;II)V"))
    private void renderMid1(DrawContext context, int mx, int my, float d, CallbackInfo ci, @Local(ordinal = 1, argsOnly = true) LocalIntRef mouseY) {
        tryDisableMask(context);
        mouseY.set(originalCursorY);
    }

    @Inject(method = "mouseClicked", at = @At(value = "HEAD"))
    private void mouseClickedMid1(double mouseX, double my, int button, CallbackInfoReturnable<Boolean> ci, @Local(ordinal = 1, argsOnly = true) LocalDoubleRef mouseY) {
        if (isInBounds((int) mouseX, (int) mouseY.get()) && isInBounds((int) mouseX, (int) mouseY.get() - SmoothSc.getCreativeDrawOffset()))
            mouseY.set(my - SmoothSc.getCreativeDrawOffset());
    }


    @Unique
    private void tryDisableMask(DrawContext context){
        if (drawingOverdrawnSlot) return;
        if (!cutEnabled) return;
        if (SmScCfg.enableMaskDebug)
            context.fill(-100, -100, context.getScaledWindowWidth(), context.getScaledWindowHeight(), ColorHelper.Argb.getArgb(50, 0, 255, 255));
        context.disableScissor();
        context.getMatrices().pop();
        cutEnabled = false;
    }
    @Unique
    private boolean isInBounds(int x, int y) {
        return y >= SmoothSc.mc.getWindow().getScaledHeight() / 2 - 51 && y <= SmoothSc.mc.getWindow().getScaledHeight() / 2 + 38;
    }
    //@ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;isPointOverSlot(Lnet/minecraft/screen/slot/Slot;DD)Z"), index = 2)
    //double modifyCursorPosY(double my) {
    //    if(!cutEnabled || originalCursorY < savedContext.getScaledWindowHeight() / 2 - 51 || originalCursorY > savedContext.getScaledWindowHeight() / 2 + 38) return my;
    //    return originalCursorY - SmoothSc.getCreativeDrawOffset();
    //}
}