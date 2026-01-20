package io.github.smajloslovakian.smoothscroll.mixin.client.Hotbar;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import io.github.smajloslovakian.smoothscroll.SmoothSc;
import io.github.smajloslovakian.smoothscroll.cfg.SmScCfg;

@Mixin(MouseHandler.class)
public class MouseMixin {

    @Unique private int oldSlot;

    @Inject(method = "onScroll", at = @At("HEAD"))
    private void onScrollHead(long window, double horizontal, double vertical, CallbackInfo ci) {

        if (SmoothSc.mc.player != null) {
             this.oldSlot = SmoothSc.mc.player.getInventory().getSelectedSlot();
        }
    }

    @Inject(method = "onScroll", at = @At("TAIL"))
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
