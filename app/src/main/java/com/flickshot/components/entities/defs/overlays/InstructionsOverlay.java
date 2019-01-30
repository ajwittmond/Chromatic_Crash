package com.flickshot.components.entities.defs.overlays;

import android.text.method.Touch;

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

/**
 * Created by Alex on 5/7/2015.
 */
public class InstructionsOverlay extends VisibleEntity{
    public static final String ENTITY_NAME = "InstructionsOverlay";

    public static final Sprite cancel = new Sprite("cancel_button");

    public static final Sprite splash = new Sprite("instructions_1");
    public static final String[] splashTextures = new String[]{
        "instructions_1","instructions_2","instructions_3","instructions_4"
    };

    Sprite leftArrow = new Sprite("arrow");
    Sprite rightArrow = new Sprite("arrow");

    Sound click = new Sound("menu_click");

    FlickShot.HardwareButtonListener hb = new FlickShot.HardwareButtonListener() {
        @Override
        public void onBack() {
            super.onBack();
            kill();
            Entities.newInstance(TitleScreenOverlay.class, 0, 0);
        }

        @Override
        public void onHome(){
        }

        @Override
        public void onMenu(){
        }
    };

    TouchListener tl = new TouchListener(){

        @Override
        public void onDown(TouchEvent evt) {
            Screen s = Graphics.getCurrentScene().screen;
            if(CollisionLib.pointBox(
                    TouchManager.x(), TouchManager.y(),
                    s.getX()+s.getWidth()-(s.getHeight()/4),s.getY(),(s.getHeight()/4),(s.getHeight()/4))){
                kill();
                Entities.newInstance(TitleScreenOverlay.class,0,0);
                click.play();
            }else{
                double awidth = (((1.0-(2.5/4.0))*s.getWidth())/2);
                if(TouchManager.x()<awidth+s.getX()){
                    if(position>0){
                        position--;
                        click.play();
                    }
                }else if(TouchManager.x()>s.getX()+s.getWidth()-awidth){
                    if(position<splashTextures.length-1){
                        position++;
                        click.play();
                    }
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

    int position;



    public InstructionsOverlay(){
        setArtist(new Artist(){

            @Override
            public boolean isOnScreen(double screenX, double screenY, double screenWidth, double screenHeight) {
                return true;
            }

            @Override
            public void draw(double delta, Renderer2d renderer) {
                Screen s = Graphics.getCurrentScene().screen;
                renderer.color(0,1,1);
                renderer.setDrawMode(Renderer2d.FILL);
                renderer.shape(Renderer2d.SQUARE,s.getCX(),s.getCY(),1,s.getWidth(),s.getHeight());

                splash.setTexture(splashTextures[position]);

                if(position==0){
                    leftArrow.tintWeight = 0.8f;
                }else{
                    leftArrow.tintWeight = 0;
                }

                if(position==splashTextures.length-1){
                    rightArrow.tintWeight = 0.8f;
                }else{
                    rightArrow.tintWeight = 0;
                }

                leftArrow.draw(delta, renderer);
                rightArrow.draw(delta, renderer);

                cancel.draw(delta, renderer);

                splash.draw(delta, renderer);
            }
        });
    }

    public void init(double x, double y){
        super.init(x,y);
        TouchManager.add(tl);
        FlickShot.addListener(hb);

        position = 0;

        Screen s = Graphics.getCurrentScene().screen;
        double awidth = (((1.0-(2.5/4.0))*s.getWidth())/2);

        leftArrow.setCX(s.getX()+(awidth/2));
        leftArrow.setCY(s.getCY());
        leftArrow.setBoxWidth(awidth - 64);
        leftArrow.setBoxHeight((awidth - 64) * 2);
        leftArrow.setTheta(Math.PI);
        leftArrow.setTint(0,0,0);

        rightArrow.setCX(s.getX() + s.getWidth() - (awidth / 2));
        rightArrow.setCY(s.getCY());
        rightArrow.setBoxWidth(awidth - 64);
        rightArrow.setBoxHeight((awidth - 64) * 2);
        rightArrow.setTint(0,0,0);

        splash.setCY(s.getCY());
        splash.setCX(s.getCX());
        splash.setBoxWidth(s.getHeight());
        splash.setBoxHeight(s.getHeight());

        cancel.setCX(s.getX()+s.getWidth()-(s.getHeight()/8));
        cancel.setCY(s.getY()+(s.getHeight()/8));
        cancel.setBoxWidth(s.getHeight()/4);
        cancel.setBoxHeight(s.getHeight()/4);
    }

    public void destroy(){
        super.destroy();
        FlickShot.removeListener(hb);
        TouchManager.remove(tl);
    }

    public void unload(){
        super.unload();
        FlickShot.removeListener(hb);
        TouchManager.remove(tl);
    }


    public static EntityStateFactory getFactory(){
        return new EntityStateFactory() {
            @Override
            public EntityState construct() {
                return new InstructionsOverlay();
            }

            @Override
            public Class<? extends EntityState> getType() {
                return InstructionsOverlay.class;
            }
        };
    }
}
