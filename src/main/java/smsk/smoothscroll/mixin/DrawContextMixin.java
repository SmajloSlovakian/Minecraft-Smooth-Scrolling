package smsk.smoothscroll.mixin;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalDoubleRef;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.util.math.MatrixStack;
import smsk.smoothscroll.SmoothSc;

@Mixin(DrawContext.class)
public class DrawContextMixin {
    @Shadow MatrixStack matrices;

    /*
     * this took a long time to do at least remotely functional, i hope i won't need to go back to this later again much
     */
    @ModifyVariable(method = "setScissor", at = @At(value = "STORE"), ordinal = 4)
    double preciseScissora(double h, @Local @Nullable ScreenRect rect, @Local(ordinal = 0) LocalIntRef i, @Local(ordinal = 0) LocalDoubleRef d, @Local(ordinal = 1) LocalDoubleRef e, @Local(ordinal = 2) LocalDoubleRef f, @Local(ordinal = 3) LocalDoubleRef g) {
        if (!SmoothSc.scissorMatrixEnabled) return h;

        Vector3f tr = matrices.peek().getPositionMatrix().getTranslation(new Vector3f(0, 0, 0));
        Vector3f sc = matrices.peek().getPositionMatrix().getScale(new Vector3f(0, 0, 0));

        e.set((rect.getLeft() + tr.x) * d.get());

        g.set(rect.width() * sc.x * d.get());
        h = rect.height() * sc.y * d.get();

        f.set(i.get() - (rect.getTop() + tr.y) * d.get() - h);

        return h;
    }

    //@ModifyVariable(method = "setScissor", at = @At(value = "STORE"), ordinal = 4)
    double preciseScissor(double h, @Local @Nullable ScreenRect rect, @Local(ordinal = 0) LocalIntRef i, @Local(ordinal = 0) LocalDoubleRef d, @Local(ordinal = 1) LocalDoubleRef e, @Local(ordinal = 2) LocalDoubleRef f, @Local(ordinal = 3) LocalDoubleRef g) {
        if (!SmoothSc.scissorMatrixEnabled) return h;

        float x1 = rect.getLeft();
        float y1 = rect.getTop();
        float x2 = rect.getRight();
        float y2 = rect.getBottom();

        Vector4f tl = matrices.peek().getPositionMatrix().transform(new Vector4f(x1, y1, 0, 0));
        Vector4f br = matrices.peek().getPositionMatrix().transform(new Vector4f(x2, y2, 0, 0));

        e.set(tl.x);
        f.set(i.get() - br.y);
        g.set(br.x - tl.x);
        return br.y - tl.y;
    }
}
