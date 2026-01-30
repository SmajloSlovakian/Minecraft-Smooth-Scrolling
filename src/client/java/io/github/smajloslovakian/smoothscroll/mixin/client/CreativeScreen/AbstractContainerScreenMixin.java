package io.github.smajloslovakian.smoothscroll.mixin.client.CreativeScreen;

import org.spongepowered.asm.mixin.Mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import io.github.smajloslovakian.smoothscroll.CreativeModeInventoryScreenDuck;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {

    @WrapMethod(method = "renderSlotHighlightBack")
    private void renderHighlightBackWrap(GuiGraphics graphics, Operation<Void> operation) {
        var a = ((CreativeModeInventoryScreenDuck)this);
        if (!a.isMouseInbounds()) {
            operation.call(graphics);
            return;
        }
        a.enMask(graphics);
        operation.call(graphics);
        a.deMask(graphics);
    }

    @WrapMethod(method = "renderSlotHighlightFront")
    private void renderHighlightFrontWrap(GuiGraphics graphics, Operation<Void> operation) {
        var a = ((CreativeModeInventoryScreenDuck)this);
        if (!a.isMouseInbounds()) {
            operation.call(graphics);
            return;
        }
        a.enMask(graphics);
        operation.call(graphics);
        a.deMask(graphics);
    }
}
