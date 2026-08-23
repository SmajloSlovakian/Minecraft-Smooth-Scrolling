package io.github.smajloslovakian.smoothscroll.mixin.client.Miscellaneous;

import org.lwjgl.sdl.SDLHints;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.util.TimeSource;

@Mixin(RenderSystem.class)
public class RenderSystemMixin {
    //@Inject(method = "initBackendSystem", at = @At("TAIL"))
    private static void initBackendSystemTail(CallbackInfoReturnable<TimeSource.NanoTimeSource> cir) {
       // SDLHints.SDL_SetHint(SDLHints.SDL_HINT_TOUCH_MOUSE_EVENTS, );
    }
}
