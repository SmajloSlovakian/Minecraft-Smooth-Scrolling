package smsk.smoothscroll.mixin.CreativeScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import smsk.smoothscroll.Config;
import smsk.smoothscroll.SmoothSc;

@Mixin(HandledScreen.class)
public class HandledScreenMixin {
    int scrollItemCount=0;
    @Inject(method = "render",at = @At("HEAD"))
    void render(DrawContext context, int mx, int my, float d, CallbackInfo ci){
        if(Config.cfg.creativeScreenSpeed==0)return;
        if(SmoothSc.creativeSH==null)return;
        SmoothSc.creativeScreenOffsetY/=Math.pow(Config.cfg.creativeScreenSpeed, SmoothSc.mc.getLastFrameDuration());
        SmoothSc.creativeScreenScrollMixin=false;

        SmoothSc.creativeSH.scrollItems(SmoothSc.creativeScreenGetPos(SmoothSc.creativeScreenPredRow-SmoothSc.creativeScreenOffsetY/18, SmoothSc.creativeSH) );
        SmoothSc.creativeScreenScrollMixin=true;
        scrollItemCount=SmoothSc.creativeScreenItemCount;
    }
    @ModifyArg(method = "drawSlot",at = @At(value = "INVOKE",target = "Lnet/minecraft/client/gui/DrawContext;drawItem(Lnet/minecraft/item/ItemStack;III)V"),index = 2)
    int drawItemY(int y){
        if(Config.cfg.creativeScreenSpeed==0)return(y);
        if(scrollItemCount<=0)return(y);
        scrollItemCount-=1;
        return(y+SmoothSc.creativeScreenOffsetY-SmoothSc.creativeScreenOffsetY/18*18
        );
    }
}
