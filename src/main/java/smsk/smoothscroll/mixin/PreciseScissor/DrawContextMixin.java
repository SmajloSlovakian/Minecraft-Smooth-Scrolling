package smsk.smoothscroll.mixin.PreciseScissor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.pipeline.RenderPipeline;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.Window;
import net.minecraft.util.Identifier;
import smsk.smoothscroll.SmoothSc;

@Mixin(DrawContext.class)
public class DrawContextMixin {
    
    //@Redirect(method = "setScissor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/Window;getScaleFactor()D"))
    private double windowScale(Window w) {
        if (!SmoothSc.preciseScissor) return w.getScaleFactor();
        return 1;
    }
}
