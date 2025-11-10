package smsk.smoothscroll.mixin.Widgets;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ScrollableWidget;
import smsk.smoothscroll.SmoothSc;
import smsk.smoothscroll.cfg.SmScCfg;

@Mixin(ScrollableWidget.class)
public class ScrollableWidgetMixin extends ClickableWidget {
    @Shadow private double scrollY; // scroll position - number of pixels scrolled down (up < down)

    @Unique private double smoothScrollPos;
    @Unique private double targetScrollPos;

    @Unique private boolean updateScActive = false; // this makes the mod know, when things aren't working as expected and lets the user scroll non-smoothly
    @Unique private boolean noSetScrollT = false;

    @Inject(method = "setScrollY", at = @At("TAIL"))
    private void setScrollT(double s, CallbackInfo ci) {
        if (noSetScrollT) return;
        targetScrollPos = scrollY;
        smoothScrollPos = scrollY;
    }

    @Inject(method = "drawScrollbar", at = @At("HEAD"), require = 0)
    private void updateScroll(DrawContext dc, int mx, int my, CallbackInfo ci) {
        if (SmScCfg.entryListSmoothness == 0) return;
        updateScActive = true;


        smoothScrollPos = (smoothScrollPos - targetScrollPos) * Math.pow(SmScCfg.entryListSmoothness, SmoothSc.getLastFrameDuration()) + targetScrollPos;
        scrollY = Math.round(smoothScrollPos);

        // TODO not so pretty workaround, might fix later
        // basically setscroll also makes the screen redraw
        noSetScrollT = true;
        setScrollY(scrollY);
        noSetScrollT = false;
    }

    @WrapMethod(method = "mouseScrolled")
    private boolean mouseScrolledWrap(double mouseX, double mouseY, double hA, double vA, Operation<Boolean> operation) {
        noSetScrollT = true;
        var prevScrollPos = scrollY;
        if (SmScCfg.entryListSmoothness != 0) {
            setScrollY(targetScrollPos);
        }
        var ret = operation.call(mouseX, mouseY, hA, vA);

        if (SmScCfg.entryListAmount != 0 && ret) {
            setScrollY(targetScrollPos - SmScCfg.entryListAmount * vA);
        }
        targetScrollPos = scrollY;
        if (SmScCfg.entryListSmoothness != 0) {
            setScrollY(prevScrollPos);
        }
        noSetScrollT = false;
        return ret;
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
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'renderWidget'");
    }
}
