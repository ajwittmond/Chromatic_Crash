package com.flickshot.components.entities.defs.enemies;

import java.util.ArrayList;

import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.entities.defs.PuckState;
import com.flickshot.components.entities.defs.enemies.IceProjectile.Factory;
import com.flickshot.components.graphics.Sprite;
import com.flickshot.components.physics.Collider;
import com.flickshot.components.physics.Manifold;
import com.flickshot.components.physics.PhysShape;
import com.flickshot.components.physics.shapes.Circle;
import com.flickshot.config.Config;
import com.flickshot.geometry.Transformation;
import com.flickshot.geometry.Vector2d;
import com.flickshot.scene.Updater.UpdateEvent;
import com.flickshot.util.MutableDouble;

public class Bullet extends Enemy{
	public static String ENTITY_NAME = "bullet";
	
	final Sprite sprite;
	
	public int damage;

    private double stime = 0.1;

	public Bullet(){
		super();
		sprite = new Sprite("circle_small",new Transformation(collider.tx.translation,new Vector2d(32,32),new MutableDouble()));
		sprite.setTint(0,0,0);
		sprite.tintWeight=1;
        maxHealth=1;
		setArtist(sprite);
	}
	
	public void init(double x, double y){
		super.init(x,y);
		damage = 10;
	}
	
	public void update(UpdateEvent evt){
		super.update(evt);
		sprite.setTheta(collider.velocity.getDir());
        stime-=evt.getDelta();
        if(stime<=0){
            Bomb.smoke.create(1,collider.tx.translation.x,collider.tx.translation.y,0,0,Math.PI*Math.random()*2,32);
            stime = 0.03;
        }
	}
	
	@Override
	public void onCollision(Manifold m){
		kill();
		Collider other = (m.a==collider) ? m.b : m.a ;
		if(other.state instanceof PuckState){
//			((PuckState)other.state).stun();
			((PuckState)other.state).damage(damage);
		}
        if(other.state instanceof Enemy){
            if(other.state instanceof Bomb){
                ((Bomb)other.state).explode();
            }else if(other.state instanceof Rocket) {
                ((Rocket)other.state).explode();
            }else{
                ((Enemy) other.state).doDamage(1, m);
            }
        }

        final int particles = 3;
        double vtheta = m.normal.getDir();
        for(int i = 0; i<particles; i++){
            double theta = vtheta+((Math.PI*Math.random())-(Math.PI/2));
            drop.create(1,m.contacts[0].x,m.contacts[0].y,300,theta,0,16);
        }
	}
	
	@Override
	protected ArrayList<PhysShape> getShapes() {
		return shapeAsList(new Circle(0,0,16));
	}
	
	
	public static final EntityStateFactory getFactory(){
		return new Factory();
	}
	
	public static class Factory extends EntityStateFactory{
		@Override
		public EntityState construct() {
			return new Bullet();
		}

		@Override
		public Class<? extends EntityState> getType() {
			return Bullet.class;
		}
		
		@Override
		public Config getConfig(){
			return new EnemyConfig();
		}
		
		@Override
		public void getAssets(ArrayList<String[]> assets){
			assets.add(new String[]{"texture","circle_small"});
		}
	}
}
