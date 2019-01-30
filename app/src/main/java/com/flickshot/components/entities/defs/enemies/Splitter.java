package com.flickshot.components.entities.defs.enemies;

import com.flickshot.assets.sfx.Sound;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.entities.defs.PuckState;
import com.flickshot.components.entities.defs.enemies.blocks.Block;
import com.flickshot.components.entities.defs.fx.Explosion;
import com.flickshot.components.graphics.Renderer2d;
import com.flickshot.components.graphics.Sprite;
import com.flickshot.components.physics.Bounds;
import com.flickshot.components.physics.Collider;
import com.flickshot.components.physics.Manifold;
import com.flickshot.components.physics.MassData;
import com.flickshot.components.physics.PhysMaterial;
import com.flickshot.components.physics.PhysShape;
import com.flickshot.components.physics.shapes.Circle;
import com.flickshot.components.physics.shapes.Polygon;
import com.flickshot.config.Config;
import com.flickshot.geometry.Transformation;
import com.flickshot.geometry.Vector2d;
import com.flickshot.scene.Updater;
import com.flickshot.util.MiscLib;

import java.util.ArrayList;
import java.util.Arrays;

public class Splitter extends Enemy{
    public static final String ENTITY_NAME = "splitter";
    public static final String EDITOR_SPRITE = "splitter_{COLOR};160;160;0";

    final double pauseTime = 2;
    double pauseTimer;

    final double splitTime = 5;
    double splitTimer=5;

    boolean splitting;


    private final double aDeccel = Math.PI;
    private final double aFix = Math.PI*2;
    private final double deccel = 300;

    double extSpeed = 1500;

    private double splitWidth;
    private Polygon center;
    private Polygon splitEast;
    private Polygon splitWest;
    private Polygon splitNorth;
    private Polygon splitSouth;
    private Circle endEast;
    private Circle endWest;
    private Circle endNorth;
    private Circle endSouth;

    double eastDist;
    double westDist;
    double northDist;
    double southDist;

    boolean eastExt;
    boolean westExt;
    boolean northExt;
    boolean southExt;

    boolean eastDmg;
    boolean westDmg;
    boolean northDmg;
    boolean southDmg;

    private Sprite sprite;

    private double splitTheta;

    final MassData originalData;
    final MassData splitData = new MassData(Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY);

    Sound ping = new Sound("ping");
    Sound pulseSound = new Sound("pulse");

    public Splitter(){
        super();
        maxHealth = 10;
        killForce = 30000000;
        setPoints(30);
        setArtist(sprite = new Sprite("splitter_red",
                new Transformation(collider.tx.translation,new Vector2d(160,160),collider.tx.theta)){
            @Override
            public void draw(double delta, Renderer2d r){
                super.draw(delta,r);

                r.translate(collider.tx.translation.x,collider.tx.translation.y,0.001);
                r.rotate(collider.tx.theta.val);

                r.setDrawMode(Renderer2d.FILL);
                r.color(0,0,0);

                double overlap = 20;

                r.shape(Renderer2d.SQUARE,(80+((splitWidth/2+eastDist)/2)-overlap/2),0,0,overlap+eastDist+splitWidth/2,splitWidth);
                r.shape(Renderer2d.SQUARE,-(80+((splitWidth/2+westDist)/2)-overlap/2),0,0,overlap+westDist+splitWidth/2,splitWidth);
                r.shape(Renderer2d.SQUARE,0,(80+((splitWidth/2+northDist)/2)-overlap/2),0,splitWidth,overlap+northDist+splitWidth/2);
                r.shape(Renderer2d.SQUARE,0,-(80+((splitWidth/2+southDist)/2)-overlap/2),0,splitWidth,overlap+southDist+splitWidth/2);

                if(westExt) r.color(color.r,color.g,color.b); else r.color(0,0,0);
                r.shape(Renderer2d.ELLIPSE,-(80+westDist+splitWidth/2),0,0,splitWidth,splitWidth);
                if(eastExt) r.color(color.r,color.g,color.b); else r.color(0,0,0);
                r.shape(Renderer2d.ELLIPSE,(80+eastDist+splitWidth/2),0,0,splitWidth,splitWidth);
                if(northExt) r.color(color.r,color.g,color.b); else r.color(0,0,0);
                r.shape(Renderer2d.ELLIPSE,0,80+northDist+splitWidth/2,0,splitWidth,splitWidth);
                if(southExt) r.color(color.r,color.g,color.b); else r.color(0,0,0);
                r.shape(Renderer2d.ELLIPSE,0,-(80+southDist+splitWidth/2),0,splitWidth,splitWidth);
            }
        });


        damageSound = new Sound("metal_hit");
        deathSound = new Sound("metal_impact");

        sprite.setTint(0,0,0);
        originalData = collider.massData;

    }

    @Override
    public void init(double x, double y){
        super.init(x,y);
        splitTheta=0;
        eastExt = false;
        northExt = false;
        southExt = false;
        westExt = false;
        eastDist = 0;
        westDist = 0;
        northDist = 0;
        southDist = 0;
        pauseTimer = 0;
        splitTimer = splitTime;
        collider.massData = originalData;
        splitting = false;
        ping.setVolume(0.5f,0.5f);
        damageSound.setVolume(0.5f,0.5f);
        deathSound.setVolume(0.8f,0.8f);
        setGeometry();
    }

    public void update(Updater.UpdateEvent evt){
        super.update(evt);
        sprite.tintWeight = 1.0f - ((float)health/(float)maxHealth);
        if(!splitting){
            pauseTimer-=evt.getDelta();
            if(collider.angularVelocity!=0 || collider.velocity.getMagSquared()!=0){
                collider.angularDeccelerate(aDeccel,evt.getDelta());
                collider.deccelerate(deccel,evt.getDelta());
            }else if(pauseTimer<=0){
                splitting = true;
                eastExt = true;
                westExt = true;
                southExt = true;
                northExt = true;
                eastDmg = true;
                westDmg = true;
                southDmg = true;
                northDmg = true;
                collider.massData = splitData;
                pauseTimer = pauseTime;
                if (splitTheta == 0) {
                    splitTheta = Math.PI / 4;
                } else {
                    splitTheta = 0;
                }
            }
        }
        if(splitting){
            if (eastExt) {
                eastDist += extSpeed * evt.getDelta();
            }
            if (westExt) {
                westDist += extSpeed * evt.getDelta();
            }
            if (southExt) {
                southDist += extSpeed * evt.getDelta();
            }
            if (northExt) {
                northDist += extSpeed * evt.getDelta();
            }
            eastDmg = eastExt;
            westDmg = westExt;
            northDmg = northExt;
            southDmg = southExt;
            eastExt = true;
            westExt = true;
            southExt = true;
            northExt = true;
            splitTimer-=evt.getDelta();
            if(splitTimer<=0){
                double dt = extSpeed * evt.getDelta();
                northDist = Math.max(0,northDist-dt);
                southDist = Math.max(0,southDist-dt);
                westDist = Math.max(0,westDist-dt);
                eastDist = Math.max(0,eastDist-dt);
                eastExt = false;
                westExt = false;
                southExt = false;
                northExt = false;
                if(northDist<=0 && southDist<=0 && westDist<=0 && eastDist<=0){
                    splitting=false;
                    collider.massData = originalData;
                    splitTimer = splitTime;
                }
            }
            setGeometry();
        }
    }

    @Override
    public void postUpdate(Updater.UpdateEvent evt){
    }

    private void setGeometry(){
        double dist = northDist+splitWidth/2;
        endNorth.setCY(80+dist);
        splitNorth.setVertex(0,-splitWidth/2, 80+dist);
        splitNorth.setVertex(1,-splitWidth/2, 80);
        splitNorth.setVertex(2,splitWidth/2, 80);
        splitNorth.setVertex(3,splitWidth/2, 80+dist);
        dist = southDist+splitWidth/2;
        endSouth.setCY(-80-dist);
        splitSouth.setVertex(0,-splitWidth/2, -80);
        splitSouth.setVertex(1,-splitWidth/2, -80-dist);
        splitSouth.setVertex(2,splitWidth/2, -80-dist);
        splitSouth.setVertex(3,splitWidth/2, -80);
        dist = westDist+splitWidth/2;
        endWest.setCX(-80-dist);
        splitWest.setVertex(0,-80-dist, splitWidth/2);
        splitWest.setVertex(1,-80-dist, -splitWidth/2);
        splitWest.setVertex(2,-80, -splitWidth/2);
        splitWest.setVertex(3,-80, splitWidth/2);
        dist = eastDist+splitWidth/2;
        endEast.setCX(80+dist);
        splitEast.setVertex(0,80, splitWidth/2);
        splitEast.setVertex(1,80, -splitWidth/2);
        splitEast.setVertex(2,80+dist, -splitWidth/2);
        splitEast.setVertex(3,80+dist, splitWidth/2);
    }

    final private Vector2d impulse = new Vector2d();
    final private Vector2d normal = new Vector2d();
    @Override
    public void onCollision(Manifold m){
        if(splitting && m.sa != center && m.sb !=center){
            Collider other = (m.a == collider)?m.b:m.a;
            PhysShape shape = (m.a == collider) ? m.sa : m.sb;
            if(other.getShape(0) instanceof Bounds || other.state instanceof Block || other.state instanceof Splitter || other.state instanceof Bouncer) {
                if (shape == splitEast || shape == endEast) {
                    eastExt = false;
                }else if (shape == splitWest || shape == endWest) {
                    westExt = false;
                }else if (shape == splitNorth || shape == endNorth) {
                    northExt = false;
                }else if (shape == splitSouth || shape == endSouth) {
                    southExt = false;
                }
            }else if(other.state instanceof Enemy){
                if ((
                        eastDmg && ( shape == endEast))
                        || (westDmg && (shape == endWest))
                        || (northDmg && (shape == endNorth))
                        || (southDmg && (shape == endSouth))) {
                    ((Enemy)other.state).doDamage(1,m);
                    normal.set(m.normal);
                    if(m.b==collider)normal.neg();
                    impulse.set(normal);
                    impulse.mul(10000000);
                    other.applyImpulse(impulse,normal);
                    Bouncer.wave.create(1,m.contacts[0].x,m.contacts[0].y,0,0,normal.getDir(),0,0,0,0);
                    pulseSound.play();
                }
            }else if(other.state instanceof PuckState){
                if ((
                        eastDmg && ( shape == endEast))
                        || (westDmg && (shape == endWest))
                        || (northDmg && (shape == endNorth))
                        || (southDmg && (shape == endSouth))) {
                    ((PuckState)other.state).stun();
                    ((PuckState)other.state).damage(10);
                    normal.set(m.normal);
                    if(m.b==collider)normal.neg();
                    impulse.set(normal);
                    impulse.mul(10000000);
                    other.applyImpulse(impulse,normal);
                    Bouncer.wave.create(1,m.contacts[0].x,m.contacts[0].y,0,0,normal.getDir(),0,0,0,0);
                    pulseSound.play();
                }else if(shape == center){
                    super.onCollision(m);
                }else if(Math.sqrt(m.forceSquared)>SOUND_THRESHOLD){
                    float gain = (float)Math.min(1,(Math.sqrt(m.forceSquared)-SOUND_THRESHOLD)/(SOUND_MAX-SOUND_THRESHOLD));
                    gain*=gain;
                    gain*=0.5;
                    ping.setVolume(gain,gain);
                    ping.play();
                }
            }

        }else{
            super.onCollision(m);
        }
    }


    private final Vector2d smokeVec = new Vector2d();
    @Override
    public void destroy(){
        super.destroy();
        final int particles = 32;
        for(int i = 0; i<particles; i++){
            double theta = (Math.PI*2)*Math.random();
            double dist = 80 * Math.random();
            double speed = 100+Math.random()*100;
            Explosion.flare.create(1,
                    collider.tx.translation.x + Math.cos(theta)*dist,
                    collider.tx.translation.y + Math.sin(theta)*dist,
                    speed,theta,0,32+(80.0*Math.random()),0,0,0);
        }


        double dist = 12;
        smokeVec.set(0,dist);
        smokeVec.setDir(collider.tx.theta.val);
        for(int i = 0; (i*dist)<eastDist+160;i++){
            Bomb.smoke.create(1,
                    collider.tx.translation.x+(smokeVec.x*i),collider.tx.translation.y+(smokeVec.y*i),
                    0,0,0,splitWidth,0,0,0);
        }
        smokeVec.setDir((Math.PI/2)+collider.tx.theta.val);
        for(int i = 0; (i*dist)<northDist+160;i++){
            Bomb.smoke.create(1,
                    collider.tx.translation.x+(smokeVec.x*i),collider.tx.translation.y+(smokeVec.y*i),
                    0,0,0,splitWidth,0,0,0);
        }
        smokeVec.setDir(Math.PI+collider.tx.theta.val);
        for(int i = 0; (i*dist)<westDist+160;i++){
            Bomb.smoke.create(1,
                    collider.tx.translation.x+(smokeVec.x*i),collider.tx.translation.y+(smokeVec.y*i),
                    0,0,0,splitWidth,0,0,0);
        }
        smokeVec.setDir((Math.PI*(3.0/2.0))+collider.tx.theta.val);
        for(int i = 0; (i*dist)<southDist+160;i++){
            Bomb.smoke.create(1,
                    collider.tx.translation.x+(smokeVec.x*i),collider.tx.translation.y+(smokeVec.y*i),
                    0,0,0,splitWidth,0,0,0);
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
    public void doDamage(int d,Manifold m){
        PhysShape shape = (m.a==collider) ? m.sa : m.sb;
        if(!splitting || shape == center) {
            super.doDamage(d, m);
        }
    }

    protected PhysMaterial getMaterial(){
        return new PhysMaterial(1,0.2,0,0);
    }

    private final Vector2d misc = new Vector2d();
    @Override
    public Vector2d getVelocity(PhysShape shape){
        if(splitting){
            if(eastDmg && (shape == endEast || shape==splitEast)){
                misc.set(extSpeed,0);
                misc.setDir(collider.tx.theta.val);
                return misc;
            }else if(westDmg && (shape == endWest || shape==splitWest)){
                misc.set(extSpeed,0);
                misc.setDir(collider.tx.theta.val+Math.PI);
                return misc;
            }else if(northDmg && (shape == endNorth || shape==splitNorth)){
                misc.set(extSpeed,0);
                misc.setDir(collider.tx.theta.val+(Math.PI/2));
                return misc;
            }else if(southDmg && (shape == endSouth || shape==splitSouth)){
                misc.set(extSpeed,0);
                misc.setDir(collider.tx.theta.val+(Math.PI*(6.0/4.0)));
                return misc;
            }
        }
        return collider.velocity;
    }

    @Override
    public boolean isStill(){
        return !splitting && super.isStill();
    }

    @Override
    protected ArrayList<PhysShape> getShapes() {

        double[] hexagonVerts = new double[16];
        double theta = MiscLib.DEG_TO_RAD*22.5;
        double width = 160;
        double radius = (width/2)/Math.cos(theta);
        splitWidth = 2*((width/2)*Math.tan(theta));
        for(int i = 0; i<16; i+=2){
            hexagonVerts[i] = Math.cos(theta)*radius;
            hexagonVerts[i+1] = Math.sin(theta)*radius;
            theta+=MiscLib.DEG_TO_RAD*45;
        }


        center = new Polygon(0,2,8,hexagonVerts,0,0);

        double offset = (width/2)+(splitWidth/4);
        splitEast = new Polygon(0,2,4,new double[]{
            offset-splitWidth/4, splitWidth/2,
            offset-splitWidth/4, -splitWidth/2,
            offset+splitWidth/4, -splitWidth/2,
            offset+splitWidth/4, splitWidth/2
        },0,0);
        splitWest = new Polygon(0,2,4,new double[]{
                (-offset)-splitWidth/4, splitWidth/2,
                (-offset)-splitWidth/4, -splitWidth/2,
                (-offset)+splitWidth/4, -splitWidth/2,
                (-offset)+splitWidth/4, splitWidth/2
        },0,0);
        splitNorth = new Polygon(0,2,4,new double[]{
                -splitWidth/2, offset+splitWidth/4,
                -splitWidth/2, offset-splitWidth/4,
                splitWidth/2, offset-splitWidth/4,
                splitWidth/2, offset+splitWidth/4
        },0,0);
        splitSouth = new Polygon(0,2,4,new double[]{
                -splitWidth/2, (-offset)+splitWidth/4,
                -splitWidth/2, (-offset)-splitWidth/4,
                splitWidth/2, (-offset)-splitWidth/4,
                splitWidth/2, (-offset)+splitWidth/4
        },0,0);

        offset = (width/2)+(splitWidth/2);
        endEast = new Circle(offset,0,splitWidth/2);
        endWest = new Circle(-offset,0,splitWidth/2);
        endNorth = new Circle(0,offset,splitWidth/2);
        endSouth = new Circle(0,-offset,splitWidth/2);

        return new ArrayList<PhysShape>(Arrays.asList(
                new PhysShape[]{
                        center, splitEast, splitWest, splitNorth,splitSouth,
                        endEast, endWest, endSouth, endNorth
                }));
    }

    @Override
    public void configure(Config c){
        super.configure(c);
        sprite.setTexture("splitter_"+color.name);
    }

    public static final EntityStateFactory getFactory(){
        return new Factory();
    }

    public static class Factory extends EntityStateFactory{
        @Override
        public EntityState construct() {
            return new Splitter();
        }

        @Override
        public Class<? extends EntityState> getType() {
            return Splitter.class;
        }

        @Override
        public Config getConfig(){
            return new EnemyConfig();
        }

        @Override
        public void getAssets(ArrayList<String[]> assets){
            assets.add(new String[]{"texture","splitter_red"});
            assets.add(new String[]{"texture","splitter_green"});
            assets.add(new String[]{"texture","splitter_pink"});
            assets.add(new String[]{"sound","wood_wack"});
            assets.add(new String[]{"sound","ping"});
            assets.add(new String[]{"sound","pulse"});
            assets.add(new String[]{"texture","wave"});
            assets.add(new String[]{"sound","metal_hit"});
            assets.add(new String[]{"sound","metal_impact"});
        }
    }
}
