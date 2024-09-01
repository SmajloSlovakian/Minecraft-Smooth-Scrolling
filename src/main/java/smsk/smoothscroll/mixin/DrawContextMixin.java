package smsk.smoothscroll.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.client.gui.DrawContext;
import smsk.smoothscroll.SmoothSc;

@Mixin(DrawContext.class)
public class DrawContextMixin {
    @ModifyVariable(method = "setScissor", at = @At(value = "STORE"), ordinal = 0)
    private double changeScissorScaleFactor(double d) {
        if (SmoothSc.scissorScaleFactor == 0) return d;
        return SmoothSc.scissorScaleFactor;
    }
}
