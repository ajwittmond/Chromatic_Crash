package com.flickshot.components.entities.defs.enemies;

import com.flickshot.assets.sfx.Sound;
import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.entities.defs.PhysObject;
import com.flickshot.components.entities.defs.PuckState;
import com.flickshot.components.entities.defs.fx.Explosion;
import com.flickshot.components.graphics.Renderer2d;
import com.flickshot.components.graphics.Sprite;
import com.flickshot.components.particles.ParticleSystem;
import com.flickshot.components.particles.ParticleType;
import com.flickshot.components.particles.Particles;
import com.flickshot.components.physics.Manifold;
import com.flickshot.components.physics.MassData;
import com.flickshot.components.physics.PhysShape;
import com.flickshot.components.physics.shapes.Circle;
import com.flickshot.config.Config;
import com.flickshot.geometry.Transformation;
import com.flickshot.geometry.Vector2d;
import com.flickshot.scene.Updater;

import java.util.ArrayList;

/**
 * Created by Alex on 3/19/2015.
 */
public class Pulser extends Enemy{
    public static final String ENTITY_NAME = "pulser";
    public static final String EDITOR_SPRITE = "pulser_{COLOR};128;128;0";

    public static final ParticleType flareType;
    public static final ParticleSystem flare;

    static{
        flareType = new ParticleType();
        flareType.life = 1;
        flareType.z = 1;
        flareType.color1(1,0,0,1,0,0);
        flareType.oneColor();
        flareType.alpha(1,1,1);
        flareType.dimensions(1,1);
        flareType.startScale(32,90);
        flareType.scale(-90,0);
        flareType.startSpeed(300,600);
        flareType.startDir(0,Math.PI/2);
        flareType.texture = "flare";

        flare= Particles.createSystem("flare2", flareType, 256);
    }

    public static ParticleSystem pulse;
    public static ParticleType pulseType;

    static{
        pulseType = new ParticleType();
        pulseType.life = 0.5;
        pulseType.z = 1;
        pulseType.color1(0, 0, 0, 0, 0, 0);
        pulseType.oneColor();
        pulseType.alpha(1, 0.8f, 0);
        pulseType.dimensions(1, 1);
        pulseType.scale(4000, 0);
        pulseType.texture = "radial_pulse";

        pulse = Particles.createSystem("pulse", pulseType, 16);
    }

    private final double deccel = 500;

    final double pauseTime = 1;
    double pauseTimer;

    final double pulseTime = 3;
    double pulseTimer;


    final double pulseStrength = 300000000;
    final double pullForce = 200000000;
    final double rangeStart = 90;
    final double rangeEnd = 500;

    boolean pulsing;

    Sprite sprite;
    Sprite field;

    MassData originalData;
    MassData pulsingData;

    Sound pulserSound = new Sound("pulser_sound");

    public Pulser(){
        maxHealth = 6;
        killForce = 30000000;
        field = new Sprite("field",new Transformation(
                collider.tx.translation,new Vector2d(rangeEnd*2,rangeEnd*2),collider.tx.theta));
        field.z = 2;
        setArtist(sprite = new Sprite("pulser_red",new Transformation(
                collider.tx.translation,new Vector2d(128,128),collider.tx.theta)){
            @Override
            public void draw(double delta,Renderer2d r){
                super.draw(delta,r);
                if(pulsing){
                    field.draw(delta,r);
                }
            }
        });
        sprite.setTint(0,0,0);
        originalData = collider.massData;
        System.out.println("massData "+ originalData.mass);
        pulsingData = new MassData(originalData.mass*10000,originalData.inertia*10000);

        damageSound = new Sound("splat");
        deathSound = new Sound("messy_splat");
    }

    public void init(double x, double y){
        super.init(x,y);
        pulseTimer = pulseTime;
        pauseTimer = pauseTime;
        pulsing = false;
        deathSound.setVolume(0.1f,0.1f);
    }

    private final ArrayList<EntityState> states = new ArrayList<EntityState>();
    private final Vector2d misc = new Vector2d();
    public void update(Updater.UpdateEvent evt){
        super.update(evt);
        sprite.tintWeight = 1.0f - (float)health/(float)maxHealth;
        if(pulsing){
            pulseTimer-=evt.getDelta();
            if(pulseTimer<=0){
                collider.angularVelocity = 0;
                pulsing = false;
                pulseTimer = pulseTime;
                collider.massData = originalData;
                flare.clear();

                //do pulse
                pulse.create(1,collider.tx.translation.x,collider.tx.translation.y,0,0,0,0,color.r,color.g,color.b);

                //do pulse
                states.clear();
                Entities.getStates(PhysObject.class, states);
                for (int i = 0; i<states.size(); i++) {
                    EntityState e = states.get(i);
                    if (e != this) {
                        PhysObject o = (PhysObject) e;
                        double dist = Vector2d.distSquared(collider.tx.translation, o.collider.tx.translation);
                        if (dist < rangeEnd * rangeEnd) {
                            dist = Math.sqrt(dist);
                            double u = 1.0 - Math.max(0, (dist - rangeStart) / (rangeEnd - rangeStart));
                            double pulse = pulseStrength*u*o.collider.massData.invMass;
                            misc.set(o.collider.tx.translation);
                            misc.sub(collider.tx.translation);
                            misc.setMag(pulse);
                            o.collider.velocity.add(misc);
                            if(dist<rangeEnd/2 && o instanceof Enemy){
                                if(o instanceof Bomb)
                                    ((Bomb)o).explode();
                                else
                                    ((Enemy)o).health -= 1;
                            }
                        }
                    }
                }

                PuckState puck = (PuckState)Entities.getEntity(PuckState.class).getState(0);
                if(puck!=null) {
                    double dist = Vector2d.distSquared(collider.tx.translation, puck.collider.tx.translation);
                    if (dist < rangeEnd * rangeEnd) {
                        dist = Math.sqrt(dist);
                        double u = 1.0 - Math.max(0, (dist - rangeStart) / (rangeEnd - rangeStart));
                        double pulse = pulseStrength*u*puck.collider.massData.invMass;
                        misc.set(puck.collider.tx.translation);
                        misc.sub(collider.tx.translation);
                        misc.setMag(pulse);
                        puck.collider.velocity.add(misc);
                        if(dist<rangeEnd/2){
                            puck.stun();
                            puck.damage(30);
                        }
                    }
                }
            }else {
                float v = (float)(0.3 + (0.7*(1.0-(pulseTimer/pulseTime))));

                pulserSound.setVolume(v,v);

                //spin;
                collider.angularAccelerationToVelocity(1000 * Math.PI / 8, Math.PI * 4);

                //pull objects toward pulser
                states.clear();
                Entities.getStates(PhysObject.class, states);
                for (int i = 0; i< states.size(); i++) {
                    EntityState e = states.get(i);
                    if (e != this) {
                        PhysObject o = (PhysObject) e;
                        double dist = Vector2d.distSquared(collider.tx.translation, o.collider.tx.translation);
                        if (dist < rangeEnd * rangeEnd) {
                            dist = Math.sqrt(dist);
                            double u = 1.0 - Math.max(0, (dist - rangeStart) / (rangeEnd - rangeStart));
                            double force = pullForce * u;
                            misc.set(collider.tx.translation);
                            misc.sub(o.collider.tx.translation);
                            misc.setMag(force);
                            o.collider.force.add(misc);
                        }
                    }
                }
                PuckState puck = (PuckState)Entities.getEntity(PuckState.class).getState(0);
                if(puck!=null) {
                    double dist = Vector2d.distSquared(collider.tx.translation, puck.collider.tx.translation);
                    if (dist < rangeEnd * rangeEnd) {
                        dist = Math.sqrt(dist);
                        double u = 1.0 - Math.max(0, (dist - rangeStart) / (rangeEnd - rangeStart));
                        double force = pullForce * u;
                        misc.set(collider.tx.translation);
                        misc.sub(puck.collider.tx.translation);
                        misc.setMag(force);
                        puck.collider.force.add(misc);
                    }
                }

                //spawn particles
                if (Math.random() < evt.getDelta() * 100) {
                    double theta = Math.random() * Math.PI * 2;
                    double r = Math.random();
                    double dist = r * r * rangeEnd;
                    flare.create(1,
                            collider.tx.translation.x + (dist * (Math.cos(theta))),
                            collider.tx.translation.y + (dist * (Math.sin(theta))),
                            rangeEnd, theta + Math.PI, 0, 90 * (dist / rangeEnd),
                            color.r,color.g,color.b);
                }
            }

        }else{
            collider.deccelerate(deccel,evt.getDelta());
            pauseTimer-=evt.getDelta();
            if(pauseTimer<=0 && collider.velocity.getMag()==0){
                pulsing = true;
                pauseTimer = pauseTime;
                collider.massData = pulsingData;
                pulserSound.play();
            }
        }
    }


    @Override
    public void configure(Config c){
        super.configure(c);
        sprite.setTexture("pulser_"+color.name);
        field.setTint(color.r,color.g,color.b);
        field.tintWeight = 1;
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
        pulserSound.stop();
    }

    @Override
    public void unload(){
        super.unload();
        pulserSound.stop();
    }

    @Override
    public void onDamage(Manifold m){
        super.onDamage(m);
        final int particles = 8;
        double vtheta = m.normal.getDir()+(Math.PI);
        for(int i = 0; i<particles; i++){
            double theta = vtheta+((Math.PI*Math.random())-(Math.PI/2));
            drop.create(
                    1,m.contacts[0].x,m.contacts[0].y,300,theta,0,32,
                    color.r*(1.0f-sprite.tintWeight),color.g*(1.0f-sprite.tintWeight),
                    color.b*(1.0f-sprite.tintWeight));
        }
    }

    @Override
    protected ArrayList<PhysShape> getShapes() {
        return shapeAsList(new Circle(0,0,64));
    }

    public static final EntityStateFactory getFactory(){
        return new Factory();
    }

    public static class Factory extends EntityStateFactory{
        @Override
        public EntityState construct() {
            return new Pulser();
        }

        @Override
        public Class<? extends EntityState> getType() {
            return Pulser.class;
        }

        @Override
        public Config getConfig(){
            return new EnemyConfig();
        }

        @Override
        public void getAssets(ArrayList<String[]> assets){
            assets.add(new String[]{"texture","pulser_red"});
            assets.add(new String[]{"texture","pulser_green"});
            assets.add(new String[]{"texture","pulser_pink"});
            assets.add(new String[]{"texture","field"});
            assets.add(new String[]{"texture","radial_pulse"});
            assets.add(new String[]{"sound","splat"});
            assets.add(new String[]{"sound","messy_splat"});
            assets.add(new String[]{"sound","pulser_sound"});
        }
    }
}
