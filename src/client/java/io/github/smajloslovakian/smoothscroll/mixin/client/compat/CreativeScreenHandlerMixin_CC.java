package io.github.smajloslovakian.smoothscroll.mixin.client.compat;

import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import io.github.smajloslovakian.smoothscroll.SmoothSc;

// Specific mixin for targeting CondensedCreative call for setting Entry within Creative Screen
@Mixin(value = CreativeModeInventoryScreen.ItemPickerMenu.class, priority = 1001)
public class CreativeScreenHandlerMixin_CC {
    @Inject(method = "scrollTo", at = @At(value = "INVOKE", target = "Lio/wispforest/condensed_creative/entry/EntryContainer;setEntryStack(ILio/wispforest/condensed_creative/entry/Entry;)V"))
    private void itemCount(float pos, CallbackInfo ci) {
        SmoothSc.creativeScreenItemCount += 1;
    }
}
