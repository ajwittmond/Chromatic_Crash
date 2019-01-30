package com.flickshot.components.entities.defs.managers;

import com.flickshot.assets.AssetLibrary;
import com.flickshot.assets.music.Song;
import com.flickshot.assets.sfx.Sound;
import com.flickshot.components.entities.CommonEntity;
import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.Entity;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.entities.InfiniteModeWaveFactory;
import com.flickshot.components.entities.defs.PuckState;
import com.flickshot.components.entities.defs.fx.TextBlob;
import com.flickshot.components.entities.defs.gui.Bar;
import com.flickshot.components.entities.defs.overlays.EndOverlay;
import com.flickshot.components.graphics.Graphics;
import com.flickshot.components.graphics.Screen;
import com.flickshot.components.physics.Physics;
import com.flickshot.components.timeline.Timelines;
import com.flickshot.config.Config;
import com.flickshot.scene.Scene;
import com.flickshot.scene.Updater;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Alex on 4/4/2015.
 */
public class InfiniteModeManager extends CommonEntity {
    public static String ENTITY_NAME = "InfiniteModeManager";


    Bar health;
    ScoreTracker scoreTracker;

    boolean first;

    private int wave;
    private boolean bonus;

    public Song levelSong;
    Sound outOfTime = new Sound("air_horn");

    private double drain = 3;

    double deathTimer;
    double endTimer;

    private double bwidth;

    public double time;

    public void init(double x, double y){
        super.init(x,y);
        first=true;
        deathTimer=2;
        endTimer=3;
        Entity scores = Entities.getEntity("scoreTracker");
        if(scores.numberOfInstances()>0){
            scoreTracker = (ScoreTracker)scores.getState(0);
        }else{
            scoreTracker = (ScoreTracker)scores.newInstance(0,0);
        }
        scoreTracker.setScore(0);
        levelSong=null;

        wave = 0;

        time = 0;

        if(!AssetLibrary.has("sound", "trill_2"))
            AssetLibrary.loadAsset("sound","trill_2");
        if(!AssetLibrary.has("sound", "trill_3"))
            AssetLibrary.loadAsset("sound","trill_3");
        if(!AssetLibrary.has("sound", "trill_1"))
            AssetLibrary.loadAsset("sound","trill_1");
    }

    public void update(Updater.UpdateEvent evt){
        if(first){
            levelSong.setVolume(1,1);
            levelSong.play();
            Screen s = Graphics.getCurrentScene().screen;
            com.flickshot.components.physics.Scene ps = Physics.getScene(0);

            Entity bar = Entities.getEntity(Bar.class);

            bwidth = (s.getWidth()-ps.width)/4;

            health = (Bar)bar.newInstance(0,0);
            health.setPosition(ps.x+ps.width+(bwidth/2),ps.y);
            health.setDimensions(bwidth,ps.height);
            health.setColor(0,1,0,1);
            health.setBorderColor(0,0.6f,0,1);
            health.drawBorder = true;
            health.borderWidth=8;
            health.setValue(1);

            newWave();

            first = false;
        }else{
            health.setDimensions(bwidth, Physics.getScene(0).height);
            PuckState puck = (PuckState)Entities.getEntity(PuckState.class).getState(0);
            if(puck!=null) {
                time += evt.getDelta();
                health.setValue((double) puck.getHealth() / (double) PuckState.START_HEALTH);
                float v = (float) health.getValue();
                health.setColor(1 * (1 - v), v, 0, 1);
                health.setBorderColor(0.6f * (1 - v), v * 0.6f, 0, 1);
                puck.setHealth(puck.getHealth()-(drain*evt.getDelta()));
            }else{
                if(levelSong.isPlaying())
                    levelSong.setVolume(0.5f,0.5f);
                health.setValue(0);
                deathTimer-=evt.getDelta();
                if(deathTimer<=0){
                    if(Entities.getStateCount(EndOverlay.class)<=0)
                        Entities.newInstance(EndOverlay.class,0,0);
                }
            }
        }
    }

    public String getWave(){
        if(bonus)
            return "BONUS";
        else
            return wave+"";
    }

    public int getHighScore(){
        return 1000000;
    }

    public void configure(Config config){
        InfiniteModeConfig c = (InfiniteModeConfig)config;
        if(!AssetLibrary.has("music",c.levelMusic)){
            AssetLibrary.loadAsset("music",c.levelMusic);
        }
        levelSong = (Song)AssetLibrary.get("music",c.levelMusic);
        scoreTracker.configure(c.scoreConfig);
    }

    public void newWave(){
        Screen screen = Graphics.getCurrentScene().screen;
        if(wave!=0 && !bonus && wave%5==0){
            InfiniteModeWaveFactory.generateBonusWave(Timelines.get("default"), wave);
            TextBlob.create("BONUS!", screen.getCX(), screen.getCY(),512,1024,1,1,1,1);
            bonus = true;
        }else {
            bonus = false;
            wave++;
            InfiniteModeWaveFactory.generateWave(Timelines.get("default"), wave);
            TextBlob.create("Wave " + wave, screen.getCX(), screen.getCY(),512,1024,1,1,1,1);
        }
    }

    public static final EntityStateFactory getFactory(){
        return new Factory();
    }

    public static class Factory extends EntityStateFactory{
        @Override
        public EntityState construct() {
            return new InfiniteModeManager();
        }

        @Override
        public Class<? extends EntityState> getType() {
            return InfiniteModeManager.class;
        }

        @Override
        public Config getConfig(){
            return new InfiniteModeConfig();
        }

        @Override
        public void getAssets(ArrayList<String[]> assets){
            assets.add(new String[]{"texture","google_play_button"});
        }
    }

    public static final class InfiniteModeConfig extends Config{
        public String levelMusic = "citric_wedge";
        public ScoreConfig scoreConfig = new ScoreConfig();

        public void TimeAttackConfig(){}

        @Override
        public void setValue(String text) {
        }

        @Override
        public void getAliases(HashMap<String, String> map) {
        }

    }
}
