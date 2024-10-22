package smsk.smoothscroll.mixin.Hotbar;

import I;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import smsk.smoothscroll.Config;
import smsk.smoothscroll.SmoothSc;

@Mixin(value = InGameHud.class, priority = 999) // if bedrockify applies its mixin before smoothsc, modifyarg crashes
public class HotbarMixin {

	@Unique private int rolloverOffsetR = 4; // TODO
	@Unique private int rolloverOffsetL = 4;
	@Unique private int rolloverOffset = 4;
	@Unique private float selectedPixelBuffer = 0;
	@Unique private boolean masked = false;

	@Inject(method = "renderHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V", ordinal = 1))
	private void draw1(DrawContext context, RenderTickCounter rtc, CallbackInfo ci) {
		if (Config.cfg.hotbarSpeed == 0) return;
	}

	@ModifyArgs(method = "renderHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V", ordinal = 1))
	private void selectedSlotX(Args args, @Local(argsOnly = true) DrawContext context) {
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
			enableMask(context);
			SmoothSc.drawHotbarRolloverMirror(context, texture, x, 9 * 20, rolloverOffset, y, width, height);
		} else if (Math.round(selectedPixelBuffer) > 20 * 8) {
			enableMask(context);
			SmoothSc.drawHotbarRolloverMirror(context, texture, x, -9 * 20, -rolloverOffset, y, width, height);
		}
	}

	@Inject(method = "renderHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V", ordinal = 1, shift = At.Shift.AFTER))
	private void draw2(DrawContext context, RenderTickCounter rtc, CallbackInfo ci) {
		if (!masked) return;
        if (Config.cfg.enableMaskDebug) context.fill(-100, -100, context.getScaledWindowWidth(), context.getScaledWindowHeight(), ColorHelper.Argb.getArgb(50, 0, 255, 255));
		context.disableScissor();
	}

	@Unique
	private void enableMask(DrawContext context) {
		var x2 = context.getScaledWindowWidth() / 2 - 91;
		var y2 = context.getScaledWindowHeight() - 22;
		if (FabricLoader.getInstance().getObjectShare().get("raised:hud") instanceof Integer distance) y2 -= distance;
		context.enableScissor(x2 - 1, y2 - 1, x2 + 182 + 1, y2 + 22 + 1);
		masked = true;
	}
}
