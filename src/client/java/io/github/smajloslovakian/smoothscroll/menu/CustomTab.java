package io.github.smajloslovakian.smoothscroll.menu;

import java.util.List;
import java.util.Arrays;
import java.util.function.Consumer;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;

/**
 * A container that does not render but provides inner widgets to
 * {@link net.minecraft.client.gui.components.tabs.TabNavigationBar} instances
 */
public class CustomTab implements Tab {
    private final Component title;
    public final List<AbstractWidget> children;

    public CustomTab(Component title, AbstractWidget[] children) {
        this.title = title;
        this.children = Arrays.asList(children);
    }

    public void addChild(AbstractWidget child) {
        this.children.add(child);
    }

    @Override
    public Component getTabTitle() {
        return this.title;
    }

    /**
     * Used to load/unload children when switching tab
     */
    @Override
    public void visitChildren(Consumer<AbstractWidget> consumer) {
        children.forEach(consumer);
    }

    /**
     * Seems useless here
     */
    @Override
    public void doLayout(ScreenRectangle tabArea) {}

    @Override
    public Component getTabExtraNarration() {
        return Component.literal("Custom Tab Smooth Scrolling Settings");
    }
}
