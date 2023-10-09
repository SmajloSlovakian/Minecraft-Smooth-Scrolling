package smsk.smoothscroll.mixin.CreativeScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.item.ItemGroup;
import smsk.smoothscroll.SmoothSc;

@Mixin(CreativeInventoryScreen.class)
public class CreativeScreenMixin {
    @Inject(method = "setSelectedTab",at = @At("TAIL"))
    void setSelectedTabT(ItemGroup group, CallbackInfo ci){
        SmoothSc.creativeScreenOffsetY=0;
    }
}
