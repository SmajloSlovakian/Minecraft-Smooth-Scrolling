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
    float step = 0.001f;

    public CfgValue(String name, float defaultVal, float min, float max, float round) {
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
        if (currentValue instanceof List){
            List<?> list = (List<?>) currentValue;
            if (!list.isEmpty() && list.get(0) instanceof CfgValue) {
                List<CfgValue> cfgValueList = (List<CfgValue>) currentValue;
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

    public void resetValue() {
        currentValue = defaultValue;
    }

    public void setValue(Object value) {
        currentValue = value;
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
        if (source.currentValue == null) return;
        if (destination != null && !destination.currentValue.getClass().equals(source.currentValue.getClass())) return;
        var a = source.getList();
        var b = destination.getList();
        if (a == null && b == null) {
            destination.currentValue = source.currentValue;
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
        return new Gson().toJsonTree(toExport.currentValue);
    }

    @Override
    public String toString() {
        return "(" + valueName + ":" + currentValue + ")";
    }
}