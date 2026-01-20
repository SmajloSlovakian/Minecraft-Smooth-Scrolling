package io.github.smajloslovakian.smoothscroll.menu;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import io.github.smajloslovakian.smoothscroll.cfg.CfgValue;

public class CustomSlider extends AbstractSliderButton {

    String txt;
    CfgValue entry;

    public CustomSlider(CfgValue cfgValue) {
        super(0, 0, 150, 20, Component.literal(""), 0);
        entry = cfgValue;
        cfgValue.resetTempValue();
        value = entry.deMinMax((float) cfgValue.getValue());
        txt = cfgValue.getName();
        updateMessage();
    }

    public void refreshValue() {
        value = entry.deMinMax((float) entry.getTempValue());
        updateMessage();
    }

    private Component makeText() {
        return Component.translatable(entry.getFormatKey(), entry.getDisplayName(), entry.tryTranslate(entry.enStep(entry.enMinMax(value))));
    }


    @Override
    protected void applyValue() {
        entry.setTempValue((float) entry.enStep(entry.enMinMax(value)));
        value = entry.deMinMax(entry.enStep(entry.enMinMax(value)));
    }

    @Override
    protected void updateMessage() {
        this.setMessage(makeText());
    }

}
