package io.github.smajloslovakian.smoothscroll.mixin.client.Widgets;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import io.github.smajloslovakian.smoothscroll.SmoothSc;
import io.github.smajloslovakian.smoothscroll.cfg.SmScCfg;

@Mixin(AbstractScrollArea.class)
public class AbstractScrollAreaMixin extends AbstractWidget {
    @Shadow private double scrollAmount; // scroll position - number of pixels scrolled down (up < down)

    @Unique private double smoothScrollPos;
    @Unique private double targetScrollPos;

    @Unique private boolean updateScActive = false; // this makes the mod know, when things aren't working as expected and lets the user scroll non-smoothly
    @Unique private boolean noSetScrollT = false;

    @Inject(method = "setScrollAmount", at = @At("TAIL"))
    private void setScrollT(double s, CallbackInfo ci) {
        if (noSetScrollT) return;
        targetScrollPos = scrollAmount;
        smoothScrollPos = scrollAmount;
    }

    @Inject(method = "renderScrollbar", at = @At("HEAD"), require = 0)
    private void updateScroll(GuiGraphics dc, int mx, int my, CallbackInfo ci) {
        if (SmScCfg.entryListSmoothness == 0) return;
        updateScActive = true;


        smoothScrollPos = (smoothScrollPos - targetScrollPos) * Math.pow(SmScCfg.entryListSmoothness, SmoothSc.getLastFrameDuration()) + targetScrollPos;
        scrollAmount = Math.round(smoothScrollPos);

        // TODO not so pretty workaround, might fix later
        // basically setscroll also makes the screen redraw
        noSetScrollT = true;
        setScrollAmount(scrollAmount);
        noSetScrollT = false;
    }

    @WrapMethod(method = "mouseScrolled")
    private boolean mouseScrolledWrap(double mouseX, double mouseY, double hA, double vA, Operation<Boolean> operation) {
        noSetScrollT = true;
        var prevScrollPos = scrollAmount;
        if (SmScCfg.entryListSmoothness != 0) {
            setScrollAmount(targetScrollPos);
        }
        var ret = operation.call(mouseX, mouseY, hA, vA);

        if (SmScCfg.entryListAmount != 0 && ret) {
            setScrollAmount(targetScrollPos - SmScCfg.entryListAmount * vA);
        }
        targetScrollPos = scrollAmount;
        if (SmScCfg.entryListSmoothness != 0) {
            setScrollAmount(prevScrollPos);
        }
        noSetScrollT = false;
        return ret;
    }

    @Shadow
    public void setScrollAmount(double sc) {}






    public ScrollableWidgetMixin() {
        super(0, 0, 0, 0, null);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {
    }

    @Override
    public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
    }
}
