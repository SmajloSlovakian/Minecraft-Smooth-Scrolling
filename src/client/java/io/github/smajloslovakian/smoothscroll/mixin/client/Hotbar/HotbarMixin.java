package io.github.smajloslovakian.smoothscroll.mixin.client.Hotbar;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import io.github.smajloslovakian.smoothscroll.SmoothSc;
import io.github.smajloslovakian.smoothscroll.cfg.SmScCfg;

/*
 * Priority
 * <1000: if bedrockify applies its mixin before smoothsc, modifyarg crashes
 * >-999999999: to apply wrapoperation after raised mod (fix for the underside of raised hotbar selector with "PATCH" option)
 */

@Mixin(value = Gui.class, priority = 999)
public class HotbarMixin {

	@Unique private int slotWidth = 20;
	@Unique private int slotCount = 9;
	@Unique private int rolloverSpace = 4;

	@Unique private boolean masked = false;
	@Unique private float smoothSelectorPos = 0;

	@WrapMethod(method = "renderItemHotbar")
	private void renderHotbarWrap(GuiGraphics context, DeltaTracker tickCounter, Operation<Void> operation) {
		Inventory inv = SmoothSc.mc.player.getInventory();

		rolloverSpace = SmScCfg.staticSelector ? 1 : 4;
		var target = (inv.getSelectedSlot() - SmoothSc.hotbarRollover * slotCount) * slotWidth - SmoothSc.hotbarRollover * rolloverSpace;
		smoothSelectorPos = (float) ((smoothSelectorPos - target) * Math.pow(SmScCfg.hotbarSmoothness, SmoothSc.getLastFrameDuration()) + target);
		
		if (Math.round(smoothSelectorPos) <  rolloverSpace - (slotWidth / 2)) {
			smoothSelectorPos += slotCount * slotWidth + rolloverSpace;
			SmoothSc.hotbarRollover -= 1;
		} else if (Math.round(smoothSelectorPos) > rolloverSpace - (slotWidth / 2) + slotWidth * slotCount) {
			smoothSelectorPos -= slotCount * slotWidth + rolloverSpace;
			SmoothSc.hotbarRollover += 1;
		}

		operation.call(context, tickCounter);
	}

	@WrapOperation(method = "renderItemHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V", ordinal = 1))
	private void moveSelector(GuiGraphics context, RenderPipeline pipeline, Identifier texture, int x, int y, int width, int height, Operation<Void> operation) {
		Inventory inv = SmoothSc.mc.player.getInventory();
		var hotbarStart = x - inv.getSelectedSlot() * slotWidth;

		if (SmScCfg.staticSelector) {
			context.pose().pushMatrix();
			context.pose().translate(- (x - hotbarStart) + slotCount / 2 * slotWidth, 0);

			operation.call(context, pipeline, texture, x, y, width, height);

        	context.pose().popMatrix();
			return;
		}


		if (Math.round(smoothSelectorPos) < 0) {
			enableMask(context);
			context.pose().pushMatrix();
			context.pose().translate(smoothSelectorPos - (x - hotbarStart) + slotCount * slotWidth + rolloverSpace, 0);

			operation.call(context, pipeline, texture, x, y, width, height);

        	context.pose().popMatrix();

		} else if (Math.round(smoothSelectorPos) > slotWidth * 8) {
			enableMask(context);
			context.pose().pushMatrix();
			context.pose().translate(smoothSelectorPos - (x - hotbarStart) - slotCount * slotWidth - rolloverSpace, 0);

			operation.call(context, pipeline, texture, x, y, width, height);

        	context.pose().popMatrix();
		}


        context.pose().pushMatrix();
        context.pose().translate(smoothSelectorPos - (x - hotbarStart), 0);
		operation.call(context, pipeline, texture, x, y, width, height);

        context.pose().popMatrix();
		
		if (masked) {
			if (SmScCfg.enableMaskDebug) context.fill(-100, -100, context.guiWidth(), context.guiHeight(), ARGB.color(50, 0, 255, 255));
			context.disableScissor();
			masked = false;
		}
	}


	@Unique
	private void enableMask(GuiGraphics context) {
		var x2 = context.guiWidth() / 2 - 91;
		var y2 = context.guiHeight() - 22;
		if (!SmScCfg.staticSelector) {
			context.enableScissor((int) x2 - 1, (int) y2 - 1, (int) x2 + 182 + 1, (int) y2 + 22 + 1);
		} else {
			context.enableScissor((int) x2, (int) y2 - 1, (int) x2 + 182, (int) y2 + 22 + 1);
		}
		masked = true;
	}



	@WrapOperation(method = "renderItemHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V", ordinal = 0))
	private void moveHotbar(GuiGraphics context, RenderPipeline pipeline, Identifier texture, int x, int y, int width, int height, Operation<Void> operation) {
		if (!SmScCfg.staticSelector) {
			operation.call(context, pipeline, texture, x, y, width, height);
			return;
		}
		enableMask(context);
		context.pose().pushMatrix();
		context.pose().translate(-smoothSelectorPos + slotCount / 2 * slotWidth, 0);

		operation.call(context, pipeline, texture, x, y, width, height);

		context.pose().popMatrix();

		context.pose().pushMatrix();
		context.pose().translate(-smoothSelectorPos + slotCount * slotWidth + rolloverSpace + slotCount / 2 * slotWidth, 0);

		operation.call(context, pipeline, texture, x, y, width, height);

		context.pose().popMatrix();

		context.pose().pushMatrix();
		context.pose().translate(-smoothSelectorPos - slotCount * slotWidth - rolloverSpace + slotCount / 2 * slotWidth, 0);

		operation.call(context, pipeline, texture, x, y, width, height);

		context.pose().popMatrix();
		
		if (masked) {
			if (SmScCfg.enableMaskDebug) context.fill(-100, -100, context.guiWidth(), context.guiHeight(), ARGB.color(50, 0, 255, 255));
			context.disableScissor();
			masked = false;
		}
	}

	@WrapOperation(method = "renderItemHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderSlot(Lnet/minecraft/client/gui/GuiGraphics;IILnet/minecraft/client/DeltaTracker;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;I)V", ordinal = 0))
	private void moveItems(Gui igh, GuiGraphics context, int x, int y, DeltaTracker tickCounter, Player player, ItemStack stack, int seed, Operation<Void> operation) {
		if (!SmScCfg.staticSelector) {
			operation.call(igh, context, x, y, tickCounter, player, stack, seed);
			return;
		}
		enableMask(context);
		context.pose().pushMatrix();
		context.pose().translate(-smoothSelectorPos + slotCount / 2 * slotWidth, 0);

		operation.call(igh, context, x, y, tickCounter, player, stack, seed);

		context.pose().popMatrix();

		context.pose().pushMatrix();
		context.pose().translate(-smoothSelectorPos + slotCount * slotWidth + rolloverSpace + slotCount / 2 * slotWidth, 0);

		operation.call(igh, context, x, y, tickCounter, player, stack, seed);

		context.pose().popMatrix();

		context.pose().pushMatrix();
		context.pose().translate(-smoothSelectorPos - slotCount * slotWidth - rolloverSpace + slotCount / 2 * slotWidth, 0);

		operation.call(igh, context, x, y, tickCounter, player, stack, seed);

		context.pose().popMatrix();

		if (masked) {
			if (SmScCfg.enableMaskDebug) context.fill(-100, -100, context.guiWidth(), context.guiHeight(), ARGB.color(50, 0, 255, 255));
			context.disableScissor();
			masked = false;
		}
	}

}
