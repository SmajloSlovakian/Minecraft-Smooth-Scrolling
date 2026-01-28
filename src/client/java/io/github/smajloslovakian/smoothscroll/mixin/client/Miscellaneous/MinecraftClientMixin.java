package io.github.smajloslovakian.smoothscroll.mixin.client.Miscellaneous;

import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen.ItemPickerMenu;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.At;
import io.github.smajloslovakian.smoothscroll.SmoothSc;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {
    /*@Inject(method = "setScreen", at = @At("TAIL"))
    private void setScreenT(@Nullable Screen s, CallbackInfo ci) {
        try {
            var sh = ((ItemPickerMenu) ((MenuAccess<?>) s).getMenu());
            if (sh != null) SmoothSc.creativeSH = sh;
        } catch (Exception ignored) {}
        SmoothSc.creativeScreenScrollOffset = 0;
    }/* */

    @Inject(method = "reloadResourcePacks", at = @At("HEAD"))
    private void onResReload(CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        SmoothSc.readConfig();
    }
}
