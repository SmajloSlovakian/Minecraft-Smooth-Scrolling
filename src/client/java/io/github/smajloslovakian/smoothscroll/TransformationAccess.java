package io.github.smajloslovakian.smoothscroll;

import net.minecraft.client.gui.ActiveTextCollector.Parameters;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public interface TransformationAccess {
    public Parameters getTransformation();
    public void setTransformation(Parameters transformation);
    public GuiGraphicsExtractor getContext();
}