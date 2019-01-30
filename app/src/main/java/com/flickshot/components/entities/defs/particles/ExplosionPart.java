package com.flickshot.components.entities.defs.particles;

import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.entities.defs.VisibleEntity;
import com.flickshot.components.graphics.Sprite;
import com.flickshot.components.physics.Physics;
import com.flickshot.geometry.Transformation;
import com.flickshot.geometry.Vector2d;
import com.flickshot.scene.Updater.UpdateEvent;
import com.flickshot.util.MutableDouble;

public class ExplosionPart extends MovingParticle{
	public final static String ENTITY_NAME = "explosionPart";
	
	
	public static final double deccel = 1000;

	public final double time = 1;
	private double t;
	
	public ExplosionPart(){
		super();
		setArtist(sprite = new Sprite("explosion_part",new Transformation(new Vector2d(),new Vector2d(90,90),new MutableDouble(0))));
		sprite.z=1;
	}
	
	@Override
	public void init(double x, double y){
		super.init(x,y);
		sprite.alpha = 1;
		sprite.tintWeight = 1;
		sprite.setTint(1,0,0);
		t = time;
		double s = Math.random()-1;
		//angularVelocity = Math.PI*2*(s/Math.abs(s));
		bounds = Physics.getScene(Physics.getSceneIndex("main"));
	}
	
	public void update(UpdateEvent evt){
		super.update(evt);
		
		velocity.setMag(Math.max(0,velocity.getMag()-(deccel*evt.getDelta())));
		
		t -= evt.getDelta();
		sprite.setTint((float)(t/time),0,0);
		sprite.alpha = (float)(t/time);
		if(t<0)
			kill();
	}
	
	public static final EntityStateFactory getFactory(){
		return new EntityStateFactory(){
			@Override
			public EntityState construct() {
				return new ExplosionPart();
			}

			@Override
			public Class<? extends EntityState> getType() {
				return ExplosionPart.class;
			}
		};
	}
}