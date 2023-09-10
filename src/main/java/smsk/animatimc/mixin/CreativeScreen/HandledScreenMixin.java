package smsk.animatimc.mixin.CreativeScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import smsk.animatimc.AnimatiMC;

@Mixin(HandledScreen.class)
public class HandledScreenMixin {
    int scrollItemCount=0;
    @Inject(method = "render",at = @At("HEAD"))
    void render(DrawContext context, int mx, int my, float d, CallbackInfo ci){
        if(AnimatiMC.creativeSH==null)return;
        AnimatiMC.creativeScreenOffsetY/=Math.pow(2, AnimatiMC.mc.getLastFrameDuration());
        AnimatiMC.creativeScreenScrollMixin=false;

        AnimatiMC.print(Integer.toString(AnimatiMC.creativeScreenPredRow-AnimatiMC.creativeScreenOffsetY/18));

        AnimatiMC.creativeSH.scrollItems(AnimatiMC.creativeScreenGetPos(AnimatiMC.creativeScreenPredRow-AnimatiMC.creativeScreenOffsetY/18, AnimatiMC.creativeSH) );
        AnimatiMC.creativeScreenScrollMixin=true;
        scrollItemCount=AnimatiMC.creativeScreenItemCount;
    }
    @ModifyArg(method = "drawSlot",at = @At(value = "INVOKE",target = "Lnet/minecraft/client/gui/DrawContext;drawItem(Lnet/minecraft/item/ItemStack;III)V"),index = 2)
    int drawItemY(int y){
        if(scrollItemCount<=0)return(y);
        scrollItemCount-=1;
        return(y+AnimatiMC.creativeScreenOffsetY-AnimatiMC.creativeScreenOffsetY/18*18
        );
    }
}
