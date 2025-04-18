package smsk.smoothscroll.cfg;

import java.util.ArrayList;
import java.util.Arrays;

import smsk.smoothscroll.SmoothSc;

public class SmScCfg extends NewConfig {
    public final static float format = 2.2f;

    public static float hotbarSmoothness;
    public static float chatSmoothness;
    public static float chatOpeningSmoothness;
    public static float creativeScreenSmoothness;
    public static float entryListSmoothness;
    public static double entryListAmount;
    public static boolean enableMaskDebug;
    public static boolean hotbarRollover;

    static CfgValue template = new CfgValueBuilder("root", new ArrayList<CfgValue>(Arrays.asList(
        new CfgValueBuilder("Notes", new ArrayList<String>(Arrays.asList(
            "Safe values for settings are 0 - 1 (inclusive).",
            "0 means animation off (no smoothness) and bigger values mean slower animation speed (high smoothness).",
            "Press F3+T in a world to update the config.",
            "To access config ingame, use the mod modmenu."
        ))).build(),
        new CfgValueBuilder("Hotbar", new ArrayList<CfgValue>(Arrays.asList(
            new CfgValueBuilder("Smoothness", 0.2f).minMax(0, 1).map(0.0, "Off").map(1.0, "No Scrolling").build(),
            new CfgValueBuilder("Rollover", true).build()
        ))).build(),
        new CfgValueBuilder("Chat", new ArrayList<CfgValue>(Arrays.asList(
            new CfgValueBuilder("Smoothness", 0.5f).minMax(0, 1).map(0.0, "Off").map(1.0, "No Scrolling").build(),
            new CfgValueBuilder("Opening Speed", 0.5f).minMax(0, 1).map(0.0, "Off").map(1.0, "No Scrolling").build()
        ))).build(),
        new CfgValueBuilder("Creative Screen", new ArrayList<CfgValue>(Arrays.asList(
            new CfgValueBuilder("Smoothness", 0.5f).minMax(0, 1).map(0.0, "Off").map(1.0, "No Scrolling").build()
        ))).build(),
        new CfgValueBuilder("Entry List", new ArrayList<CfgValue>(Arrays.asList(
            new CfgValueBuilder("Smoothness", 0.5f).minMax(0, 1).map(0.0, "Off").map(1.0, "No Scrolling").build(),
            new CfgValueBuilder("Speed", 30.0f).minMax(0, 100).step(1).format("%s: %s px").map(0.0, "Auto").build()
        ))).build(),
        new CfgValueBuilder("Misc", new ArrayList<CfgValue>(Arrays.asList(
            new CfgValueBuilder("Enable mask debug", false).build()
        ))).build(),
        new CfgValueBuilder("Format", format).build()
    ))).build();

    public SmScCfg() {
        super("smoothscroll.json", template);
    }


    @Override
    void intoVariables() {
        hotbarSmoothness = (float) root.get("Hotbar").get("Smoothness").getValue();
        hotbarRollover = (boolean) root.get("Hotbar").get("Rollover").getValue();

        chatSmoothness = (float) root.get("Chat").get("Smoothness").getValue();
        chatOpeningSmoothness = (float) root.get("Chat").get("Opening Speed").getValue();

        creativeScreenSmoothness = (float) root.get("Creative Screen").get("Smoothness").getValue();

        entryListSmoothness = (float) root.get("Entry List").get("Smoothness").getValue();
        entryListAmount = (float) root.get("Entry List").get("Speed").getValue();

        enableMaskDebug = (boolean) root.get("Misc").get("Enable mask debug").getValue();
    }


    @Override
    void dataCorrectPermanent() {
        if (rawRoot.get("cfgVersion").exists() && rawRoot.get("cfgVersion").getValue() instanceof Float) {
            SmoothSc.print("Found old format entries in the config file, attempting to update them.");
            var cfgver = (float) rawRoot.get("cfgVersion").getValue();
            
            var a = rawRoot.get("hotbarSpeed");
            if (a.exists() && a.getValue() instanceof Float) {
                SmoothSc.print(a.getValue());
                if (cfgver < 1.6f && (float) a.getValue() >= 1) a.setValue(1 / (float) a.getValue());
                root.get("Hotbar").get("Smoothness").setValue(a.getValue());
            }
            a = rawRoot.get("chatSpeed");
            if (a.exists() && a.getValue() instanceof Float) {
                if (cfgver < 1.6f && (float) a.getValue() >= 1) a.setValue(1 / (float) a.getValue());
                root.get("Chat").get("Smoothness").setValue(a.getValue());
            }
            a = rawRoot.get("chatOpeningSpeed");
            if (a.exists() && a.getValue() instanceof Float) {
                root.get("Chat").get("Opening Speed").setValue(a.getValue());
            }
            a = rawRoot.get("creativeScreenSpeed");
            if (a.exists() && a.getValue() instanceof Float) {
                if (cfgver < 1.6f && (float) a.getValue() >= 1) a.setValue(1 / (float) a.getValue());
                root.get("Creative Screen").get("Smoothness").setValue(a.getValue());
            }
            a = rawRoot.get("entryListSpeed");
            if (a.exists() && a.getValue() instanceof Float) {
                if (cfgver < 1.6f && (float) a.getValue() >= 1) a.setValue(1 / (float) a.getValue());
                if (cfgver < 1.9f && (float) a.getValue() == 0.334f) a.setValue(0.5f);
                root.get("Entry List").get("Smoothness").setValue(a.getValue());
            }
            a = rawRoot.get("enableMaskDebug");
            if (a.exists() && a.getValue() instanceof Float) {
                root.get("Chat").get("Smoothness").setValue(a.getValue());
            }
        }
        // New file format corrections go here

        // Notes and Format should always be up to date and not modified
        root.get("Notes").defaultToTemp();
        root.get("Format").defaultToTemp();
        
    }
    @Override
    void dataCorrectTemporary() {
        // Disable entry list smooth scrolling if the mod smooth scrolling refurbished is present
        if (SmoothSc.isSmoothScrollingRefurbishedLoaded) {
            entryListSmoothness = 0;
        }
    }

    @Override
    void problemReading() {
        super.problemReading();
        SmoothSc.print("There was a problem reading the config file. Using default values.");
    }
    @Override
    void problemWriting() {
        super.problemWriting();
        SmoothSc.print("There was a problem writing to the config file.");
    }
    @Override
    void fileNotFound() {
        super.fileNotFound();
        SmoothSc.print("There is no config file, creating a new one.");
    }
}
