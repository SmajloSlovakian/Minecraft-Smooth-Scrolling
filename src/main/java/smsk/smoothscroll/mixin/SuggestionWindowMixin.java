package smsk.smoothscroll.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatInputSuggestor.SuggestionWindow;
import net.minecraft.client.util.math.Rect2i;
import smsk.smoothscroll.Config;
import smsk.smoothscroll.SmoothSc;

@Mixin(SuggestionWindow.class)
public class SuggestionWindowMixin {
    private int pIndex;
    private int offsetY=0;
    @Shadow int inWindowIndex;
    @Shadow private Rect2i area;
    private int tarIndex;

    @Inject(method = "render",at = @At("HEAD"))
    private void renderH(DrawContext context, int mouseX, int mouseY,CallbackInfo ci){
        offsetY*=Math.pow(Config.cfg.chatSpeed,SmoothSc.mc.getLastFrameDuration());
        inWindowIndex=tarIndex-offsetY/12;
        context.enableScissor(area.getX()-1, area.getY()-1, area.getX()+area.getWidth()+1, area.getY()+area.getHeight()+1);
    }
    @Inject(method = "render",at = @At("TAIL"))
    private void renderT(DrawContext context, int mouseX, int mouseY,CallbackInfo ci){
        inWindowIndex=tarIndex;
        context.disableScissor();
    }
    @ModifyArg(method = "render",at = @At(value = "INVOKE",target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;III)I"),index = 3)
    private int textPosY(int s){
        return(s+offsetY-offsetY/12*12);
    }


    @Inject(method = "mouseScrolled",at = @At("HEAD"))
    private void mScrollH(double am,CallbackInfoReturnable<Boolean> ci){
        SmoothSc.print(inWindowIndex);
        pIndex=inWindowIndex;
    }
    @Inject(method = "mouseScrolled",at = @At("RETURN"))
    private void mScrollT(double am,CallbackInfoReturnable<Boolean> ci){
        offsetY+=(inWindowIndex-pIndex)*12;
        tarIndex=inWindowIndex;
        inWindowIndex=pIndex;
    }
    @Inject(method = "scroll",at = @At("HEAD"))
    private void scrollH(int off,CallbackInfo ci){
        pIndex=inWindowIndex;
    }
    @Inject(method = "scroll",at = @At("TAIL"))
    private void scrollT(int off,CallbackInfo ci){
        offsetY+=(inWindowIndex-pIndex)*12;
        tarIndex=inWindowIndex;
        inWindowIndex=pIndex;
    }
}
