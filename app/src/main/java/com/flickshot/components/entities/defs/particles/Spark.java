package com.flickshot.components.entities.defs.particles;

import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.entities.defs.enemies.Flower;
import com.flickshot.components.graphics.Sprite;
import com.flickshot.components.physics.Physics;
import com.flickshot.geometry.Transformation;
import com.flickshot.geometry.Vector2d;
import com.flickshot.scene.Updater.UpdateEvent;
import com.flickshot.util.MutableDouble;

public class Spark extends MovingParticle{
	public static String ENTITY_NAME = "spark";
	
	private final Transformation stx;
	
	private final Vector2d dimensions = new Vector2d();
	
	final double time = 1;
	
	double t;
	
	public Spark(){
		stx = new Transformation(new Vector2d(),new Vector2d(48,48),new MutableDouble());
		sprite = new Sprite("spark",stx);
		setArtist(sprite);
		sprite.z=1;
	}
	
	public void init(double x, double y){
		super.init(x,y);
		bounds = Physics.getScene(Physics.getSceneIndex("main"));
		sprite.alpha=1;
		double a = (Math.random()*2.0)-1.0;
		angularVelocity = Math.PI*4*(a/Math.abs(a));
		t=time;
	}
	
	public void update(UpdateEvent evt){
		super.update(evt);
		t-=evt.getDelta();
		double u = t/time;
		sprite.setBoxWidth(dimensions.x*u);
		sprite.setBoxHeight(dimensions.y*u);
		if(t<0)kill();
	}
	
	public static void create(double x, double y, double w, double h, float r, float g, float b, double vx , double vy){
		Spark spark = (Spark)Entities.getEntity(Spark.class).newInstance(x,y);
		spark.dimensions.set(w,h);
		spark.sprite.setTint(r,g,b);
		spark.sprite.tintWeight = 1;
		spark.velocity.set(vx,vy);
	}
	
	public static final EntityStateFactory getFactory(){
		return new EntityStateFactory(){
			@Override
			public EntityState construct() {
				return new Spark();
			}

			@Override
			public Class<? extends EntityState> getType() {
				return Spark.class;
			}
		};
	}
	

}
