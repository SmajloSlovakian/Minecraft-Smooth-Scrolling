package io.github.smajloslovakian.smoothscroll.mixin.client.CreativeScreen;

import org.spongepowered.asm.mixin.Final;
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
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen.ItemPickerMenu;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import io.github.smajloslovakian.smoothscroll.SmoothSc;
import io.github.smajloslovakian.smoothscroll.cfg.SmScCfg;
import io.github.smajloslovakian.smoothscroll.duck.CreativeModeInventoryScreenDuck;

// TODO check compatibility with: item borders, flow

@Mixin(value = CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin extends AbstractContainerScreen<ItemPickerMenu> implements CreativeModeInventoryScreenDuck {

    @Unique private boolean mouseInBounds = true;
    @Unique private int slotSize = 18;
    @Unique private int rowOffset = 0; // this offsets, from which row to pull items from and populate slots, it also counter-affects drawoffset
    @Unique private float smoothScrollOffs = 0; // counted in proportion
    @Shadow float scrollOffs; // this is the target
    @Shadow static CreativeModeTab selectedTab;
    @Shadow private static @Final SimpleContainer CONTAINER;

    @WrapOperation(method = "extractBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIII)V"))
    private void drawBackgroundWrap(GuiGraphicsExtractor graphics, RenderPipeline renderPipeline, Identifier texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, Operation<Void> operation, @Local(name = "mouseY") int mouseY, @Local(name = "mouseX") int mouseX) {
        operation.call(graphics, renderPipeline, texture, x, y, u, v, width, height, textureWidth, textureHeight);

        if (FabricLoader.getInstance().getObjectShare().get("flow:is_caching_screen") instanceof Boolean isCaching && isCaching)
            return;

        // test item count in tabs
        /*while (menu.items.size() > 45) {
            SmoothSc.print("LALLALALA");
            menu.items.remove(0);
        }/* */

        //SmoothSc.print(scrollOffs);
        smoothScrollOffs = (smoothScrollOffs - scrollOffs) * (float) Math.pow(SmScCfg.creativeScreenSmoothness, SmoothSc.getLastFrameDuration()) + scrollOffs;

        float rowCount = calculateRowCount();
        if (rowCount != 0) {
            if (mouseY - y > 60 && smoothScrollOffs != 0) {
                rowOffset = 1;
            }
            else {
                rowOffset = 0;
            }
            rowCount = rowOffset / rowCount;
        }
        menu.scrollTo(smoothScrollOffs + rowCount);
        
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
        var iheight = 90;/* */

        mouseInBounds = mouseX >= ix && mouseX < ix + iwidth && mouseY >= iy && mouseY < iy + iheight;

        var yOffset = -(scrollOffsToPixels(smoothScrollOffs) % (slotSize * 5));

        //graphics.fill(x, y, x+1000, y+1000, ARGB.color(50, 0, 255, 255));

        graphics.enableScissor(ix, iy + 1, ix + iwidth, iy + iheight - 1);
        graphics.pose().pushMatrix();
        //graphics.pose().translate(8, 17);
        graphics.pose().translate(0, yOffset);
        operation.call(graphics, renderPipeline, texture, x, y, u, v, width, height, textureWidth, textureHeight);
        graphics.pose().translate(0, slotSize * 5);
        if (SmScCfg.creativeUseScissorTexture) {
            graphics.enableScissor(ix, iy + 1, ix + iwidth, iy + iheight - 1);
            operation.call(graphics, renderPipeline, texture, x, y, u, v, width, height, textureWidth, textureHeight);
            graphics.disableScissor();
        } else {
            operation.call(graphics, renderPipeline, texture, x, iy, u, iv, width, iheight, textureWidth, textureHeight);
        }
        graphics.pose().popMatrix();

        graphics.pose().pushMatrix();
        graphics.pose().translate(0, getDrawOffset());
        var currRow = (int)(double)(smoothScrollOffs * calculateRowCount());
        var fromIndex = (currRow + 5 - rowOffset * 5) * 9;
        for (int i = fromIndex; i >= 0 && i < menu.items.size() && i < fromIndex + 9; i++) {
            // TODO slot-based rendering of items may be more accurate
            //var tempSlot = new Slot(CONTAINER, 0, 9 + i % 9 * 18, 0);;
            ItemStack item = menu.items.get(i);
            //tempSlot.set(item);
            //extractSlot(graphics, tempSlot, mouseX, mouseY);
            graphics.item(item, x + 9 + i % 9 * slotSize, y + 6 * slotSize - rowOffset * 6 * slotSize);
        }
        graphics.pose().popMatrix();

        //graphics.fill(x, y, x+1000, y+1000, ARGB.color(50, 255, 255, 0));
        graphics.disableScissor();
    }

    @Unique private int scrollItemCount = 45;
    @Unique private int itemCounter = scrollItemCount;
    @Override
    protected void extractSlots(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (selectedTab.canScroll()) {
            itemCounter = 0;
            enMask(graphics);
        }
        super.extractSlots(graphics, mouseX, mouseY);
    }

    @Override
    protected void extractSlot(GuiGraphicsExtractor graphics, Slot slot, int mouseX, int mouseY) {
        super.extractSlot(graphics, slot, mouseX, mouseY);

        itemCounter++;
        if (itemCounter == scrollItemCount) {
            deMask(graphics);
        }
    }

    @WrapMethod(method = "mouseScrolled")
    private boolean mouseScrolledWrap(double x, double y, double scrollX, double scrollY, Operation<Boolean> operation) {
        var prevScroll = scrollOffs;
        var ret = operation.call(x, y, scrollX, scrollY);
        if (scrollOffs == prevScroll || SmScCfg.creativeScreenAmount == 0) {
            return ret;
        }

        var pixelscroll = scrollOffsToPixels(prevScroll);
        pixelscroll -= SmScCfg.creativeScreenAmount * scrollY;
        scrollOffs = Math.clamp(pixelsToScrollOffs(pixelscroll), 0, 1);
        
        return ret;
    }
    @Override
    public void extractContents(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float a) {
        var offset = mouseInBounds ? - Math.round(getDrawOffset()) : 0;
        super.extractContents(graphics, mouseX, mouseY + offset, a);
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
    public void enMask(GuiGraphicsExtractor graphics) {
        graphics.enableScissor(8, 18, 170, 106);
        graphics.pose().pushMatrix();
        graphics.pose().translate(0, getDrawOffset());
    }
    @Override
    public void deMask(GuiGraphicsExtractor graphics) {
        graphics.pose().popMatrix();

        //graphics.fill(0, 0, 10000, 10000, ARGB.color(50, 255, 0, 255));
        graphics.disableScissor();
    }
    @Override
    public boolean isMouseInbounds() {
        return mouseInBounds;
    }
    @Override
    public boolean isSelectedTabScrollable() {
        return selectedTab.canScroll();
    }

    public CreativeModeInventoryScreenMixin(ItemPickerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }
}
