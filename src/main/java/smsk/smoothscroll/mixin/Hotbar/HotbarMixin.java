package smsk.smoothscroll.mixin.Hotbar;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerInventory;
import smsk.smoothscroll.Config;
import smsk.smoothscroll.SmoothSc;

@Mixin(value = InGameHud.class, priority = 999) // if bedrockify applies its mixin before smoothsc, modifyarg crashes
public class HotbarMixin {

	int selectedPixelBuffer = 0;
	float lFDBuffer;

	@ModifyArg(method = "renderHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V", ordinal = 1), index = 1)
	private int selectedSlotX(int x) {
		if (Config.cfg.hotbarSpeed == 0) return (x);
		PlayerInventory inv = SmoothSc.mc.player.getInventory();

		lFDBuffer += SmoothSc.mc.getLastFrameDuration();
		var a = selectedPixelBuffer;
		selectedPixelBuffer = (int) Math.round((selectedPixelBuffer - (inv.selectedSlot - SmoothSc.hotbarRollover * 9) * 20) * Math.pow(Config.cfg.hotbarSpeed, lFDBuffer) + (inv.selectedSlot - SmoothSc.hotbarRollover * 9) * 20);
		if (selectedPixelBuffer != a || selectedPixelBuffer == inv.selectedSlot * 20) lFDBuffer = 0;
		
		if (selectedPixelBuffer < -10) {
			selectedPixelBuffer += 9 * 20;
			SmoothSc.hotbarRollover -= 1;
		} else if (selectedPixelBuffer > 20 * 9 - 10) {
			selectedPixelBuffer -= 9 * 20;
			SmoothSc.hotbarRollover += 1;
		}

		x -= inv.selectedSlot * 20;
		x += selectedPixelBuffer;
		return (x);
	}
}
