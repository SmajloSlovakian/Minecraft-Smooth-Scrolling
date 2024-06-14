package smsk.smoothscroll;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Serializable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import net.fabricmc.loader.api.FabricLoader;

public class Config {
    public static Cdata cfg;
    public static float cfgVersion = 2.1f;
    public static boolean problemReading = false;

    public Config() {
        File cfgfile = FabricLoader.getInstance().getConfigDir().resolve("smoothscroll.json").toFile();
        if (cfgfile.exists()) {
            cfg = readFile(cfgfile);
            /*try {
                SmoothSc.print(NbtHelper.toFormattedString(NbtHelper.fromNbtProviderString("{lala:987,popo:{oink:220}}")));
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }/* */
            if (cfg == null) {
                problemReading = true;
                SmoothSc.print("There was a problem reading the config file, using the default values.");
                cfg = new Cdata();
            }
        } else {
            SmoothSc.print("Config file not found, making a new one.");
            cfg = new Cdata();
            writeFile(cfgfile);
        }

        if (!problemReading) { // config updating system
            if (cfg.cfgVersion < cfgVersion)
                SmoothSc.print("Config values before updating:\n" + printify());
            if (cfg.cfgVersion < 1.6f) { // speeds before this version were divisors and not multipliers
                if (cfg.hotbarSpeed != 0.2f) {
                    if (cfg.hotbarSpeed != 0) cfg.hotbarSpeed = 1 / cfg.hotbarSpeed;
                    if (cfg.chatSpeed != 0) cfg.chatSpeed = 1 / cfg.chatSpeed;
                    if (cfg.creativeScreenSpeed != 0) cfg.creativeScreenSpeed = 1 / cfg.creativeScreenSpeed;
                    if (cfg.entryListSpeed != 0) cfg.entryListSpeed = 1 / cfg.entryListSpeed;
                }
            }
            if (cfg.cfgVersion < 1.9f) {
                if (cfg.entryListSpeed == 0.334f) cfg.entryListSpeed = 0.5f;
            }
            if (cfg.cfgVersion < 1.91f) {
                cfg.chatOpeningSpeed = cfg.chatSpeed;
            }
            cfg.cfgVersion = cfgVersion;
            cfg.note = "Safe values for settings are 0 - 1 (inclusive). 0 means animation off (infinite speed) and bigger values mean slower speed (up to 1). Press F3+T in a world to update config.";
            writeFile(cfgfile);
        }
        SmoothSc.print("Config values:\n" + printify());
    }

    Cdata readFile(File f) {
        FileReader fr = null;
        Cdata ret = null;
        try {
            var gson = new Gson();
            fr = new FileReader(f);
            ret = gson.fromJson(fr, Cdata.class);
        } catch (Exception e) {}
        try {
            fr.close();
        } catch (Exception e) {}

        return (ret);
    }

    void writeFile(File f) {
        FileWriter fw = null;
        try {
            var gson = new GsonBuilder().setPrettyPrinting().create();
            fw = new FileWriter(f);
            fw.write(gson.toJson(cfg));
        } catch (Exception e) {}
        try {
            fw.close();
        } catch (Exception e) {}
    }

    String printify() {
        return ("Hotbar speed: " + cfg.hotbarSpeed +
                "\nChat speed: " + cfg.chatSpeed +
                "\nChat Opening speed: " + cfg.chatOpeningSpeed +
                "\nCreative screen speed: " + cfg.creativeScreenSpeed +
                "\nEntry list speed: " + cfg.entryListSpeed +
                "\nMask debug enabled: " + cfg.enableMaskDebug +
                "\nConfig version: " + cfg.cfgVersion);
    }

    public class Cdata implements Serializable {
        @Expose
        public String note = "";
        @Expose
        public float hotbarSpeed = 0.2f;
        @Expose
        public float chatSpeed = 0.5f;
        @Expose
        public float chatOpeningSpeed = 0.5f;
        @Expose
        public float creativeScreenSpeed = 0.5f;
        @Expose
        public float entryListSpeed = 0.5f;
        @Expose
        public boolean enableMaskDebug = false;
        @Expose
        public float cfgVersion = 0;
    }
}
