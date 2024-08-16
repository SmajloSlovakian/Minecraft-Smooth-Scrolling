package smsk.smoothscroll.mixin.CreativeScreen;

import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen.CreativeScreenHandler;
import smsk.smoothscroll.SmoothSc;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "setScreen", at = @At("TAIL"))
    private void setScreenT(@Nullable Screen s, CallbackInfo ci) {
        try {
            var sh = ((CreativeScreenHandler) ((ScreenHandlerProvider<?>) s).getScreenHandler());
            if (sh != null) SmoothSc.creativeSH = sh;
        } catch (Exception ignored) {}
        SmoothSc.creativeScreenScrollOffset = 0;
    }

    @Inject(method = "reloadResources", at = @At("HEAD"))
    private void onResReload(CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        SmoothSc.updateConfig();
    }
}
