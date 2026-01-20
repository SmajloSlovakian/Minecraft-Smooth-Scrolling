package io.github.smajloslovakian.smoothscroll.mixin.client.Chat;

import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.CommandSuggestions.SuggestionsList;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.Message;
import io.github.smajloslovakian.smoothscroll.SmoothSc;
import io.github.smajloslovakian.smoothscroll.cfg.SmScCfg;

@Mixin(SuggestionsList.class)
public class SuggestionWindowMixin {
    @Final @Shadow private List<Suggestion> suggestionList;
    @Final @Shadow private Rect2i rect;
    @Shadow private int offset; // scroll position - index of the uppermost line (up < down)

    @Unique private int lineHeight = 12;

    @Unique private float smoothScrollPos = offset;
    @Unique private float targetScrollPos = offset;
    @Unique private boolean translated = false;

    @WrapMethod(method = "render")
    private void renderH(GuiGraphics context, int mouseX, int mouseY, Operation<Void> operation) {
        if (SmScCfg.suggestionWindowSmoothness == 0) {
            operation.call(context, mouseX, mouseY);
            return;
        }

        smoothScrollPos = (smoothScrollPos - targetScrollPos) * (float) Math.pow(SmScCfg.suggestionWindowSmoothness, SmoothSc.getLastFrameDuration()) + targetScrollPos;

        // snap on less than half a pixel difference
        if (Math.abs(smoothScrollPos - targetScrollPos) < 1f / lineHeight / 2)
            smoothScrollPos = targetScrollPos;

        offset = (int) Math.floor(smoothScrollPos);

        operation.call(context, mouseX, (int) Math.floor(mouseY - (int) Math.floor(getDrawOffset())));
        
        tryUntranslate(context);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Ljava/util/List;get(I)Ljava/lang/Object;", ordinal = 0))
    private void translate(GuiGraphics context, int mouseX, int mouseY, CallbackInfo ci) {
        if (SmScCfg.suggestionWindowSmoothness == 0) return;
        if (translated) return;
        context.enableScissor(0, rect.getY(), context.guiWidth(), rect.getY() + rect.getHeight());
        context.pose().pushMatrix();
        context.pose().translate(0, (int) Math.floor(getDrawOffset()));
        translated = true;
    }
    @ModifyVariable(method = "render", at = @At(value = "STORE"), ordinal = 0)
    private Message unTranslate(Message a, @Local GuiGraphics context) {
        tryUntranslate(context);
        return a;
    }

    private void tryUntranslate(GuiGraphics context) {
        if (translated) {
            context.pose().popMatrix();
            if (SmScCfg.enableMaskDebug)
                context.fill(-100, -100, context.guiWidth(), context.guiHeight(), ARGB.color(50, 255, 255, 0));
            context.disableScissor();
        }
        translated = false;
    }


    @WrapMethod(method = "mouseScrolled")
    private boolean mScroll(double am, Operation<Boolean> operation) {
        if (SmScCfg.suggestionWindowSmoothness == 0) return operation.call(am);

        var indexBefore = offset;

        var newTarget = targetScrollPos - am * (SmScCfg.suggestionWindowAmount != 0 ? SmScCfg.suggestionWindowAmount / lineHeight : 1);
        offset = (int) Math.ceil(newTarget);


        // we'll only use the clamp from the call and also the return value
        var ret = operation.call(0d);

        if (!ret) {
            offset = indexBefore;
            return ret;
        }

        if (newTarget > offset) {
            newTarget = offset;
        }
        if (newTarget < 0) {
            newTarget = 0;
        }

        targetScrollPos = (float) newTarget;

        return ret;
    }

    //this shouldn't touch the scroll amount, it is called when scrolling by keyboard-selecting
    @WrapMethod(method = "cycle")
    private void scroll(int off, Operation<Void> operation) {
        if (SmScCfg.suggestionWindowSmoothness == 0) {
            operation.call();
            return;
        }

        var indexBefore = offset;
        offset = (int) Math.floor(targetScrollPos);

        operation.call(off);
        
        targetScrollPos = offset;
        offset = indexBefore;
    }

    @ModifyVariable(method = "render", at = @At("STORE"), ordinal = 2)
    private int addLineUnder(int i) {
        if (SmScCfg.suggestionWindowSmoothness == 0 || getDrawOffset() == 0) return i;
        return i + 1;
    }

    @Unique
    private float getDrawOffset() {
        return (offset - smoothScrollPos) * lineHeight;
    }
}
