package smsk.smoothscroll.mixin.PreciseScissor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.gui.ScreenRect;
import net.minecraft.util.math.MathHelper;
import smsk.smoothscroll.SmoothSc;

@Mixin(ScreenRect.class)
public class ScreenRectMixin {

    //@Redirect(method = "transform", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;floor(F)I"))
    private int windowScale(float val) {
        if (!SmoothSc.preciseScissor) return MathHelper.floor(val);
        return MathHelper.floor(val * SmoothSc.mc.getWindow().getScaleFactor());
    }
}
