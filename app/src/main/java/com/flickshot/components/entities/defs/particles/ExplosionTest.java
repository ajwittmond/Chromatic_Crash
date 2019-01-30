package com.flickshot.components.entities.defs.particles;

import android.util.Log;

import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.entities.defs.VisibleEntity;
import com.flickshot.components.graphics.Sprite;
import com.flickshot.geometry.Transformation;
import com.flickshot.geometry.Vector2d;
import com.flickshot.scene.Updater.UpdateEvent;
import com.flickshot.util.MutableDouble;

public class ExplosionTest extends VisibleEntity{
	public static final String ENTITY_NAME = "explosion_test";
	public static final String EDITOR_SPRITE = "explosion;64;64;0";

	private final Vector2d dimensions = new Vector2d(128,128);
	private final Sprite sprite;
	
	public double time = 3;
	private double t;
	
	public ExplosionTest(){
		super();
		setArtist(sprite = new Sprite("explosion",new Transformation(new Vector2d(),new Vector2d(64,64),new MutableDouble())));
	}
	
	@Override
	public void init(double x, double y){
		super.init(x,y);
		sprite.setCX(x);
		sprite.setCY(y);
		sprite.setAnimation("explode");
		sprite.tintWeight=0;
		sprite.alpha=1;
		sprite.play();
	}
	
	@Override
	public void update(UpdateEvent evt){
		if(!sprite.isAnimating()) kill();
	}
	
	public static final EntityStateFactory getFactory(){
		return new EntityStateFactory(){
			@Override
			public EntityState construct() {
				return new ExplosionTest();
			}

			@Override
			public Class<? extends EntityState> getType() {
				return ExplosionTest.class;
			}
		};
	}
}
