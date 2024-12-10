package smsk.smoothscroll.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ScrollableWidget;
import smsk.smoothscroll.SmoothSc;
import smsk.smoothscroll.cfg.SmScCfg;

@Mixin(ScrollableWidget.class)
public class ScrollableWidgetMixin extends ClickableWidget{
    @Shadow private double scrollY; // this is the number of pixels

    @Unique private double scrollAmountBuffer;
    @Unique private double targetScroll;
    @Unique private boolean mousescrolling = false;

    @Unique private double prevScrollVal;
    @Unique private boolean updateScActive = false; // this makes the mod know, when things aren't working as expected and lets the user scroll non-smoothly

    @Inject(method = "setScrollY", at = @At("TAIL"))
    private void setScrollT(double s, CallbackInfo ci) {
        if (mousescrolling) return;
        targetScroll = scrollY;
        scrollAmountBuffer = scrollY;
    }

    @Inject(method = "drawScrollbar", at = @At("HEAD"), require = 0)
    private void updateScroll(DrawContext dc, CallbackInfo ci) {
        if (SmScCfg.entryListSmoothness == 0) return;
        updateScActive = true;

        scrollAmountBuffer = (scrollAmountBuffer - targetScroll) * Math.pow(SmScCfg.entryListSmoothness, SmoothSc.getLastFrameDuration()) + targetScroll;
        scrollY = Math.round(scrollAmountBuffer);
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), require = 0)
    private void mouseScrollH(double mouseX, double mouseY, double hA, double vA, CallbackInfoReturnable<Boolean> cir) {
        if (SmScCfg.entryListSmoothness == 0 || !updateScActive) return;
        mousescrolling = true;
        prevScrollVal = scrollY;
        setScrollY(targetScroll);
    }

    @Inject(method = "mouseScrolled", at = @At("TAIL"), require = 0)
    private void mouseScrollT(double mouseX, double mouseY, double hA, double vA, CallbackInfoReturnable<Boolean> cir) {
        var diff = scrollY - targetScroll;
        if (SmScCfg.entryListAmount != 0)
            diff = - SmScCfg.entryListAmount * vA;
        setScrollY(targetScroll + diff);
        
        if (SmScCfg.entryListSmoothness == 0 || !updateScActive) return;

        targetScroll = scrollY;
        setScrollY(prevScrollVal);
        mousescrolling = false;
    }

    @Shadow
    public void setScrollY(double sc) {}






    public ScrollableWidgetMixin() {
        super(0, 0, 0, 0, null);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'appendClickableNarrations'");
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'renderWidget'");
    }
}
