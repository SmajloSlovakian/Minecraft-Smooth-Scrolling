package smsk.animatimc.mixin.CreativeScreen;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen.CreativeScreenHandler;
import smsk.animatimc.AnimatiMC;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "setScreen",at = @At("TAIL"))
    private void setScreenT(@Nullable Screen s, CallbackInfo ci){
        try {
            var sh=((CreativeScreenHandler)((ScreenHandlerProvider<?>)s).getScreenHandler());
            if(sh!=null)AnimatiMC.creativeSH=sh;
        } catch (Exception e){}
    }
}
