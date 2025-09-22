package smsk.smoothscroll.mixin.Chat;

import java.util.List;
import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.suggestion.Suggestion;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.screen.ChatInputSuggestor.SuggestionWindow;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.util.math.ColorHelper;
import com.mojang.brigadier.Message;
import smsk.smoothscroll.SmoothSc;
import smsk.smoothscroll.cfg.SmScCfg;

@Mixin(SuggestionWindow.class)
public class SuggestionWindowMixin {
    @Shadow private int inWindowIndex;
    @Final @Shadow private List<Suggestion> suggestions;
    @Final @Shadow private Rect2i area;

    @Unique private int lineHeight = 12;
    @Unique private int maxLinesShown = 10;

    @Unique private float currentIndex = inWindowIndex;
    @Unique private int targetIndex = inWindowIndex;
    @Unique private boolean translated = false;

    @WrapMethod(method = "render")
    private void renderH(DrawContext context, int mouseX, int mouseY, Operation<Void> operation) {
        if (SmScCfg.chatSmoothness == 0) {
            operation.call(context, mouseX, mouseY);
            return;
        }

        currentIndex = (currentIndex - targetIndex) * (float) Math.pow(SmScCfg.chatSmoothness, SmoothSc.getLastFrameDuration()) + targetIndex;
        inWindowIndex = (int) Math.floor(currentIndex);

        operation.call(context, mouseX, mouseY - getDrawOffset());
        
        tryUnTextPosY(context);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Ljava/util/List;get(I)Ljava/lang/Object;", ordinal = 0))
    private void textPosY(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
        if (SmScCfg.chatSmoothness == 0) return;
        if (translated) return;
        context.enableScissor(0, area.getY(), context.getScaledWindowWidth(), area.getY() + area.getHeight());
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(0, getDrawOffset());
        translated = true;
    }
    @ModifyVariable(method = "render", at = @At(value = "STORE"), ordinal = 0)
    private Message unTextPosY(Message a, @Local DrawContext context) {
        tryUnTextPosY(context);
        return a;
    }

    private void tryUnTextPosY(DrawContext context) {
        if (translated) {
            context.getMatrices().popMatrix();
            if (SmScCfg.enableMaskDebug)
                context.fill(-100, -100, context.getScaledWindowWidth(), context.getScaledWindowHeight(), ColorHelper.getArgb(50, 255, 255, 0));
            context.disableScissor();
        }
        translated = false;
    }


    @WrapMethod(method = "mouseScrolled")
    private boolean mScrollH(double am, Operation<Boolean> operation) {
        return commonScrollWrap(() -> {
            return operation.call(am);
        });
    }
    @WrapMethod(method = "scroll")
    private void scrollT(int off, Operation<Void> operation) {
        commonScrollWrap(() -> {
            operation.call(off);
            return null;
        });
    }

    @Unique
    private <T> T commonScrollWrap(Supplier<T> operation){
        if (SmScCfg.chatSmoothness == 0) return operation.get();

        var indexBefore = inWindowIndex;
        inWindowIndex = targetIndex;

        var ret = operation.get();
        
        targetIndex = inWindowIndex;
        inWindowIndex = indexBefore;
        
        return ret;
    }

    @ModifyVariable(method = "render", at = @At("STORE"), ordinal = 2)
    private int addLineUnder(int i) {
        if (SmScCfg.chatSmoothness == 0 || getDrawOffset() == 0) return i;
        return i + 1;
    }

    @Unique
    private int getDrawOffset() {
        return (int) Math.floor((inWindowIndex - currentIndex) * lineHeight);
    }
}
