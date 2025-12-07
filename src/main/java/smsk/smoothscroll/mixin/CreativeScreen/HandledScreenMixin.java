package smsk.smoothscroll.mixin.CreativeScreen;

import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import smsk.smoothscroll.SmoothSc;
import smsk.smoothscroll.cfg.SmScCfg;

@Mixin(value = HandledScreen.class, priority = 999)
public abstract class HandledScreenMixin<T extends ScreenHandler> {

    @Shadow
    @Final
    protected T handler;

    @Shadow
    protected abstract void drawSlot(DrawContext context, Slot slot, int mouseX, int mouseY);

    @Unique
    private final Identifier backTex = Identifier.ofVanilla("textures/gui/container/creative_inventory/tab_items");
    @Unique
    private boolean cutEnabled = false;
    @Unique
    private int originalCursorY;
    @Unique
    private boolean drawingOverdrawnSlot = false;

    @Inject(method = "renderMain", at = @At("HEAD"))
    private void renderMainH(DrawContext context, int mx, int my, float d, CallbackInfo ci) {
        if (FabricLoader.getInstance().getObjectShare().get("flow:is_caching_screen") instanceof Boolean isCaching
                && isCaching)
            return;
        this.originalCursorY = my;
        if (SmScCfg.creativeScreenSmoothness == 0 || SmoothSc.creativeSH == null
                || SmoothSc.getCreativeScrollOffset() == 0)
            return;


        // this shares the y offset of the items in the creative inventory i want other
        // mods to follow for better compatibility
        FabricLoader.getInstance().getObjectShare().put("smoothscroll:creative_screen/y_offset",
                SmoothSc.getCreativeDrawOffset());
        FabricLoader.getInstance().getObjectShare().put("smoothscroll:creative_screen/item_count",
                SmoothSc.creativeScreenItemCount);
    }

    @Inject(method = "renderMain", at = @At(shift = At.Shift.AFTER, value = "INVOKE", target = "Lorg/joml/Matrix3x2fStack;translate(FF)Lorg/joml/Matrix3x2f;", remap = false))
    private void renderMain0(DrawContext context, int mx, int my, float d, CallbackInfo ci,
            @Local(ordinal = 1, argsOnly = true) LocalIntRef mouseY) {
        if (FabricLoader.getInstance().getObjectShare().get("flow:is_caching_screen") instanceof Boolean isCaching
                && isCaching)
            return;
        if (SmScCfg.creativeScreenSmoothness == 0 || SmoothSc.creativeScreenItemCount <= 0
                || SmoothSc.getCreativeScrollOffset() == 0)
            return;
        if (isInBounds(mx, originalCursorY))
            mouseY.set(my - SmoothSc.getCreativeDrawOffset());

        // the fix for instantly disappearing items on the opposite side of scrolling...
        // it gets the items that just left the slots and draws them in the correct
        // position
        tryEnableMask(context);
        var overUnder = SmoothSc.getCreativeScrollOffset() < 0 ? 9 * 5 : -9;
        var currRow = SmoothSc.creativeScreenPrevRow - SmoothSc.getCreativeScrollOffset() / 18;
        var fromIndex = currRow * 9 + overUnder;
        for (int i = fromIndex; i >= 0 && i < SmoothSc.creativeSH.itemList.size() && i < fromIndex + 9; i++) {
            var tempSlot = new Slot(SmoothSc.getDelegatingInventory(this.handler), i, 9 + i % 9 * 18,
                    SmoothSc.getCreativeScrollOffset() > 0 ? 0 : 18 * 6);

            this.drawSlotOverridden(context, tempSlot, mx, my);
        }
        tryDisableMask(context);
    }

    @Unique
    private void drawSlotOverridden(DrawContext context, Slot slot, int mouseX, int mouseY) {
        this.drawingOverdrawnSlot = true;
        this.drawSlot(context, slot, mouseX, mouseY);
        this.drawingOverdrawnSlot = false;
    }

    @Inject(method = "drawSlot", at = @At(value = "HEAD"))
    private void drawItemY(DrawContext context, Slot slot, int x, int y, CallbackInfo ci) {
        if (FabricLoader.getInstance().getObjectShare().get("flow:is_caching_screen") instanceof Boolean isCaching
                && isCaching)
            return;
        if (drawingOverdrawnSlot)
            return;
        SmoothSc.creativeScreenItemCount -= 1;
        if (SmoothSc.creativeScreenItemCount < 0)
            tryDisableMask(context);
    }

    @SuppressWarnings("rawtypes")
    @WrapOperation(method = "renderMain", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawSlotHighlightBack(Lnet/minecraft/client/gui/DrawContext;)V"))
    private void highlightBack(HandledScreen hs, DrawContext context, Operation<Void> original,
            @Local(argsOnly = true, ordinal = 0) LocalIntRef mouseX) {
        if (FabricLoader.getInstance().getObjectShare().get("flow:is_caching_screen") instanceof Boolean isCaching
                && isCaching) {
            original.call(hs, context);
            return;
        }
        if (SmoothSc.getCreativeScrollOffset() == 0 || !isInBounds(mouseX.get(), originalCursorY)) {
            original.call(hs, context);
            return;
        }
        tryEnableMask(context);
        original.call(hs, context);
        tryDisableMask(context);
    }

    @Inject(method = "renderMain", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawSlots(Lnet/minecraft/client/gui/DrawContext;II)V"))
    private void renderMain1(DrawContext context, int mx, int my, float d, CallbackInfo ci) {
        if (FabricLoader.getInstance().getObjectShare().get("flow:is_caching_screen") instanceof Boolean isCaching
                && isCaching)
            return;
        if (SmoothSc.getCreativeScrollOffset() == 0)
            return;
        tryEnableMask(context);
    }

    @SuppressWarnings("rawtypes")
    @WrapOperation(method = "renderMain", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawSlotHighlightFront(Lnet/minecraft/client/gui/DrawContext;)V"))
    private void highlightFront(HandledScreen hs, DrawContext context, Operation<Void> original,
            @Local(argsOnly = true, ordinal = 0) LocalIntRef mouseX,
            @Local(argsOnly = true, ordinal = 1) LocalIntRef mouseY) {
        if (FabricLoader.getInstance().getObjectShare().get("flow:is_caching_screen") instanceof Boolean isCaching
                && isCaching) {
            original.call(hs, context);
            return;
        }
        if (SmoothSc.getCreativeScrollOffset() == 0 || !isInBounds(mouseX.get(), originalCursorY)) {
            original.call(hs, context);
            return;
        }
        tryEnableMask(context);
        original.call(hs, context);
        tryDisableMask(context);
        mouseY.set(originalCursorY);
    }

    @Inject(method = "renderMain", at = @At(value = "TAIL"))
    private void renderMainT(DrawContext context, int mx, int my, float d, CallbackInfo ci,
            @Local(ordinal = 1, argsOnly = true) LocalIntRef mouseY) {
        if (FabricLoader.getInstance().getObjectShare().get("flow:is_caching_screen") instanceof Boolean isCaching
                && isCaching)
            return;
        tryDisableMask(context);
        mouseY.set(originalCursorY);
    }

    @Inject(method = "mouseClicked", at = @At(value = "HEAD"))
    private void mouseClickedH(Click click, boolean doubled, CallbackInfoReturnable<Boolean> ci,
            @Local(ordinal = 0, argsOnly = true) LocalRef<Click> clickref) {
        if (FabricLoader.getInstance().getObjectShare().get("flow:is_caching_screen") instanceof Boolean isCaching
                && isCaching)
            return;
        if (isInBounds((int) click.x(), (int) click.y())
                && isInBounds((int) click.x(), (int) click.y() - SmoothSc.getCreativeDrawOffset())){
            var newclick = new Click(click.x(), click.y() - SmoothSc.getCreativeDrawOffset(), click.buttonInfo());
            clickref.set(newclick);
        }
        
    }

    @Unique
    private void tryEnableMask(DrawContext context) {
        if (cutEnabled)
            return;
        context.enableScissor(8, 18, 170, 106);
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(0, SmoothSc.getCreativeDrawOffset());
        cutEnabled = true;
    }

    @Unique
    private void tryDisableMask(DrawContext context) {
        if (!cutEnabled)
            return;
        if (SmScCfg.enableMaskDebug)
            context.fill(-100, -100, context.getScaledWindowWidth(), context.getScaledWindowHeight(),
                    ColorHelper.getArgb(50, 0, 255, 255));
        context.disableScissor();
        context.getMatrices().popMatrix();
        cutEnabled = false;
    }

    @Unique
    private boolean isInBounds(int x, int y) {
        return y >= SmoothSc.mc.getWindow().getScaledHeight() / 2 - 51
                && y <= SmoothSc.mc.getWindow().getScaledHeight() / 2 + 38;
    }
}