package io.github.smajloslovakian.smoothscroll.mixin.client.CreativeScreen;

import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen.ItemPickerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = ItemPickerMenu.class)
public interface CreativeScreenHandlerAccessor {
    @Invoker("getScrollForRowIndex")
    float getPos(int row);
}
