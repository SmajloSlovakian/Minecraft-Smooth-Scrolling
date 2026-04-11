package io.github.smajloslovakian.smoothscroll.mixin.client.CreativeScreen;

import org.spongepowered.asm.mixin.Mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import io.github.smajloslovakian.smoothscroll.duck.CreativeModeInventoryScreenDuck;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {

    @WrapMethod(method = "extractSlotHighlightBack")
    private void extractSlotHighlightBackWrap(GuiGraphicsExtractor graphics, Operation<Void> operation) {
        if (this instanceof CreativeModeInventoryScreenDuck a && a.isMouseInbounds() && a.isSelectedTabScrollable()) {
            a.enMask(graphics);
            operation.call(graphics);
            a.deMask(graphics);
        } else {
            operation.call(graphics);
        }
    }

    @WrapMethod(method = "extractSlotHighlightFront")
    private void extractSlotHighlightFrontWrap(GuiGraphicsExtractor graphics, Operation<Void> operation) {
        if (this instanceof CreativeModeInventoryScreenDuck a && a.isMouseInbounds() && a.isSelectedTabScrollable()) {
            a.enMask(graphics);
            operation.call(graphics);
            a.deMask(graphics);
        } else {
            operation.call(graphics);
        }
    }
}
