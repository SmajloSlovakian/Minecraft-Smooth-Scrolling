package io.github.smajloslovakian.smoothscroll.mixin.client.Miscellaneous;

import org.lwjgl.sdl.SDL_Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.platform.SDLEventHandler;

import io.github.smajloslovakian.smoothscroll.Globals;
import io.github.smajloslovakian.smoothscroll.SmoothSc;
import io.github.smajloslovakian.smoothscroll.cfg.SmScCfg;
import net.minecraft.util.Util;

@Mixin(SDLEventHandler.class)
public class SDLEventHandlerMixin {
    private double lastScrollY = 0;
    private long lastScrollYTime = 0;

    private double inertia = 0;

    //@WrapOperation(method = "pollEvents", at = @At(value = "INVOKE", target = "Lorg/lwjgl/sdl/SDLEvents;SDL_PollEvent(Lorg/lwjgl/sdl/SDL_Event;)Z"))
    private boolean SDL_PollEventWrap(SDL_Event event, Operation<Boolean> operation) {
        var ret = operation.call(event);
        if (event.type() != 32512) {
            SmoothSc.printt(event.type(), event.wheel().which());
        }
        return ret;
    }

    @Inject(method = "handleMouseWheelEvent", at = @At("HEAD"))
    private void handleMouseWheelEvent(SDL_Event event, CallbackInfo ci) {
        inertia = 0;
        // checks, whether it should consider this event touchpad-scroll
        // if the previous event was considered as such and was within touchpadTimeThreshold nanoseconds, it is also considered as touchpad-scroll
        // if the scroll amount is low enough, it is considered as touchpad-scroll
        // if the previous event was within touchpadTimeThreshold nanoseconds and is of different absolute amount, it is considered as touchpad-scroll
        if (Math.abs(event.wheel().y()) < SmScCfg.touchpadThreshold || (Util.getNanos() - lastScrollYTime < SmScCfg.touchpadTimeThreshold && (Globals.touchpadScrolledY || Math.abs(lastScrollY) != Math.abs(event.wheel().y())))) {
            Globals.touchpadScrolledY = true;
            if (Math.abs(event.wheel().y()) > SmScCfg.inertiaThreshold) {
                inertia = event.wheel().y();
            }
        } else {
            Globals.touchpadScrolledY = false;
        }
        lastScrollY = event.wheel().y();
        lastScrollYTime = Util.getNanos();
    }
    @WrapMethod(method = "pollEvents")
    private void PollEventsTail(Operation<Void> operation) {
        operation.call();
        var timeDelta = Util.getNanos() - lastScrollYTime;
        if (timeDelta == 0) {
            return;
        }
        if (Math.abs(inertia) < SmScCfg.inertiaStop) {
            return;
        }
        // TODO inertia will be applied every frame, it needs to account for that
        inertia *= Math.pow(SmScCfg.inertiaDeceleration, SmoothSc.getLastFrameDuration());
        SmoothSc.mc.execute(() -> SmoothSc.mc.mouseHandler.onScroll(SmoothSc.mc.getWindow().handle(), 0, inertia * SmoothSc.getLastFrameDuration()));
    }

}
