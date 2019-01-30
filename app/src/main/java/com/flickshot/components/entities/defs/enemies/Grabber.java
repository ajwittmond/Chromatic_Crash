package com.flickshot.components.entities.defs.enemies;

import com.flickshot.assets.sfx.Sound;
import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.entities.defs.PuckState;
import com.flickshot.components.entities.defs.fx.Explosion;
import com.flickshot.components.graphics.Renderer2d;
import com.flickshot.components.graphics.Sprite;
import com.flickshot.components.physics.Manifold;
import com.flickshot.components.physics.PhysMaterial;
import com.flickshot.components.physics.PhysShape;
import com.flickshot.components.physics.Physics;
import com.flickshot.components.physics.shapes.Circle;
import com.flickshot.config.Config;
import com.flickshot.geometry.Transformation;
import com.flickshot.geometry.Vector2d;
import com.flickshot.scene.Updater;

import java.util.ArrayList;

/**
 * Created by Alex on 3/10/2015.
 */
public class Grabber extends Enemy{
    public static final String ENTITY_NAME = "grabber";
    public static final String EDITOR_SPRITE = "grabber_{COLOR};128;128;0";

    private double minDist = 100;
    private double maxDist = 500;

    private double maxForce = 400000000;


    Sprite sprite;

    private boolean puckGrabbed = false;

    Sound grab = new Sound("grab");

    Grabber(){
        maxHealth = 5;
        killForce = 40000000;
        setPoints(40);
        setArtist(sprite=new Sprite("grabber_red",new Transformation(collider.tx.translation,new Vector2d(128,128),collider.tx.theta)){

            private final Vector2d misc = new Vector2d();
            @Override
            public void draw(double delta,Renderer2d r){
                super.draw(delta,r);
                if(puckGrabbed){
                    PuckState puck = (PuckState)Entities.getEntity(PuckState.class).getState(0);
                    if(puck==null){
                        puckGrabbed = false;
                        return;
                    }

                    r.color(color.r, color.g, color.b);
                    r.setDrawMode(Renderer2d.FILL);
                    r.translate(
                            collider.tx.translation.x + (puck.collider.tx.translation.x-collider.tx.translation.x)*0.5,
                            collider.tx.translation.y + (puck.collider.tx.translation.y-collider.tx.translation.y)*0.5,
                            0.01);
                    r.rotate((-Math.PI/2)+Math.atan2((puck.collider.tx.translation.y-collider.tx.translation.y),(puck.collider.tx.translation.x-collider.tx.translation.x)));
                    double dist = puck.collider.tx.translation.dist(collider.tx.translation);
                    r.scale(Math.max(Math.min(1, (dist-minDist) / (maxDist - minDist)),0.1)*100,dist);
                    r.shape(Renderer2d.TRIANGLE);
                    r.rotate(Math.PI);
                    r.shape(Renderer2d.TRIANGLE);
                }
            }
        });
        sprite.setTint(0, 0, 0);
        collisionSound = new Sound("wood_wack");
        deathSound = new Sound("concrete_step");
        damageSound = new Sound("concrete_slap");
    }

    @Override
    public void init(double x, double y){
        super.init(x,y);
        Explosion.flareType.bounds = Physics.getScene(0);
        grab.setVolume(0.5f,0.5f);
    }

    private final Vector2d force = new Vector2d();
    public void update(Updater.UpdateEvent evt){
        super.update(evt);
        if(health>0)
            sprite.tintWeight = 1.0f - ((float)health/(float)maxHealth);
        PuckState puck = (PuckState)Entities.getEntity(PuckState.class).getState(0);
        if(puck!=null){
            double dist = puck.collider.tx.translation.dist(collider.tx.translation);
            if(!puckGrabbed){
                if(dist<maxDist){
                    puckGrabbed = true;
                    puck.stun();
                    grab.play();
                }
            }
            if(puckGrabbed){
                if(dist>minDist) {
                    double mag = ((dist-minDist) / (maxDist - minDist))*maxForce;
                    force.set(puck.collider.tx.translation);
                    force.sub(collider.tx.translation);
                    force.setMag(mag);
                    collider.force.add(force);
                    force.neg();
                    puck.collider.force.add(force);
                }
            }
        }
    }

    @Override
    public void onDamage(Manifold m){
        super.onDamage(m);
        final int particles = 8;
        double vtheta = m.normal.getDir()+(Math.PI);
        for(int i = 0; i<particles; i++){
            double theta = vtheta+((Math.PI*Math.random())-(Math.PI/2));
            drop.create(1,m.contacts[0].x,m.contacts[0].y,300,theta,0,32);
        }
    }

    @Override
    public void destroy(){
        super.destroy();
        final int particles = 32;
        for(int i = 0; i<particles; i++){
            double theta = (Math.PI*2)*Math.random();
            double dist = 64 * Math.random();
            double speed = 100+Math.random()*100;
            Explosion.flare.create(1,
                    collider.tx.translation.x + Math.cos(theta)*dist,
                    collider.tx.translation.y + Math.sin(theta)*dist,
                    speed,theta,0,32+(64.0*Math.random()),
                    Math.max(0,(color.r*(1.0f-sprite.tintWeight))+(float)(0.2*Math.random()-0.1)),
                    Math.max(0,(color.g*(1.0f-sprite.tintWeight))+(float)(0.2*Math.random()-0.1)),
                    Math.max(0,(color.b*(1.0f-sprite.tintWeight))+(float)(0.2*Math.random()-0.1)));
        }
    }

    @Override
    protected ArrayList<PhysShape> getShapes() {
        return shapeAsList(new Circle(0,0,64));
    }

    @Override
    protected PhysMaterial getMaterial(){
        return new PhysMaterial(6,0.2,0.9,0.8);
    }

    @Override
    public void configure(Config c){
        super.configure(c);
        sprite.setTexture("grabber_"+color.name);
    }

    public static final EntityStateFactory getFactory(){
        return new Factory();
    }

    public static class Factory extends EntityStateFactory{
        @Override
        public EntityState construct() {
            return new Grabber();
        }

        @Override
        public Class<? extends EntityState> getType() {
            return Grabber.class;
        }

        @Override
        public Config getConfig(){
            return new EnemyConfig();
        }

        @Override
        public void getAssets(ArrayList<String[]> assets){
            assets.add(new String[]{"texture","grabber_red"});
            assets.add(new String[]{"texture","grabber_pink"});
            assets.add(new String[]{"texture","grabber_green"});
            assets.add(new String[]{"sound","wood_wack"});
            assets.add(new String[]{"sound","concrete_step"});
            assets.add(new String[]{"sound","concrete_slap"});
            assets.add(new String[]{"sound","grab"});
        }
    }
}
