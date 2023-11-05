package smsk.smoothscroll.mixin.CreativeScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import smsk.smoothscroll.Config;
import smsk.smoothscroll.SmoothSc;

@Mixin(HandledScreen.class)
public class HandledScreenMixin {
    int scrollItemCount=0;
    boolean cutenabled=false;
    @Inject(method = "render",at = @At("HEAD"))
    void render(DrawContext context, int mx, int my, float d, CallbackInfo ci){
        if(Config.cfg.creativeScreenSpeed==0||SmoothSc.creativeSH==null)return;
        SmoothSc.creativeScreenOffsetY*=Math.pow(Config.cfg.creativeScreenSpeed, SmoothSc.mc.getLastFrameDuration());
        SmoothSc.creativeScreenScrollMixin=false;

        SmoothSc.creativeSH.scrollItems(SmoothSc.creativeScreenGetPos(SmoothSc.creativeScreenPredRow-SmoothSc.creativeScreenOffsetY/18, SmoothSc.creativeSH));
        SmoothSc.creativeScreenScrollMixin=true;
        scrollItemCount=SmoothSc.creativeScreenItemCount;
    }
    @ModifyArg(method = "drawSlot",at = @At(value = "INVOKE",target = "Lnet/minecraft/client/gui/DrawContext;drawItem(Lnet/minecraft/item/ItemStack;III)V"),index = 2)
    int drawItemY(int y){
        if(Config.cfg.creativeScreenSpeed==0||scrollItemCount<=0)return(y);
        scrollItemCount-=1;
        return(y+SmoothSc.creativeScreenOffsetY-SmoothSc.creativeScreenOffsetY/18*18);
    }
    @Inject(method = "render",at = @At(value = "INVOKE",target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawSlot(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/screen/slot/Slot;)V"))
    void renderMid0(DrawContext context, int mx, int my, float d, CallbackInfo ci){
        if(Config.cfg.creativeScreenSpeed==0||scrollItemCount<=0)return;
        context.enableScissor(0, context.getScaledWindowHeight()/2-51, context.getScaledWindowWidth(), context.getScaledWindowHeight()/2+39);
        cutenabled=true;
    }
    @Inject(method = "render",at = @At(shift = Shift.AFTER,value = "INVOKE",target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawSlot(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/screen/slot/Slot;)V"))
    void renderMid1(DrawContext context, int mx, int my, float d, CallbackInfo ci){
        if(!cutenabled)return;
        context.disableScissor();
        cutenabled=false;
    }
}
