package com.flickshot.components.entities.defs.pickups;

import java.util.ArrayList;

import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.entities.defs.managers.TimeAttackManager;
import com.flickshot.components.entities.defs.pickups.Health.Factory;
import com.flickshot.components.graphics.Sprite;
import com.flickshot.components.particles.ParticleSystem;
import com.flickshot.components.particles.ParticleType;
import com.flickshot.components.particles.Particles;
import com.flickshot.components.physics.PhysShape;
import com.flickshot.components.physics.shapes.Circle;
import com.flickshot.config.Config;
import com.flickshot.geometry.Transformation;
import com.flickshot.geometry.Vector2d;

public class Time extends Pickup{
	public static final String ENTITY_NAME = "time";
	public static final String EDITOR_SPRITE = "time_pickup;90;90;0";
	
	static ParticleSystem clocks;
	static ParticleType clockType;
	
	static{
		clockType = new ParticleType();
		clockType.life = 3;
		clockType.startOrientation(0,Math.PI*2);
		clockType.orientationInc = Math.PI;
		clockType.color1(0,1,0,0,1,0);
		clockType.oneColor();
		clockType.alpha(1,0.5f,0);
		clockType.startSpeed(0,100);
		clockType.startDir(0,Math.PI*2);
		clockType.startScale(32,64);
		clockType.scaleInc = 32;
		clockType.dimensions(1,1);
		clockType.texture = "clock";
		
		clocks = Particles.createSystem("clocks",clockType,32);
	}
	
	double bonusTime=15;
	
	public Time(){
		maxLife = 15;
		setArtist(sprite = new Sprite("time_pickup",
				new Transformation(collider.tx.translation,new Vector2d(90,90),collider.tx.theta)));
	}
	
	
	@Override
	protected void doPickupAction() {
		TimeAttackManager m = (TimeAttackManager)Entities.getEntity(TimeAttackManager.class).getState(0);
		if(m!=null){
			m.time.setTimer(Math.max(m.time.getStartTime(),m.time.getTimeLeft()+bonusTime),m.time.getTimeLeft()+bonusTime);
		}
		clocks.create(16,collider.tx.translation.x,collider.tx.translation.y);
	}

	@Override
	protected ArrayList<PhysShape> getShapes() {
		return shapeAsList(new Circle(0,0,45));
	}
	
	public static final EntityStateFactory getFactory(){
		return new Factory();
	}
	
	public static class Factory extends EntityStateFactory{
		@Override
		public EntityState construct() {
			return new Time();
		}

		@Override
		public Class<? extends EntityState> getType() {
			return Time.class;
		}
		
		@Override
		public Config getConfig(){
			return new PickupConfig();
		}
		
		@Override
		public void getAssets(ArrayList<String[]> assets){
			assets.add(new String[]{"texture","time_pickup"});
			assets.add(new String[]{"texture","clock"});
		}
	}
}
