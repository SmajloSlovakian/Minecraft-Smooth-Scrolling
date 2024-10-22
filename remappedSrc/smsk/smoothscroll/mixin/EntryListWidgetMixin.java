package smsk.smoothscroll.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.EntryListWidget;
import smsk.smoothscroll.Config;
import smsk.smoothscroll.SmoothSc;

@Mixin(EntryListWidget.class)
public class EntryListWidgetMixin {
    @Shadow private double scrollAmount; // this is the number of pixels

    @Unique private double scrollAmountBuffer;
    @Unique private double targetScroll;
    @Unique private boolean mousescrolling = false;

    @Unique private double scrollValBefore;
    @Unique private boolean updateScActive = false; // this makes the mod know, when things aren't working as expected and lets the user scroll non-smoothly

    @Inject(method = "setScrollAmount", at = @At("TAIL"))
    private void setScrollT(double s, CallbackInfo ci) {
        if (mousescrolling) return;
        targetScroll = scrollAmount;
        scrollAmountBuffer = scrollAmount;
    }

    @Inject(method = "renderWidget", at = @At("HEAD"), require = 0)
    private void updateScroll(DrawContext dc, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (Config.cfg.entryListSpeed == 0) return;
        updateScActive = true;

        scrollAmountBuffer = (scrollAmountBuffer - targetScroll) * Math.pow(Config.cfg.entryListSpeed, SmoothSc.getLastFrameDuration()) + targetScroll;
        scrollAmount = Math.round(scrollAmountBuffer);
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), require = 0)
    private void mouseScrollH(double mouseX, double mouseY, double hA, double vA, CallbackInfoReturnable<Boolean> cir) {
        if (Config.cfg.entryListSpeed == 0 || !updateScActive) return;
        scrollValBefore = scrollAmount;
        scrollAmount = targetScroll;
        mousescrolling = true;
    }

    @Inject(method = "mouseScrolled", at = @At("TAIL"), require = 0)
    private void mouseScrollT(double mouseX, double mouseY, double hA, double vA, CallbackInfoReturnable<Boolean> cir) {
        if (Config.cfg.entryListSpeed == 0 || !updateScActive) return;
        targetScroll = scrollAmount;
        scrollAmount = scrollValBefore;
        mousescrolling = false;
    }
}
