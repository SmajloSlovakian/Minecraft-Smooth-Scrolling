package smsk.smoothscroll.mixin.CreativeScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen.CreativeScreenHandler;
import smsk.smoothscroll.SmoothSc;
import smsk.smoothscroll.cfg.SmScCfg;

@Mixin(value = CreativeScreenHandler.class, priority = 1001)
public class CreativeScreenHandlerMixin {

    @ModifyVariable(method = "scrollItems", at = @At("STORE"), ordinal = 0)
    private int scrollItems(int row) {
        if (SmScCfg.creativeScreenSpeed == 0) return (row);
        SmoothSc.creativeScreenItemCount = 0;
        if (!SmoothSc.creativeScreenScrollMixin) return (row);
        
        SmoothSc.creativeScreenScrollOffset += 18 * (row - SmoothSc.creativeScreenPrevRow);
        SmoothSc.creativeScreenPrevRow = row;
        return (row);
    }

    @Inject(method = "scrollItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/SimpleInventory;setStack(ILnet/minecraft/item/ItemStack;)V"))
    private void itemCount(CallbackInfo ci) {
        SmoothSc.creativeScreenItemCount += 1;
    }
}
