package smsk.smoothscroll.mixin.Chat;

import java.util.List;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.mojang.brigadier.suggestion.Suggestion;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatInputSuggestor.SuggestionWindow;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.util.math.ColorHelper;
import smsk.smoothscroll.Config;
import smsk.smoothscroll.SmoothSc;

@Mixin(SuggestionWindow.class)
public class SuggestionWindowMixin {
    @Shadow private int inWindowIndex;
    @Final @Shadow private List<Suggestion> suggestions;
    @Final @Shadow private Rect2i area;

    @Unique private int indexBefore;
    @Unique private float scrollPixelOffset;
    @Unique private int targetIndex;

    @Inject(method = "render", at = @At("HEAD"))
    private void renderH(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
        if(Config.cfg.chatSpeed == 0) return;
        scrollPixelOffset = (float) (scrollPixelOffset * Math.pow(Config.cfg.chatSpeed, SmoothSc.getLastFrameDuration()));
        inWindowIndex = SmoothSc.clamp(targetIndex - getScrollOffset() / 12, 0, suggestions.size() - 10); // the clamp is here as a workaround to a crash
    }
    /*@ModifyVariable(method = "render", at = @At(value = "STORE"), ordinal = 4) idk why this doesn't work
    private boolean mask(boolean a) {
        SmoothSc.print(a);
        savedContext.enableScissor(area.getX() - 1, area.getY(), area.getX() + area.getWidth() + 1, area.getY() + area.getHeight());
        return (a);
    }/* */

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;fill(IIIII)V", ordinal = 4))
    private void mask(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
        if(Config.cfg.chatSpeed == 0) return;
        // savedContext.enableScissor(area.getX() - 1, area.getY(), area.getX() + area.getWidth(), area.getY() + area.getHeight());
        context.enableScissor(0, area.getY(), context.getScaledWindowWidth(), area.getY() + area.getHeight());
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;III)I", shift = At.Shift.AFTER))
    private void demask(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
        if(Config.cfg.chatSpeed == 0) return;
        if (Config.cfg.enableMaskDebug)
            context.fill(-100, -100, context.getScaledWindowWidth(), context.getScaledWindowHeight(), ColorHelper.Argb.getArgb(50, 255, 255, 0));
        context.disableScissor();
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void renderT(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
        if(Config.cfg.chatSpeed == 0) return;
        inWindowIndex = targetIndex;
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;III)I"), index = 3)
    private int textPosY(int s) {
        if(Config.cfg.chatSpeed == 0) return (s);
        return (s + getDrawOffset());
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"))
    private void mScrollH(double am, CallbackInfoReturnable<Boolean> ci) {commonSH();}
    @Inject(method = "mouseScrolled", at = @At("RETURN"))
    private void mScrollT(double am, CallbackInfoReturnable<Boolean> ci) {commonST();}
    @Inject(method = "scroll", at = @At("HEAD"))
    private void scrollH(int off, CallbackInfo ci) {commonSH();}
    @Inject(method = "scroll", at = @At("TAIL"))
    private void scrollT(int off, CallbackInfo ci) {commonST();}

    @Unique
    private void commonSH(){
        if(Config.cfg.chatSpeed == 0) return;
        indexBefore = inWindowIndex;
    }

    @Unique
    private void commonST(){
        if(Config.cfg.chatSpeed == 0) return;
        scrollPixelOffset += (inWindowIndex - indexBefore) * 12;
        targetIndex = inWindowIndex;
        inWindowIndex = indexBefore;
    }

    @ModifyVariable(method = "render", at = @At("STORE"), ordinal = 4)
    private int addLineAbove(int r) { // this function gets called three times for just one line for some reason
        if (Config.cfg.chatSpeed == 0 || getScrollOffset() <= 0 || inWindowIndex <= 0) return (r);
        return (r - 1);
    }

    @ModifyVariable(method = "render", at = @At("STORE"), ordinal = 2)
    private int addLineUnder(int i) {
        if (Config.cfg.chatSpeed == 0 || getScrollOffset() >= 0 || inWindowIndex >= suggestions.size() - 10) return (i);
        return (i + 1);
    }

    @Unique
    private int getDrawOffset() {
        return Math.round(scrollPixelOffset) - (Math.round(scrollPixelOffset) / 12 * 12);
    }

    @Unique
    private int getScrollOffset() {
        return Math.round(scrollPixelOffset);
    }
}
