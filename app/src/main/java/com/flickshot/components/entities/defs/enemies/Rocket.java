package com.flickshot.components.entities.defs.enemies;

import java.util.ArrayList;


import com.flickshot.assets.sfx.Sound;
import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.entities.defs.PuckState;
import com.flickshot.components.entities.defs.enemies.Flower.Factory;
import com.flickshot.components.entities.defs.enemies.Flower.FlowerConfig;
import com.flickshot.components.entities.defs.fx.Explosion;
import com.flickshot.components.entities.defs.particles.ExplosionPart;
import com.flickshot.components.graphics.Sprite;
import com.flickshot.components.physics.Collider;
import com.flickshot.components.physics.Manifold;
import com.flickshot.components.physics.PhysShape;
import com.flickshot.components.physics.shapes.Polygon;
import com.flickshot.config.Config;
import com.flickshot.geometry.Transformation;
import com.flickshot.geometry.Vector2d;
import com.flickshot.scene.Updater.UpdateEvent;

public class Rocket extends Enemy{
	private static final double WIDTH = 256;
	private static final double HEIGHT = WIDTH*(3.0/10.0);
	public static final String ENTITY_NAME = "rocket";
	public static final String EDITOR_SPRITE = "rocket_{COLOR};"+WIDTH+";"+HEIGHT+";0";
	
	private Polygon head;
	

	double explosionRadius=512;
	double explosionForce=10000000;
	
	Sprite sprite;
	
	private static final double acceleration = 500;
	
	public Rocket(){
		setArtist(sprite = new Sprite("rocket_red",
				new Transformation(
						collider.tx.translation,
						new Vector2d(WIDTH,HEIGHT),
						collider.tx.theta)));
		setPoints(15);
        deathSound = new Sound("concrete_step");
		killForce = 20000000;
        maxHealth = 1;
	}
	
	
	private final Vector2d misc = new Vector2d();
	private double fireT = 0.1;
	private double flareT = 0.5;
	public void update(UpdateEvent evt){
		super.update(evt);
		misc.setMag(acceleration*evt.getDelta());
		misc.setDir(collider.tx.theta.val);
		collider.velocity.add(misc);

        collider.angularDeccelerate(Math.PI,evt.getDelta());
		
		double px=collider.tx.translation.x+(Math.cos(collider.tx.theta.val)*(-WIDTH/2));
		double py=collider.tx.translation.y+(Math.sin(collider.tx.theta.val)*(-WIDTH/2));
		
		
		fireT-=evt.getDelta();
		if(fireT<=0){
			fireT=0.1;
			ExplosionPart p = (ExplosionPart)Entities.newInstance(ExplosionPart.class,px,py);
			p.angularVelocity = Math.PI*4;
			p.orientation = Math.random()*Math.PI*2;
		}
		
		flareT-=evt.getDelta();
		if(flareT<=0){
			flareT=0.05;
			for(int i = 0; i<1; i++){
				double theta = (((Math.PI)*Math.random())-(Math.PI/2))+(collider.tx.theta.val+Math.PI);
				double speed = 100+Math.random()*100;
				misc.setMag(speed);
				misc.setDir(theta);
				misc.add(collider.velocity);
				Explosion.flare.create(1,
						px,
						py,
						misc.getMag(),misc.getDir(),0,32+(64.0*Math.random()));
			}
		}
	}
	
	@Override
	protected ArrayList<PhysShape> getShapes() {
		double bodyWidth=(12.0/20.0)*WIDTH;
		double bodyHeight=(4.0/6.0)*HEIGHT;
		double backGap = (3.0/20.0)*WIDTH;
		
		Polygon body = new Polygon(0,2,4,new double[]{
				backGap+bodyWidth-(WIDTH/2),  bodyHeight/2,
				backGap-(WIDTH/2),  bodyHeight/2,
				backGap-(WIDTH/2), -bodyHeight/2,
				backGap+bodyWidth-(WIDTH/2), -bodyHeight/2
		},0,0);
		
		head = new Polygon(0,2,3,new double[]{
				backGap+bodyWidth-(WIDTH/2), bodyHeight/2,
				backGap+bodyWidth-(WIDTH/2), -bodyHeight/2,
				(WIDTH/2), 0,
		},0,0);
		
		ArrayList<PhysShape> shapes = new ArrayList<PhysShape>();
		shapes.add(body);
		shapes.add(head);
		return shapes;
	}
	
	public void explode(){
		Explosion.create(collider.tx.translation.x,collider.tx.translation.y,explosionRadius,explosionForce);
		deathSound=null;
		kill();
	}
	
	@Override
	public void configure(Config c){
		super.configure(c);
		sprite.setTexture("rocket_"+color.name);
	}
	
	@Override
	public void destroy(){
		super.destroy();
		Bomb.smoke.create(1,collider.tx.translation.x,collider.tx.translation.y);
		for(int i = 0; i<12; i++){
			double y =(Math.random()*HEIGHT)-(HEIGHT/2);
			double x =(Math.random()*WIDTH)-(WIDTH/2);
			Bomb.smoke.create(1,collider.tx.translation.x+x,collider.tx.translation.y+y);
		}
	}
	
	@Override
	public void onCollision(Manifold m){
        Collider other  = (m.a==collider)? m.b: m.a;
		if(!(other.state instanceof PuckState) && alive() && health>=0 && (m.sa == head || m.sb == head) && m.forceSquared>=((killForce/2)*(killForce/2))){
			explode();
		}else{
			super.onCollision(m);
		}
	}
	
	public static final EntityStateFactory getFactory(){
		return new Factory();
	}
	
	public static class Factory extends EntityStateFactory{
		@Override
		public EntityState construct() {
			return new Rocket();
		}

		@Override
		public Class<? extends EntityState> getType() {
			return Rocket.class;
		}
		
		@Override
		public Config getConfig(){
			return new EnemyConfig();
		}
		
		@Override
		public void getAssets(ArrayList<String[]> assets){
			assets.add(new String[]{"texture","rocket_red"});
			assets.add(new String[]{"texture","rocket_green"});
			assets.add(new String[]{"texture","rocket_pink"});
			assets.add(new String[]{"sound","blast"});
            assets.add(new String[]{"sound","concrete_step"});
			assets.add(new String[]{"texture","spark"});
			assets.add(new String[]{"texture","explosion_part"});
		}
	}

}
