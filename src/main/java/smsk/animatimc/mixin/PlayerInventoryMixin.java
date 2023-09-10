package smsk.animatimc.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.player.PlayerInventory;
import smsk.animatimc.AnimatiMC;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin {
    
    @Inject(method = "scrollInHotbar",at = @At("HEAD"))
    private void scrollH(double d,CallbackInfo ci){
        var s=Math.signum(d);
		PlayerInventory inv=AnimatiMC.mc.player.getInventory();
        if(inv.selectedSlot-s<0)AnimatiMC.hotbarRollover+=1;
        if(inv.selectedSlot-s>8)AnimatiMC.hotbarRollover-=1;
    }
}
