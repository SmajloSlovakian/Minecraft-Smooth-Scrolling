package smsk.smoothscroll.mixin.compat;

import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smsk.smoothscroll.SmoothSc;

// Specific mixin for targeting CondensedCreative call for setting Entry within Creative Screen
@Mixin(value = CreativeInventoryScreen.CreativeScreenHandler.class, priority = 1001)
public class CreativeScreenHandlerMixin_CC {
    @Inject(method = "scrollItems", at = @At(value = "INVOKE", target = "Lio/wispforest/condensed_creative/entry/EntryContainer;setEntryStack(ILio/wispforest/condensed_creative/entry/Entry;)V"))
    private void itemCount(float pos, CallbackInfo ci) {
        SmoothSc.creativeScreenItemCount += 1;
    }
}
