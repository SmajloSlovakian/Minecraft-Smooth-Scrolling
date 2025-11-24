package smsk.smoothscroll.mixin.Hotbar;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.Mouse;
import smsk.smoothscroll.SmoothSc;
import smsk.smoothscroll.cfg.SmScCfg;

@Mixin(Mouse.class)
public class MouseMixin {

    @Unique private int oldSlot;

    @Inject(method = "onMouseScroll", at = @At("HEAD"))
    private void onScrollHead(long window, double horizontal, double vertical, CallbackInfo ci) {

        if (SmoothSc.mc.player != null) {
             this.oldSlot = SmoothSc.mc.player.getInventory().getSelectedSlot();
        }
    }

    @Inject(method = "onMouseScroll", at = @At("TAIL"))
    private void onScrollTail(long window, double horizontal, double vertical, CallbackInfo ci) {

        
        if (!SmScCfg.hotbarRollover || SmScCfg.hotbarSmoothness == 0 || SmoothSc.mc.player == null) return;
        
        int newSlot = SmoothSc.mc.player.getInventory().getSelectedSlot();
        if (newSlot == oldSlot) return;

        // vertical > 0 -> Slot Decreases (Left)
        // vertical < 0 -> Slot Increases (Right)
        if (vertical > 0) { // Scrolling Left
            if (newSlot > oldSlot) { // Wrapped (e.g. 0 -> 8)
                SmoothSc.hotbarRollover += 1;
            }
        } else if (vertical < 0) { // Scrolling Right
            if (newSlot < oldSlot) { // Wrapped (e.g. 8 -> 0)
                SmoothSc.hotbarRollover -= 1;
            }
        }
    }
}
