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
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import io.github.smajloslovakian.smoothscroll.SmoothSc;
import io.github.smajloslovakian.smoothscroll.cfg.SmScCfg;

@Mixin(EditBox.class)
public abstract class EditBoxMixin extends AbstractWidget {
    @Shadow @Final private Font font;
    @Shadow private int displayPos;
    @Shadow private String value;
    @Shadow private int textX;
    @Shadow private int textY;

    @Unique private float smoothScrollPos = 0; // in pixels (left < right)
    @Unique private float targetScrollPos = 0;
    @Unique private float prevCursorPixel = 0;

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        // safety net - vanilla checks for hovering before calling this function, mods occasionally do not
        if (!isHovered()) {
            return false;
        }
        targetScrollPos = (float) Mth.clamp(targetScrollPos - (verticalAmount + horizontalAmount) * SmScCfg.textAmount, 0, font.width(value));
        return true;
    }

    @WrapMethod(method = "extractWidgetRenderState")
    private void renderWidgetWrap(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks, Operation<Void> operation) {
        smoothScrollPos = (smoothScrollPos - targetScrollPos) * (float) Math.pow(SmScCfg.textSmoothness, SmoothSc.getLastFrameDuration()) + targetScrollPos;

        displayPos = font.plainSubstrByWidth(value, Math.round(smoothScrollPos)).length();

        graphics.enableScissor(textX, textY - 10, textX + getInnerWidth(), textY + 10);
        graphics.pose().pushMatrix();
        graphics.pose().translate(getDrawOffset(), 0);
        operation.call(graphics, mouseX, mouseY, deltaTicks);
        graphics.pose().popMatrix();
        if (SmScCfg.enableMaskDebug)
            graphics.fill(-100, -100, graphics.guiWidth(), graphics.guiHeight(), ARGB.color(50, 0, 255, 255));
        graphics.disableScissor();
    }

    @WrapOperation(method = "extractWidgetRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V", ordinal = 0))
    private void drawBackground(GuiGraphicsExtractor graphics, RenderPipeline renderPipeline, Identifier location, int x, int y, int width, int height, Operation<Void> operation) {
        graphics.pose().popMatrix(); // TODO somehow remove this code duplication
        graphics.disableScissor();
        operation.call(graphics, renderPipeline, location, x, y, width, height);
        graphics.enableScissor(textX, textY - 10, textX + getInnerWidth(), textY + 10);
        graphics.pose().pushMatrix();
        graphics.pose().translate(getDrawOffset(), 0);
    }

    @ModifyVariable(method = "extractWidgetRenderState", at = @At(value = "STORE"), name = "displayed")
    private String addCharacters(String displayed) {
        if (displayPos + displayed.length() >= value.length() || displayed.length() == 0) {
            return displayed;
        }

        var firstCharWidth = font.width(displayed.charAt(0) + "");
        var additionalChars = font.plainSubstrByWidth(value.substring(displayPos + displayed.length()), firstCharWidth);
        //var additionalChars = textRenderer.trimToWidth(text.substring(firstCharacterIndex + visibleString.length()), (int) Math.ceil(-getDrawOffset()));
        var totalLen = displayPos + displayed.length() + additionalChars.length();
        if (value.length() > totalLen) {
            additionalChars += value.charAt(totalLen);
        }
        
        return displayed + additionalChars;
    }

    @WrapMethod(method = "findClickedPositionInText")
    private int findClickedPositionInTextWrap(MouseButtonEvent event, Operation<Integer> operation) {
        return operation.call(new MouseButtonEvent(event.x() - getDrawOffset(), event.y(), event.buttonInfo()));
    }

    @WrapMethod(method = "scrollTo")
    private void scrollToWrap(int pos, Operation<Void> operation) {
        if (!SmScCfg.textCustomUpdate) {
            operation.call(pos);
            targetScrollPos = font.width(value.substring(0, displayPos));
            return;
        }

        var cursorPixel = font.width(value.substring(0, pos));
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
    
    public EditBoxMixin(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }
}
