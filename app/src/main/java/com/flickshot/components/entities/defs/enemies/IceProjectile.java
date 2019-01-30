package com.flickshot.components.entities.defs.enemies;

import java.util.ArrayList;

import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.Entity;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.entities.defs.PuckState;
import com.flickshot.components.entities.defs.enemies.Flower.Factory;
import com.flickshot.components.entities.defs.enemies.Flower.FlowerConfig;
import com.flickshot.components.entities.defs.fx.Ice;
import com.flickshot.components.graphics.Sprite;
import com.flickshot.components.physics.Collider;
import com.flickshot.components.physics.Manifold;
import com.flickshot.components.physics.PhysShape;
import com.flickshot.components.physics.shapes.Circle;
import com.flickshot.config.Config;
import com.flickshot.geometry.Vector2d;
import com.flickshot.geometry.Transformation;
import com.flickshot.scene.Updater.UpdateEvent;
import com.flickshot.util.MutableDouble;

public class IceProjectile extends Enemy{
	public static String ENTITY_NAME = "ice_projectile";
	
	final Sprite sprite;
	
	public IceProjectile(){
		sprite = new Sprite("circle_small",new Transformation(collider.tx.translation,new Vector2d(32,32),new MutableDouble()));
		sprite.setTint(0,0,2);
		sprite.tintWeight=1;
		setArtist(sprite);
	}
	
	public void init(double x, double y){
		super.init(x,y);
	}
	
	public void update(UpdateEvent evt){
		super.update(evt);
		sprite.setTheta(collider.velocity.getDir());
	}
	
	@Override
	public void onCollision(Manifold m){
		kill();
		Collider other = (m.a==collider) ? m.b : m.a;
		Entity ice = Entities.getEntity(Ice.class);
		if(other.state instanceof PuckState){
			boolean frozen = false;
			for(int i = 0; i<ice.getSize(); i++){
				if(((Ice)ice.getState(i)).target==other) frozen = true;
			}
			if(!frozen){
				Ice.create(other);
			}
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
			return new IceProjectile();
		}

		@Override
		public Class<? extends EntityState> getType() {
			return IceProjectile.class;
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
