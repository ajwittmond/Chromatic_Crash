package com.flickshot.components.entities.defs.fx;

import java.util.ArrayList;
import java.util.HashMap;

import android.util.Log;

import com.flickshot.assets.sfx.Sound;
import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.Entity;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.entities.defs.PuckState;
import com.flickshot.components.entities.defs.VisibleEntity;
import com.flickshot.components.entities.defs.enemies.Enemy;
import com.flickshot.components.entities.defs.enemies.Flower;
import com.flickshot.components.entities.defs.enemies.Flower.Factory;
import com.flickshot.components.entities.defs.enemies.Flower.FlowerConfig;
import com.flickshot.components.entities.defs.enemies.blocks.Block;
import com.flickshot.components.entities.defs.particles.ExplosionPart;
import com.flickshot.components.entities.defs.particles.Spark;
import com.flickshot.components.graphics.Artist;
import com.flickshot.components.graphics.Renderer2d;
import com.flickshot.components.particles.ParticleSystem;
import com.flickshot.components.particles.ParticleType;
import com.flickshot.components.particles.Particles;
import com.flickshot.components.physics.Physics;
import com.flickshot.config.Config;
import com.flickshot.geometry.Vector2d;
import com.flickshot.geometry.collision.CollisionLib;
import com.flickshot.scene.Updater.UpdateEvent;
import com.flickshot.components.entities.defs.PhysObject;

public class Explosion extends VisibleEntity{
	public static String ENTITY_NAME = "explosion";
	
	public static final ParticleType flareType;
	public static final ParticleSystem flare;
	
	static{
		flareType = new ParticleType();
		flareType.life = 1;
		flareType.z = 1;
		flareType.color1(1,0,0,1,1,0);
		flareType.oneColor();
		flareType.alpha(1,1,1);
		flareType.dimensions(1,1);
		flareType.startScale(32,90);
		flareType.scale(-90,0);
		flareType.startSpeed(300,600);
		flareType.startDir(0,Math.PI/2);
		flareType.texture = "flare";
		
		flare=Particles.createSystem("flare",flareType,256);
	}
	
	
	final int ringParticles = 32;
	final int sparks = 24;
	
	public double radius;
	public double force;
	
	private double startTime=0.25;
	private double time;
	
	public double x,y;
	
	private boolean exploded;
	
	private int damage = 30;

	private Sound blast;
	
	public Explosion(){
		this.setArtist(new Artist(){
			
			@Override
			public boolean isOnScreen(double screenX, double screenY,
					double screenWidth, double screenHeight) {
				double r = radius * (1-(time/startTime));
				return CollisionLib.boxBox(x-r,y-r,r*2,r*2,screenX,screenY,screenWidth,screenHeight);
			}
			
			@Override
			public void draw(double delta, Renderer2d renderer) {
//				double r = radius * (1-(time/startTime));
//				renderer.setDrawMode(Renderer2d.FILL);
//				renderer.color(1,0,0,1);
//				renderer.translate(x,y);
//				renderer.scale(r*2,r*2);
//				renderer.shape(Renderer2d.ELLIPSE);
			}
			
		});

		blast = new Sound("blast");
	}
	
	@Override
	public void init(double x,double y){
		super.init(x,y);
		this.x =x;
		this.y =y;
		time = startTime;
		exploded=false;
		flareType.bounds = Physics.getScene(0);
		blast.play();
	}
	
	
	private static final ArrayList<EntityState> states = new ArrayList<EntityState>();
	private static final Vector2d misc = new Vector2d();
	public void update(UpdateEvent evt){
		time -=evt.getDelta();
		if(time<=0)kill();
		if(!exploded){
			
			
			double theta = 0;
			double speed = Math.sqrt(2*ExplosionPart.deccel*radius);
			for(int i = 0; i<ringParticles; i++){
				ExplosionPart p = (ExplosionPart)Entities.newInstance(ExplosionPart.class,x,y);
				p.velocity.x = speed;
				p.velocity.setDir(theta);
				p.orientation = theta;
				theta+=(Math.PI*2)/ringParticles;
			}
			
			double sparkRadius = radius/2;
			for(int i =0; i<sparks; i++){
				theta = Math.PI*2*Math.random();
				double r = Math.random() * sparkRadius;
				double v = 200.0 + (1000.0*Math.random());
				Spark.create(x+(Math.cos(theta)*r),y+(Math.sin(theta)*r),90,90,1,(float)Math.random(),0,Math.cos(theta)*v,Math.sin(theta)*v);
			}
			
			final int particles = 32;
			for(int i = 0; i<particles; i++){
				theta = (Math.PI*2)*Math.random();
				double dist = sparkRadius * Math.random();
				speed = 300+Math.random()*300;
				flare.create(1,
						x + Math.cos(theta)*dist,
						y + Math.sin(theta)*dist,
						speed,theta,0,32+(64.0*Math.random()));
			}
			
			
			
			Entities.getStates(PhysObject.class,states);
			for(EntityState e:states){
				PhysObject p = (PhysObject)e;
				double dist = p.collider.tx.translation.dist(x,y);
				if(dist<radius){
					double mag = (force*(1-(dist/radius)));
					misc.set(p.collider.tx.translation);
					misc.sub(x,y);
					misc.setMag(mag);
					misc.mul(p.collider.massData.invMass);
					p.collider.velocity.add(misc);
					if(p instanceof Enemy){
						Enemy enemy = (Enemy)p;
						enemy.doForceDamage(mag);
						if(dist<radius/2){
							enemy.health--;
						}
                        if(enemy instanceof Block){
                            enemy.health -=5;
                        }
					}
				}
			}
			Entity puck = Entities.getEntity(PuckState.class);
			for(int i = 0; i<puck.getSize(); i++){
				PuckState p = (PuckState)puck.getState(i);
				double dist = p.collider.tx.translation.dist(x,y);
				if(dist<radius){
					double mag = (force*(1-(dist/radius)));
					misc.set(p.collider.tx.translation);
					misc.sub(x,y);
					misc.setMag(mag);
					misc.mul(p.collider.massData.invMass);
					p.collider.velocity.add(misc);
				}
				if(dist<radius/2){
					p.damage(damage);
					p.stun();
				}
			}
			exploded=true;
		}
		states.clear();
	}
	
	public static final EntityStateFactory getFactory(){
		return new Factory();
	}
	
	public static class Factory extends EntityStateFactory{
		@Override
		public EntityState construct() {
			return new Explosion();
		}
		
		@Override
		public Class<? extends EntityState> getType() {
			return Explosion.class;
		}
		
		@Override
		public Config getConfig(){
			return new ExplosionConfig();
		}
		
		@Override
		public void getAssets(ArrayList<String[]> assets){
			assets.add(new String[]{"texture","flower_red"});
			assets.add(new String[]{"texture","flower_green"});
			assets.add(new String[]{"texture","flower_pink"});
			assets.add(new String[]{"texture","petal_pink"});
			assets.add(new String[]{"texture","petal_green"});
			assets.add(new String[]{"texture","petal_red"});
		}
	}
	
	public static class ExplosionConfig extends Config{
		public double radius = 512;
		public double force = 30000000;
		
		public ExplosionConfig(){}
		
		@Override
		public void setValue(String text) {
		}
		
		@Override
		public void getAliases(HashMap<String, String> map) {
		}
		
	}
	
	public static void create(double x, double y, double radius, double force){
		Explosion ex = (Explosion)Entities.newInstance(Explosion.class,x,y);
		ex.force = force;
		ex.radius = radius;
	}
}
