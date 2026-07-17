package io.github.smajloslovakian.smoothscroll.mixin.client.Chat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.gui.components.CommandSuggestions;

@Mixin(CommandSuggestions.class)
public class CommandSuggestionsMixin {
    @WrapOperation(method = "mouseScrolled", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(DDD)D"))
    private double unclampScroll(double value, double min, double max, Operation<Double> operation) {
        return value; //TODO test on windows for bugs caused by unclamping this
    }
}
