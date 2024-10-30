package smsk.smoothscroll.cfg;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class CfgValue {
    final Object defaultValue;
    Object currentValue;
    String valueName;
    boolean isFake = false;

    // GUI menu options
    float minVal;
    float maxVal;
    double step = 0.01;
    Object temporaryValue;

    public CfgValue(String name, float defaultVal, float min, float max, double round) {
        this(name, defaultVal, min, max);
        step = round;
    }

    public CfgValue(String name, float defaultVal, float min, float max) {
        this(name, defaultVal);
        minVal = min;
        maxVal = max;
    }

    public CfgValue(String name, Object defaultVal) {
        valueName = name;
        defaultValue = defaultVal;
        currentValue = defaultVal;
        temporaryValue = currentValue;
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

    public static CfgValue parseJson(String name, JsonElement jsonData) {
        var a = new CfgValue(name, null);
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
    public void matchValues(CfgValue source, CfgValue destination) {
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
}