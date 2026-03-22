package io.github.smajloslovakian.smoothscroll.mixin.client.Widgets;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.components.AbstractWidget;
import io.github.smajloslovakian.smoothscroll.SmoothSc;
import io.github.smajloslovakian.smoothscroll.cfg.SmScCfg;

@Mixin(AbstractScrollArea.class)
public abstract class AbstractScrollAreaMixin extends AbstractWidget {
    @Shadow private double scrollAmount; // scroll position - number of pixels scrolled down (up < down)

    @Unique private double smoothScrollPos;
    @Unique private double targetScrollPos;

    @Unique private boolean noSetScrollT = false;

    @Inject(method = "setScrollAmount", at = @At("TAIL"))
    private void setScrollT(double s, CallbackInfo ci) {
        if (noSetScrollT) return;
        targetScrollPos = scrollAmount;
        smoothScrollPos = scrollAmount;
    }

    @Inject(method = "extractScrollbar", at = @At("HEAD"))
    private void updateScroll(GuiGraphicsExtractor dc, int mx, int my, CallbackInfo ci) {
        if (SmScCfg.entryListSmoothness == 0) return;

        //SmoothSc.printt(smoothScrollPos, targetScrollPos, scrollAmount);

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

    public AbstractScrollAreaMixin() {
        super(0, 0, 0, 0, null);
    }
}
