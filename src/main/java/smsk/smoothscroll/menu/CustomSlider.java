package smsk.smoothscroll.menu;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import smsk.smoothscroll.SmoothSc;

public class CustomSlider extends SliderWidget {

    String txt;
    double step;

    public CustomSlider(String text, double value, double step) {
        super(0, 0, 150, 20, makeText(text, value, step), value);
        txt = text;
        this.step = step;
    }

    private static Text makeText(String str, double val, double step) {
        return Text.literal(String.format(str, Double.toString(enstepValue(val, step))));
    }

    private static double enstepValue(double val, double step) {
        return Math.round(val / step);
    }

    @Override
    protected void applyValue() {
        SmoothSc.print(value);
    }

    @Override
    protected void updateMessage() {
        this.setMessage(makeText(txt, value, step));
    }

}
