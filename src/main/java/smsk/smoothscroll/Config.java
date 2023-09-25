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
    public Config(){
        File cfgfile=FabricLoader.getInstance().getConfigDir().resolve("smoothscroll.json").toFile();
        if(cfgfile.exists()){
            cfg=readFile(cfgfile);
            if(cfg==null){
                SmoothSc.print("There was a problem reading the config file, using the default values.");
                cfg=new Cdata();
            }
        }else{
            SmoothSc.print("Config file not found, making a new one.");
            cfg=new Cdata();
            writeFile(cfgfile);
        }

        if(cfg.chatSpeed==0){
        }else if(cfg.chatSpeed<1)SmoothSc.print("Safe values for chatSpeed are >1");

        if(cfg.hotbarSpeed==0){
        }else if(cfg.hotbarSpeed<1)SmoothSc.print("Safe values for hotbarSpeed are >1");

        if(cfg.creativeScreenSpeed==0){
        }else if(cfg.creativeScreenSpeed<1)SmoothSc.print("Safe values for creativeScreenSpeed are >1");

        if(cfg.entryListSpeed==0){
        }else if(cfg.entryListSpeed<1)SmoothSc.print("Safe values for entryListSpeed are >1");

    }
    Cdata readFile(File f){
        FileReader fr=null;
        Cdata ret=null;
        try{
            var gson=new Gson();
            fr=new FileReader(f);
            ret=gson.fromJson(fr, Cdata.class);
        } catch (Exception e) {}
        try {
            fr.close();
        } catch (Exception e) {}

        return(ret);
    }
    void writeFile(File f){
        FileWriter fw=null;
        try{
            var gson=new GsonBuilder().setPrettyPrinting().create();
            fw=new FileWriter(f);
            fw.write(gson.toJson(cfg));
        }catch(Exception e){}
        try{
            fw.close();
        }catch(Exception e){}
    }
    public class Cdata implements Serializable{
        @Expose
        public float hotbarSpeed=5;
        @Expose
        public float chatSpeed=2;
        @Expose
        public float creativeScreenSpeed=3;
        @Expose
        public float entryListSpeed=3;
    }
}
