package io.github.smajloslovakian.smoothscroll.mixin.client.PreciseScissor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.gui.GuiGraphics;
import io.github.smajloslovakian.smoothscroll.SmoothSc;

@Mixin(GuiGraphics.class)
public class DrawContextMixin {
    
    //@Redirect(method = "setScissor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/Window;getScaleFactor()D"))
    private double windowScale(Window w) {
        if (!SmoothSc.preciseScissor) return w.getGuiScale();
        return 1;
    }
}
