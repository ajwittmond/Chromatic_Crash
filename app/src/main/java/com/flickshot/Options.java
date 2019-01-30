package com.flickshot;

import android.content.SharedPreferences;

/**
 * Created by Alex on 4/27/2015.
 */

public class Options {
    public double musicVolume=1;
    public double soundFXVolume=1;
    public double powerAssist;
    public boolean autoSignIn;

    private SharedPreferences file;

    public Options(){
        //TODO: load from file
    }

    public void read(SharedPreferences sp){
        musicVolume = sp.getFloat("musicVolume",1);
        soundFXVolume = sp.getFloat("soundFXVolume",1);
        powerAssist = sp.getFloat("powerAssist",1);
        autoSignIn = sp.getBoolean("autoSignIn",true);
        System.out.println(soundFXVolume+" "+musicVolume);
        file = sp;
    }

    public void write(){
        if(file!=null){
            SharedPreferences.Editor editor = file.edit();
            editor.putFloat("musicVolume",(float)musicVolume);
            editor.putFloat("soundFXVolume",(float)soundFXVolume);
            editor.putFloat("powerAssist",(float)powerAssist);
            editor.putBoolean("autoSignIn",autoSignIn);
            editor.commit();
        }
    }
}
