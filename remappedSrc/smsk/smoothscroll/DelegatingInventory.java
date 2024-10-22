package smsk.smoothscroll;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

import java.util.function.Function;
import java.util.function.IntFunction;

/**
 * An implementation of {@link Inventory} used to allow for wrapping any method of getting a
 * stack from a given collection. Used to support Condensed Creative method of entries.
 */
public class DelegatingInventory<S> implements Inventory {

    protected final IntFunction<S> getter;
    protected final Function<S, ItemStack> mapper;

    protected DelegatingInventory(IntFunction<S> getter, Function<S, ItemStack> mapper){
        this.getter = getter;
        this.mapper = mapper;
    }

    public static DelegatingInventory<ItemStack> itemStackBased(IntFunction<ItemStack> stackGetter) {
        return new DelegatingInventory<>(stackGetter, stack -> stack);
    }

    @Override
    public ItemStack getStack(int slot) {
        return mapper.apply(getter.apply(slot));
    }

    //-- DEFAULT IMPLEMENTATION SECTION BELOW --//

    @Override public int size() { return 1; }
    @Override public boolean isEmpty() { return false; }
    @Override public ItemStack removeStack(int slot, int amount) { return ItemStack.EMPTY; }
    @Override public ItemStack removeStack(int slot) { return ItemStack.EMPTY; }
    @Override public void setStack(int slot, ItemStack stack) {}
    @Override public void markDirty() {}
    @Override public boolean canPlayerUse(PlayerEntity player) { return true; }
    @Override public void clear() {}
}
