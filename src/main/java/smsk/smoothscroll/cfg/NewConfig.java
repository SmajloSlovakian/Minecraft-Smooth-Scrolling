package smsk.smoothscroll.cfg;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import net.fabricmc.loader.api.FabricLoader;

public class NewConfig {
    private final Gson gs = new GsonBuilder().setPrettyPrinting().create();

    String fileName;
    CfgValue root;
    CfgValue rawRoot = CfgValue.parseJson("root",null);

    public NewConfig(String file, CfgValue template) {
        fileName = file;
        root = template;
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
    public void save() {
        toFile();
        intoVariables();
        dataCorrectTemporary();
    }

    boolean fromFile() {
        File cfgfile = FabricLoader.getInstance().getConfigDir().resolve(fileName).toFile();
        Scanner scnr = null;
        boolean dontSave = false;
        if (cfgfile.exists()) {
            try {
                scnr = new Scanner(cfgfile);
                scnr.useDelimiter("\\Z");
                String data = scnr.next();

                var jsonData = gs.fromJson(data, JsonObject.class);
                rawRoot = CfgValue.parseJson("root",jsonData);
                root.matchValues(rawRoot);
                
            } catch (FileNotFoundException e) {
                fileNotFound();
                e.printStackTrace();
            } catch (Exception e) {
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
            fw.write(gs.toJson(CfgValue.exportJson(root)));
        } catch (Exception e) {
            problemWriting();
            e.printStackTrace();
        }
        try {
            if (fw != null) fw.close();
        } catch (IOException e) {}
    }

    public CfgValue getConfigForModifying() {
        return root;
    }

    void problemWriting() {
    }

    void problemReading() {
    }

    void fileNotFound() {
        toFile();
    }

    /**
     * Data corrects config values (updates config values from previous format, etc.)
     * Called before intoVariables()
     */
    void dataCorrectPermanent() {
    }

    /**
     * Inserts config values into variables
     */
    void intoVariables() {
    }

    /**
     * Modifies the config temporarily (values changed don't get saved to the file)
     * Called after intoVariables()
     */
    void dataCorrectTemporary() {
    }
}
