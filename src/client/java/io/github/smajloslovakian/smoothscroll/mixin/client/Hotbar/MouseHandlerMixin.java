package io.github.smajloslovakian.smoothscroll.mixin.client.Hotbar;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import io.github.smajloslovakian.smoothscroll.SmoothSc;
import io.github.smajloslovakian.smoothscroll.cfg.SmScCfg;
import io.github.smajloslovakian.smoothscroll.duck.GuiDuck;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    @WrapMethod(method = "onScroll")
    private void onScrollWrap(long handle, double xoffset, double yoffset, Operation<Void> operation) {
        if (SmoothSc.mc.player == null) {
            operation.call(handle, xoffset, yoffset);
            return;
        }

        var oldSlot = SmoothSc.mc.player.getInventory().getSelectedSlot();
        var guiDuck = (GuiDuck)(Object)SmoothSc.mc.gui;

        operation.call(handle, xoffset, yoffset);
        
        if (!SmScCfg.hotbarRollover || SmScCfg.hotbarSmoothness == 0 || SmoothSc.mc.player == null) return;
        
        int newSlot = SmoothSc.mc.player.getInventory().getSelectedSlot();
        if (newSlot == oldSlot) return;

        // vertical > 0 -> Slot Decreases (Left)
        // vertical < 0 -> Slot Increases (Right)
        if (yoffset > 0) { // Scrolling Left
            if (newSlot > oldSlot) { // Wrapped (e.g. 0 -> 8)
                guiDuck.increaseRollover();
            }
        } else if (yoffset < 0) { // Scrolling Right
            if (newSlot < oldSlot) { // Wrapped (e.g. 8 -> 0)
                guiDuck.decreaseRollover();
            }
        }
    }
}
