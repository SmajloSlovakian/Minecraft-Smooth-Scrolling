package smsk.smoothscroll.compat;

import io.wispforest.condensed_creative.ducks.CreativeInventoryScreenHandlerDuck;
import io.wispforest.condensed_creative.entry.Entry;
import io.wispforest.condensed_creative.entry.EntryContainer;
import io.wispforest.condensed_creative.entry.impl.ItemEntry;
import net.minecraft.screen.ScreenHandler;
import smsk.smoothscroll.DelegatingInventory;

public class CondensedInventoryCompat {

    public static DelegatingInventory<Entry> of(ScreenHandler handler) {
        return new CondensedEntryDelegatingInventory(handler);
    }

    public static class CondensedEntryDelegatingInventory extends DelegatingInventory<Entry> implements EntryContainer {
        protected CondensedEntryDelegatingInventory(ScreenHandler handler) {
            super(value -> {
                try {
                    if(handler instanceof CreativeInventoryScreenHandlerDuck duck) {
                        return duck.getFilteredEntryList().get(value);
                    }
                } catch (IndexOutOfBoundsException ignored) {}

                return ItemEntry.EMPTY;
            }, Entry::getDisplayStack);
        }

        @Override
        public Entry getEntryStack(int slot) {
            return this.getter.apply(slot);
        }

        @Override
        public void setEntryStack(int slot, Entry entryStack){}
    }
}
