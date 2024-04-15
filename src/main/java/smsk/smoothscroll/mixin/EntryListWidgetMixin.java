package smsk.smoothscroll.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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
    @Shadow double scrollAmount; // this is the number of pixels
    double targetScroll;
    double scrollValBefore;
    boolean updateScActive = false; // this makes the mod know, when things aren't working as expected and lets the user scroll non-smoothly
    float lFDBuffer;

    @Inject(method = "setScrollAmount", at = @At("TAIL"))
    private void setScrollT(double s, CallbackInfo ci) {
        targetScroll = scrollAmount;
    }

    @Inject(method = "render", at = @At("HEAD"), require = 0)
    private void updateScroll(DrawContext dc, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (Config.cfg.entryListSpeed == 0) return;
        updateScActive = true;

        lFDBuffer += SmoothSc.mc.getLastFrameDuration();
        var a = scrollAmount;
        scrollAmount = Math.round((scrollAmount - targetScroll) * Math.pow(Config.cfg.entryListSpeed, lFDBuffer) + targetScroll);
        if(a != scrollAmount || scrollAmount == targetScroll) lFDBuffer = 0;
    }

    @Inject(method = "method_25401", at = @At("HEAD"), require = 0)
    private void mouseScrollH(double mouseX, double mouseY, double vA, CallbackInfoReturnable<Boolean> cir) {
        if (Config.cfg.entryListSpeed == 0 || !updateScActive) return;
        scrollValBefore = scrollAmount;
        scrollAmount = targetScroll;
    }

    @Inject(method = "method_25401", at = @At("TAIL"), require = 0)
    private void mouseScrollT(double mouseX, double mouseY, double vA, CallbackInfoReturnable<Boolean> cir) {
        if (Config.cfg.entryListSpeed == 0 || !updateScActive) return;
        scrollAmount = scrollValBefore;
    }
}
