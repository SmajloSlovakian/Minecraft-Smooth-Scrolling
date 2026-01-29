package io.github.smajloslovakian.smoothscroll.mixin.client.CreativeScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import io.github.smajloslovakian.smoothscroll.SmoothSc;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen.ItemPickerMenu;

@Mixin(ItemPickerMenu.class)
abstract class ItemPickerMenuMixin {

    @WrapMethod(method = "scrollTo")
    private void scrollToWrap(float scrollOffs, Operation<Void> operation) {
        // based on mouse position, use items from a row before or not
        operation.call(scrollOffs);
    }

    @WrapMethod(method = "getRowIndexForScroll")
    protected int getRowIndexForScroll(float scrollOffs, Operation<Integer> operation) {
        return Math.max((int)((double)(scrollOffs * (float)this.calculateRowCount())), 0);
    }
    @Shadow protected abstract int calculateRowCount();
}