package com.flickshot.components.entities.defs;

import java.util.ArrayList;

import com.flickshot.FlickShot;
import com.flickshot.assets.AssetLibrary;
import com.flickshot.assets.sfx.Sound;
import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.entities.defs.enemies.Enemy;
import com.flickshot.components.entities.defs.fx.Explosion;
import com.flickshot.components.entities.defs.fx.TextBlob;
import com.flickshot.components.entities.defs.managers.ScoreTracker;
import com.flickshot.components.entities.defs.particles.Blob;
import com.flickshot.components.entities.defs.particles.Smoke;
import com.flickshot.components.graphics.Graphics;
import com.flickshot.components.graphics.Renderer2d;
import com.flickshot.components.graphics.Screen;
import com.flickshot.components.graphics.Sprite;
import com.flickshot.components.input.TouchEvent;
import com.flickshot.components.input.TouchListener;
import com.flickshot.components.input.TouchListenerInterface;
import com.flickshot.components.input.TouchManager;
import com.flickshot.components.physics.Collider;
import com.flickshot.components.physics.Manifold;
import com.flickshot.components.physics.PhysMaterial;
import com.flickshot.components.physics.shapes.Circle;
import com.flickshot.geometry.Transformation;
import com.flickshot.geometry.Vector2d;
import com.flickshot.scene.Scene;
import com.flickshot.scene.Updater.UpdateEvent;

public class PuckState extends EntityState{
    private static final double DEFAULT_ICONVERSION = 426.66666666666663;

	public static final String ENTITY_NAME = "Puck";
	public static final String EDITOR_SPRITE = "puck;96;96;0";
	
	public static final int START_HEALTH = 100;

    private static double MAX_SAMPLE_SPEED = 60;

	public Collider collider;
	Sprite sprite;
	TouchListenerInterface t;
	private double health = 100;
	double decel = 100;
	double SSsize = 0.8;
	private Vector2d dimensions = new Vector2d(96,96);
	private Vector2d previousPosition = new Vector2d();
	private boolean pressed;
	
	private boolean stunned;
	public boolean stunLock;
	
	private Sound collisionSound;
	
	private double targetDX,targetDY;

    private Sound grabSound = new Sound("click");

    private boolean alive;

    double ajustment;

	public PuckState(){
		super();
		collider = new Collider(0,0,new Circle(0,0,48),new PhysMaterial(1,0.4,0,0),this,true,true){
			public void onCollision(Manifold m){
				double force = Math.sqrt(m.forceSquared);
				if(collisionSound!=null && force> Enemy.SOUND_THRESHOLD){
					float gain = (float)Math.min(1,(force-Enemy.SOUND_THRESHOLD)/(Enemy.SOUND_MAX-Enemy.SOUND_THRESHOLD));
					gain*=gain;//scale exponentially
					gain*=0.5;
					collisionSound.setVolume(gain,gain);
					collisionSound.play();
				}

			}
			
		};
		collider.canRotate=false;
		Transformation stx = new Transformation(collider.tx.translation,dimensions,collider.tx.theta);
		sprite = new Sprite("puck",stx){
            public void draw(double delta, Renderer2d r){
                super.draw(delta, r);
//                r.translate(getCX(), getCY(), -20);
//                r.color(0, 0, 0);
//                r.push();
//                    r.color(1,0,0);
//                    r.shape(Renderer2d.ELLIPSE,sprite.getBoxWidth()*(5.0/4.0)*2,sprite.getBoxWidth()*(5.0/4.0)*2);
//                    r.shape(Renderer2d.ELLIPSE,sprite.getIConversion()*SSsize,sprite.getIConversion()*SSsize);
//                r.pop();
//                r.scale(90/r.textHeight(),90/r.textHeight());
//                r.text("" + ajustment + " " + sprite.getIConversion() + " " + DEFAULT_ICONVERSION);
            }
        };
		sprite.setTint(1,0,0,1);
		sprite.z = -1;
		
		collider.dragMul = 0.2;
		t = new TouchListener(){
			final Vector2d velocity = new Vector2d();

            final double filterStrength = 0.2;

			double px,py,dx,dy;
			long pt,dt;
			@Override
			public void onDown(TouchEvent evt) {

                if(alive) {
                    double x = TouchManager.x();
                    double y = TouchManager.y();
                    stunLock = false;
                    if (grab(x, y)) {
                        velocity.set(0,0);
                        collider.angularVelocity = 0;
                        collider.velocity.set(0, 0);
                        pressed = true;
                        collider.doMove = false;
                        px = x;
                        py = y;
                        pt = System.nanoTime();
                        stunned = false;
                        scoreGrab();
                        grabSound.play();
                    }
                }
			}

			@Override
			public void onMove(TouchEvent evt) {
                if(alive) {
                    double x = TouchManager.x();
                    double y = TouchManager.y();
                    if (pressed) {

                        //sprite.setCX( sprite.getCX()+dx);
                        //sprite.setCY( sprite.getCY()+dy);
                        long t = System.nanoTime();
                        long dtt = t - pt;
                        double delta = dtt / 1000000000.0;
                        if(delta< 1.0/MAX_SAMPLE_SPEED){
                            return;
                        }

                        dt = dtt;

                        collider.velocity.set(0, 0);
                        dx = x - px;
                        dy = y - py;
                        targetDX += dx;
                        targetDY += dy;
                        px = x;
                        py = y;




                        velocity.add(((dx/delta)-velocity.x)*filterStrength,((dy/delta)-velocity.y)*filterStrength);

                        if (!Double.isNaN(delta) && delta != 0) {
                            collider.velocity.x = dx / delta;
                            collider.velocity.y = dy / delta;
                        }
                        pt = t;
                    } else if (grab(x, y) && !stunLock) {
                        velocity.set(0,0);
                        stunned = false;
                        collider.angularVelocity = 0;
                        pressed = true;
                        collider.doMove = false;
                        collider.velocity.set(0, 0);
                        px = x;
                        py = y;
                        pt = System.nanoTime();
                        scoreGrab();
                        grabSound.play();
                    }
                }
			}

			@Override
			public void onUp(TouchEvent evt) {
				if(pressed){
                    ajustment = Math.max(1,1+(((DEFAULT_ICONVERSION/sprite.getIConversion())-1) * FlickShot.options.powerAssist));
					double delta = dt/1000000000.0;
					if(!Double.isNaN(delta) && delta!=0){
						collider.velocity.x = (dx/delta)*ajustment;
						collider.velocity.y = (dy/delta)*ajustment;
//                        velocity.mul(ajustment);
//                        collider.velocity.set(velocity);
					}
				}
				pressed = false;
				collider.doMove = true;
                targetDX = 0;
                targetDY = 0;
			}
			
		};
		collisionSound = new Sound("wood_wack");
        TouchManager.add(t);
	}
	
	public void stun(){
		if(!stunned){
			pressed = false;
			stunned = true;
			stunLock = true;
			collider.doMove = true;
			TextBlob.screenMessage("STUNNED!",1,0,0,1);
		}
	}
	
	private boolean grab(double x, double y){
		double dist = Math.sqrt(Math.pow(x-sprite.getCX(), 2)+Math.pow(y-sprite.getCY(), 2));
		return !Scene.getCurrent().updater.paused.get() &&
                (dist<sprite.getBoxWidth()*(5.0/4.0) || dist<sprite.getIConversion()*SSsize*0.5);
	}
	
	private void scoreGrab(){
		ScoreTracker s = (ScoreTracker)Entities.getEntity("scoreTracker").getState(0);
		if(s!=null)
			s.grab();
	}
	
	public boolean grabbed(){
		return pressed;
	}
	
	public boolean stunned(){
		return stunned;
	}
	
	@Override
	public void init(double x, double y) {
		collider.tx.translation.set(x,y);
		collider.velocity.set(0, 0);
		collider.force.set(0, 0);
		sprite.bind();
		collider.bind();
		previousPosition.set(x, y);
		TouchManager.setScreen(sprite.getScreen());
		health = START_HEALTH;
		stunned = false;
		pressed = false;
		stunLock = false;
        grabSound.setVolume(0.5f,0.5f);
        alive = true;
	}
	
	
	@Override
	public void preUpdate(UpdateEvent evt) {
		
		//limits movement per frame outside of the physics  simulation in
		//order to reduce probability of phasing through objects
		double maxDist = sprite.getBoxWidth()/8;
		if(targetDX!=0 || targetDY!=0){
			double dist = Vector2d.mag(targetDX,targetDY);
			if(dist<maxDist){
				sprite.setCX(sprite.getCX()+targetDX);
				sprite.setCY(sprite.getCY()+targetDY);
				targetDX = 0;
				targetDY = 0;
			}else{
				double u = maxDist/dist;
				sprite.setCX(sprite.getCX()+targetDX*u);
				sprite.setCY(sprite.getCY()+targetDY*u);
				targetDX-=targetDX*u;
				targetDY-=targetDY*u;
			}
		}
	}
	
	final double pdist = 10;
	double dist = 0;
	
	double st;
	@Override
	public void update(UpdateEvent evt) {
		
		
		
//		//mover.velocity.setMag(Math.max(mover.velocity.getMag()-(decel*evt.getDelta()),0));
//		Log.e("puck",sprite.getCX()+" "+collider.tx.translation.x);
//		dt+=evt.getDelta();
//		if(!pressed && dt>0.02){
//			Blob.create(collider.tx.translation.x,collider.tx.translation.y,32,32,0,1,0);
//			dt =0;
//		}
		if(stunned){
			sprite.tintWeight = 0.5f;
			sprite.setTint(0,0,0);
			st-=evt.getDelta();
			if(st<-0){
				st=0.1;
				Smoke.create(collider.tx.translation.x,collider.tx.translation.y,64,64,0.5f,0.5f,0.5f);
			}
		}else if(!pressed){
			double d = previousPosition.dist(collider.tx.translation);
			if(dist+d>pdist){
				double dx = collider.tx.translation.x-previousPosition.x;
				double dy = collider.tx.translation.y-previousPosition.y;
				double l = pdist - dist;
				do{
					double u = l/d;
					//Blob.create(previousPosition.x + dx*u,previousPosition.y + dy*u,32,32,0,1,0,collider.velocity.getDir());
					l+=pdist;
				}while(l+pdist<d);
				dist=d-l;
			}else{
				dist+=d;
			}
			sprite.tintWeight = 0;
		}else{
			sprite.tintWeight = 0.5f;
			sprite.setTint(1,0,0);
			dist = 0;
		}
		previousPosition.set(collider.tx.translation);
	}
	
	public double getHealth(){
		return health;
	}

    public void setHealth(double health){
        this.health = health;
    }
	
	public void damage(int dmg){
		health-=Math.abs(dmg);
		final int particles = 12;
		for(int i = 0; i<particles; i++){
			double theta = (Math.PI*2/particles)*i;
			Enemy.drop.create(1,collider.tx.translation.x ,collider.tx.translation.y,300,theta,0,64,1,0,0);
		}
	}
	
	public void heal(int heal){
		health=Math.min(100,Math.abs(heal)+health);
	}

	@Override
	public void postUpdate(UpdateEvent evt) {
	}

	@Override
	public boolean active() {
		return true;
	}

	@Override
	public boolean alive() {
		return health>0;
	}

	@Override
	public void destroy() {
		collider.unbind();
		sprite.unbind();
		//TouchManager.remove(t);
		final int particles = 64;
		for(int i = 0; i<particles; i++){
			double theta = (Math.PI*2)*Math.random();
			double dist = 48 * Math.random();
//			double r = Math.random();
//			double g = Math.min(r,Math.random());
			double speed = 300+Math.random()*300;
//			Drop.create(
//					x + Math.cos(theta)*dist,
//					y + Math.sin(theta)*dist,
//					32+(64.0*Math.random()),64+(32.0*Math.random()),
//					1,(float)(r),0,
//					Math.cos(theta)*speed,Math.sin(theta)*speed);
			Explosion.flare.create(1,
					collider.tx.translation.x + Math.cos(theta)*dist,
					collider.tx.translation.y + Math.sin(theta)*dist,
					speed,theta,0,32+(64.0*Math.random()));
		}

        Explosion.create(collider.tx.translation.x,collider.tx.translation.y,1000,10000000000.0);
        alive = false;
	}

	@Override
	public void unload() {
		collider.unbind();
		sprite.unbind();
		//TouchManager.remove(t);
        alive = false;
	}
	
	
	public static final EntityStateFactory getFactory(){
		return new EntityStateFactory(){
			@Override
			public EntityState construct() {
				return new PuckState();
			}

			@Override
			public Class<? extends EntityState> getType() {
				return PuckState.class;
			}
			
			@Override
			public void getAssets(ArrayList<String[]> assets){
				assets.add(new String[]{"texture","puck"});
				assets.add(new String[]{"texture","blank_white"});
				assets.add(new String[]{"texture","flare"});
                assets.add(new String[]{"texture","explosion_part"});
                assets.add(new String[]{"texture","spark"});
				assets.add(new String[]{"sound","wood_wack"});
                assets.add(new String[]{"sound","click"});
                assets.add(new String[]{"sound","blast"});
			}
		};
	}
}
