package io.github.smajloslovakian.smoothscroll.mixin.client.Miscellaneous;

import com.mojang.renderpearl.api.commands.RenderPass.RenderArea;
import com.mojang.renderpearl.frontend.FrontendRenderPass;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

@Mixin(FrontendRenderPass.class)
public class RenderPassMixin {
    @Shadow private @Final RenderArea renderArea;

    /** This is needed because the scissor values in this mod are almost always calculated and have
     * some safety margins most of the time for better compatibility. That means that the scissor
     * area will sometimes be "invalid" (ex. out of bounds). It's safer for me to just disable this
     * invalid argument exception entirely than to clamp my scissors everywhere. This mixin clamps
     * the values so that an InvalidArgumentException never occurs here.
     */
    @WrapMethod(method = "enableScissor")
    public void ignoreArgumentException(int x, int y, int width, int height, Operation<Void> operation) {
        
        //if (x >= this.renderArea.x() && y >= this.renderArea.y() && x + width <= this.renderArea.x() + this.renderArea.width() && y + height <= this.renderArea.height()) {
        x = Math.clamp(x, renderArea.x(), renderArea.x() + renderArea.width());
        y = Math.clamp(y, renderArea.y(), renderArea.y() + renderArea.height());
        
        width = Math.clamp(width + x, renderArea.x(), renderArea.x() + renderArea.width()) - x;
        height = Math.clamp(height + y, renderArea.y(), renderArea.y() + renderArea.height()) - y;

        if (width == 0 || height == 0) {
            width = 1;
            height = 1;
            x = renderArea.x();
            y = renderArea.y();
        }

        operation.call(x, y, width, height);
    }
}
