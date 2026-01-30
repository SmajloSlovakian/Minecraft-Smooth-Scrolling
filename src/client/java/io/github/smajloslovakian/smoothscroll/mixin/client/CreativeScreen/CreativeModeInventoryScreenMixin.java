package io.github.smajloslovakian.smoothscroll.mixin.client.CreativeScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.pipeline.RenderPipeline;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen.ItemPickerMenu;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import io.github.smajloslovakian.smoothscroll.CreativeModeInventoryScreenDuck;
import io.github.smajloslovakian.smoothscroll.SmoothSc;
import io.github.smajloslovakian.smoothscroll.cfg.SmScCfg;

// TODO check compatibility with: item borders, flow, item highlighter, bedrockify

@Mixin(value = CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin extends AbstractContainerScreen<ItemPickerMenu> implements CreativeModeInventoryScreenDuck {

    @Unique private boolean mouseInBounds = true;
    @Unique private int slotSize = 18;
    @Unique private int rowOffset = 0; // this offsets, from which row to pull items from and populate slots, it also counter-affects drawoffset
    @Unique private float smoothScrollOffs = 0; // counted in proportion
    @Shadow float scrollOffs; // this is the target
    @Shadow static CreativeModeTab selectedTab;

    @WrapOperation(method = "renderBg", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIII)V"))
    private void renderBgWrap(GuiGraphics graphics, RenderPipeline renderPipeline, Identifier texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, Operation<Void> operation, @Local(name = "ym") int ym, @Local(name = "xm") int xm) {

        operation.call(graphics, renderPipeline, texture, x, y, u, v, width, height, textureWidth, textureHeight);

        if (FabricLoader.getInstance().getObjectShare().get("flow:is_caching_screen") instanceof Boolean isCaching && isCaching)
            return;

        //SmoothSc.print(scrollOffs);
        smoothScrollOffs = (smoothScrollOffs - scrollOffs) * (float) Math.pow(SmScCfg.creativeScreenSmoothness, SmoothSc.getLastFrameDuration()) + scrollOffs;

        if (ym - y > 60) {
            rowOffset = 1;
        }
        else {
            rowOffset = 0;
        }
        menu.scrollTo(smoothScrollOffs + rowOffset / (float) calculateRowCount());
        
        // background rendering
        //graphics.fill(x, y, x+1, y+1, ARGB.color(255, 0, 255, 255));
        //graphics.fill(xm, ym, xm+1, ym+1, ARGB.color(255, 255, 0, 255));
        //graphics.setTooltipForNextFrame(Component.literal(xm - x + ", " + (ym - y)), xm, ym);
        // xy 142, 63
        // ixiy 150, 80
        // ixxiyy 311, 169
        var ix = x + 8;
        var iy = y + 17;
        var iu = u + 8;
        var iv = v + 17;
        var iwidth = 162;
        var iheight = 90;

        mouseInBounds = xm >= ix && xm < ix + iwidth && ym >= iy && ym < iy + iheight;

        var yOffset = -(scrollOffsToPixels(smoothScrollOffs) % (slotSize * 5));

        //graphics.fill(x, y, x+1000, y+1000, ARGB.color(50, 0, 255, 255));

        graphics.enableScissor(ix, iy + 1, ix + iwidth, iy + iheight - 1);
        graphics.pose().pushMatrix();
        graphics.pose().translate(0, yOffset);
        operation.call(graphics, renderPipeline, texture, ix, iy, iu, iv, iwidth, iheight, textureWidth, textureHeight);
        operation.call(graphics, renderPipeline, texture, ix, iy + slotSize * 5, iu, iv, iwidth, iheight, textureWidth, textureHeight);
        graphics.pose().popMatrix();


        graphics.pose().pushMatrix();
        graphics.pose().translate(0, getDrawOffset());
        var currRow = (int)(double)(smoothScrollOffs * calculateRowCount());
        var fromIndex = (currRow + 5 - rowOffset * 5) * 9;
        for (int i = fromIndex; i >= 0 && i < menu.items.size() && i < fromIndex + 9; i++) {
            // TODO slot-based rendering of items may be more accurate
            //var tempSlot = new Slot(this.menu, i, 9 + i % 9 * 18, 0);
            ItemStack item = menu.items.get(i);
            graphics.renderItem(item, x + 9 + i % 9 * slotSize, y + 6 * slotSize - rowOffset * 6 * slotSize);
        }
        graphics.pose().popMatrix();

        //graphics.fill(x, y, x+1000, y+1000, ARGB.color(50, 255, 255, 0));
        graphics.disableScissor();
    }

    @Unique private int scrollItemCount = 45;
    @Unique private int itemCounter = scrollItemCount;
    @Override
    protected void renderSlots(GuiGraphics graphics, int mouseX, int mouseY) {
        if (selectedTab.canScroll()) {
            itemCounter = 0;
            enMask(graphics);
        }
        super.renderSlots(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderSlot(GuiGraphics graphics, Slot slot, int mouseX, int mouseY) {
        super.renderSlot(graphics, slot, mouseX, mouseY);

        itemCounter++;
        if (itemCounter == scrollItemCount) {
            deMask(graphics);
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
        pixelscroll -= SmScCfg.creativeScreenAmount * scrollY;
        scrollOffs = Math.clamp(pixelsToScrollOffs(pixelscroll), 0, 1);
        
        return ret;
    }
    @Override
    public void renderContents(final GuiGraphics graphics, final int mouseX, final int mouseY, final float a) {
        var offset = mouseInBounds ? - Math.round(getDrawOffset()) : 0;
        super.renderContents(graphics, mouseX, mouseY + offset, a);
    }
    @WrapMethod(method = "mouseClicked")
    public boolean mouseClickedWrap(MouseButtonEvent event, boolean doubleClick, Operation<Boolean> operation) {
        var offset = mouseInBounds ? - Math.round(getDrawOffset()) : 0;
        return operation.call(new MouseButtonEvent(event.x(), event.y() + offset, event.buttonInfo()), doubleClick);
    }
    @WrapMethod(method = "mouseReleased")
    public boolean mouseReleasedWrap(MouseButtonEvent event, Operation<Boolean> operation) {
        var offset = mouseInBounds ? - Math.round(getDrawOffset()) : 0;
        return operation.call(new MouseButtonEvent(event.x(), event.y() + offset, event.buttonInfo()));
    }
    @WrapMethod(method = "selectTab")
    public void selectTabWrap(CreativeModeTab tab, Operation<Void> operation) {
        smoothScrollOffs = 0;
        operation.call(tab);
    }
    



    //@WrapOperation(method = "mouseScrolled", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/CreativeModeInventoryScreen/ItemPickerMenu;scrollTo(F)V"))
    //private void dontScrollTo(ItemPickerMenu itemPickerMenu, float scrollOffs, Operation<Void> operation) {}

    @Unique
    private float getDrawOffset() {
        if (!selectedTab.canScroll()) {
            return 0;
        }
        return -scrollOffsToPixels(smoothScrollOffs) % slotSize + slotSize * rowOffset;
    }
    @Unique
    private float scrollOffsToPixels(float scrollOffs) {
        return scrollOffs * calculateRowCount() * slotSize;
    }
    @Unique
    private float pixelsToScrollOffs(float pixels) {
        return pixels / slotSize / calculateRowCount();
    }
    @Unique
    private int calculateRowCount() {
        return ((ItemPickerMenuAccessor)menu).getCalculatedRowCount();
    }
    @Override
    public void enMask(GuiGraphics graphics) {
        graphics.enableScissor(8, 18, 170, 106);
        graphics.pose().pushMatrix();
        graphics.pose().translate(0, getDrawOffset());
    }
    @Override
    public void deMask(GuiGraphics graphics) {
        graphics.pose().popMatrix();

        //graphics.fill(0, 0, 10000, 10000, ARGB.color(50, 255, 0, 255));
        graphics.disableScissor();
    }
    @Override
    public boolean isMouseInbounds() {
        return mouseInBounds;
    }

    public CreativeModeInventoryScreenMixin(ItemPickerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }
}
