package com.flickshot.components.entities.defs.fx;

import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.entities.defs.PuckState;
import com.flickshot.components.entities.defs.VisibleEntity;
import com.flickshot.components.graphics.Artist;
import com.flickshot.components.graphics.Graphics;
import com.flickshot.components.graphics.Renderer2d;
import com.flickshot.components.graphics.Screen;
import com.flickshot.components.graphics.Sprite;
import com.flickshot.geometry.Vector2d;
import com.flickshot.scene.Scene;

/**
 * Created by Alex on 6/14/2015.
 */
public class Transition extends VisibleEntity {
    public static final String ENTITY_NAME = "transition";

    public String target;


    private final Sprite puck = new Sprite("puck");

    final double t = 1;
    double dt = 0;

    boolean in;
    boolean puckSpawned;

    public Transition(){
        persistant = true;

        puck.setCX(0);
        puck.setCY(0);
        puck.setBoxWidth(1);
        puck.setBoxHeight(1);
        puck.z = 0;

        setArtist(new Artist(){

            @Override
            public boolean isOnScreen(double screenX, double screenY, double screenWidth, double screenHeight) {
                return true;
            }

            @Override
            public void draw(double delta, Renderer2d renderer) {
                Screen s = Graphics.getCurrentScene().screen;
                double u = dt/t;
                if(in){
                    renderer.translate(s.getCX(),s.getCY(),-99.999999999);
                    renderer.scale(u * s.getWidth() * (4.0 / 3.0), u * s.getWidth() * (4.0 / 3.0));
                    renderer.rotate(Math.PI*2*3*u);
                    puck.draw(delta, renderer);

                    dt+=delta;
                    if(dt>=t){
                        dt = t;
                        in = false;
                        Scene.newScene(target);
                    }
                }else{
                    if(!puckSpawned){
                        if(Entities.getStateCount(PuckState.class)>0){
                            Scene.getCurrent().updater.paused.set(true);
                            puckSpawned = true;
                        }
                    }else{
                        PuckState puckState = (PuckState)Entities.getEntity(PuckState.class).getState(0);
                        Vector2d pos = puckState.collider.tx.translation;

                        renderer.translate(pos.x,pos.y,-99.99999999999);

                        double p = puckState.collider.tx.scale.x/(s.getWidth()*(4.0/3.0));
                        renderer.scale(Math.min(1,u+p),Math.min(1,u+p));

                        renderer.translate(s.getCX()-pos.x,s.getCY()-pos.y);
                        renderer.scale(s.getWidth()*(4.0/3.0),s.getWidth()*(4.0/3.0));
                        renderer.rotate(Math.PI * 2 * 3 * u);
                        puck.draw(delta, renderer);

                        dt -= delta;
                        if(dt<=0){
                            kill();
                            Scene.getCurrent().updater.paused.set(false);
                        }
                    }
                }
            }
        });
    }

    @Override
    public void init(double x, double y){
        super.init(x,y);
        dt = 0;
        in = true;
        puckSpawned = false;
        if(Entities.getStateCount(Transition.class)>1) {
            kill();
        }
    }

    public static EntityStateFactory getFactory(){
        return new EntityStateFactory() {
            @Override
            public EntityState construct() {
                return new Transition();
            }

            @Override
            public Class<? extends EntityState> getType() {
                return Transition.class;
            }
        };
    }
}
