package io.github.smajloslovakian.smoothscroll.mixin.client.CreativeScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen.ItemPickerMenu;

@Mixin(ItemPickerMenu.class)
interface ItemPickerMenuAccessor {
    @Invoker("calculateRowCount")
    int getCalculatedRowCount();
}