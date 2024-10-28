package smsk.smoothscroll.cfg;

import java.util.ArrayList;
import java.util.Arrays;

import org.spongepowered.asm.mixin.Overwrite;

import smsk.smoothscroll.SmoothSc;

public class SmScCfg extends NewConfig {
    public final static float format = 2.2f;

    public static float hotbarSpeed;
    public static float chatSpeed;
    public static float chatOpeningSpeed;
    public static float creativeScreenSpeed;
    public static float entryListSpeed;
    public static boolean enableMaskDebug;
    public static boolean hotbarRollover;

    static CfgValue template = new CfgValue("root", new ArrayList<CfgValue>(Arrays.asList(
        new CfgValue("Notes", new ArrayList<String>(Arrays.asList(
            "Safe values for settings are 0 - 1 (inclusive).",
            "0 means animation off (no smoothness) and bigger values mean slower animation speed (high smoothness).",
            "Press F3+T in a world to update the config.",
            "To access config ingame, use the mod modmenu."
        ))),
        new CfgValue("Hotbar", new ArrayList<CfgValue>(Arrays.asList(
            new CfgValue("Smoothness", 0.2f, 0, 1),
            new CfgValue("Rollover", true)
        ))),
        new CfgValue("Chat", new ArrayList<CfgValue>(Arrays.asList(
            new CfgValue("Smoothness", 0.5f, 0, 1),
            new CfgValue("Opening Speed", 0.5f, 0, 1)
        ))),
        new CfgValue("Creative Screen", new ArrayList<CfgValue>(Arrays.asList(
            new CfgValue("Smoothness", 0.5f, 0, 1)
        ))),
        new CfgValue("Entry List", new ArrayList<CfgValue>(Arrays.asList(
            new CfgValue("Smoothness", 0.5f, 0, 1)
        ))),
        new CfgValue("Misc", new ArrayList<CfgValue>(Arrays.asList(
            new CfgValue("Enable mask debug", false)
        ))),
        new CfgValue("Format", format)
    )));

    public SmScCfg() {
        super("smoothscroll.json", template);
        SmoothSc.print("USING:\n");
    }

    @Override
    void dataCorrectPermanent() {
        if (rawRoot.get("cfgVersion").exists() && rawRoot.get("cfgVersion").currentValue instanceof Float) {
            SmoothSc.print("Found old format entries in the config file, attempting to update them.");
            var cfgver = (float) rawRoot.get("cfgVersion").currentValue;
            
            var a = rawRoot.get("hotbarSpeed");
            if (a.exists() && a.currentValue instanceof Float) {
                SmoothSc.print(a.currentValue);
                if (cfgver < 1.6f && (float) a.currentValue >= 1) a.currentValue = 1 / (float) a.currentValue;
                root.get("Hotbar").get("Smoothness").setValue(a.currentValue);
            }
            a = rawRoot.get("chatSpeed");
            if (a.exists() && a.currentValue instanceof Float) {
                if (cfgver < 1.6f && (float) a.currentValue >= 1) a.currentValue = 1 / (float) a.currentValue;
                root.get("Chat").get("Smoothness").setValue(a.currentValue);
            }
            a = rawRoot.get("chatOpeningSpeed");
            if (a.exists() && a.currentValue instanceof Float) {
                root.get("Chat").get("Opening Speed").setValue(a.currentValue);
            }
            a = rawRoot.get("creativeScreenSpeed");
            if (a.exists() && a.currentValue instanceof Float) {
                if (cfgver < 1.6f && (float) a.currentValue >= 1) a.currentValue = 1 / (float) a.currentValue;
                root.get("Creative Screen").get("Smoothness").setValue(a.currentValue);
            }
            a = rawRoot.get("entryListSpeed");
            if (a.exists() && a.currentValue instanceof Float) {
                if (cfgver < 1.6f && (float) a.currentValue >= 1) a.currentValue = 1 / (float) a.currentValue;
                if (cfgver < 1.9f && (float) a.currentValue == 0.334f) a.currentValue = 0.5f;
                root.get("Entry List").get("Smoothness").setValue(a.currentValue);
            }
            a = rawRoot.get("enableMaskDebug");
            if (a.exists() && a.currentValue instanceof Float) {
                root.get("Chat").get("Smoothness").setValue(a.currentValue);
            }
        }
        // New file format corrections go here

        // Notes and Format should always be up to date and not modified
        root.get("Notes").resetValue();
        root.get("Format").resetValue();
        
    }

    @Override
    void intoVariables() {
        hotbarSpeed = (float) root.get("Hotbar").get("Smoothness").getValue();
        hotbarRollover = (boolean) root.get("Hotbar").get("Rollover").getValue();

        chatSpeed = (float) root.get("Chat").get("Smoothness").getValue();
        chatOpeningSpeed = (float) root.get("Chat").get("Opening Speed").getValue();

        creativeScreenSpeed = (float) root.get("Creative Screen").get("Smoothness").getValue();

        entryListSpeed = (float) root.get("Entry List").get("Smoothness").getValue();

        enableMaskDebug = (boolean) root.get("Misc").get("Enable mask debug").getValue();
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
