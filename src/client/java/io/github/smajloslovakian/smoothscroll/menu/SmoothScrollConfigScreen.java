package io.github.smajloslovakian.smoothscroll.menu;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import io.github.smajloslovakian.smoothscroll.SmoothSc;

public class SmoothScrollConfigScreen extends Screen {
    private final Screen parentScreen;
    protected SmoothScrollConfigScreen(Screen parent) {
        super(Component.literal("Smooth Scrolling Options"));
        parentScreen = parent;
    }
    
    public Button button1 = Button.builder(Component.literal("lalal1"), button -> {
        System.out.println("adfdddsfasdfasd!");
    }).build();
    public Button button2 = Button.builder(Component.literal("popopop2"), button -> {
        System.out.println("You clicked button2!");
    }).build();
    //public CustomSlider slider1 = new CustomSlider("Smoothness: %s", 0, 0.01);
   
    @Override
    protected void init() {
        SmoothSc.cfg.loadAndSave();
        button1.setPosition(width / 2, height / 2);
        button2.setPosition(width / 2, height / 2 + 20);
        //slider1.setPosition(width / 2, height / 2 + 40);
        addRenderableWidget(button1);
        addRenderableWidget(button2);
        //addDrawableChild(slider1);
    }
    @Override
    public void onClose() {
      minecraft.setScreen(parentScreen);
    }
}