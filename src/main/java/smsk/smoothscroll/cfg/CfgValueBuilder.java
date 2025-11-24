package smsk.smoothscroll.cfg;

import java.util.function.Function;

import net.minecraft.text.Text;

public class CfgValueBuilder {

    private CfgValue cfgVal;

    public CfgValueBuilder(String name, Object defaultVal) {
        cfgVal = new CfgValue(name, defaultVal);
    }

    public CfgValueBuilder step(double step) {
        cfgVal.step = step;
        return this;
    }
    public CfgValueBuilder minMax(float min, float max) {
        cfgVal.minVal = min;
        cfgVal.maxVal = max;
        return this;
    }
    public CfgValueBuilder tooltip(String t) {
        return tooltip(Text.literal(t));
    }
    public CfgValueBuilder tooltip(Text t) {
        cfgVal.tooltiptxt = t;
        return this;
    }
    public CfgValueBuilder translatable(String key) {
        cfgVal.setTranslationKey(key);
        return this;
    }
    public CfgValueBuilder map(Object from, String to) {
        cfgVal.translationMap.put(from, to);
        return this;
    }
    public CfgValueBuilder format(String s) {
        cfgVal.setFormatKey(s);
        return this;
    }
    public CfgValueBuilder disableWhen(Function<CfgValue, Boolean> condition) {
        cfgVal.disableWhen = condition;
        return this;
    }

    public CfgValue build() {
        return cfgVal;
    }
}
