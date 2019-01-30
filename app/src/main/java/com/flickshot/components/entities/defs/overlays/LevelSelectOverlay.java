package com.flickshot.components.entities.defs.overlays;

import com.flickshot.FlickShot;
import com.flickshot.GameData;
import com.flickshot.assets.sfx.Sound;
import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.Entity;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.entities.defs.VisibleEntity;
import com.flickshot.components.entities.defs.enemies.Enemy;
import com.flickshot.components.entities.defs.fx.Transition;
import com.flickshot.components.graphics.Artist;
import com.flickshot.components.graphics.Graphics;
import com.flickshot.components.graphics.Renderer2d;
import com.flickshot.components.graphics.Screen;
import com.flickshot.components.graphics.Sprite;
import com.flickshot.components.input.TouchEvent;
import com.flickshot.components.input.TouchListener;
import com.flickshot.components.input.TouchManager;
import com.flickshot.config.Config;
import com.flickshot.geometry.Square;
import com.flickshot.geometry.collision.CollisionLib;
import com.flickshot.scene.Scene;
import com.flickshot.scene.Updater;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Alex on 3/20/2015.
 */
public class LevelSelectOverlay extends VisibleEntity {
    public static final String ENTITY_NAME = "LevelSelectOverlay";

    final double sratio = 2.5/4.0;


    Group[] groups;
    int group;

    Level selectedLevel;

    TouchListener tl;

    final double scaleTime = 0.1;
    double dt;

    boolean scaleOut;
    boolean scaleIn;

    int nextGroup;

    private GameData levelData;

    Sprite pinkSpinner = new Sprite("pink_spinner");

    Sprite leftArrow = new Sprite("arrow");
    Sprite rightArrow = new Sprite("arrow");

    Sound click = new Sound("menu_click");

    FlickShot.HardwareButtonListener hb = new FlickShot.HardwareButtonListener() {
        @Override
        public void onBack() {
            if(selectedLevel!=null){
                selectedLevel.scaleOut=true;
            }else{
                Scene.newScene("title_scene");
            }
        }

        @Override
        public void onHome(){
        }

        @Override
        public void onMenu(){
        }
    };

    public LevelSelectOverlay(){
        setArtist(new Artist(){

            @Override
            public boolean isOnScreen(double screenX, double screenY, double screenWidth, double screenHeight) {
                return true;
            }

            @Override
            public void draw(double delta, Renderer2d r) {
                Screen s = Graphics.getCurrentScene().screen;
                pinkSpinner.draw(delta, r);

                r.setDrawMode(Renderer2d.FILL);
                double awidth = (((1.0-sratio)*s.getWidth())/2);
                double aheight = s.getHeight()*(3.0/4.0);

                if(group==groups.length-1){
                    rightArrow.setTint(0,0,0);
                    rightArrow.tintWeight = 0.8f;
                }else{
                    rightArrow.tintWeight = 0;
                }
                //draw arrows
                if(selectedLevel==null) {
//                    r.color(0, 0.5, 0.5);
//                    r.setShape(Renderer2d.TRIANGLE);
//                    r.push();
//                    r.translate(s.getX() + (awidth / 2), s.getCY(), 1);
//                    r.scale(awidth - 64, aheight);
//                    r.rotate(Math.PI / 2);
//                    r.shape();
//                    r.pop();
//                    r.push();
//                    r.translate(s.getX() + s.getWidth() - (awidth / 2), s.getCY(), 1);
//                    r.scale(awidth - 64, aheight);
//                    r.rotate(-Math.PI / 2);
//                    r.shape();
//                    r.pop();
                    leftArrow.draw(delta, r);
                    rightArrow.draw(delta, r);
                }

                groups[group].draw(delta,r);

                if(scaleIn){
                    dt+=delta;
                    if(dt>=scaleTime){
                        dt=scaleTime;
                        scaleIn = false;
                    }
                }else if(scaleOut){
                    dt -= delta;
                    if(dt<=0){
                        dt = 0;
                        group = nextGroup;
                        scaleIn = true;
                        scaleOut = false;
                    }
                }

            }
        });

        tl = new TouchListener() {
            @Override
            public void onDown(TouchEvent evt) {
                if(!(scaleOut || scaleIn)) {
                    Screen s = Graphics.getCurrentScene().screen;
                    double x = TouchManager.x();
                    double awidth = (((1.0 - sratio) * s.getWidth()) / 2);
                    if (selectedLevel == null) {
                        if (x < s.getX()+awidth) {
                            click.play();
                            if (group > 0) {
                                nextGroup = group - 1;
                                scaleOut = true;
                            } else {
                                Scene.newScene("title_scene");
                            }
                        } else if (x > s.getX() + s.getWidth() - awidth) {
                            if(group<groups.length-1) {
                                click.play();
                                nextGroup = group+1;
                                scaleOut = true;
                            }
                        } else {
                            groups[group].touch(x,TouchManager.y());
                        }
                    }else{
                        selectedLevel.touch(x, TouchManager.y());
                    }
                }
            }

            @Override
            public void onMove(TouchEvent evt) {

            }

            @Override
            public void onUp(TouchEvent evt) {

            }
        };
    }

    public void init(double x, double y){
        super.init(x,y);
        TouchManager.setScreen(Graphics.getCurrentScene().screen);
        TouchManager.add(tl);
        scaleIn = true;
        dt = 0;
        levelData = FlickShot.gameData;
        FlickShot.addListener(hb);
        System.out.println("ad");
        TitleMusic music = (TitleMusic)Entities.getEntity(TitleMusic.class).getState(0);
        if(!music.titleMusic.isPlaying()){
            music.titleMusic.play();
        }


        Screen s = Graphics.getCurrentScene().screen;
        double awidth = (((1.0-sratio)*s.getWidth())/2);

        leftArrow.setCX(s.getX()+(awidth/2));
        leftArrow.setCY(s.getCY());
        leftArrow.setBoxWidth(awidth - 64);
        leftArrow.setBoxHeight((awidth - 64) * 2);
        leftArrow.setTheta(Math.PI);

        rightArrow.setCX(s.getX() + s.getWidth() - (awidth / 2));
        rightArrow.setCY(s.getCY());
        rightArrow.setBoxWidth(awidth - 64);
        rightArrow.setBoxHeight((awidth - 64) * 2);
    }

    public void destroy(){
        super.destroy();
        TouchManager.remove(tl);
        FlickShot.removeListener(hb);
    }

    public void unload(){
        super.unload();
        TouchManager.remove(tl);
        FlickShot.removeListener(hb);
    }

    public void update(Updater.UpdateEvent evt){
        Screen screen = Graphics.getCurrentScene().screen;
        pinkSpinner.setBoxWidth(screen.getWidth()*2);
        pinkSpinner.setBoxHeight(screen.getWidth()*2);
        pinkSpinner.setCX(screen.getCX());
        pinkSpinner.setCY(screen.getCY());
        pinkSpinner.setTheta(pinkSpinner.getTheta()-((Math.PI/4.0)*evt.getDelta()));
        pinkSpinner.z=2;
    }

    @Override
    public void configure(Config c){
        LevelSelectConfig cfg = (LevelSelectConfig)c;
        int num = 0;
        groups = new Group[cfg.rows.length];
        for(int i = 0; i<cfg.rows.length; i++){
            groups[i] = new Group(cfg.rows[i],i,num);
            num+=cfg.rows[i].scenes.length;
        }
        FlickShot.gameData.checkAchievements();
    }

    private class Group{
        Level[] levels;
        String name;

        Sprite lock = new Sprite("lock");
        Sprite gold = new Sprite("gold");

        final int textHeight = 128;

        final int unlock;

        final int index;

        Group(GroupConfig config,int index,int num){
            this.index = index;
            name = config.name;
            levels = new Level[config.scenes.length];

            unlock = config.unlock;


            Screen screen = Graphics.getCurrentScene().screen;

            lock.setBoxWidth(screen.getHeight()/2);
            lock.setBoxHeight(screen.getHeight() / 2);

            gold.setBoxWidth(lock.getWidth()/4);
            gold.setBoxHeight(lock.getWidth() / 4);
            gold.setCX(-(screen.getHeight() / 4) + (lock.getWidth() / 4));
            gold.setCY(-(screen.getHeight() / 4) - (lock.getWidth() / 8));

            double r = (screen.getHeight()/4);
            double offset = (screen.getHeight()/4)+64;

            if(levels.length>1) {
                for (int i = 0; i < levels.length; i++) {
                    double theta = (Math.PI / 2) + (i * ((Math.PI * 2.0) / levels.length));
                    levels[i] = new Level(i, config.scenes[i].id, config.scenes[i].name, config.scenes[i].description,
                            screen.getCX() + (Math.cos(theta) * offset), screen.getCY() + (Math.sin(theta) * offset), r);
                    GameData.LevelData data = FlickShot.gameData.getLevelData(config.scenes[i].id);
                    data.index = num++;
                    data.displayName = config.scenes[i].name;
                    data.group = index;
                }
            }else{
                levels[0] = new Level(0,config.scenes[0].id,config.scenes[0].name,config.scenes[0].description,
                        screen.getCX(), screen.getCY(), r);
                GameData.LevelData data = FlickShot.gameData.getLevelData(config.scenes[0].id);
                data.index = num++;
                data.displayName = config.scenes[0].name;
                data.group = index;
            }
        }

        public void draw(double delta,Renderer2d r){
            if(isLocked()) {
                Screen screen = Graphics.getCurrentScene().screen;
                double radius = (screen.getHeight() / 4);
                double offset = (screen.getHeight() / 4) + 64;

                double w = (offset + radius) * 2;
                double u = dt / scaleTime;
                r.push();
                    r.color(0.5, 0.5, 0.5, 0.5);
                    r.translate(screen.getCX(), screen.getCY(), -15);
                    r.scale(u,u);
                    r.setDrawMode(Renderer2d.FILL);
                    r.shape(Renderer2d.ELLIPSE, w, w);

                    r.translate(0,0,-1);
                    lock.draw(delta, r);
                    gold.draw(delta, r);

                    r.translate(-48,-(screen.getHeight()/4)-(lock.getWidth()/8));
                    double scale = 90.0/r.textHeight();
                    r.scale(scale,scale);
                    r.color(1,0,0);
                    r.text(" = "+(FlickShot.gameData.getMedalCount()-unlock));
                r.pop();
            }
            for(int i = 0; i<levels.length; i++){
                levels[i].draw(delta,r);
            }
        }

        public void touch(double x, double y){
            for(Level l: levels){
               if(CollisionLib.pointCircle(x,y,l.cx,l.cy,l.radius)){
                   if(!l.isLocked()) {
                       click.play();
                       l.select();
                       selectedLevel = l;
                   }
                   break;
               }
            }
        }

        public boolean isLocked(){
            return unlock>FlickShot.gameData.getMedalCount();
        }
    }

    public static final EntityStateFactory getFactory(){
        return new Factory();
    }

    private class Level{
        Sprite playSprite = new Sprite("play_button");
        Sprite backSprite = new Sprite("cancel_button");
        Sprite leaderBoardsSprite = new Sprite("google_play_button");

        Sprite flowerRed = new Sprite("flower_red");
        Sprite flowerGreen = new Sprite("flower_green");
        Sprite flowerPink = new Sprite("flower_pink");

        Sprite background = new Sprite("menu_background");

        Sprite lock = new Sprite("lock");

        Sprite gold = new Sprite("gold");
        Sprite silver = new Sprite("silver");
        Sprite bronze = new Sprite("bronze");

        String name;
        String description;
        String id;
        int index;

        boolean selected = false;

        private final int textSize1 = 64;

        boolean scaleIn;
        boolean scaleOut;
        double ldt;

        Square window;
        Square play;
        Square back;

        GameData.LevelData data;

        double cx,cy;
        double radius;

        String[][] leaderBoardData = null;



        Level(int index, String id, String name, String description, double cx, double cy, double radius){
            this.index = index;
            this.name = name;
            this.id = id;
            this.description = description;
            this.cx=cx;
            this.cy=cy;
            this.radius=radius;

            Screen s = Graphics.getCurrentScene().screen;
            double w = s.getWidth();
            double h = s.getHeight();

            flowerGreen.setCX(0);
            flowerGreen.setCY(0);
            flowerGreen.setBoxWidth(radius*2.2);
            flowerGreen.setBoxHeight(radius*2.2);

            flowerRed.setCX(0);
            flowerRed.setCY(0);
            flowerRed.setBoxWidth(radius*2*(3.0/4.0));
            flowerRed.setBoxHeight(radius*2*(4.0/6.0));

            flowerPink.setCX(0);
            flowerPink.setCY(0);
            flowerPink.setBoxWidth(radius*2*(1.0/2.0));
            flowerPink.setBoxHeight(radius*2*(1.0/2.0));

            lock.setBoxWidth(radius/2);
            lock.setBoxHeight(radius/2);

            background.setCX(s.getCX()-cx);
            background.setCY(s.getCY() - cy);
            background.setBoxWidth(s.getHeight());
            background.setBoxHeight(s.getHeight());

            double bwidth = s.getHeight()/3;
            playSprite.setCX((s.getX()+s.getWidth()-(bwidth/2))-cx);
            playSprite.setCY((s.getY() + (bwidth / 2))-cy);
            playSprite.setBoxWidth(bwidth);
            playSprite.setBoxHeight(bwidth);

            backSprite.setCX((s.getX()+(bwidth/2))-cx);
            backSprite.setCY((s.getY()+(bwidth/2))-cy);
            backSprite.setBoxWidth(bwidth);
            backSprite.setBoxHeight(bwidth);

            leaderBoardsSprite.setCX((s.getX()+(bwidth/2))-cx);
            leaderBoardsSprite.setCY((s.getY()+s.getHeight()-(bwidth/2))-cy);
            leaderBoardsSprite.setBoxWidth(bwidth);
            leaderBoardsSprite.setBoxHeight(bwidth);

            GameData.LevelData lv = FlickShot.gameData.getLevelData(id);
            bronze.setCY(-cy + s.getY() + s.getHeight() / 4);
            bronze.setCX(-cx + s.getCX() - 128);
            bronze.setBoxWidth(90);
            bronze.setBoxHeight(90);
            bronze.setTint(0.5f,0,0.5f);
            bronze.tintWeight = (lv.medals>0)? 0 : 1;

            silver.setCY(-cy + s.getY() + s.getHeight() / 4);
            silver.setCX(-cx + s.getCX());
            silver.setBoxWidth(90);
            silver.setBoxHeight(90);
            silver.setTint(0.5f, 0, 0.5f);
            silver.tintWeight = (lv.medals>1)? 0 : 1;

            gold.setCY(-cy + s.getY() + s.getHeight() / 4);
            gold.setCX(-cx + s.getCX() + 128);
            gold.setBoxWidth(90);
            gold.setBoxHeight(90);
            gold.setTint(0.5f, 0, 0.5f);
            gold.tintWeight = (lv.medals>2)? 0 : 1;
        }


        public void select(){
            selected = true;
            scaleIn = true;
            ldt = 0;
            leaderBoardData = null;
            FlickShot.getLeaderboardScores(id,new FlickShot.LoadScoresCallback() {
                @Override
                public void onLoad(String[][] scores) {
                    leaderBoardData = scores;
                }
            },5);
        }

        public void draw(double delta,Renderer2d r){
            flowerGreen.setTheta(flowerGreen.getTheta()+(delta*Math.PI*0.5));
            flowerRed.setTheta(flowerRed.getTheta()-(delta*Math.PI*(0.75)));
            flowerPink.setTheta(flowerPink.getTheta()+(delta*Math.PI));

            Screen s = Graphics.getCurrentScene().screen;
            GameData.LevelData lv = FlickShot.gameData.getLevelData(id);

            double u = dt/scaleTime;
            if(selectedLevel==null || (selectedLevel.scaleIn || selectedLevel.scaleOut)) {
                r.push();
                    r.translate(cx, cy);
                    r.scale(u, u);
                    if (lv.medals >= 1) {
                        flowerGreen.draw(delta, r);
                    }else{
                        r.color(0.25,0.25,isLocked() ? 0.25 : 0);
                        r.shape(Renderer2d.ELLIPSE, radius*2, radius*2);
                    }
                    r.translate(0, 0, -1);
                    if (lv.medals >= 2) {
                        flowerRed.draw(delta, r);
                    }else{
                        r.color(0.5,0.5,isLocked() ? 0.5 : 0);
                        r.shape(Renderer2d.ELLIPSE, flowerRed.getBoxWidth(), flowerRed.getBoxWidth());
                    }
                    r.translate(0, 0, -1);
                    if (lv.medals >= 3) {
                        flowerPink.draw(delta, r);
                    }else{
                        r.color(0.75,0.75,isLocked() ? 0.75 : 0);
                        r.shape(Renderer2d.ELLIPSE, flowerPink.getBoxWidth(), flowerPink.getBoxWidth());
                    }
                    r.translate(0, 0, -1);
                    r.color(1, 1, isLocked() ? 1 : 0);
                    r.setDrawMode(Renderer2d.FILL);
                    r.shape(Renderer2d.ELLIPSE, radius / 2, radius / 2);
                    if(!isLocked()) {
                        double scale = 56 / r.textHeight();
                        r.scale(scale, scale);
                        r.align(Renderer2d.CENTER, Renderer2d.CENTER);
                        r.color(0, 0.3, 0.3);
                        r.text((index + 1) + "");
                    }else{
                        lock.draw(delta,r);
                    }
                r.pop();
            }

            if(selected){
                u = ldt/scaleTime;
                if(scaleIn){
                    ldt+=delta;
                    if(ldt>=scaleTime){
                        ldt = scaleTime;
                        scaleIn = false;
                    }
                }else if(scaleOut){
                    ldt-=delta;
                    if(ldt<=0){
                        ldt=0;
                        scaleOut = false;
                        selected=false;
                        selectedLevel=null;
                    }
                }



                r.push();
                    r.translate(cx, cy, -10);
                    r.scale(u, u);
                    background.draw(delta, r);
                    backSprite.draw(delta, r);
                    playSprite.draw(delta, r);
                    leaderBoardsSprite.draw(delta, r);
                    r.translate(0,0,-1);
                    bronze.draw(delta, r);
                    silver.draw(delta, r);
                    gold.draw(delta, r);
                    //draw title
                    r.push();
                        r.translate(s.getCX()-cx,(s.getCY()+s.getHeight()/4)-cy);
                        double scale = 90/r.textHeight();
                        r.scale(scale,scale);
                        r.align(Renderer2d.CENTER,Renderer2d.CENTER);
                        r.color(0.5,0,0.5);
                        r.text(name);
                    r.pop();
                    //draw info
                    scale = 60/r.textHeight();
                    r.push();
                        r.translate(s.getCX()-cx,(s.getCY()+60)-cy);
                        r.scale(scale,scale);
                        r.align(Renderer2d.LEFT,Renderer2d.CENTER);
                        r.color(0.5,0,0.5);
                        r.text("HighScore: "+lv.topScore);
                    r.pop();
                    r.push();
                        r.translate(s.getCX()-cx,s.getCY()-cy);
                        r.scale(scale,scale);
                        r.align(Renderer2d.LEFT,Renderer2d.CENTER);
                        r.color(0.5,0,0.5);
                        r.text("Highest Combo: "+lv.highestCombo);
                    r.pop();
                    r.push();
                        r.translate(s.getCX()-cx,(s.getCY()-60)-cy);
                        r.scale(scale,scale);
                        r.align(Renderer2d.LEFT,Renderer2d.CENTER);
                        r.color(0.5,0,0.5);
                        r.text("Best Time: "+String.format("%d:%04.2f",(int)(lv.minTime/60),(Math.round((lv.minTime%60)*100)/100.0)));
                    r.pop();
                    //draw leaderBoard border
                    r.push();
                        r.translate(s.getCX() - (s.getHeight() / 4) - cx, s.getCY() - cy);
                        r.color(0.5f, 0, 0.5f);
//                        r.push();
//                            //r.lineWidth(4);
////                            r.setDrawMode(Renderer2d.STROKE);
//                            r.shape(Renderer2d.SQUARE,0,0,(s.getHeight()/3),(s.getHeight()/3));
//
//                        r.pop();
//                        r.color(0,1,1);
                        if(FlickShot.googleApiClient == null || !FlickShot.googleApiClient.isConnected()){
                            r.align(Renderer2d.CENTER, Renderer2d.CENTER);
                            r.scale(48 / r.textHeight(), 48 / r.textHeight());
                            r.text("not connected");
                        }else if(leaderBoardData==null) {
                            r.align(Renderer2d.CENTER, Renderer2d.CENTER);
                            r.scale(48 / r.textHeight(), 48 / r.textHeight());
                            r.text("loading scores...");
                        }else{
                            double textHeight = 40;
                            scale = textHeight / r.textHeight();
                            r.translate(0,(s.getHeight()/8)-(s.getHeight()/40));
                            for(int i = 0; i<leaderBoardData.length; i++){
                                r.push();
                                    r.translate(-(s.getHeight()/5)+32,0);
                                    r.align(Renderer2d.LEFT,Renderer2d.CENTER);
                                    r.scale(scale,scale);
                                    String name = leaderBoardData[i][0];
                                    String score = leaderBoardData[i][1];
                                    if(
                                            r.textWidth(name)*scale >
                                            ((s.getHeight()/5)-32)+((s.getHeight()/4)-32)-(r.textWidth(score)*scale)){
                                        while(
                                                r.textWidth(name+"...")*scale >
                                                ((s.getHeight()/5)-32)+((s.getHeight()/4)-32)-(r.textWidth(score)*scale)-64){
                                            name = name.substring(0,name.length()-1);
                                        }
                                        name += "...";
                                    }
                                    r.text(name);
                                r.pop();
                                r.push();
                                    r.translate((s.getHeight()/4)-32,0);
                                    r.align(Renderer2d.RIGHT,Renderer2d.CENTER);
                                    r.scale(scale,scale);
                                    r.text(score);
                                r.pop();
                                r.translate(0,-s.getHeight()/20);
                            }
                        }
                    r.pop();
                r.pop();


            }

        }

        public boolean isLocked(){
            if(groups[group].isLocked()) {
                return true;
            }else if(index==0){
                return false;
            }else{
                GameData.LevelData ld = FlickShot.gameData.getLevelData(groups[group].levels[index-1].id);
                return !(ld.medals>0);
            }
        }

        public void touch(double x, double y){
            Screen s = Graphics.getCurrentScene().screen;
            double w = backSprite.getBoxWidth();
            if(CollisionLib.pointBox(x, y,s.getX(),s.getY(),w,w)){
                scaleOut = true;
                click.play();
            }else if(CollisionLib.pointBox(x, y, s.getX()+s.getWidth()-w,s.getY(),w,w)){
                dt = 0;
                selected = false;
                selectedLevel = null;
                click.play();
                ((TitleMusic)Entities.getEntity(TitleMusic.class).getState(0)).titleMusic.pause();
                //Scene.newScene(id);
                Transition t = (Transition)Entities.newInstance(Transition.class,0,0);
                t.target = id;
                //kill();
            }else if(CollisionLib.pointBox(x, y, s.getX(),s.getY()+s.getHeight()-w,w,w)){
                FlickShot.showLeaderboard(id);
                click.play();
            }
        }
    }

    public static class Factory extends EntityStateFactory {
        @Override
        public EntityState construct() {
            return new LevelSelectOverlay();
        }

        @Override
        public Class<? extends EntityState> getType() {
            return LevelSelectOverlay.class;
        }

        @Override
        public Config getConfig(){
            return new LevelSelectConfig();
        }

        @Override
        public void getAssets(ArrayList<String[]> assets){
            assets.add(new String[]{"texture","play_button"});
            assets.add(new String[]{"texture","cancel_button"});
            assets.add(new String[]{"texture","pink_spinner"});
            assets.add(new String[]{"texture","spinner"});
            assets.add(new String[]{"texture","flower_red"});
            assets.add(new String[]{"texture","flower_green"});
            assets.add(new String[]{"texture","flower_pink"});
            assets.add(new String[]{"texture","menu_background"});
            assets.add(new String[]{"texture","arrow"});
            assets.add(new String[]{"texture","lock"});
            assets.add(new String[]{"texture","gold"});
            assets.add(new String[]{"texture","silver"});
            assets.add(new String[]{"texture","bronze"});
            assets.add(new String[]{"texture","google_play_button"});
            assets.add(new String[]{"texture","puck"});
            assets.add(new String[]{"sound","menu_click"});
        }
    }

    public static final class LevelSelectConfig extends Config {
        public GroupConfig[] rows = new GroupConfig[0];

        @Override
        public void setValue(String text) {
        }

        @Override
        public void getAliases(HashMap<String, String> map) {
        }

    }

    public static final class GroupConfig extends Config{
        public SceneConfig[] scenes = new SceneConfig[0];
        public String name;
        public int unlock;

        @Override
        public void setValue(String text) {
        }

        @Override
        public void getAliases(HashMap<String, String> map) {
        }

    }

    public static final class SceneConfig extends Config{
        public String name;
        public String id;
        public String description;

        @Override
        public void setValue(String text) {
        }

        @Override
        public void getAliases(HashMap<String, String> map) {
        }

    }
}
