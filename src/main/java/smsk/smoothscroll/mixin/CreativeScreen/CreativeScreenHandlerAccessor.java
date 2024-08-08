package smsk.smoothscroll.mixin.CreativeScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen.CreativeScreenHandler;

@Mixin(CreativeScreenHandler.class)
public interface CreativeScreenHandlerAccessor {
    @Invoker("getScrollPosition")
    public float getPos(int row);
}
