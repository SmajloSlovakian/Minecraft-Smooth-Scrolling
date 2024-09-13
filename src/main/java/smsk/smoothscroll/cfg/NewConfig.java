package smsk.smoothscroll.cfg;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.fabricmc.loader.api.FabricLoader;
import smsk.smoothscroll.SmoothSc;

public class NewConfig {
    private Gson gs = new GsonBuilder().setPrettyPrinting().create();
    private JsonObject defaultCfg;

    String fileName;
    JsonObject jsonConfig;

    public NewConfig(String file, JsonObject defaultConfig) {
        fileName = file;
        defaultCfg = defaultConfig;
        loadAndSave();
    }

    public void loadAndSave() {
        var dontSave = fromFile();
        try {
            dataCorrectPermanent();
            if (!dontSave) toFile();
            intoVariables();
            dataCorrectTemporary();
        } catch (Exception e) {
            problemReading();
            e.printStackTrace();
        }
    }

    public void modify(JsonObject modifier) {
        fromFile();
        dataCorrectPermanent();
        mergeJson(jsonConfig, modifier);
        toFile();
        intoVariables();
        dataCorrectTemporary();
    }

    public JsonObject getCopy() {
        return jsonConfig.deepCopy();
    }

    boolean fromFile() {
        jsonConfig = defaultCfg;
        File cfgfile = FabricLoader.getInstance().getConfigDir().resolve(fileName).toFile();
        Scanner scnr = null;
        boolean dontSave = false;
        if (cfgfile.exists()) {
            try {
                scnr = new Scanner(cfgfile);
                scnr.useDelimiter("\\Z");
                String data = scnr.next();

                var jsonData = gs.fromJson(data, JsonObject.class);
                mergeJson(jsonConfig, jsonData);
                
            } catch (FileNotFoundException e) {
                fileNotFound();
                e.printStackTrace();
            } catch (JsonSyntaxException e) {
                problemReading();
                e.printStackTrace();
                dontSave = true;
            }
            if (scnr != null) scnr.close();
        } else {
            fileNotFound();
        }
        return dontSave;
    }

    void toFile() {
        File cfgfile = FabricLoader.getInstance().getConfigDir().resolve(fileName).toFile();
        FileWriter fw = null;
        try {
            fw = new FileWriter(cfgfile);
            fw.write(gs.toJson(jsonConfig));
            SmoothSc.print("WRITING:\n"+gs.toJson(jsonConfig));
        } catch (Exception e) {
            problemWriting();
            e.printStackTrace();
        }
        try {
            if (fw != null) fw.close();
        } catch (IOException e) {}
    }
    void mergeJson(JsonObject to, JsonObject from) {
        for (String key : from.keySet()) {
            to.add(key, from.get(key));
        }
    }
    
    void problemWriting() {
    }

    void problemReading() {
    }

    void fileNotFound() {
    }

    /**
     * Override this, so you can data correct (update config values from previous format etc.)
     * Variables aren't initiated yet. Modify jsonConfig.
     */
    void dataCorrectPermanent() {
    }

    /**
     * Override this, so you can modify the config temporarily (values changed don't get saved to the file)
     * Only change the variables.
     */
    void dataCorrectTemporary() {
    }

    /**
     * Override this so you can have your own variables and don't have to interact with JsonObject.
     */
    void intoVariables() {
    }
}
