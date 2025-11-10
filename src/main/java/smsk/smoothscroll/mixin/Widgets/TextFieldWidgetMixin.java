package smsk.smoothscroll.mixin.Widgets;

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

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import smsk.smoothscroll.SmoothSc;
import smsk.smoothscroll.cfg.SmScCfg;

@Mixin(TextFieldWidget.class)
public class TextFieldWidgetMixin {

    @Shadow @Final private TextRenderer textRenderer;
    @Shadow private int firstCharacterIndex;
    @Shadow private String text;
    @Shadow private int textX;
    @Shadow private int textY;

    @Unique private float smoothScrollPos = 0; // in pixels (left < right)
    @Unique private float targetScrollPos = 0;
    @Unique private float prevCursorPixel = 0;

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        targetScrollPos = (float) MathHelper.clamp(targetScrollPos - (verticalAmount + horizontalAmount) * SmScCfg.textAmount, 0, textRenderer.getWidth(text));
        return true;
    }

    @WrapMethod(method = "renderWidget")
    private void renderWidgetWrap(DrawContext context, int mouseX, int mouseY, float deltaTicks, Operation<Void> operation) {
        smoothScrollPos = (smoothScrollPos - targetScrollPos) * (float) Math.pow(SmScCfg.textSmoothness, SmoothSc.getLastFrameDuration()) + targetScrollPos;

        firstCharacterIndex = textRenderer.trimToWidth(text, Math.round(smoothScrollPos)).length();

        context.enableScissor(textX, textY - 10, textX + getInnerWidth(), textY + 10);
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(getDrawOffset(), 0);
        operation.call(context, mouseX, mouseY, deltaTicks);
        context.getMatrices().popMatrix();
        if (SmScCfg.enableMaskDebug)
            context.fill(-100, -100, context.getScaledWindowWidth(), context.getScaledWindowHeight(), ColorHelper.getArgb(50, 0, 255, 255));
        context.disableScissor();
    }

    @WrapOperation(method = "renderWidget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIII)V", ordinal = 0))
    private void drawBackground(DrawContext context, RenderPipeline pipeline, Identifier sprite, int x, int y, int width, int height, Operation<Void> operation) {
        context.getMatrices().popMatrix(); // TODO somehow remove this code duplication
        context.disableScissor();
        operation.call(context, pipeline, sprite, x, y, width, height);
        context.enableScissor(textX, textY - 10, textX + getInnerWidth(), textY + 10);
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(getDrawOffset(), 0);
    }

    @ModifyVariable(method = "renderWidget", at = @At(value = "STORE"), ordinal = 0)
    private String addCharacters(String visibleString) {
        if (firstCharacterIndex + visibleString.length() >= text.length()) {
            return visibleString;
        }
        
        var firstCharWidth = textRenderer.getWidth(visibleString.charAt(0) + "");
        var additionalChars = textRenderer.trimToWidth(text.substring(firstCharacterIndex + visibleString.length()), firstCharWidth);
        //var additionalChars = textRenderer.trimToWidth(text.substring(firstCharacterIndex + visibleString.length()), (int) Math.ceil(-getDrawOffset()));
        var totalLen = firstCharacterIndex + visibleString.length() + additionalChars.length();
        if (text.length() > totalLen) {
            additionalChars += text.charAt(totalLen);
        }
        
        return visibleString + additionalChars;
    }

    @WrapMethod(method = "calculateCursorPos")
    private int calculateCursorPosWrap(Click click, Operation<Integer> operation) {
        return operation.call(new Click(click.x() - getDrawOffset(), click.y(), click.buttonInfo()));
    }

    @WrapMethod(method = "updateFirstCharacterIndex")
    private void updateFirstCharacterIndexWrap(int cursor, Operation<Void> operation) {
        if (!SmScCfg.textCustomUpdate) {
            operation.call(cursor);
            targetScrollPos = textRenderer.getWidth(text.substring(0, firstCharacterIndex));
            return;
        }

        var cursorPixel = textRenderer.getWidth(text.substring(0, cursor));
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
        targetScrollPos = Math.clamp(targetScrollPos, 0, textRenderer.getWidth(text));
        firstCharacterIndex = 0;
        prevCursorPixel = cursorPixel;
    }

    @Unique
    private float getDrawOffset() {
        return textRenderer.getWidth(textRenderer.trimToWidth(text, Math.round(smoothScrollPos))) - smoothScrollPos;
    }

    @Shadow private int getInnerWidth() {return 0;}
}
