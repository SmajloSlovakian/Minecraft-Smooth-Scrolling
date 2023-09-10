package smsk.animatimc.mixin.CreativeScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen.CreativeScreenHandler;
import smsk.animatimc.AnimatiMC;

@Mixin(CreativeScreenHandler.class)
public class CreativeScreenHandlerMixin {

    @ModifyVariable(method = "scrollItems",at = @At("STORE"),ordinal = 0)
    private int scrollItems(int row){
        AnimatiMC.creativeScreenItemCount=0;
        if(!AnimatiMC.creativeScreenScrollMixin)return(row);
        AnimatiMC.creativeScreenOffsetY+=18*(row-AnimatiMC.creativeScreenPredRow);
        AnimatiMC.creativeScreenPredRow=row;
        return(row);
    }

    @Inject(method="scrollItems",at=@At(value="INVOKE",target="Lnet/minecraft/inventory/SimpleInventory;setStack(ILnet/minecraft/item/ItemStack;)V"))
    private void ItemCount(CallbackInfo ci){
        AnimatiMC.creativeScreenItemCount+=1;
    }
}
