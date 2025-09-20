package smsk.smoothscroll.mixin.Widgets;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.gui.screen.dialog.DialogScreen;
import net.minecraft.client.gui.widget.ScrollableLayoutWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;

@Mixin(DialogScreen.class)
public interface DialogScreenAccessor {
    @Invoker("refreshWidgetPositions")
    void refreshScroll();
    @Accessor("layout")
    ThreePartsLayoutWidget getLayout();
    @Accessor("contents")
    ScrollableLayoutWidget getContents();
}
