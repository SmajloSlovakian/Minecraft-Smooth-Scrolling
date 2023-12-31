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
    double targetScroll;
    double predScroll;
    boolean updateScActive=false;
    
    @Inject(method="setScrollAmount",at = @At("TAIL"))
    private void setScrollT(double s, CallbackInfo ci){
        targetScroll=scrollAmount;
    }
    
    @Inject(method="render",at = @At("HEAD"),require = 0) private void renderH201(DrawContext dc, int mouseX, int mouseY, float delta, CallbackInfo ci){updateScroll(dc, mouseX, mouseY, delta, ci);}
    @Inject(method="renderWidget",at = @At("HEAD"),require = 0) private void renderH203(DrawContext dc, int mouseX, int mouseY, float delta, CallbackInfo ci){updateScroll(dc, mouseX, mouseY, delta, ci);}
    private void updateScroll(DrawContext dc, int mouseX, int mouseY, float delta, CallbackInfo ci){
        updateScActive=true;
        if(Config.cfg.entryListSpeed==0||targetScroll==scrollAmount)return;
        scrollAmount=(int)((scrollAmount-targetScroll)*Math.pow(Config.cfg.entryListSpeed, SmoothSc.mc.getLastFrameDuration())+targetScroll);
    }

    @Inject(method="method_25401(DDD)Z",at=@At("HEAD"),require = 0) private void mouseScrollH201(double mouseX, double mouseY, double hA, CallbackInfoReturnable<Boolean> cir){mouseScrollH(mouseX, mouseY, hA, 0, cir);}
    @Inject(method="mouseScrolled",     at=@At("HEAD"),require = 0) private void mouseScrollH202(double mouseX, double mouseY, double hA, double vA, CallbackInfoReturnable<Boolean> cir){mouseScrollH(mouseX, mouseY, hA, vA, cir);}
    private void mouseScrollH(double mouseX, double mouseY, double hA, double vA, CallbackInfoReturnable<Boolean> cir){
        if(Config.cfg.entryListSpeed==0||!updateScActive)return;
        predScroll=scrollAmount;
        scrollAmount=targetScroll;
    }
    @Inject(method="method_25401(DDD)Z",at=@At("TAIL"),require = 0) private void mouseScrollT201(double mouseX, double mouseY, double hA, CallbackInfoReturnable<Boolean> cir){mouseScrollT(mouseX, mouseY, hA, 0, cir);}
    @Inject(method="mouseScrolled",     at=@At("TAIL"),require = 0) private void mouseScrollT202(double mouseX, double mouseY, double hA, double vA, CallbackInfoReturnable<Boolean> cir){mouseScrollT(mouseX, mouseY, hA, vA, cir);}
    private void mouseScrollT(double mouseX, double mouseY, double hA, double vA, CallbackInfoReturnable<Boolean> cir){
        if(Config.cfg.entryListSpeed==0||!updateScActive)return;
        scrollAmount=predScroll;
    }
}
