package io.github.smajloslovakian.smoothscroll.mixin.client.Chat;

import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.CommandSuggestions.SuggestionsList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.Message;
import io.github.smajloslovakian.smoothscroll.SmoothSc;
import io.github.smajloslovakian.smoothscroll.cfg.SmScCfg;

@Mixin(SuggestionsList.class)
public class SuggestionsListMixin {
    @Final @Shadow private List<Suggestion> suggestionList;
    @Final @Shadow private Rect2i rect;
    @Shadow private int offset; // scroll position - index of the uppermost line (up < down)

    @Unique private int lineHeight = 12;

    @Unique private float smoothScrollPos = offset;
    @Unique private float targetScrollPos = offset;
    @Unique private boolean translated = false;

    @WrapMethod(method = "extractRenderState")
    private void extractRenderStateWrap(GuiGraphicsExtractor graphics, int mouseX, int mouseY, Operation<Void> operation) {

        smoothScrollPos = (smoothScrollPos - targetScrollPos) * (float) Math.pow(SmScCfg.suggestionWindowSmoothness, SmoothSc.getLastFrameDuration()) + targetScrollPos;

        // snap on less than half a pixel difference
        if (Math.abs(smoothScrollPos - targetScrollPos) < 1f / lineHeight / 2)
            smoothScrollPos = targetScrollPos;

        offset = (int) Math.floor(smoothScrollPos);

        operation.call(graphics, rect.contains(mouseX, mouseY) ? mouseX : -1, (int) Math.floor(mouseY - getDrawOffset()));
        
        tryUntranslate(graphics);
    }

    @Inject(method = "extractRenderState", at = @At(value = "INVOKE", target = "Ljava/util/List;get(I)Ljava/lang/Object;", ordinal = 0))
    private void translate(GuiGraphicsExtractor graphics, int mouseX, int mouseY, CallbackInfo ci) {
        if (translated) return;
        graphics.enableScissor(0, rect.getY(), graphics.guiWidth(), rect.getY() + rect.getHeight());
        graphics.pose().pushMatrix();
        graphics.pose().translate(0, getDrawOffset());
        translated = true;
    }
    @ModifyVariable(method = "extractRenderState", at = @At(value = "STORE"), ordinal = 0)
    private Message unTranslate(Message a, @Local GuiGraphicsExtractor graphics) {
        tryUntranslate(graphics);
        return a;
    }

    private void tryUntranslate(GuiGraphicsExtractor graphics) {
        if (translated) {
            graphics.pose().popMatrix();
            if (SmScCfg.enableMaskDebug)
                graphics.fill(-100, -100, graphics.guiWidth(), graphics.guiHeight(), ARGB.color(50, 255, 255, 0));
            graphics.disableScissor();
        }
        translated = false;
    }


    @WrapMethod(method = "mouseScrolled")
    private boolean mouseScrolledWrap(double scroll, Operation<Boolean> operation) {
        if (SmScCfg.suggestionWindowAmount == 0) return operation.call(scroll);

        var indexBefore = offset;

        var newTarget = targetScrollPos - scroll * (SmScCfg.suggestionWindowAmount != 0 ? SmScCfg.suggestionWindowAmount / lineHeight : 1);
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
    private void cycleWrap(int off, Operation<Void> operation) {

        var indexBefore = offset;
        offset = (int) Math.floor(targetScrollPos);

        operation.call(off);
        
        targetScrollPos = offset;
        offset = indexBefore;
    }

    @WrapMethod(method = "mouseClicked")
    public boolean mouseClickedWrap(int x, int y, Operation<Boolean> operation) {
        if (!rect.contains(x, y)) {
            return false;
        }
        return operation.call(x, y - Math.round(getDrawOffset()));
    }
    @Redirect(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/Rect2i;contains(II)Z"))
    public boolean mouseClickedRedirectCondition(Rect2i rect, int x, int y) {
        return true;
    }

    @ModifyVariable(method = "extractRenderState", at = @At("STORE"), ordinal = 2)
    private int addLineUnder(int limit) {
        if (getDrawOffset() == 0) return limit;
        return limit + 1;
    }

    @WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/Rect2i;contains(II)Z"))
    public boolean requestCursorRectCondition(Rect2i rect, int x, int y, Operation<Boolean> operation) {
        return operation.call(rect, x, (int) Math.floor(y + getDrawOffset()));
    }

    boolean align_pixels = false;
    @Unique
    private float getDrawOffset() {
        var ret = (offset - smoothScrollPos) * lineHeight;
        return align_pixels ? Math.round(ret) : ret;
    }
}
