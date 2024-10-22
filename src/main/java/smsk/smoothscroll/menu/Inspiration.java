package smsk.smoothscroll.menu;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tab.TabManager;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TabNavigationWidget;
import net.minecraft.text.Text;
import smsk.smoothscroll.SmoothSc;
import smsk.smoothscroll.cfg.SmScCfg;

public class Inspiration extends Screen {
    private final Screen parent;

    private final TabManager tabManager;
    private TabNavigationWidget tabNav;

    private CustomTab[] tabs;

    public Inspiration(Screen parent) {
        super(Text.literal("Config Screen"));
        this.parent = parent;
        this.tabManager = new TabManager(this::addDrawableChild, this::remove);
    }

    @Override
    protected void init() {
        SmoothSc.cfg.loadAndSave();
        tabs = new CustomTab[] {
            new CustomTab(Text.literal("Chat"), new CustomSlider[] {
                new CustomSlider("Smoothness: %s", SmScCfg.chatSpeed, 0.01),
                new CustomSlider("Opening speed: %s", SmScCfg.chatOpeningSpeed, 0.01)
            }),
            new CustomTab(Text.literal("Hotbar"), new CustomSlider[] {
                new CustomSlider("Smoothness: %s", SmScCfg.hotbarSpeed, 0.01)
            }),
            new CustomTab(Text.literal("Creative Screen"), new CustomSlider[] {
                new CustomSlider("Smoothness: %s", SmScCfg.creativeScreenSpeed, 0.01)
            })
        };
        this.tabNav = TabNavigationWidget.builder(this.tabManager, this.width).tabs(tabs).build();
        this.addDrawableChild(tabNav);
        this.tabNav.selectTab(0, false);
        this.initTabNavigation();
        for (CustomTab tab : tabs) {
            int a = -1;
            for (ClickableWidget widget : tab.children) {
                a++;
                widget.setPosition(0, 50 + a * 20);
            }
        }
    }

    @Override
    protected void initTabNavigation() {
        if (this.tabNav == null) return;

        this.tabNav.setWidth(this.width);
        this.tabNav.init();
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
    }
}