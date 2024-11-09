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
        value = deMinMax((float) cfgValue.getValue());
        txt = cfgValue.getName();
        updateMessage();
    }

    public void refreshValue() {
        value = (float) entry.getTempValue();
        updateMessage();
    }

    private Text makeText() {
        var a = String.format(entry.getUnformatted(), entry.getName(), "" + entry.tryTranslate(enStep(enMinMax(value))));
        return Text.literal(a);
    }

    private long enHelfStep(double val) {
        return Math.round(val / entry.getStep());
    }
    private double enStep(double val) {
        return enHelfStep(val) * entry.getStep();
    }
    private double enMinMax(double val) {
        return val * (entry.getMax() - entry.getMin()) + entry.getMin();
    }
    private double deMinMax(double val) {
        return (val - entry.getMin()) / entry.getMax();
    }

    @Override
    protected void applyValue() {
        entry.setTempValue((float) enStep(enMinMax(value)));
        value = deMinMax(enStep(enMinMax(value)));
    }

    @Override
    protected void updateMessage() {
        this.setMessage(makeText());
    }

}
