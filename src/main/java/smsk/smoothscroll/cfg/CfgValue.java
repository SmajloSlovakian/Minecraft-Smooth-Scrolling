package smsk.smoothscroll.cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import smsk.smoothscroll.menu.CustomSlider;

public class CfgValue {
    final Object defaultValue;
    Object currentValue;
    String valueName;
    boolean isFake = false;
    CfgValue parent = null;

    // GUI menu options
    float minVal;
    float maxVal;
    double step = 0.01;
    Object temporaryValue;
    ClickableWidget myWidget;
    ButtonWidget myResetButton;
    Text tooltiptxt;
    Map<Object, String> translationMap = new HashMap<>(){{
        put(true, "options.on");
        put(false, "options.off");
    }};
// {true: "options.on" false: "options.off"};
    String formatKey = "smoothscroll.config.format.default";
    String translationKey;
    Function<CfgValue, Boolean> disableWhen;

    public CfgValue(String name, Object defaultVal) {
        valueName = name;
        defaultValue = defaultVal;
        currentValue = defaultVal;
        temporaryValue = currentValue;
        var list = getList();
        if (list != null) {
            for (CfgValue cfgValue : list) {
                cfgValue.parent = this;
            }
        }
    }
    public static CfgValueBuilder builder(String name, Object defaultVal) {
        return new CfgValueBuilder(name, defaultVal);
    }

    /**
     * If the value inside the CfgValue is of type
     * List<CfgValue> then it tries to find a
     * CfgValue with the specified name inside the
     * list. If it does not find it or the value
     * isn't of type List<CfgValue>, it will return
     * a fake instance of CfgValue.
     */
    public CfgValue get(String name) {
        if (name == "..") {
            return parent != null ? parent : makeFake();
        }

        List<CfgValue> cfgValueList = getList();
        if (cfgValueList == null)
            return makeFake();
        
        for (CfgValue cfgValue : cfgValueList) {
            if (cfgValue.valueName.equals(name))
                return cfgValue;
        }
        
        return makeFake();
    }

    /**
     * If the value inside the CfgValue is of type
     * List<CfgValue> then it returns the list,
     * if not, it returns null.
     */
    @SuppressWarnings("unchecked")
    public List<CfgValue> getList() {
        if (getValue() instanceof List){
            List<?> list = (List<?>) getValue();
            if (!list.isEmpty() && list.get(0) instanceof CfgValue) {
                List<CfgValue> cfgValueList = (List<CfgValue>) getValue();
                return cfgValueList;
            }
        }
        return null;
    }

    public String getName() {
        return valueName;
    }

    public Object getValue() {
        return currentValue;
    }

    public boolean exists() {
        return !isFake;
    }

    public static CfgValue makeFake() {
        var ret = new CfgValue("", null);
        ret.isFake = true;
        return ret;
    }

    public void setValue(Object value) {
        currentValue = value;
        temporaryValue = currentValue;
    }
    public void setTempValue(Object value) {
        temporaryValue = value;
    }
    public Object getTempValue() {
        return temporaryValue;
    }
    public void saveTempValue() {
        //SmoothSc.print(this + " is saving: " + temporaryValue);
        currentValue = temporaryValue;
    }
    public void resetTempValue() {
        temporaryValue = currentValue;
    }
    public void defaultToTemp() {
        temporaryValue = defaultValue;
        if (myWidget != null) {
            if (myWidget instanceof CustomSlider cs) {
                cs.refreshValue();
            }
            if (myWidget instanceof ButtonWidget bw) {
                bw.setMessage(makeButtonText());
            }
        }
    }
    public void recursiveSaveTempValue() {
        var a = getList();
        if (a != null) {
            for (CfgValue cfgValue2 : a) {
                cfgValue2.recursiveSaveTempValue();
            }
            return;
        }
        saveTempValue();
    }

    public ClickableWidget[] generateWidget() {
        myResetButton = ButtonWidget.builder(Text.literal("🗑"), button -> {this.defaultToTemp();}).build();


        if (this.getValue() instanceof Number) {
            myWidget = new CustomSlider(this);
            this.assignWidget(myWidget);
            if(tooltiptxt != null)
                myWidget.setTooltip(Tooltip.of(tooltiptxt));
            return new ClickableWidget[] {myWidget, myResetButton};
        }
        else if (this.getValue() instanceof Boolean) {
            myWidget = ButtonWidget.builder(
                makeButtonText(),
                button -> {
                    this.setTempValue(!(boolean) this.getTempValue());
                    button.setMessage(makeButtonText());
                }
            ).build();
            if(tooltiptxt != null)
                myWidget.setTooltip(Tooltip.of(tooltiptxt));
            this.assignWidget(myWidget);
            return new ClickableWidget[] {myWidget, myResetButton};
        }
        myWidget = ButtonWidget.builder(makeButtonText(), button -> {}).build();
        return new ClickableWidget[] {myWidget, myResetButton};
    }

    private Text makeButtonText() {
        return Text.translatable(formatKey, getDisplayName(), tryTranslate(getTempValue()));
    }

    public static CfgValue parseJson(String name, JsonElement jsonData) {
        var a = new CfgValue(name, null);
        if (jsonData == null) return a;
        if (jsonData.isJsonObject()) {
            var objMap = jsonData.getAsJsonObject().asMap();
            List<CfgValue> b = new ArrayList<CfgValue>();
            for (String elementName : objMap.keySet()) {
                b.add(parseJson(elementName, objMap.get(elementName)));
            }
            a.setValue(b);
        }
        // TODO handle jsonarray
        else if (jsonData.isJsonPrimitive()) {
            JsonPrimitive primitive = jsonData.getAsJsonPrimitive();

            if (primitive.isString()) {
                a.setValue(primitive.getAsString());
            } else if (primitive.isBoolean()) {
                a.setValue(primitive.getAsBoolean());
            } else if (primitive.isNumber()) {
                Number number = primitive.getAsNumber();
                a.setValue(number.floatValue());
            }
        }
        return a;
    }

    public void matchValues(CfgValue source) {
        matchValues(source, this);
    }
    public static void matchValues(CfgValue source, CfgValue destination) {
        if (source.getValue() == null) return;
        if (destination != null && !destination.getValue().getClass().equals(source.getValue().getClass())) return;
        var a = source.getList();
        var b = destination.getList();
        if (a == null && b == null) {
            destination.setValue(source.getValue());
        }

        if (a == null || b == null) return;
        for (CfgValue destval : b)
            for (CfgValue srcval : a)
                if (destval.valueName.equals(srcval.valueName))
                    matchValues(srcval, destval);
    }

    public static JsonElement exportJson(CfgValue toExport) {
        var a = toExport.getList();
        if (a != null) {
            var b = new JsonObject();
            for (CfgValue cfgValue : a) {
                b.add(cfgValue.valueName, exportJson(cfgValue));
            }
            return b;
        }
        return new Gson().toJsonTree(toExport.getValue());
    }

    @Override
    public String toString() {
        return "(" + valueName + ":" + currentValue + ")";
    }

    public double getStep() {
        return step;
    }
    public void assignWidget(ClickableWidget cw) {
        myWidget = cw;
    }

    public double getMax() {
        return maxVal;
    }

    public double getMin() {
        return minVal;
    }
    public String getFormatKey() {
        return formatKey;
    }
    public Text tryTranslate(Object value) {
        if (translationMap.containsKey(value)) {
            return Text.translatable(translationMap.get(value));
        }
        return Text.literal("" + value);
    }
    public void setTranslationKey(String key) {
        this.translationKey = key;
    }
    public void setFormatKey(String key) {
        this.formatKey = key;
    }
    public Text getDisplayName() {
        if (translationKey != null) {
            return Text.translatable(translationKey);
        }
        return Text.literal(valueName);
    }
    public void refreshDisableRecursive() {
        var list = getList();
        if (list == null) {
            if (disableWhen == null) {
                return;
            }
            var shouldBeDisabled = disableWhen.apply(this);
            myWidget.active = !shouldBeDisabled;
            myResetButton.active = !shouldBeDisabled;
            return;
        }
        for (CfgValue cfgValue : list) {
            cfgValue.refreshDisableRecursive();
        }
    }

    public long enHelfStep(double val) {
        return Math.round(val / getStep());
    }
    public double enStep(double val) {
        return enHelfStep(val) * getStep();
    }
    public double enMinMax(double val) {
        return val * (getMax() - getMin()) + getMin();
    }
    public double deMinMax(double val) {
        return (val - getMin()) / getMax();
    }

}