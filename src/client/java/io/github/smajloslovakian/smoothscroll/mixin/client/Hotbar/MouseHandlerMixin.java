package io.github.smajloslovakian.smoothscroll.mixin.client.Hotbar;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import io.github.smajloslovakian.smoothscroll.SmoothSc;
import io.github.smajloslovakian.smoothscroll.cfg.SmScCfg;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    @WrapMethod(method = "onScroll")
    private void onScrollWrap(long handle, double xoffset, double yoffset, Operation<Void> operation) {
        if (SmoothSc.mc.player == null) return;

        var oldSlot = SmoothSc.mc.player.getInventory().getSelectedSlot();

        operation.call(handle, xoffset, yoffset);
        
        if (!SmScCfg.hotbarRollover || SmScCfg.hotbarSmoothness == 0 || SmoothSc.mc.player == null) return;
        
        int newSlot = SmoothSc.mc.player.getInventory().getSelectedSlot();
        if (newSlot == oldSlot) return;

        // vertical > 0 -> Slot Decreases (Left)
        // vertical < 0 -> Slot Increases (Right)
        if (yoffset > 0) { // Scrolling Left
            if (newSlot > oldSlot) { // Wrapped (e.g. 0 -> 8)
                SmoothSc.hotbarRollover += 1; // TODO use duck interface
            }
        } else if (yoffset < 0) { // Scrolling Right
            if (newSlot < oldSlot) { // Wrapped (e.g. 8 -> 0)
                SmoothSc.hotbarRollover -= 1;
            }
        }
    }
}
