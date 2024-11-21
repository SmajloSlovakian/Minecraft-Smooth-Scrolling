package smsk.smoothscroll.mixin.Hotbar;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.entity.player.PlayerInventory;
import smsk.smoothscroll.SmoothSc;
import smsk.smoothscroll.cfg.SmScCfg;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin {

    @Inject(method = "setSelectedSlot", at = @At("HEAD"))
    private void setselect(int slot, CallbackInfo ci) {
        if (!SmScCfg.hotbarRollover) return;
        PlayerInventory inv = SmoothSc.mc.player.getInventory();
        if (inv.selectedSlot == 8 && slot == 0) SmoothSc.hotbarRollover += -1;
        if (inv.selectedSlot == 0 && slot == 8) SmoothSc.hotbarRollover += 1;
    }
}
