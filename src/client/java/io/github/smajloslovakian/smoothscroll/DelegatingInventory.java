package io.github.smajloslovakian.smoothscroll;

import java.util.function.Function;
import java.util.function.IntFunction;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * An implementation of {@link Container} used to allow for wrapping any method of getting a
 * stack from a given collection. Used to support Condensed Creative method of entries.
 */
public class DelegatingInventory<S> implements Container {

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
    public ItemStack getItem(int slot) {
        return mapper.apply(getter.apply(slot));
    }

    //-- DEFAULT IMPLEMENTATION SECTION BELOW --//

    @Override public int getContainerSize() { return 1; }
    @Override public boolean isEmpty() { return false; }
    @Override public ItemStack removeItem(int slot, int amount) { return ItemStack.EMPTY; }
    @Override public ItemStack removeItemNoUpdate(int slot) { return ItemStack.EMPTY; }
    @Override public void setItem(int slot, ItemStack stack) {}
    @Override public void setChanged() {}
    @Override public boolean stillValid(Player player) { return true; }
    @Override public void clearContent() {}
}
