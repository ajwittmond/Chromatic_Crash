package com.flickshot.components.entities.defs.overlays;

import com.flickshot.FlickShot;
import com.flickshot.assets.sfx.Sound;
import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.entities.defs.VisibleEntity;
import com.flickshot.components.graphics.Artist;
import com.flickshot.components.graphics.Graphics;
import com.flickshot.components.graphics.Renderer2d;
import com.flickshot.components.graphics.Screen;
import com.flickshot.components.graphics.Sprite;
import com.flickshot.components.input.TouchEvent;
import com.flickshot.components.input.TouchListener;
import com.flickshot.components.input.TouchManager;
import com.flickshot.geometry.collision.CollisionLib;
import com.flickshot.scene.Scene;
import com.flickshot.scene.Updater;
import com.google.android.gms.games.Games;

import java.util.ArrayList;

/**
 * Created by Alex on 4/27/2015.
 */
public class TitleScreenOverlay extends VisibleEntity{
    public static final String ENTITY_NAME = "TitleScreenOverlay";

    Sprite spinner = new Sprite("spinner");
    Sprite pinkSpinner = new Sprite("pink_spinner");
    Sprite information = new Sprite("help_icon");
    Sprite options = new Sprite("options_icon");
    Sprite leaderBoards = new Sprite("leader_boards_icon");
    Sprite credits = new Sprite("information_icon");
    Sprite puck = new Sprite("puck");

    Sound click = new Sound("menu_click");

    FlickShot.HardwareButtonListener hb = new FlickShot.HardwareButtonListener() {
        @Override
        public void onBack() {
        }

        public void onHome(){
        }

        public void onMenu(){
        }
    };


    TouchListener tl = new TouchListener(){

        private boolean inSprite(Sprite s, double x, double y){
            return CollisionLib.pointBox(x,y,s.getX(),s.getY(),s.getBoxWidth(),s.getBoxHeight());
        }

        @Override
        public void onDown(TouchEvent evt) {
            double x = TouchManager.x();
            double y = TouchManager.y();
            click.play();
            if(inSprite(credits,x,y)){
                kill();
                Entities.newInstance(CreditsOverlay.class,0,0);
            }else if(inSprite(options,x,y)) {
                kill();
                Entities.newInstance(OptionsOverlay.class, 0, 0);
            }else if(inSprite(information,x,y)) {
                kill();
                Entities.newInstance(InstructionsOverlay.class, 0, 0);
            }else if(inSprite(leaderBoards,x,y)){
                FlickShot.showAchievments();
            }else{
                Scene.newScene("scene_select");
            }
        }

        @Override
        public void onMove(TouchEvent evt) {

        }

        @Override
        public void onUp(TouchEvent evt) {

        }
    };

    boolean first;

    public TitleScreenOverlay(){
        setArtist(new Artist(){

            @Override
            public boolean isOnScreen(double screenX, double screenY, double screenWidth, double screenHeight) {
                return true;
            }


            double t=0.8;
            boolean textVisable;

            @Override
            public void draw(double delta, Renderer2d r) {
                pinkSpinner.draw(delta,r);
                spinner.draw(delta, r);
                credits.draw(delta, r);
                information.draw(delta, r);
                options.draw(delta,r);
                leaderBoards.draw(delta, r);
                puck.draw(delta, r);

                t-=delta;
                if(t<=0){
                    if(textVisable){
                        t=0.2;
                    }else{
                        t=0.8;
                    }
                    textVisable = !textVisable;
                }
                if(textVisable) {
                    Screen screen = Graphics.getCurrentScene().screen;
                    r.translate(screen.getCX(), screen.getY() + (screen.getHeight()/4), -10);
                    r.align(Renderer2d.CENTER,Renderer2d.CENTER);
                    r.color(1,0.5,1);
                    double scale = 90/r.textHeight();
                    r.scale(scale,scale);
                    r.text("Tap To Begin");
                }
            }
        });
    }

    @Override
    public void init(double x, double y){
        super.init(x,y);
        first = true;
        TouchManager.add(tl);
        TouchManager.setScreen(Graphics.getCurrentScene().screen);
        TitleMusic music = (TitleMusic)Entities.getEntity(TitleMusic.class).getState(0);
        if(music==null){
            music = (TitleMusic)Entities.newInstance(TitleMusic.class,0,0);
        }
        music.titleMusic.play();
    }

    @Override
    public void update(Updater.UpdateEvent evt){
        if(first){
            FlickShot.addListener(hb);
            first = false;
        }
        super.update(evt);
        Screen screen = Graphics.getCurrentScene().screen;

        double iconSize = screen.getHeight()*(1/3.0);

        credits.setBoxWidth(iconSize);
        credits.setBoxHeight(iconSize);
        credits.setCX(screen.getX()+(iconSize/4)+(iconSize/2));
        credits.setCY(screen.getY()+(iconSize/4)+(iconSize/2));
        credits.z = -1;

        options.setBoxWidth(iconSize);
        options.setBoxHeight(iconSize);
        options.setCX(screen.getX()+(iconSize/4)+(iconSize/2));
        options.setCY(screen.getY()+screen.getHeight()-((iconSize/4)+(iconSize/2)));
        options.z = -1;

        leaderBoards.setBoxWidth(iconSize);
        leaderBoards.setBoxHeight(iconSize);
        leaderBoards.setCX(screen.getX()+screen.getWidth()-((iconSize/4)+(iconSize/2)));
        leaderBoards.setCY(screen.getY()+screen.getHeight()-((iconSize/4)+(iconSize/2)));
        leaderBoards.z = -1;

        information.setBoxWidth(iconSize);
        information.setBoxHeight(iconSize);
        information.setCX(screen.getX()+screen.getWidth()-((iconSize/4)+(iconSize/2)));
        information.setCY(screen.getY()+(iconSize/4)+(iconSize/2));
        information.z = -1;

        double spinnerSize = screen.getHeight();

        spinner.setBoxWidth(spinnerSize);
        spinner.setBoxHeight(spinnerSize);
        spinner.setCX(screen.getCX());
        spinner.setCY(screen.getCY());
        spinner.setTheta(spinner.getTheta()+(Math.PI/2.0)*evt.getDelta());

        pinkSpinner.setBoxWidth(screen.getWidth()*2);
        pinkSpinner.setBoxHeight(screen.getWidth()*2);
        pinkSpinner.setCX(screen.getCX());
        pinkSpinner.setCY(screen.getCY());
        pinkSpinner.setTheta(pinkSpinner.getTheta()-((Math.PI/4.0)*evt.getDelta()));
        pinkSpinner.z=1;

        puck.setBoxWidth(spinner.getBoxWidth()/3);
        puck.setBoxHeight(spinner.getBoxWidth()/3);
        puck.setCX(screen.getCX());
        puck.setCY(screen.getCY());
        puck.setTheta(puck.getTheta()-((Math.PI)*evt.getDelta()));
        puck.z=-2;
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


    public static final EntityStateFactory getFactory(){
        return new EntityStateFactory(){

            @Override
            public EntityState construct() {
                return new TitleScreenOverlay();
            }

            @Override
            public Class<? extends EntityState> getType() {
                return TitleScreenOverlay.class;
            }

            @Override
            public void getAssets(ArrayList<String[]> assets){
                assets.add(new String[]{"texture","spinner"});
                assets.add(new String[]{"texture","exit_icon"});
                assets.add(new String[]{"texture","options_icon"});
                assets.add(new String[]{"texture","leader_boards_icon"});
                assets.add(new String[]{"texture","information_icon"});
                assets.add(new String[]{"texture","pink_spinner"});
                assets.add(new String[]{"texture","puck"});
                assets.add(new String[]{"music","chip_sauce"});
                assets.add(new String[]{"texture","cancel_button"});
                assets.add(new String[]{"texture","instructions_1"});
                assets.add(new String[]{"texture","instructions_2"});
                assets.add(new String[]{"texture","instructions_3"});
                assets.add(new String[]{"texture","instructions_4"});
                assets.add(new String[]{"texture","arrow"});
                assets.add(new String[]{"texture","help_icon"});
                assets.add(new String[]{"texture","credits"});
                assets.add(new String[]{"sound","menu_click"});
            }
        };
    }
}
