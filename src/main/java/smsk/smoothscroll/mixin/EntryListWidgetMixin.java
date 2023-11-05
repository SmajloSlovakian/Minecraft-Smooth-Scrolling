package smsk.smoothscroll.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.EntryListWidget;
import smsk.smoothscroll.Config;
import smsk.smoothscroll.SmoothSc;

@Mixin(EntryListWidget.class)
public class EntryListWidgetMixin {
    @Shadow double scrollAmount;
    @Shadow public int getMaxScroll(){return(0);}
    double targetScroll;
    double predScroll;
    
    @Inject(method = "setScrollAmount",at = @At("TAIL"))
    private void setScrollT(double s,CallbackInfo ci){
        targetScroll=scrollAmount;
    }
    
    @Inject(method = "render",at = @At("HEAD"))
    private void renderH(DrawContext dc, int mouseX, int mouseY, float delta,CallbackInfo ci){
        if(Config.cfg.entryListSpeed==0)return;
        scrollAmount=(int)((scrollAmount-targetScroll)*Math.pow(Config.cfg.entryListSpeed, SmoothSc.mc.getLastFrameDuration())+targetScroll);
    }

    @Inject(method="mouseScrolled",at=@At("HEAD")) //1.20.1: no double verticalAmount
    private void mouseScrollH(double mouseX, double mouseY, double horizontalAmount, double verticalAmount, CallbackInfoReturnable<Boolean> cir){
        if(Config.cfg.entryListSpeed==0)return;
        predScroll=scrollAmount;
        scrollAmount=targetScroll;
    }
    @Inject(method="mouseScrolled",at=@At("TAIL")) //1.20.1: no double verticalAmount
    private void mouseScrollT(double mouseX, double mouseY, double horizontalAmount, double verticalAmount, CallbackInfoReturnable<Boolean> cir){
        if(Config.cfg.entryListSpeed==0)return;
        scrollAmount=predScroll;
    }
}
