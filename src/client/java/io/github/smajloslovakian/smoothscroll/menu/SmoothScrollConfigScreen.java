package io.github.smajloslovakian.smoothscroll.menu;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ScrollableLayout;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import io.github.smajloslovakian.smoothscroll.SmoothSc;
import io.github.smajloslovakian.smoothscroll.cfg.CfgValue;

public class SmoothScrollConfigScreen extends Screen {
    private final Screen parent;

    private final HeaderAndFooterLayout layout;
    private Button button1;
    private Button button2;
    private ScrollableLayout scrollableLayout;

    public SmoothScrollConfigScreen(Screen parent) {
        super(Component.translatable("smoothscroll.config.title"));
        this.parent = parent;
        layout = new HeaderAndFooterLayout(this);
    }

    protected void init() {
        SmoothSc.cfg.loadAndSave();

        GridLayout contents = (new GridLayout()).columnSpacing(8).rowSpacing(4);
        scrollableLayout = new ScrollableLayout(minecraft, contents, layout.getContentHeight());
        layout.addToContents(scrollableLayout);
        contents.defaultCellSetting().alignHorizontallyCenter();
        GridLayout.RowHelper rowHelper = contents.createRowHelper(2);

        for (CfgValue cfgValue : SmoothSc.cfg.getConfigForModifying().getList()) {
            var cfglist = cfgValue.getList();
            if (cfglist != null) {
                rowHelper.addChild(new StringWidget(cfgValue.getDisplayName(), font), 2);
                for (CfgValue innerCfgValue : cfglist) {
                    var a = innerCfgValue.generateWidget();
                    if (a[0] != null) {
                        a[0].setWidth(150 * 2);
                        a[1].setWidth(50);
                        rowHelper.addChild(a[0]);
                        rowHelper.addChild(a[1]);
                    }
                }
                rowHelper.addChild(new StringWidget(Component.empty(), font), 2);
            }
        }

        GridLayout footer = (GridLayout)this.layout.addToFooter((new GridLayout()).columnSpacing(8).rowSpacing(4));
        contents.defaultCellSetting().alignHorizontallyCenter();
        rowHelper = footer.createRowHelper(2);
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
        
        rowHelper.addChild(button1);
        rowHelper.addChild(button2);
        layout.addTitleHeader(title, font);
        this.layout.visitWidgets((x$0) -> this.addRenderableWidget(x$0));
        layout.arrangeElements();
    }

    @Override
    public void onClose() {
        minecraft.gui.setScreen(parent);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        SmoothSc.cfg.getConfigForModifying().refreshDisableRecursive();
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    @Override
    protected void repositionElements() {
        scrollableLayout.setMaxHeight(Math.max(layout.getContentHeight(), 0));
        layout.arrangeElements();
        //scrollableLayout.arrangeElements();
    }
}