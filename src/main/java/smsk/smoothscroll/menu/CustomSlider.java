package smsk.smoothscroll.menu;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import smsk.smoothscroll.cfg.CfgValue;

public class CustomSlider extends SliderWidget {

    String txt;
    CfgValue entry;

    public CustomSlider(CfgValue cfgValue) {
        super(0, 0, 150, 20, Text.literal(""), 0);
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

    private Text makeText() {
        return Text.translatable(entry.getFormatKey(), entry.getDisplayName(), entry.tryTranslate(entry.enStep(entry.enMinMax(value))));
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
