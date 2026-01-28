package io.github.smajloslovakian.smoothscroll.mixin.client.CreativeScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen.ItemPickerMenu;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import io.github.smajloslovakian.smoothscroll.SmoothSc;
import io.github.smajloslovakian.smoothscroll.cfg.SmScCfg;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin extends AbstractContainerScreen<ItemPickerMenu> {

    @Unique private int slotHeight = 18;
    @Unique private float scrollAmount = 30;
    @Unique private float smoothScrollOffs = 0; // counted in proportion
    @Shadow float scrollOffs; // this is the target

    @WrapOperation(method = "renderBg", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIII)V"))
    private void renderBgWrap(GuiGraphics graphics, RenderPipeline renderPipeline, Identifier texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, Operation<Void> operation) {

        operation.call(graphics, renderPipeline, texture, x, y, u, v, width, height, textureWidth, textureHeight);

        if (FabricLoader.getInstance().getObjectShare().get("flow:is_caching_screen") instanceof Boolean isCaching && isCaching)
            return;

        //SmoothSc.print(scrollOffs);
        smoothScrollOffs = (smoothScrollOffs - scrollOffs) * (float) Math.pow(SmScCfg.creativeScreenSmoothness, SmoothSc.getLastFrameDuration()) + scrollOffs;

        menu.scrollTo(smoothScrollOffs);
        //SmoothSc.printt("drawoffset", getDrawOffset());
    }

    @Unique private int itemCounter = 0;
    @Unique private int scrollItemCount = 45;
    @Override
    protected void renderSlots(GuiGraphics graphics, int mouseX, int mouseY) {
        itemCounter = 0;
        super.renderSlots(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderSlot(GuiGraphics graphics, Slot slot, int mouseX, int mouseY) {
        itemCounter++;
        if (itemCounter <= scrollItemCount) {
            graphics.pose().pushMatrix();
            graphics.pose().translate(0, getDrawOffset());
        }
        super.renderSlot(graphics, slot, mouseX, mouseY);

        if (itemCounter <= scrollItemCount) {
            graphics.pose().popMatrix();
        }
    }

    @WrapMethod(method = "mouseScrolled")
    private boolean mouseScrolledWrap(double x, double y, double scrollX, double scrollY, Operation<Boolean> operation) {
        var prevScroll = scrollOffs;
        var ret = operation.call(x, y, scrollX, scrollY);
        if (scrollOffs == prevScroll) {
            return ret;
        }

        var pixelscroll = scrollOffsToPixels(prevScroll);
        pixelscroll -= scrollAmount * scrollY;
        scrollOffs = Math.clamp(pixelsToScrollOffs(pixelscroll), 0, 1);
        
        return ret;
    }

    //@WrapOperation(method = "mouseScrolled", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/CreativeModeInventoryScreen/ItemPickerMenu;scrollTo(F)V"))
    //private void dontScrollTo(ItemPickerMenu itemPickerMenu, float scrollOffs, Operation<Void> operation) {}

    @Unique
    private float getDrawOffset() {
        return -scrollOffsToPixels(smoothScrollOffs) % slotHeight; // TODO recorrection for mouse position slot correction
    }
    @Unique
    private float scrollOffsToPixels(float scrollOffs) {
        return scrollOffs * calculateRowCount() * slotHeight;
    }
    @Unique
    private float pixelsToScrollOffs(float pixels) {
        return pixels / slotHeight / calculateRowCount();
    }

    @Unique
    private int calculateRowCount() {
        return ((ItemPickerMenuAccessor)menu).getCalculatedRowCount();
    } 












    public CreativeModeInventoryScreenMixin(ItemPickerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }
    /*
    @Shadow
    private static CreativeModeTab selectedTab;

    public void renderContents(GuiGraphics context, int mx, int my, float d) {
        super.renderContents(context, mx, my, d);
    }

    @Inject(method = "selectTab", at = @At("TAIL"))
    private void setSelectedTabT(CreativeModeTab group, CallbackInfo ci) {
        SmoothSc.creativeScreenScrollOffset = 0;
    }

    @Inject(method = "renderBg", at = @At(value = "INVOKE", shift = Shift.AFTER, target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIII)V"))
    private void drawBackground(GuiGraphics context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        if (SmoothSc.getCreativeScrollOffset() == 0 || SmScCfg.creativeScreenSmoothness == 0 || SmoothSc.creativeSH == null) return;

        if (FabricLoader.getInstance().getObjectShare().get("flow:is_caching_screen") instanceof Boolean isCaching
                && isCaching)
            return;
        
        SmoothSc.creativeScreenScrollOffset = (float) ((SmoothSc.creativeScreenScrollOffset)
                * Math.pow(SmScCfg.creativeScreenSmoothness, SmoothSc.getLastFrameDuration()));

        SmoothSc.creativeScreenScrollMixin = false;
        SmoothSc.creativeSH.scrollTo(((CreativeScreenHandlerAccessor) SmoothSc.creativeSH)
                .getPos(SmoothSc.creativeScreenPrevRow - SmoothSc.getCreativeScrollOffset() / 18));
        SmoothSc.creativeScreenScrollMixin = true;

        int posx = Math.round(context.guiWidth() / 2f) - 90;
        int posy = context.guiHeight() / 2 - 51;
        int width = 162;
        int height = 90;
        int u = 8;
        int v = 17;


        //context.drawText(SmoothSc.mc.textRenderer, mouseX + " - " + mouseY, 10, 10, ColorHelper.getArgb(255, 0, 255, 255), true);
        //context.fill(0, 0, 1920, 1080, ColorHelper.getArgb(50, 255, 128, 0));
        context.enableScissor(posx, posy + 1, posx + width, posy + height - 1);
        context.blit(RenderPipelines.GUI_TEXTURED, selectedTab.getBackgroundTexture(), posx, 
            posy + SmoothSc.getCreativeDrawOffset(),
                u, v, width, height, 256, 256);
        context.blit(RenderPipelines.GUI_TEXTURED, selectedTab.getBackgroundTexture(), posx,
            (int) (posy + SmoothSc.getCreativeDrawOffset() - height * Math.signum(SmoothSc.getCreativeScrollOffset())),
                u, v, width, height, 256, 256);

        if (SmScCfg.enableMaskDebug)
            context.fill(-100, -100, context.guiWidth(), context.guiHeight(), ARGB.color(50, 255, 255, 0));
        
        context.disableScissor();
    }

    */
}
