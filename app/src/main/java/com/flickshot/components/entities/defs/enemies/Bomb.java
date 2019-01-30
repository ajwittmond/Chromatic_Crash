package com.flickshot.components.entities.defs.enemies;

import java.util.ArrayList;

import android.util.Log;

import com.flickshot.assets.AssetLibrary;
import com.flickshot.assets.sfx.Sound;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.entities.defs.enemies.Flower.Factory;
import com.flickshot.components.entities.defs.enemies.Flower.FlowerConfig;
import com.flickshot.components.entities.defs.fx.Explosion;
import com.flickshot.components.entities.defs.fx.Explosion.ExplosionConfig;
import com.flickshot.components.entities.defs.particles.Smoke;
import com.flickshot.components.entities.defs.particles.Spark;
import com.flickshot.components.graphics.CircleArtist;
import com.flickshot.components.graphics.Renderer2d;
import com.flickshot.components.graphics.Sprite;
import com.flickshot.components.particles.ParticleSystem;
import com.flickshot.components.particles.ParticleType;
import com.flickshot.components.particles.Particles;
import com.flickshot.components.physics.PhysShape;
import com.flickshot.components.physics.Physics;
import com.flickshot.components.physics.shapes.Circle;
import com.flickshot.config.Config;
import com.flickshot.geometry.Transformation;
import com.flickshot.geometry.Vector2d;
import com.flickshot.scene.Updater.UpdateEvent;
import com.flickshot.components.graphics.Artist;

public class Bomb extends Enemy{
	public static final String ENTITY_NAME = "bomb";
	public static final String EDITOR_SPRITE = "bomb;128;128;0";

	public static final ParticleType sparkType;
	public static final ParticleSystem spark;
	
	public static final ParticleType smokeType;
	public static final ParticleSystem smoke;
	
	static{
		sparkType = new ParticleType();
		sparkType.life = 1;
		sparkType.z = 1;
		sparkType.color1(1,0,0,1,1,0);
		sparkType.oneColor();
		sparkType.alpha(1,1,1);
		sparkType.dimensions(1,1);
		sparkType.startScale(90,90);
		sparkType.scale(-90,10);
		sparkType.startSpeed(600,600);
		sparkType.startDir(0,Math.PI*2);
		sparkType.orientation(Math.PI,0);
		sparkType.texture = "spark";
		
		spark=Particles.createSystem("spark",sparkType,256);
		
		smokeType = new ParticleType();
		smokeType.life = 1;
		smokeType.z = 2;
		smokeType.color1(0,0,0,0.5f,0.5f,0.5f);
		smokeType.oneColor();
		smokeType.alpha(1,0.5f,0);
		smokeType.dimensions(1,1);
		smokeType.startScale(64,90);
		smokeType.scale(90,0);
		smokeType.startSpeed(0,0);
		smokeType.texture = "flare";
		
		smoke=Particles.createSystem("smoke",smokeType,64);
	}
	
	private double width = 128;
	private Vector2d dimensions = new Vector2d();
	
	private final double scaleTime=0.2;
	private double scaleTimer;
	
	public Circle circle;
	
	double explosionRadius=512;
	double explosionForce=10000000;
	
	double startTime = 10;
	double time;
	
	Sprite sprite;
	
	private Sound fuse;
	
	public Bomb(){
		super();
		final Transformation stx = new Transformation(collider.tx.translation,dimensions,collider.tx.theta);
		sprite = new Sprite("bomb",stx);
		setArtist(new Artist(){

			@Override
			public boolean isOnScreen(double screenX, double screenY,
					double screenWidth, double screenHeight) {
				return sprite.isOnScreen(screenX,screenY,screenWidth,screenHeight);
			}

			@Override
			public void draw(double delta, Renderer2d renderer) {
				double u = time/startTime;
				renderer.push();
					renderer.transform(stx);
					renderer.translate(0,0,0.1);
					renderer.color(color.r*u,color.g*u,color.b*u,1);
					renderer.setDrawMode(Renderer2d.FILL);
					renderer.shape(Renderer2d.ELLIPSE);
				renderer.pop();
				sprite.draw(delta,renderer);
			}
			
		});
		sprite.setTint(1,0,0);
		randColor();
		killForce = 20000000;
		collisionSound = new Sound("wood_wack");
		fuse = new Sound("fuse");
		deathSound = new Sound("porcelain_break");
	}
	
	public void init(double x, double y){
		super.init(x,y);
		sprite.tintWeight = 0;
		time = startTime = 10;
		scaleTimer = 0;
		fuse.play();
		sparkType.bounds = Physics.getScene(0);
        deathSound.setVolume(0.5f,0.5f);
	}
	
	public void update(UpdateEvent evt){
		super.update(evt);
		scaleTimer=Math.min(scaleTime,scaleTimer+evt.getDelta());
		double u = scaleTimer/scaleTime;
		dimensions.set(width*u,width*u);
		
		time-=evt.getDelta();
		
		double v = Math.sqrt((1-(time/startTime)));
		fuse.setVolume((float)v,(float)v);
		
		if(time<=0){
			explode();
		}
		
		if(Math.random()<evt.getDelta()*v*10){
			spark.create(1,collider.tx.translation.x,collider.tx.translation.y);
		}
	}
	
	public void explode(){
		Explosion.create(collider.tx.translation.x,collider.tx.translation.y,explosionRadius,explosionForce);
		deathSound.setVolume(0,0);
		kill();
	}
	
	
	public void destroy(){
		smoke.create(1,collider.tx.translation.x,collider.tx.translation.y);
		for(int i = 0; i<8; i++){
			double mag = (width/2)*Math.random();
			double theta = Math.PI*2*Math.random();
			double y =Math.sin(theta)*mag;
			double x =Math.cos(theta)*mag;
			smoke.create(1,collider.tx.translation.x+x,collider.tx.translation.y+y);
		}
		fuse.stop();
		super.destroy();
	}
	
	public void unload(){
		super.unload();	
		fuse.stop();
	}
	
	@Override
	protected ArrayList<PhysShape> getShapes() {
		if(circle==null)circle=new Circle(0,0,64);
		return shapeAsList(circle);
	}
	
	
	public void configure(Config c){
		super.configure(c);
		if(c instanceof BombConfig){
			BombConfig b = (BombConfig)c;
			this.time = b.time;
			this.startTime = b.time; 
			this.explosionForce = b.explosion.force;
			this.explosionRadius = b.explosion.radius;
		}
	}
	
	public static final EntityStateFactory getFactory(){
		return new Factory();
	}
	
	public static class Factory extends EntityStateFactory{
		@Override
		public EntityState construct() {
			return new Bomb();
		}

		@Override
		public Class<? extends EntityState> getType() {
			return Bomb.class;
		}
		
		@Override
		public Config getConfig(){
			return new BombConfig();
		}
		
		@Override
		public void getAssets(ArrayList<String[]> assets){
			assets.add(new String[]{"texture","bomb"});
			assets.add(new String[]{"texture","spark"});
			assets.add(new String[]{"texture","explosion_part"});
			assets.add(new String[]{"sound","wood_wack"});
			assets.add(new String[]{"sound","blast"});
			assets.add(new String[]{"sound","porcelain_break"});
			assets.add(new String[]{"sound","fuse"});
		}
	}
	
	public static class BombConfig extends EnemyConfig{
		public ExplosionConfig explosion = new ExplosionConfig();
		
		public double time=10;
		
		public BombConfig(){}
	}

}
