package smsk.smoothscroll.mixin.Hotbar;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.entity.player.PlayerInventory;
import smsk.smoothscroll.SmoothSc;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin {

    /*@Inject(method = "setSelectedSlot", at = @At("HEAD"))
    private void setselect(int slot, CallbackInfo ci) {
        if (!SmScCfg.hotbarRollover || SmScCfg.hotbarSmoothness == 0 || SmoothSc.mc.player == null) return;
        PlayerInventory inv = SmoothSc.mc.player.getInventory();
        if (inv.selectedSlot == 8 && slot == 0) SmoothSc.hotbarRollover += -1;
        if (inv.selectedSlot == 0 && slot == 8) SmoothSc.hotbarRollover += 1;
    }/* */
    @Inject(method = "scrollInHotbar", at = @At("HEAD"))
    private void scrollH(double d, CallbackInfo ci) {
        var s = Math.signum(d);
        PlayerInventory inv = SmoothSc.mc.player.getInventory();
        if (inv.selectedSlot - s < 0) SmoothSc.hotbarRollover += 1;
        if (inv.selectedSlot - s > 8) SmoothSc.hotbarRollover += -1;
    }
}
