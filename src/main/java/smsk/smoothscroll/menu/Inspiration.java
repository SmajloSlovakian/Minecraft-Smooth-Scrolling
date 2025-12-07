package smsk.smoothscroll.menu;

import java.util.ArrayList;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tab.TabManager;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TabNavigationWidget;
import net.minecraft.text.Text;
import smsk.smoothscroll.SmoothSc;
import smsk.smoothscroll.cfg.CfgValue;

public class Inspiration extends Screen {
    private final Screen parent;

    private final TabManager tabManager;
    private TabNavigationWidget tabNav;
    private ArrayList<CustomTab> tabs;
    private ButtonWidget button1;
    private ButtonWidget button2;

    public Inspiration(Screen parent) {
        super(Text.translatable("smoothscroll.config.title"));
        this.parent = parent;
        this.tabManager = new TabManager(this::addDrawableChild, this::remove);
    }

    @Override
    protected void init() {
        SmoothSc.cfg.loadAndSave();

        //SmoothSc.print(SmoothSc.mc.getWindow().getScaledWidth() + " x " + SmoothSc.mc.getWindow().getScaledHeight());

        tabs = new ArrayList<CustomTab>();
        for (CfgValue cfgValue : SmoothSc.cfg.getConfigForModifying().getList()) {
            var cfglist = cfgValue.getList();
            if (cfglist != null) {
                var widgets = new ArrayList<ClickableWidget>();
                for (CfgValue innerCfgValue : cfglist) {
                    var a = innerCfgValue.generateWidget();
                    if (a[0] != null) {
                        widgets.add(a[0]);
                        widgets.add(a[1]);
                    }
                }
                // var entryList = new EntryListWidget<Entry<ClickableWidget>>(SmoothSc.mc, 200, 200, 10, 10);
                var newTab = new CustomTab(cfgValue.getDisplayName(), widgets.toArray(new ClickableWidget[0]));
                tabs.add(newTab);
            }
        }

        this.tabNav = TabNavigationWidget.builder(this.tabManager, this.width).tabs(tabs.toArray(new CustomTab[0])).build();
        this.addDrawableChild(tabNav);
        //this.addDrawableChild(ButtonWidget.builder(Text.literal("print"), button -> {SmoothSc.print(button.getHeight() + "" + button.getWidth());}).build());
        button1 = ButtonWidget.builder(
            Text.translatable("smoothscroll.config.save"), button -> {
                SmoothSc.cfg.getConfigForModifying().recursiveSaveTempValue();
                SmoothSc.cfg.save();
                this.close();
            }).build();
        button2 = ButtonWidget.builder(
            Text.translatable("smoothscroll.config.exit"), button -> {
                this.close();
            }).build();
        this.addDrawableChild(button1);
        this.addDrawableChild(button2);
        this.tabNav.selectTab(0, false);
        this.tabNav.setWidth(this.width);
        this.tabNav.init();
        reposition();
    }

    void reposition() {
        //SmoothSc.print(SmoothSc.mc.getWindow().getScaledWidth() + " x " + SmoothSc.mc.getWindow().getScaledHeight());
        for (CustomTab tab : tabs) {
            int a = -1;
            for (ClickableWidget widget : tab.children) {
                a++;
                var x = SmoothSc.mc.getWindow().getScaledWidth() / 6;
                if (a % 2 == 0) {
                    widget.setPosition(x, 50 + a * 22 / 2);
                }
                else {
                    widget.setDimensionsAndPosition(20, 20, x + 152, 50 + (a - 1) * 22 / 2);
                }
            }
        }
        button1.setPosition(SmoothSc.mc.getWindow().getScaledWidth() / 2, SmoothSc.mc.getWindow().getScaledHeight() - 27);
        button2.setPosition(SmoothSc.mc.getWindow().getScaledWidth() / 2 - 150, SmoothSc.mc.getWindow().getScaledHeight() - 27);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        reposition();
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        SmoothSc.cfg.getConfigForModifying().refreshDisableRecursive();
        super.render(context, mouseX, mouseY, delta);
    }
}