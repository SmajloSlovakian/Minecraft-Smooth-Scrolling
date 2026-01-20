package io.github.smajloslovakian.smoothscroll.mixin.client.CreativeScreen;

import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen.ItemPickerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import io.github.smajloslovakian.smoothscroll.SmoothSc;
import io.github.smajloslovakian.smoothscroll.cfg.SmScCfg;

@Mixin(value = ItemPickerMenu.class, priority = 1001)
public class CreativeScreenHandlerMixin {

    @ModifyVariable(method = "scrollTo", at = @At("STORE"), ordinal = 0)
    private int scrollItems(int row) {
        if (SmScCfg.creativeScreenSmoothness == 0) return (row);
        SmoothSc.creativeScreenItemCount = 0;
        if (!SmoothSc.creativeScreenScrollMixin) return (row);
        
        SmoothSc.creativeScreenScrollOffset += 18 * (row - SmoothSc.creativeScreenPrevRow);
        SmoothSc.creativeScreenPrevRow = row;
        return (row);
    }

    @Inject(method = "scrollTo", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/SimpleContainer;setStack(ILnet/minecraft/world/item/ItemStack;)V"))
    private void itemCount(CallbackInfo ci) {
        SmoothSc.creativeScreenItemCount += 1;
    }
}
