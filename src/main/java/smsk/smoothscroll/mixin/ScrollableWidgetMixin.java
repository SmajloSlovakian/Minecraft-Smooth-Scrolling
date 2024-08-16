package smsk.smoothscroll.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ScrollableWidget;
import smsk.smoothscroll.SmoothSc;

@Mixin(ScrollableWidget.class)
public class ScrollableWidgetMixin { // DISABLED

    @Shadow private double scrollY;
    @Unique private double predScrollY;
    @Unique private double targetScrollY;

    @Inject(method = "setScrollY(D)V", at = @At("HEAD"))
    private void SetScrollYH(double s, CallbackInfo ci) {
        predScrollY = scrollY;
    }

    @Inject(method = "setScrollY(D)V", at = @At("TAIL"))
    private void SetScrollYT(double s, CallbackInfo ci) {
        targetScrollY = scrollY;
        scrollY = predScrollY;
    }

    @Inject(method = "drawScrollbar", at = @At("HEAD"))
    private void drawScrollbarH(DrawContext context, CallbackInfo ci) {
        scrollY = (scrollY - targetScrollY) / Math.pow(2, SmoothSc.getLastFrameDuration()) + targetScrollY;
        if (scrollY < targetScrollY + 0.1 && scrollY > targetScrollY - 0.1)
            scrollY = targetScrollY;
    }
}
