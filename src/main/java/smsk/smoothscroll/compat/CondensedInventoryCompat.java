package smsk.smoothscroll.compat;

import io.wispforest.condensed_creative.util.CondensedInventory;
import net.minecraft.item.ItemStack;
import smsk.smoothscroll.mixin.CreativeScreen.CreativeScreenAccessor;

public class CondensedInventoryCompat {
    public static ItemStack getStack(int i) {
        return ((CondensedInventory) CreativeScreenAccessor.INVENTORY()).getEntryStack(i - 9).getEntryStack();
    }
}
