package io.github.smajloslovakian.smoothscroll.mixin.client.Miscellaneous;

import org.joml.Matrix3x2fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.ActiveTextCollector.Parameters;
import net.minecraft.client.gui.navigation.ScreenRectangle;

@Mixin(Parameters.class)
public class TransformationMixin {

    @Shadow ScreenRectangle scissor;
    @Shadow Matrix3x2fc pose;
    @Shadow float opacity;

    @WrapMethod(method = "withScissor")
    private Parameters withScissorWrap(ScreenRectangle scissor, Operation<Parameters> operation) {
        if (scissor == null) {
            if (this.scissor != null) {
                return new Parameters(pose, opacity, null);
            }
            if (this.scissor == null) {
                return (Parameters) (Object) this;
            }
        }
        return operation.call(scissor);
    }
}
