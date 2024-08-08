package smsk.smoothscroll.mixin.CreativeScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.inventory.SimpleInventory;

@Mixin(CreativeInventoryScreen.class)
public interface CreativeScreenAccessor {
    @Accessor("INVENTORY")
    static SimpleInventory INVENTORY() { throw new UnsupportedOperationException(); }
}
