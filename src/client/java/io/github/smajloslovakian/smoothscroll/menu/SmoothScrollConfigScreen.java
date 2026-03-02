package io.github.smajloslovakian.smoothscroll.menu;

import java.util.ArrayList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import io.github.smajloslovakian.smoothscroll.SmoothSc;
import io.github.smajloslovakian.smoothscroll.cfg.CfgValue;

public class SmoothScrollConfigScreen extends Screen {
    private final Screen parent;

    private final TabManager tabManager;
    private TabNavigationBar tabNav;
    private ArrayList<CustomTab> tabs;
    private Button button1;
    private Button button2;

    public SmoothScrollConfigScreen(Screen parent) {
        super(Component.translatable("smoothscroll.config.title"));
        this.parent = parent;
        this.tabManager = new TabManager(this::addRenderableWidget, this::removeWidget);
    }

    @Override
    protected void init() {
        SmoothSc.cfg.loadAndSave();

        //SmoothSc.print(SmoothSc.mc.getWindow().getScaledWidth() + " x " + SmoothSc.mc.getWindow().getScaledHeight());

        tabs = new ArrayList<CustomTab>();
        for (CfgValue cfgValue : SmoothSc.cfg.getConfigForModifying().getList()) {
            var cfglist = cfgValue.getList();
            if (cfglist != null) {
                var widgets = new ArrayList<AbstractWidget>();
                for (CfgValue innerCfgValue : cfglist) {
                    var a = innerCfgValue.generateWidget();
                    if (a[0] != null) {
                        widgets.add(a[0]);
                        widgets.add(a[1]);
                    }
                }
                // var entryList = new EntryListWidget<Entry<ClickableWidget>>(SmoothSc.mc, 200, 200, 10, 10);
                var newTab = new CustomTab(cfgValue.getDisplayName(), widgets.toArray(new AbstractWidget[0]));
                tabs.add(newTab);
            }
        }

        this.tabNav = TabNavigationBar.builder(this.tabManager, this.width).addTabs(tabs.toArray(new CustomTab[0])).build();
        this.addRenderableWidget(tabNav);
        //this.addDrawableChild(ButtonWidget.builder(Text.literal("print"), button -> {SmoothSc.print(button.getHeight() + "" + button.getWidth());}).build());
        button1 = Button.builder(
            Component.translatable("smoothscroll.config.save"), button -> {
                SmoothSc.cfg.getConfigForModifying().recursiveSaveTempValue();
                SmoothSc.cfg.save();
                this.onClose();
            }).build();
        button2 = Button.builder(
            Component.translatable("smoothscroll.config.exit"), button -> {
                this.onClose();
            }).build();
        this.addRenderableWidget(button1);
        this.addRenderableWidget(button2);
        this.tabNav.selectTab(0, false);
        this.tabNav.updateWidth(this.width);
        this.tabNav.arrangeElements();
        reposition();
    }

    void reposition() {
        //SmoothSc.print(SmoothSc.mc.getWindow().getScaledWidth() + " x " + SmoothSc.mc.getWindow().getScaledHeight());
        for (CustomTab tab : tabs) {
            int a = -1;
            for (AbstractWidget widget : tab.children) {
                a++;
                var x = SmoothSc.mc.getWindow().getGuiScaledWidth() / 6;
                if (a % 2 == 0) {
                    widget.setPosition(x, 50 + a * 22 / 2);
                }
                else {
                    widget.setRectangle(20, 20, x + 152, 50 + (a - 1) * 22 / 2);
                }
            }
        }
        button1.setPosition(SmoothSc.mc.getWindow().getGuiScaledWidth() / 2, SmoothSc.mc.getWindow().getGuiScaledHeight() - 27);
        button2.setPosition(SmoothSc.mc.getWindow().getGuiScaledWidth() / 2 - 150, SmoothSc.mc.getWindow().getGuiScaledHeight() - 27);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        reposition();
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        SmoothSc.cfg.getConfigForModifying().refreshDisableRecursive();
        super.render(context, mouseX, mouseY, delta);
    }
}