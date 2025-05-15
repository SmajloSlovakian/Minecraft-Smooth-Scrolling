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
import net.minecraft.util.math.MathHelper;
import smsk.smoothscroll.SmoothSc;
import smsk.smoothscroll.cfg.SmScCfg;

@Mixin(EntryListWidget.class)
public class ScrollableWidgetMixin {
    @Shadow private double scrollAmount; // this is the number of pixels

    @Unique private double scrollAmountBuffer;
    @Unique private double targetScroll;
    @Unique private boolean mousescrolling = false;

    @Unique private double prevScrollVal;
    @Unique private boolean updateScActive = false; // this makes the mod know, when things aren't working as expected and lets the user scroll non-smoothly

    @Inject(method = "setScrollAmount", at = @At("TAIL"))
    private void setScrollT(double s, CallbackInfo ci) {
        if (mousescrolling) return;
        targetScroll = scrollAmount;
        scrollAmountBuffer = scrollAmount;
    }

    @Inject(method = "render", at = @At("HEAD"), require = 0)
    private void updateScroll(DrawContext dc, int mx, int my, float d, CallbackInfo ci) {
        if (SmScCfg.entryListSmoothness == 0) return;
        updateScActive = true;

        scrollAmountBuffer = (scrollAmountBuffer - targetScroll) * Math.pow(SmScCfg.entryListSmoothness, SmoothSc.getLastFrameDuration()) + targetScroll;
        scrollAmount = Math.round(scrollAmountBuffer);
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), require = 0)
    private void mouseScrollH(double mouseX, double mouseY, double vA, CallbackInfoReturnable<Boolean> cir) {
        if (SmScCfg.entryListSmoothness == 0 || !updateScActive) return;
        mousescrolling = true;
        prevScrollVal = scrollAmount;
        setScrollY(targetScroll);
    }

    @Inject(method = "mouseScrolled", at = @At("TAIL"), require = 0)
    private void mouseScrollT(double mouseX, double mouseY, double vA, CallbackInfoReturnable<Boolean> cir) {
        var diff = scrollAmount - targetScroll;
        if (SmScCfg.entryListAmount != 0)
            diff = - SmScCfg.entryListAmount * vA;
        setScrollY(targetScroll + diff);

        if (SmScCfg.entryListSmoothness == 0 || !updateScActive) return;

        targetScroll = MathHelper.clamp(scrollAmount, 0.0, (double)this.getMaxScroll());
        setScrollY(prevScrollVal);
        mousescrolling = false;
    }

    @Unique
    private void setScrollY(double sc) {
        scrollAmount = sc;
    }

    @Shadow public int getMaxScroll() {return 0;}
}
