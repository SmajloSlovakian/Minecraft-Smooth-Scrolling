package smsk.smoothscroll.cfg;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import smsk.smoothscroll.SmoothSc;

public class SmScCfg extends NewConfig {
    public final static float format = 2.2f;

    public static float hotbarSpeed;
    public static float chatSpeed;
    public static float chatOpeningSpeed;
    public static float creativeScreenSpeed;
    public static float entryListSpeed;
    public static boolean enableMaskDebug;

	static final String defaultCfg = """
{
    "Notes": [
        "Safe values for settings are 0 - 1 (inclusive).",
        "0 means animation off (no smoothness) and bigger values mean slower animation speed (high smoothness).",
        "Press F3+T in a world to update the config.",
        "To access config ingame, use the mod modmenu."
    ],
    "ScrollSmoothness": {
        "hotbar": 0.2,
        "chat": 0.5,
        "creativeScreen": 0.5,
        "entryList": 0.5
    },
    "Misc": {
        "enableMaskDebug": false,
        "chatOpeningSpeed": 0.5
    },
    "Format": 2.2
}
        """;

    public static JsonObject jsonDefaultCfg;

    static {
        try {
            var gs = new Gson();
            jsonDefaultCfg = gs.fromJson(defaultCfg, JsonObject.class);
        } catch (JsonSyntaxException e) {}
    }

    public SmScCfg() {
        super("smoothscroll.json", jsonDefaultCfg);
        SmoothSc.print("USING:\n"+jsonConfig);
    }

    @Override
    void dataCorrectPermanent() {
        // Old file format corrections
        if (jsonConfig.get("cfgVersion") != null) {
            // getting the old values
            float oldcfgver = jsonConfig.get("hotbarSpeed").getAsFloat();
            float oldhotbarspeed = jsonConfig.get("hotbarSpeed") == null ? 0.2f : jsonConfig.get("hotbarSpeed").getAsFloat();
            float oldchatspeed = jsonConfig.get("chatSpeed") == null ? 0.5f : jsonConfig.get("chatSpeed").getAsFloat();
            float oldchatopeningspeed = jsonConfig.get("chatOpeningSpeed") == null ? 0.5f : jsonConfig.get("chatOpeningSpeed").getAsFloat();
            float oldcreativescreenspeed = jsonConfig.get("creativeScreenSpeed") == null ? 0.5f : jsonConfig.get("creativeScreenSpeed").getAsFloat();
            float oldentrylistspeed = jsonConfig.get("entryListSpeed") == null ? 0.5f : jsonConfig.get("entryListSpeed").getAsFloat();
            boolean oldmaskdebug = jsonConfig.get("enableMaskDebug") == null ? false : jsonConfig.get("enableMaskDebug").getAsBoolean();

            // correcting the old values just like previous code
            if (oldcfgver < 1.6f) { // speeds before this version were divisors and not multipliers
                if (oldhotbarspeed != 0.2f) {
                    if (oldhotbarspeed != 0) oldhotbarspeed = 1 / oldhotbarspeed;
                    if (oldchatspeed != 0) oldchatspeed = 1 / oldchatspeed;
                    if (oldcreativescreenspeed != 0) oldcreativescreenspeed = 1 / oldcreativescreenspeed;
                    if (oldentrylistspeed != 0) oldentrylistspeed = 1 / oldentrylistspeed;
                }
            }
            if (oldcfgver < 1.9f) {
                if (oldentrylistspeed == 0.334f) oldentrylistspeed = 0.5f;
            }
            if (oldcfgver < 1.91f) {
                oldchatopeningspeed = oldchatspeed;
            }
            // save old values to the new format
            var a = jsonConfig.get("ScrollSmoothness").getAsJsonObject();
            a.addProperty("hotbar", oldhotbarspeed);
            a.addProperty("chat", oldchatspeed);
            a.addProperty("creativeScreen", oldcreativescreenspeed);
            a.addProperty("entryList", oldentrylistspeed);

            a = jsonConfig.get("Misc").getAsJsonObject();
            a.addProperty("enableMaskDebug", oldmaskdebug);
            a.addProperty("chatOpeningSpeed", oldchatopeningspeed);
            
            // delete old format values in favor of new format
            jsonConfig.remove("note");
            jsonConfig.remove("hotbarSpeed");
            jsonConfig.remove("chatOpeningSpeed");
            jsonConfig.remove("entryListSpeed");
            jsonConfig.remove("enableMaskDebug");
            jsonConfig.remove("cfgVersion");
        }
        // New file format corrections go here

        // Notes and Format should always be up to date and not modified
        jsonConfig.add("Notes", jsonDefaultCfg.get("Notes"));
        jsonConfig.add("Format", jsonDefaultCfg.get("Format"));
    }
    @Override
    void intoVariables() {
        var a = jsonConfig.getAsJsonObject("ScrollSmoothness");
        hotbarSpeed = a.get("hotbar").getAsFloat();
        chatSpeed = a.get("chat").getAsFloat();
        creativeScreenSpeed = a.get("creativeScreen").getAsFloat();
        entryListSpeed = a.get("entryList").getAsFloat();

        a = jsonConfig.getAsJsonObject("Misc");
        enableMaskDebug = a.get("enableMaskDebug").getAsBoolean();
        chatOpeningSpeed = a.get("chatOpeningSpeed").getAsFloat();
    }
    @Override
    void dataCorrectTemporary() {
        // Disable entry list smooth scrolling if the mod smooth scrolling refurbished is present
        if (SmoothSc.isSmoothScrollingRefurbishedLoaded) {
            entryListSpeed = 0;
        }
    }

    @Override
    void problemReading() {
        SmoothSc.print("There was a problem reading the config file. Using default values.");
    }
    @Override
    void problemWriting() {
        SmoothSc.print("There was a problem writing to the config file.");
    }
    @Override
    void fileNotFound() {
        SmoothSc.print("There is no config file, creating a new one.");
    }
}
