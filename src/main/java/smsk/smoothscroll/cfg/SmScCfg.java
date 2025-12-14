package smsk.smoothscroll.cfg;

import java.util.ArrayList;
import java.util.Arrays;

import net.minecraft.text.Text;
import smsk.smoothscroll.SmoothSc;

public class SmScCfg extends NewConfig {
    public final static float format = 2.6f;

    public static float hotbarSmoothness;
    public static boolean hotbarRollover;
    public static boolean staticSelector;

    public static float chatSmoothness;
    public static float chatOpeningSmoothness;
    public static float chatAmount;

    public static float suggestionWindowSmoothness;
    public static float suggestionWindowAmount;

    public static float creativeScreenSmoothness;

    public static float entryListSmoothness;
    public static double entryListAmount;

    public static float textSmoothness;
    public static float textAmount;
    public static float textMargin;
    public static boolean textCustomUpdate;

    public static boolean enableMaskDebug;

    static CfgValue template = new CfgValueBuilder("root", new ArrayList<CfgValue>(Arrays.asList(
        new CfgValueBuilder("Notes", new ArrayList<String>(Arrays.asList(
            "Smoothness values are in %. Scrolling speed values are in pixels.",
            "0 % means animation off (no smoothness) and bigger values mean slower animation speed (high smoothness).",
            "Press F3+T in a world to update the config.",
            "To access config ingame, use the mod modmenu."
        ))).translatable("smoothscroll.config.notes").build(),

        new CfgValueBuilder("Hotbar", new ArrayList<CfgValue>(Arrays.asList(
            new CfgValueBuilder("Smoothness", 20f).translatable("smoothscroll.config.hotbar.smoothness").minMax(0, 100).map(0.0, "smoothscroll.config.value.off").map(100.0, "smoothscroll.config.value.no_scrolling").step(1).format("smoothscroll.config.format.percent").build(),
            new CfgValueBuilder("Rollover", true).translatable("smoothscroll.config.hotbar.rollover").build(),
            new CfgValueBuilder("Static Selector", false).translatable("smoothscroll.config.hotbar.static_selector").tooltip(Text.translatable("smoothscroll.config.tooltip.hotbar_static_selector")).build()
        ))).translatable("smoothscroll.config.hotbar").build(),

        new CfgValueBuilder("Chat", new ArrayList<CfgValue>(Arrays.asList(
            new CfgValueBuilder("Smoothness", 50f).translatable("smoothscroll.config.chat.smoothness").minMax(0, 100).map(0.0, "smoothscroll.config.value.off").map(100.0, "smoothscroll.config.value.no_scrolling").step(1).format("smoothscroll.config.format.percent").build(),
            new CfgValueBuilder("Opening Smoothness", 50f).translatable("smoothscroll.config.chat.opening_smoothness").minMax(0, 100).map(0.0, "smoothscroll.config.value.off").map(100.0, "smoothscroll.config.value.no_scrolling").step(1).format("smoothscroll.config.format.percent").build(),
            new CfgValueBuilder("Scrolling Speed", 0f).translatable("smoothscroll.config.chat.scrolling_speed").minMax(0, 100).step(1).map(0.0, "smoothscroll.config.value.auto").format("smoothscroll.config.format.pixels").build(),
            new CfgValueBuilder("Suggestion Smoothness", 50f).translatable("smoothscroll.config.chat.suggestion_smoothness").minMax(0, 100).map(0.0, "smoothscroll.config.value.off").map(100.0, "smoothscroll.config.value.no_scrolling").step(1).format("smoothscroll.config.format.percent").build(),
            new CfgValueBuilder("Suggestion Speed", 30f).translatable("smoothscroll.config.chat.suggestion_speed").minMax(0, 100).step(1).format("smoothscroll.config.format.pixels").map(0.0, "smoothscroll.config.value.auto").build()
        ))).translatable("smoothscroll.config.chat").build(),

        new CfgValueBuilder("Creative Screen", new ArrayList<CfgValue>(Arrays.asList(
            new CfgValueBuilder("Smoothness", 50f).translatable("smoothscroll.config.creative_screen.smoothness").minMax(0, 100).map(0.0, "smoothscroll.config.value.off").map(100.0, "smoothscroll.config.value.no_scrolling").step(1).format("smoothscroll.config.format.percent").build()
        ))).translatable("smoothscroll.config.creative_screen").build(),

        new CfgValueBuilder("Entry List", new ArrayList<CfgValue>(Arrays.asList(
            new CfgValueBuilder("Smoothness", 50f).translatable("smoothscroll.config.entry_list.smoothness").minMax(0, 100).map(0.0, "smoothscroll.config.value.off").map(100.0, "smoothscroll.config.value.no_scrolling").step(1).format("smoothscroll.config.format.percent").build(),
            new CfgValueBuilder("Speed", 30f).translatable("smoothscroll.config.entry_list.speed").minMax(0, 100).step(1).format("smoothscroll.config.format.pixels").map(0.0, "smoothscroll.config.value.auto").build()
        ))).translatable("smoothscroll.config.entry_list").build(),

        new CfgValueBuilder("Text Input Field", new ArrayList<CfgValue>(Arrays.asList(
            new CfgValueBuilder("Smoothness", 50f).translatable("smoothscroll.config.text_input.smoothness").minMax(0, 100).map(0.0, "smoothscroll.config.value.off").map(100.0, "smoothscroll.config.value.no_scrolling").step(1).format("smoothscroll.config.format.percent").build(),
            new CfgValueBuilder("Speed", 100f).translatable("smoothscroll.config.text_input.speed").minMax(0, 300).step(1).format("smoothscroll.config.format.pixels").map(0.0, "smoothscroll.config.value.auto").build(),
            new CfgValueBuilder("Cursor Margin", 10f).translatable("smoothscroll.config.text_input.cursor_margin").minMax(0, 100).step(1).format("smoothscroll.config.format.percent").disableWhen(val -> {return !(boolean)val.get("..").get("Custom Cursor Update").temporaryValue;}).build(),
            new CfgValueBuilder("Custom Cursor Update", true).translatable("smoothscroll.config.text_input.custom_cursor_update").tooltip(Text.translatable("smoothscroll.config.tooltip.custom_cursor_update")).build()
        ))).translatable("smoothscroll.config.text_input").build(),

        new CfgValueBuilder("Misc", new ArrayList<CfgValue>(Arrays.asList(
            new CfgValueBuilder("Enable mask debug", false).translatable("smoothscroll.config.misc.enable_mask_debug").build()
        ))).translatable("smoothscroll.config.misc").build(),

        new CfgValueBuilder("Format", format).translatable("smoothscroll.config.format").build()
    ))).build();

    public SmScCfg() {
        super("smoothscroll.json", template);
    }


    @Override
    void intoVariables() {
        hotbarSmoothness = (float) root.get("Hotbar").get("Smoothness").getValue() / 100f;
        hotbarRollover = (boolean) root.get("Hotbar").get("Rollover").getValue();
        staticSelector = (boolean) root.get("Hotbar").get("Static Selector").getValue();

        chatSmoothness = (float) root.get("Chat").get("Smoothness").getValue() / 100f;
        chatOpeningSmoothness = (float) root.get("Chat").get("Opening Smoothness").getValue() / 100f;
        chatAmount = (float) root.get("Chat").get("Scrolling Speed").getValue();
        suggestionWindowSmoothness = (float) root.get("Chat").get("Suggestion Smoothness").getValue() / 100f;
        suggestionWindowAmount = (float) root.get("Chat").get("Suggestion Speed").getValue();

        creativeScreenSmoothness = (float) root.get("Creative Screen").get("Smoothness").getValue() / 100f;

        entryListSmoothness = (float) root.get("Entry List").get("Smoothness").getValue() / 100f;
        entryListAmount = (float) root.get("Entry List").get("Speed").getValue();

        textSmoothness = (float) root.get("Text Input Field").get("Smoothness").getValue() / 100f;
        textAmount = (float) root.get("Text Input Field").get("Speed").getValue();
        textMargin = (float) root.get("Text Input Field").get("Cursor Margin").getValue();
        textCustomUpdate = (boolean) root.get("Text Input Field").get("Custom Cursor Update").getValue();

        enableMaskDebug = (boolean) root.get("Misc").get("Enable mask debug").getValue();
    }

    // TODO probably would be better to have a getOrMake("") and perform everything in rawRoot and then merge it
    @Override
    void dataCorrectPermanent() {
        if (rawRoot.get("cfgVersion").exists() && rawRoot.get("cfgVersion").getValue() instanceof Float cfgver) {
            SmoothSc.print("Found old format entries in the config file, attempting to update them.");
            
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
                root.get("Chat").get("Opening Smoothness").setValue(a.getValue());
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
        // the corrections can try to be performed when the values aren't set,
        // so every correction should be done from rawRoot and inside the try block
        // to not perform corrections on new default values
        // this could also be done with if blocks but i didn't want to be more
        // verbose than i already am

        var prevFormat = (float) root.get("Format").getValue();
        if (prevFormat < 2.4) {
            var merged = root.get("Hotbar");
            var raw = rawRoot.get("Hotbar");
            try {merged.get("Smoothness").setValue((float) raw.get("Smoothness").getValue() * 100);} catch (Exception e) {}
        
            merged = root.get("Chat");
            raw = rawRoot.get("Chat");
            try {merged.get("Smoothness").setValue((float) raw.get("Smoothness").getValue() * 100);} catch (Exception e) {}
            try {merged.get("Opening Smoothness").setValue((float) raw.get("Opening Speed").getValue() * 100);} catch (Exception e) {}
            try {merged.get("Suggestion Smoothness").setValue((float) raw.get("Suggestion Smoothness").getValue() * 100);} catch (Exception e) {}
        
            merged = root.get("Creative Screen");
            raw = rawRoot.get("Creative Screen");
            try {merged.get("Smoothness").setValue((float) raw.get("Smoothness").getValue() * 100);} catch (Exception e) {}

            merged = root.get("Entry List");
            raw = rawRoot.get("Entry List");
            try {merged.get("Smoothness").setValue((float) raw.get("Smoothness").getValue() * 100);} catch (Exception e) {}
        }

        if (prevFormat < 2.41) {
            // previously if you updated and didn't have suggestion
            // smoothness set (possible without tinkering),
            // it would data correct the new default value
            try {if ((float) root.get("Chat").get("Suggestion Smoothness").getValue() == 5000) {root.get("Chat").get("Suggestion Smoothness").setValue(50f);};} catch (Exception e) {}
        }


        // Notes and Format should always be up to date and not modified
        root.get("Notes").defaultToTemp();
        root.get("Notes").saveTempValue();
        root.get("Format").defaultToTemp();
        root.get("Format").saveTempValue();
        
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
