package io.github.smajloslovakian.smoothscroll.mixin.client.Hotbar;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
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
import io.github.smajloslovakian.smoothscroll.duck.HudDuck;

// TODO figure out if bedrockify prio is still needed
/*
 * Priority
 * <1000: if bedrockify applies its mixin before smoothsc, modifyarg crashes 
 * >-999999999: to apply wrapoperation after raised mod (fix for the underside of raised hotbar selector with "PATCH" option)
 */

@Mixin(value = Hud.class, priority = 999)
public class HudMixin implements HudDuck {

	@Unique private int slotWidth = 20;
	@Unique private int slotCount = 9;
	@Unique private int rolloverSpace = 4;

	@Unique private boolean masked = false;
	@Unique private float smoothSelectorPos = 0; // pixels to the right of hotbar start
	@Unique private int rollover = 0;

	@WrapMethod(method = "extractItemHotbar")
	private void extractHotbarWrap(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Operation<Void> operation) {
		Inventory inv = SmoothSc.mc.player.getInventory();

		rolloverSpace = SmScCfg.staticSelector ? 1 : 4;
		var target = (inv.getSelectedSlot() - rollover * slotCount) * slotWidth - rollover * rolloverSpace;
		smoothSelectorPos = (float) ((smoothSelectorPos - target) * Math.pow(SmScCfg.hotbarSmoothness, SmoothSc.getLastFrameDuration()) + target);
		
		if (smoothSelectorPos < 0) {
			smoothSelectorPos += slotCount * slotWidth + rolloverSpace;
			rollover -= 1;
		} else if (smoothSelectorPos > rolloverSpace * 2 + slotWidth * slotCount) {
			smoothSelectorPos -= slotCount * slotWidth + rolloverSpace;
			rollover += 1;
		}

		operation.call(graphics, deltaTracker);
	}

	@WrapOperation(method = "extractItemHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V", ordinal = 1))
	private void moveSelector(GuiGraphicsExtractor graphics, RenderPipeline pipeline, Identifier location, int x, int y, int width, int height, Operation<Void> operation) {
		Inventory inv = SmoothSc.mc.player.getInventory();
		var hotbarStart = x - inv.getSelectedSlot() * slotWidth;

		if (SmScCfg.staticSelector) {
			graphics.pose().pushMatrix();
			graphics.pose().translate(- (x - hotbarStart) + slotCount / 2 * slotWidth, 0);

			operation.call(graphics, pipeline, location, x, y, width, height);

        	graphics.pose().popMatrix();
			return;
		}

		if (Math.round(smoothSelectorPos) > slotWidth * 8) {
			enableMask(graphics);
			graphics.pose().pushMatrix();
			graphics.pose().translate(smoothSelectorPos - (x - hotbarStart) - slotCount * slotWidth - rolloverSpace, 0);

			operation.call(graphics, pipeline, location, x, y, width, height);

        	graphics.pose().popMatrix();
		}


        graphics.pose().pushMatrix();
        graphics.pose().translate(smoothSelectorPos - (x - hotbarStart), 0);
		operation.call(graphics, pipeline, location, x, y, width, height);

        graphics.pose().popMatrix();
		
		if (masked) {
			if (SmScCfg.enableMaskDebug) graphics.fill(-100, -100, graphics.guiWidth(), graphics.guiHeight(), ARGB.color(50, 0, 255, 255));
			graphics.disableScissor();
			masked = false;
		}
	}

	@Unique
	private void enableMask(GuiGraphicsExtractor context) {
		var x2 = context.guiWidth() / 2 - 91;
		var y2 = context.guiHeight() - 22;
		if (!SmScCfg.staticSelector) {
			context.enableScissor((int) x2 - 1, (int) y2 - 1, (int) x2 + 182 + 1, (int) y2 + 22 + 1);
		} else {
			context.enableScissor((int) x2, (int) y2 - 1, (int) x2 + 182, (int) y2 + 22 + 1);
		}
		masked = true;
	}

	@WrapOperation(method = "extractItemHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V", ordinal = 0))
	private void moveHotbar(GuiGraphicsExtractor graphics, RenderPipeline pipeline, Identifier location, int x, int y, int width, int height, Operation<Void> operation) {
		moveWholeHotbar(graphics, () -> operation.call(graphics, pipeline, location, x, y, width, height));
	}

	@WrapOperation(method = "extractItemHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractSlot(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IILnet/minecraft/client/DeltaTracker;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;I)V", ordinal = 0))
	private void moveItems(Hud hud, GuiGraphicsExtractor graphics, int x, int y, DeltaTracker deltaTracker, Player player, ItemStack itemStack, int seed, Operation<Void> operation) {
		moveWholeHotbar(graphics, () -> operation.call(hud, graphics, x, y, deltaTracker, player, itemStack, seed));
	}

	@Unique
	private void moveWholeHotbar(GuiGraphicsExtractor graphics, Runnable draw) {
		if (!SmScCfg.staticSelector) {
			draw.run();
			return;
		}
		enableMask(graphics);
		graphics.pose().pushMatrix();
		graphics.pose().translate(-smoothSelectorPos + slotCount / 2 * slotWidth, 0);

		draw.run();

		if (smoothSelectorPos > slotCount / 2 * slotWidth) {
			graphics.pose().translate(+(slotCount * slotWidth + rolloverSpace), 0);
		} else {
			graphics.pose().translate(-(slotCount * slotWidth + rolloverSpace), 0);
		}

		draw.run();

		graphics.pose().popMatrix();

		
		if (masked) {
			if (SmScCfg.enableMaskDebug) graphics.fill(-100, -100, graphics.guiWidth(), graphics.guiHeight(), ARGB.color(50, 0, 255, 255));
			graphics.disableScissor();
			masked = false;
		}
	}

	@Override
	public void increaseRollover() {
		rollover++;
	}

	@Override
	public void decreaseRollover() {
		rollover--;
	}
}
