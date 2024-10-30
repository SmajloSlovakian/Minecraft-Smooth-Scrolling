package smsk.smoothscroll.menu;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import smsk.smoothscroll.cfg.CfgValue;

public class CustomSlider extends SliderWidget {

    String txt;
    double step;
    CfgValue entry;

    public CustomSlider(CfgValue cfgValue) {
        super(0, 0, 150, 20, Text.literal(""), (float) cfgValue.getValue());
        entry = cfgValue;
        cfgValue.resetTempValue();
        txt = cfgValue.getName();
        this.step = cfgValue.getStep();
        updateMessage();
    }

    private static Text makeText(String str, double val, double step) {
        var a = String.format(str, "" + enstepValue(val, step));
        if (a.equals(str))
            a = String.format(str + ": %s", "" + enstepValue(val, step));
        return Text.literal(a);
    }

    private static long enstepValue(double val, double step) {
        return Math.round(val / step);
    }
    // TODO min max implementácia

    @Override
    protected void applyValue() {
        float a = (float) (enstepValue(value, step) * step);
        entry.setTempValue(a);
        value = a;
    }

    @Override
    protected void updateMessage() {
        this.setMessage(makeText(txt, value, step));
    }

}
