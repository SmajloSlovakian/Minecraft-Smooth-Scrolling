package smsk.smoothscroll.mixin.Miscellaneous;

import org.joml.Matrix3x2fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import net.minecraft.client.font.DrawnTextConsumer.Transformation;
import net.minecraft.client.gui.ScreenRect;

@Mixin(Transformation.class)
public class TransformationMixin {

    @Shadow ScreenRect scissor;
    @Shadow Matrix3x2fc pose;
    @Shadow float opacity;

    @WrapMethod(method = "withScissor")
    private Transformation withScissorWrap(ScreenRect scissor, Operation<Transformation> operation) {
        if (scissor == null) {
            if (this.scissor != null) {
                return new Transformation(pose, opacity, null);
            }
            if (this.scissor == null) {
                return (Transformation) (Object) this;
            }
        }
        return operation.call(scissor);
    }
}
