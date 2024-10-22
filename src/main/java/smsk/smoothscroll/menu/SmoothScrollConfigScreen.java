package smsk.smoothscroll.menu;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import smsk.smoothscroll.SmoothSc;

public class SmoothScrollConfigScreen extends Screen {
    private final Screen parentScreen;
    protected SmoothScrollConfigScreen(Screen parent) {
        super(Text.literal("Smooth Scrolling Options"));
        parentScreen = parent;
    }
    
    public ButtonWidget button1 = ButtonWidget.builder(Text.literal("lalal1"), button -> {
        System.out.println("adfdddsfasdfasd!");
    }).build();
    public ButtonWidget button2 = ButtonWidget.builder(Text.literal("popopop2"), button -> {
        System.out.println("You clicked button2!");
    }).build();
    public CustomSlider slider1 = new CustomSlider("Smoothness: %s", 0, 0.01);
   
    @Override
    protected void init() {
        SmoothSc.cfg.loadAndSave();
        button1.setPosition(width / 2, height / 2);
        button2.setPosition(width / 2, height / 2 + 20);
        slider1.setPosition(width / 2, height / 2 + 40);
        addDrawableChild(button1);
        addDrawableChild(button2);
        addDrawableChild(slider1);
    }
    @Override
    public void close() {
      client.setScreen(parentScreen);
    }
}