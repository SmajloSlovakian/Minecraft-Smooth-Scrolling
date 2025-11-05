package smsk.smoothscroll.mixin.Hotbar;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.pipeline.RenderPipeline;

import org.joml.Vector2i;
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

/*
 * Priority
 * <1000: if bedrockify applies its mixin before smoothsc, modifyarg crashes
 * >-999999999: to apply wrapoperation after raised mod (fix for the underside of raised hotbar selector with "PATCH" option)
 */

@Mixin(value = InGameHud.class, priority = 999)
public class HotbarMixin {

	@Unique private int slotWidth = 20;
	@Unique private int slotCount = 9;
	@Unique private int rolloverSpace = 4;

	@Unique private boolean masked = false;
	@Unique private float smoothSelectorPos = 0;

	@WrapOperation(method = "renderHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIII)V", ordinal = 1))
	private void moveSelector(DrawContext context, RenderPipeline pipeline, Identifier texture, int x, int y, int width, int height, Operation<Void> operation) {
		if (SmScCfg.hotbarSmoothness == 0) {
			operation.call(context, pipeline, texture, x, y, width, height);
			return;
		}
		PlayerInventory inv = SmoothSc.mc.player.getInventory();

		var hotbarStart = x - inv.getSelectedSlot() * slotWidth;

		var target = (inv.getSelectedSlot() - SmoothSc.hotbarRollover * slotCount) * slotWidth - SmoothSc.hotbarRollover * rolloverSpace;
		smoothSelectorPos = (float) ((smoothSelectorPos - target) * Math.pow(SmScCfg.hotbarSmoothness, SmoothSc.getLastFrameDuration()) + target);
		
		if (Math.round(smoothSelectorPos) <  rolloverSpace - (slotWidth / 2)) {
			smoothSelectorPos += slotCount * slotWidth + rolloverSpace;
			SmoothSc.hotbarRollover -= 1;
		} else if (Math.round(smoothSelectorPos) > rolloverSpace - (slotWidth / 2) + slotWidth * slotCount) {
			smoothSelectorPos -= slotCount * slotWidth + rolloverSpace;
			SmoothSc.hotbarRollover += 1;
		}

		masked = false;
		
		if (Math.round(smoothSelectorPos) < 0) {
			enableMask(context);
			context.getMatrices().pushMatrix();
			context.getMatrices().translate(smoothSelectorPos - (x - hotbarStart) + slotCount * slotWidth + rolloverSpace, 0);

			operation.call(context, pipeline, texture, x, y, width, height);

        	context.getMatrices().popMatrix();

		} else if (Math.round(smoothSelectorPos) > slotWidth * 8) {
			enableMask(context);
			context.getMatrices().pushMatrix();
			context.getMatrices().translate(smoothSelectorPos - (x - hotbarStart) - slotCount * slotWidth - rolloverSpace, 0);

			operation.call(context, pipeline, texture, x, y, width, height);

        	context.getMatrices().popMatrix();
		}


        context.getMatrices().pushMatrix();
        context.getMatrices().translate(smoothSelectorPos - (x - hotbarStart), 0);
		operation.call(context, pipeline, texture, x, y, width, height);

        context.getMatrices().popMatrix();
		
		if (masked) {
			if (SmScCfg.enableMaskDebug) context.fill(-100, -100, context.getScaledWindowWidth(), context.getScaledWindowHeight(), ColorHelper.getArgb(50, 0, 255, 255));
			context.disableScissor();
		}
	}


	@Unique
	private void enableMask(DrawContext context) {
		var x2 = context.getScaledWindowWidth() / 2 - 91;
		var y2 = context.getScaledWindowHeight() - 22;
		context.enableScissor((int) x2 - 1, (int) y2 - 1, (int) x2 + 182 + 1, (int) y2 + 22 + 1);
		masked = true;
	}
}
