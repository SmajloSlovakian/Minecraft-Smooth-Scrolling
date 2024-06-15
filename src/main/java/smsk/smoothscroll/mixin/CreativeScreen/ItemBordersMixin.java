package smsk.smoothscroll.mixin.CreativeScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.anthonyhilyard.itemborders.ItemBorders;

import smsk.smoothscroll.SmoothSc;


@Mixin(ItemBorders.class)
public class ItemBordersMixin {
    @ModifyArg(method = "renderBorder", at = @At(value = "INVOKE", target = "Lcom/anthonyhilyard/itemborders/ItemBorders;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/item/ItemStack;II)V"),index = 3)
    private static int lala(int y) {
        if (SmoothSc.creativeScreenItemCount < 0) return (y);
        return (y + SmoothSc.getCreativeDrawOffset());
    }
}
