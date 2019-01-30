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
 * Created by Alex on 6/14/2015.
 */
public class CreditsOverlay extends VisibleEntity {
    public static final String ENTITY_NAME = "CreditsOverlay";

    private final Sprite cancel = new Sprite("cancel_button");

    private final Sprite credits = new Sprite("credits");

    Sound click = new Sound("menu_click");

    private final FlickShot.HardwareButtonListener hb = new FlickShot.HardwareButtonListener() {
        @Override
        public void onBack() {
            super.onBack();
            kill();
            Entities.newInstance(TitleScreenOverlay.class,0,0);
        }
    };

    private final TouchListener tl = new TouchListener() {
        @Override
        public void onDown(TouchEvent evt) {
            if(CollisionLib.pointBox(TouchManager.x(),TouchManager.y(),
                    cancel.getX(),cancel.getY(),cancel.getBoxWidth(),cancel.getBoxHeight())){
                kill();
                Entities.newInstance(TitleScreenOverlay.class,0,0);
                click.play();
            }
        }

        @Override
        public void onMove(TouchEvent evt) {

        }

        @Override
        public void onUp(TouchEvent evt) {

        }
    };

    CreditsOverlay(){
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

                credits.draw(delta, renderer);
                cancel.draw(delta, renderer);
            }
        });
    }

    @Override
    public void init(double x, double y){
        super.init(x, y);


        TouchManager.add(tl);
        FlickShot.addListener(hb);

        Screen s = Graphics.getCurrentScene().screen;
        cancel.setCX(s.getX()+s.getWidth()-(s.getHeight()/8));
        cancel.setCY(s.getY()+(s.getHeight()/8));
        cancel.setBoxWidth(s.getHeight()/4);
        cancel.setBoxHeight(s.getHeight()/4);

        credits.setCX(s.getCX());
        credits.setCY(s.getCY());
        credits.setBoxWidth(s.getHeight());
        credits.setBoxHeight(s.getHeight());
    }

    @Override
    public void destroy(){
        super.destroy();
        FlickShot.removeListener(hb);
        TouchManager.remove(tl);
    }


    public void unload(){
        super.destroy();
        FlickShot.removeListener(hb);
        TouchManager.remove(tl);
    }


    public static EntityStateFactory getFactory(){
        return new EntityStateFactory() {
            @Override
            public EntityState construct() {
                return new CreditsOverlay();
            }

            @Override
            public Class<? extends EntityState> getType() {
                return CreditsOverlay.class;
            }
        };
    }

}
