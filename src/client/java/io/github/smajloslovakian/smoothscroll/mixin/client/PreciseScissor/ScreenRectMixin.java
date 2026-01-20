package io.github.smajloslovakian.smoothscroll.mixin.client.PreciseScissor;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import io.github.smajloslovakian.smoothscroll.SmoothSc;

@Mixin(ScreenRectangle.class)
public class ScreenRectMixin {

    //@Redirect(method = "transform", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;floor(F)I"))
    private int windowScale(float val) {
        if (!SmoothSc.preciseScissor) return Mth.floor(val);
        return Mth.floor(val * SmoothSc.mc.getWindow().getGuiScale());
    }
}
