package io.github.smajloslovakian.smoothscroll.mixin.client.Widgets;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import io.github.smajloslovakian.smoothscroll.SmoothSc;
import io.github.smajloslovakian.smoothscroll.cfg.SmScCfg;

@Mixin(EditBox.class)
public class TextFieldWidgetMixin {

    @Shadow @Final private Font font;
    @Shadow private int displayPos;
    @Shadow private String value;
    @Shadow private int textX;
    @Shadow private int textY;

    @Unique private float smoothScrollPos = 0; // in pixels (left < right)
    @Unique private float targetScrollPos = 0;
    @Unique private float prevCursorPixel = 0;

    // mouseScrolled == method_25401
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        targetScrollPos = (float) Mth.clamp(targetScrollPos - (verticalAmount + horizontalAmount) * SmScCfg.textAmount, 0, font.width(value));
        return true;
    }
    public boolean method_25401(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        targetScrollPos = (float) Mth.clamp(targetScrollPos - (verticalAmount + horizontalAmount) * SmScCfg.textAmount, 0, font.width(value));
        return true;
    }

    @WrapMethod(method = "renderWidget")
    private void renderWidgetWrap(GuiGraphics context, int mouseX, int mouseY, float deltaTicks, Operation<Void> operation) {
        smoothScrollPos = (smoothScrollPos - targetScrollPos) * (float) Math.pow(SmScCfg.textSmoothness, SmoothSc.getLastFrameDuration()) + targetScrollPos;

        displayPos = font.plainSubstrByWidth(value, Math.round(smoothScrollPos)).length();

        context.enableScissor(textX, textY - 10, textX + getInnerWidth(), textY + 10);
        context.pose().pushMatrix();
        context.pose().translate(getDrawOffset(), 0);
        operation.call(context, mouseX, mouseY, deltaTicks);
        context.pose().popMatrix();
        if (SmScCfg.enableMaskDebug)
            context.fill(-100, -100, context.guiWidth(), context.guiHeight(), ARGB.color(50, 0, 255, 255));
        context.disableScissor();
    }

    @WrapOperation(method = "renderWidget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V", ordinal = 0))
    private void drawBackground(GuiGraphics context, RenderPipeline pipeline, Identifier sprite, int x, int y, int width, int height, Operation<Void> operation) {
        context.pose().popMatrix(); // TODO somehow remove this code duplication
        context.disableScissor();
        operation.call(context, pipeline, sprite, x, y, width, height);
        context.enableScissor(textX, textY - 10, textX + getInnerWidth(), textY + 10);
        context.pose().pushMatrix();
        context.pose().translate(getDrawOffset(), 0);
    }

    @ModifyVariable(method = "renderWidget", at = @At(value = "STORE"), ordinal = 0)
    private String addCharacters(String visibleString) {
        if (displayPos + visibleString.length() >= value.length()) {
            return visibleString;
        }
        
        var firstCharWidth = font.width(visibleString.charAt(0) + "");
        var additionalChars = font.plainSubstrByWidth(value.substring(displayPos + visibleString.length()), firstCharWidth);
        //var additionalChars = textRenderer.trimToWidth(text.substring(firstCharacterIndex + visibleString.length()), (int) Math.ceil(-getDrawOffset()));
        var totalLen = displayPos + visibleString.length() + additionalChars.length();
        if (value.length() > totalLen) {
            additionalChars += value.charAt(totalLen);
        }
        
        return visibleString + additionalChars;
    }

    @WrapMethod(method = "findClickedPositionInText")
    private int calculateCursorPosWrap(MouseButtonEvent click, Operation<Integer> operation) {
        return operation.call(new MouseButtonEvent(click.x() - getDrawOffset(), click.y(), click.buttonInfo()));
    }

    @WrapMethod(method = "scrollTo")
    private void updateFirstCharacterIndexWrap(int cursor, Operation<Void> operation) {
        if (!SmScCfg.textCustomUpdate) {
            operation.call(cursor);
            targetScrollPos = font.width(value.substring(0, displayPos));
            return;
        }

        var cursorPixel = font.width(value.substring(0, cursor));
        var margin = SmScCfg.textMargin / 100 * getInnerWidth();

        if (SmScCfg.textMargin > 50) {
            if (cursorPixel < prevCursorPixel && cursorPixel < targetScrollPos + margin) {
                targetScrollPos = cursorPixel - margin;
            } else if (cursorPixel > prevCursorPixel && cursorPixel > targetScrollPos + getInnerWidth() - margin) {
                targetScrollPos = cursorPixel - getInnerWidth() + margin;
            }
            targetScrollPos = Math.clamp(targetScrollPos, cursorPixel - getInnerWidth(), cursorPixel);
        } else if (cursorPixel < targetScrollPos + margin) {
            targetScrollPos = cursorPixel - margin;
        } else if (cursorPixel > targetScrollPos + getInnerWidth() - margin) {
            targetScrollPos = cursorPixel - getInnerWidth() + margin;
        }
        targetScrollPos = Math.clamp(targetScrollPos, 0, font.width(value));
        displayPos = 0;
        prevCursorPixel = cursorPixel;
    }

    @Unique
    private float getDrawOffset() {
        return font.width(font.plainSubstrByWidth(value, Math.round(smoothScrollPos))) - smoothScrollPos;
    }

    @Shadow private int getInnerWidth() {return 0;}
}
