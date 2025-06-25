package smsk.smoothscroll.mixin.Hotbar;

import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import smsk.smoothscroll.SmoothSc;
import smsk.smoothscroll.cfg.SmScCfg;

@Mixin(value = InGameHud.class, priority = 999) // if bedrockify applies its mixin before smoothsc, modifyarg crashes
public class HotbarMixin {

	@Unique private int rolloverOffsetR = 4; // TODO
	@Unique private int rolloverOffsetL = 4;
	@Unique private int rolloverOffset = 4;
	@Unique private float selectedPixelBuffer = 0;
	@Unique private boolean masked = false;

	@ModifyArgs(method = "renderHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIII)V", ordinal = 1))
	private void selectedSlotX(Args args, @Local(argsOnly = true) DrawContext context) {
		if (SmScCfg.hotbarSmoothness == 0) return;
		Identifier texture = args.get(1);
		int x = args.get(2);
		int y = args.get(3);
		int width = args.get(4);
		int height = args.get(5);
		PlayerInventory inv = SmoothSc.mc.player.getInventory();

		var target = (inv.getSelectedSlot() - SmoothSc.hotbarRollover * 9) * 20 - SmoothSc.hotbarRollover * rolloverOffset;
		selectedPixelBuffer = (float) ((selectedPixelBuffer - target) * Math.pow(SmScCfg.hotbarSmoothness, SmoothSc.getLastFrameDuration()) + target);
		
		if (Math.round(selectedPixelBuffer) < -10 - rolloverOffset) {
			selectedPixelBuffer += 9 * 20 + rolloverOffset;
			SmoothSc.hotbarRollover -= 1;
		} else if (Math.round(selectedPixelBuffer) > 20 * 9 - 10 + rolloverOffset) {
			selectedPixelBuffer -= 9 * 20 + rolloverOffset;
			SmoothSc.hotbarRollover += 1;
		}

		x -= inv.getSelectedSlot() * 20;
		x += Math.round(selectedPixelBuffer);
		args.set(2, x);

		masked = false;
		if (Math.round(selectedPixelBuffer) < 0) {
			enableMask(context);
			SmoothSc.drawHotbarRolloverMirror(context, texture, x, 9 * 20, rolloverOffset, y, width, height);
		} else if (Math.round(selectedPixelBuffer) > 20 * 8) {
			enableMask(context);
			SmoothSc.drawHotbarRolloverMirror(context, texture, x, -9 * 20, -rolloverOffset, y, width, height);
		}
	}

	@Inject(method = "renderHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIII)V", ordinal = 1, shift = At.Shift.AFTER))
	private void draw2(DrawContext context, RenderTickCounter rtc, CallbackInfo ci) {
		if (!masked) return;
        if (SmScCfg.enableMaskDebug) context.fill(-100, -100, context.getScaledWindowWidth(), context.getScaledWindowHeight(), ColorHelper.getArgb(50, 0, 255, 255));
		context.disableScissor();
	}

	@Unique
	private void enableMask(DrawContext context) {
		var x2 = context.getScaledWindowWidth() / 2 - 91;
		var y2 = context.getScaledWindowHeight() - 22;
		context.enableScissor((int) x2 - 1, (int) y2 - 1, (int) x2 + 182 + 1, (int) y2 + 22 + 1);
		masked = true;
	}
}
