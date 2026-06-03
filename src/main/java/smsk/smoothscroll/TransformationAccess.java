package smsk.smoothscroll;

import net.minecraft.client.font.DrawnTextConsumer;
import net.minecraft.client.gui.DrawContext;

public interface TransformationAccess {
    public DrawnTextConsumer.Transformation getTransformation();
    public void setTransformation(DrawnTextConsumer.Transformation transformation);
    public DrawContext getContext();
}
