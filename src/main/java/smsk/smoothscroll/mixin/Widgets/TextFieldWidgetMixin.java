package smsk.smoothscroll.mixin.Widgets;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.math.MathHelper;
import smsk.smoothscroll.SmoothSc;

@Mixin(TextFieldWidget.class)
public class TextFieldWidgetMixin {
    @Shadow private int firstCharacterIndex;
    @Shadow private String text;

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        firstCharacterIndex += verticalAmount;
        if (firstCharacterIndex < 0) firstCharacterIndex = 0;
        firstCharacterIndex = MathHelper.clamp(firstCharacterIndex, 0, text.length());
        return true;
    }

}
