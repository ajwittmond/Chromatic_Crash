package com.flickshot.components.entities.defs.pickups;

import java.util.ArrayList;

import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.entities.defs.PuckState;
import com.flickshot.components.entities.defs.enemies.Flower;
import com.flickshot.components.entities.defs.enemies.Flower.Factory;
import com.flickshot.components.entities.defs.enemies.Flower.FlowerConfig;
import com.flickshot.components.graphics.Sprite;
import com.flickshot.components.particles.ParticleSystem;
import com.flickshot.components.particles.ParticleType;
import com.flickshot.components.particles.Particles;
import com.flickshot.components.physics.PhysShape;
import com.flickshot.components.physics.shapes.Polygon;
import com.flickshot.config.Config;

public class Health extends Pickup{
	public static final String ENTITY_NAME = "health";
	public static final String EDITOR_SPRITE = "health_pickup;90;90;0";
	
	static ParticleSystem crosses;
	static ParticleType crossType;
	
	static{
		crossType = new ParticleType();
		crossType.life = 3;
		crossType.startOrientation(0,Math.PI*2);
		crossType.orientationInc = Math.PI;
		crossType.color1(0,1,0,0,1,0);
		crossType.oneColor();
		crossType.alpha(1,0.5f,0);
		crossType.startSpeed(0,100);
		crossType.startDir(0,Math.PI*2);
		crossType.startScale(32,64);
		crossType.scaleInc = 32;
		crossType.dimensions(1,1);
		crossType.texture = "cross";
		
		crosses = Particles.createSystem("crosses",crossType,32);
	}
	
	
	public Health(){
		maxLife = 15;
		collider.tx.scale.set(90,90);
		collider.resetMassData();
		setArtist(sprite = new Sprite("health_pickup",collider.tx));
	}
	
	
	@Override
	protected void doPickupAction() {
		PuckState p = (PuckState) Entities.getEntity(PuckState.class).getState(0);
		if(p!=null) p.heal(25);
		crosses.create(16,collider.tx.translation.x,collider.tx.translation.y);
	}

	@Override
	protected ArrayList<PhysShape> getShapes() {
		return shapeAsList(new Polygon(0,2,4,new double[]{
			 0.5,  0.5,
			-0.5,  0.5,
			-0.5, -0.5,
			 0.5, -0.5
		},0,0));
	}
	
	public static final EntityStateFactory getFactory(){
		return new Factory();
	}
	
	public static class Factory extends EntityStateFactory{
		@Override
		public EntityState construct() {
			return new Health();
		}

		@Override
		public Class<? extends EntityState> getType() {
			return Health.class;
		}
		
		@Override
		public Config getConfig(){
			return new PickupConfig();
		}
		
		@Override
		public void getAssets(ArrayList<String[]> assets){
			assets.add(new String[]{"texture","health_pickup"});
			assets.add(new String[]{"texture","cross"});
		}
	}

}
