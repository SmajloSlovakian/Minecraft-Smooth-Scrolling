package io.github.smajloslovakian.smoothscroll.mixin.client.Chat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import io.github.smajloslovakian.smoothscroll.duck.ChatComponentDuck;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.ChatScreen;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    @WrapOperation(method = "mouseScrolled", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(DDD)D"))
    private double unclampScroll(double value, double min, double max, Operation<Double> operation) {
        return value; //TODO test windows for bugs caused by unclamping this
    }
    @WrapOperation(method = "mouseScrolled", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;scrollChat(I)V"))
    private void mouseScrolledWrap(ChatComponent chatComponent, int dir, Operation<Boolean> operation, @Local(name = "scrollY") double scrollY) {
        ((ChatComponentDuck)chatComponent).floatyScroll(scrollY);
    }
}