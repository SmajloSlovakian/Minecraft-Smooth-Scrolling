package io.github.smajloslovakian.smoothscroll.duck;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public interface CreativeModeInventoryScreenDuck {
    void enMask(GuiGraphicsExtractor graphics);
    void deMask(GuiGraphicsExtractor graphics);
    boolean isMouseInbounds();
    boolean canScroll();
}
