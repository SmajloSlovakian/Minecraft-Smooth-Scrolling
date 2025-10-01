package smsk.smoothscroll.mixin.Widgets;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.gui.screen.Screen;

@Mixin(Screen.class)
public interface ScreenAccessor {
    @Invoker("refreshWidgetPositions")
    void refreshScroll();
}
