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
        if (SmoothSc.isScrolling) return;
        if (!SmScCfg.hotbarRollover || SmScCfg.hotbarSmoothness == 0 || SmoothSc.mc.player == null) return;
        PlayerInventory inv = SmoothSc.mc.player.getInventory();
        // Ensure we only run for the client player's inventory to avoid double counting
        if ((Object) this != inv) return;

        if (inv.getSelectedSlot() == 8 && slot == 0) SmoothSc.hotbarRollover += -1;
        if (inv.getSelectedSlot() == 0 && slot == 8) SmoothSc.hotbarRollover += 1;
    }
}
