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

/**
 * Created by Alex on 3/20/2015.
 */
public class OptionsOverlay extends VisibleEntity {
    public static final String ENTITY_NAME = "OptionsOverlay";

    private final Slider soundFx = new Slider();
    private final Slider music = new Slider();
    private final Slider powerAssist = new Slider();

    private final Sprite cancel = new Sprite("cancel_button");

    boolean touched;

    Sound click = new Sound("menu_click");

    private final TouchListener tl = new TouchListener() {
        Slider selected;

        @Override
        public void onDown(TouchEvent evt) {
            touched = true;
            Screen screen = Graphics.getCurrentScene().screen;
            if(CollisionLib.pointBox(
                    TouchManager.x(),TouchManager.y(),
                    screen.getX()+screen.getWidth()-(screen.getHeight()/4),
                    screen.getY(),(screen.getHeight()/4),(screen.getHeight()/4))){
                kill();
                Entities.newInstance(TitleScreenOverlay.class,0,0);
                click.play();
            }else if(CollisionLib.pointBox(
                    TouchManager.x(),TouchManager.y(),
                    screen.getX(),
                    screen.getY(),(cancel.getBoxHeight()*2),(cancel.getBoxHeight()))){
                if(FlickShot.googleApiClient!=null) {
                    if (FlickShot.googleApiClient.isConnected() || FlickShot.googleApiClient.isConnecting()) {
                        FlickShot.instance.tryToLogOut();
                    } else {
                        FlickShot.instance.tryToConnect();
                    }
                }
                click.play();
            }else{
                select(TouchManager.x(),TouchManager.y());
            }
        }

        @Override
        public void onMove(TouchEvent evt) {
            if(touched) {
                if (selected == null) {
                    select(TouchManager.x(), TouchManager.y());
                } else {
                    setSelectedPosition(TouchManager.x(), TouchManager.y());
                }
            }
        }

        @Override
        public void onUp(TouchEvent evt) {
            touched = false;
            selected = null;
            FlickShot.options.musicVolume = music.position;
            FlickShot.options.soundFXVolume = soundFx.position;
            FlickShot.options.write();
            ((TitleMusic)Entities.getEntity(TitleMusic.class).getState(0)).titleMusic.setVolume(1,1);
        }

        private void select(double x, double y){
            if(CollisionLib.pointBox(x,y,soundFx.cx-(soundFx.width/2),soundFx.cy-(soundFx.height/2),soundFx.width,soundFx.height)){
                selected = soundFx;
                click.play();
            }else if(CollisionLib.pointBox(x,y,music.cx-(music.width/2),music.cy-(music.height/2),music.width,music.height)){
                selected = music;
                click.play();
            }else if(CollisionLib.pointBox(x,y,
                    powerAssist.cx-(powerAssist.width/2),powerAssist.cy-(powerAssist.height/2),
                    powerAssist.width,powerAssist.height)){
                selected = powerAssist;
                click.play();
            }else{
                selected=null;
            }
            setSelectedPosition(x,y);
        }

        private void setSelectedPosition(double x, double y){
            if(selected!=null){
                selected.position =  Math.min(1,Math.max(0,(x-(selected.cx-selected.width/2))/selected.width));
                FlickShot.options.musicVolume = music.position;
                FlickShot.options.soundFXVolume = soundFx.position;
                FlickShot.options.powerAssist = powerAssist.position;
                ((TitleMusic)Entities.getEntity(TitleMusic.class).getState(0)).titleMusic.setVolume(1,1);
            }
        }
    };

    private final FlickShot.HardwareButtonListener hb = new FlickShot.HardwareButtonListener() {
        @Override
        public void onHome(){ }

        @Override
        public void onBack() {
            super.onBack();
            kill();
            Entities.newInstance(TitleScreenOverlay.class,0,0);
        }
    };

    public OptionsOverlay(){
        setArtist(new Artist(){

            @Override
            public boolean isOnScreen(double screenX, double screenY, double screenWidth, double screenHeight) {
                return true;
            }

            @Override
            public void draw(double delta, Renderer2d r) {
                Screen screen = Graphics.getCurrentScene().screen;

                r.setDrawMode(Renderer2d.FILL);
                r.color(1,0.6,1,1);
                r.shape(Renderer2d.SQUARE,screen.getCX(),screen.getCY(),50,screen.getWidth(), screen.getHeight());

                soundFx.draw(r);
                music.draw(r);
                powerAssist.draw(r);


                double scale = 64/r.textHeight();
                
                r.push();
                    r.translate(soundFx.cx-(soundFx.width/2),soundFx.cy+(soundFx.height/2)+64);
                    r.scale(scale,scale);
                    r.align(Renderer2d.LEFT,Renderer2d.CENTER);
                    r.color(0,0,0,1);
                    r.text("Sound FX Volume");
                r.pop();

                r.push();
                    r.translate(music.cx-(music.width/2),music.cy+(music.height/2)+64);
                    r.scale(scale,scale);
                    r.align(Renderer2d.LEFT,Renderer2d.CENTER);
                    r.color(0,0,0,1);
                    r.text("Music Volume");
                r.pop();

                r.push();
                    r.translate(powerAssist.cx-(powerAssist.width/2),powerAssist.cy+(powerAssist.height/2)+64);
                    r.scale(scale,scale);
                    r.align(Renderer2d.LEFT,Renderer2d.CENTER);
                    r.color(0,0,0,1);
                    r.text("Power Assist (Tablet Only)");
                r.pop();

                if(FlickShot.googleApiClient!=null) {
                    r.push();
                        r.translate(screen.getX() + cancel.getBoxWidth(), screen.getY() + cancel.getBoxHeight() / 2);
                        r.color(0.5f, 0, 0.5f);
                        r.shape(Renderer2d.SQUARE, cancel.getBoxWidth() * 2, cancel.getBoxHeight());
                        r.color(0, 1, 1);
                        r.shape(Renderer2d.SQUARE, 0, 0, -1,
                                (cancel.getBoxWidth() * 2) - (cancel.getBoxHeight() * 0.2),
                                (cancel.getBoxHeight()) - (cancel.getBoxHeight() * 0.2));
                        r.translate(0,0,-3);
                        r.color(0.5f, 0, 0.5f);
                        r.align(Renderer2d.CENTER,Renderer2d.CENTER);
                        r.scale(scale,scale);
                        r.text((FlickShot.googleApiClient.isConnected())?"sign out":"sign in");
                    r.pop();
                }

                cancel.draw(delta, r);
            }
        });
    }

    public void init(double x, double y){
        super.init(x,y);
        touched = false;
        TouchManager.add(tl);
        FlickShot.addListener(hb);

        Screen screen = Graphics.getCurrentScene().screen;

        cancel.setCX(screen.getX()+screen.getWidth()-(screen.getHeight()/8));
        cancel.setCY(screen.getY()+(screen.getHeight()/8));
        cancel.setBoxWidth(screen.getHeight()/4);
        cancel.setBoxHeight(screen.getHeight()/4);

        double sHeight = 90;
        double sWidth = screen.getWidth()*(5.0/6.0);

        soundFx.cx = screen.getCX();
        soundFx.cy = screen.getY()+(screen.getHeight()*(4.0/5.0));
        soundFx.height = sHeight;
        soundFx.width = sWidth;
        soundFx.position = FlickShot.options.soundFXVolume;

        music.cx = screen.getCX();
        music.cy = screen.getY()+(screen.getHeight()*(3.0/5.0));
        music.height = sHeight;
        music.width = sWidth;
        music.position = FlickShot.options.musicVolume;

        powerAssist.cx = screen.getCX();
        powerAssist.cy = screen.getY()+(screen.getHeight()*(2.0/5.0));
        powerAssist.height = sHeight;
        powerAssist.width = sWidth;
        powerAssist.position = FlickShot.options.powerAssist;

    }

    @Override
    public void destroy(){
        super.destroy();
        TouchManager.remove(tl);
        FlickShot.removeListener(hb);
    }

    @Override
    public void unload(){
        super.unload();
        TouchManager.remove(tl);
        FlickShot.removeListener(hb);
    }

    private class Slider{
        double cx,cy;
        double width;
        double height;
        double position;

        public void draw(Renderer2d r){
            double barHeight = height/4;

            r.setDrawMode(Renderer2d.FILL);

            r.color(0.8,0,0.8,1);
            r.shape(Renderer2d.SQUARE, cx, cy, 0, width, barHeight);
            r.shape(Renderer2d.ELLIPSE,cx-(width/2),cy,0,barHeight,barHeight);
            r.shape(Renderer2d.ELLIPSE,cx+(width/2),cy,0,barHeight,barHeight);

            r.color(0,1,1,1);
            r.shape(Renderer2d.SQUARE,cx-(width/2)+(width*position),cy,-1,32,height);

        }

    }


    public static EntityStateFactory getFactory(){
        return new EntityStateFactory(){

            @Override
            public EntityState construct() {
                return new OptionsOverlay();
            }

            @Override
            public Class<? extends EntityState> getType() {
                return OptionsOverlay.class;
            }
        };
    }

}
