package smsk.smoothscroll.mixin.Hotbar;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import smsk.smoothscroll.Config;
import smsk.smoothscroll.SmoothSc;

@Mixin(value = InGameHud.class, priority = 999) // if bedrockify applies its mixin before smoothsc, modifyarg crashes
public class HotbarMixin {

	int rolloverOffsetR = 4; // TODO
	int rolloverOffsetL = 4;
	int rolloverOffset = 4;
	float selectedPixelBuffer = 0;
	boolean masked = false;
	DrawContext savedContext;

	@Inject(method = "renderHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V", ordinal = 1))
	private void draw1(float tickDelta, DrawContext context, CallbackInfo ci) {
		if (Config.cfg.hotbarSpeed == 0) return;
		savedContext = context;
	}

	@ModifyArgs(method = "renderHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V", ordinal = 1))
	private void selectedSlotX(Args args) {
		if (Config.cfg.hotbarSpeed == 0) return;
		Identifier texture = args.get(0);
		int x = args.get(1);
		int y = args.get(2);
		int width = args.get(3);
		int height = args.get(4);
		PlayerInventory inv = SmoothSc.mc.player.getInventory();

		var target = (inv.selectedSlot - SmoothSc.hotbarRollover * 9) * 20 - SmoothSc.hotbarRollover * rolloverOffset;
		selectedPixelBuffer = (float) ((selectedPixelBuffer - target) * Math.pow(Config.cfg.hotbarSpeed, SmoothSc.getLastFrameDuration()) + target);
		
		if (Math.round(selectedPixelBuffer) < -10 - rolloverOffset) {
			selectedPixelBuffer += 9 * 20 + rolloverOffset;
			SmoothSc.hotbarRollover -= 1;
		} else if (Math.round(selectedPixelBuffer) > 20 * 9 - 10 + rolloverOffset) {
			selectedPixelBuffer -= 9 * 20 + rolloverOffset;
			SmoothSc.hotbarRollover += 1;
		}

		x -= inv.selectedSlot * 20;
		x += Math.round(selectedPixelBuffer);
		args.set(1, x);

		masked = false;
		if (Math.round(selectedPixelBuffer) < 0) {
			enableMask();
			SmoothSc.drawHotbarRolloverMirror(savedContext, texture, x, 9 * 20, rolloverOffset, y, width, height);
		} else if (Math.round(selectedPixelBuffer) > 20 * 8) {
			enableMask();
			SmoothSc.drawHotbarRolloverMirror(savedContext, texture, x, -9 * 20, -rolloverOffset, y, width, height);
		}
	}

	@Inject(method = "renderHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V", ordinal = 1, shift = At.Shift.AFTER))
	private void draw2(float tickDelta, DrawContext context, CallbackInfo ci) {
		if (!masked) return;
        if (Config.cfg.enableMaskDebug) savedContext.fill(-100, -100, savedContext.getScaledWindowWidth(), savedContext.getScaledWindowHeight(), ColorHelper.Argb.getArgb(50, 0, 255, 255));
		context.disableScissor();
	}
	void enableMask() {
		var x2 = savedContext.getScaledWindowWidth() / 2 - 91;
		var y2 = savedContext.getScaledWindowHeight() - 22;
		if (FabricLoader.getInstance().getObjectShare().get("raised:hud") instanceof Integer distance) y2 -= distance;
		savedContext.enableScissor(x2 - 1, y2 - 1, x2 + 182 + 1, y2 + 22 + 1);
		masked = true;
	}
}
