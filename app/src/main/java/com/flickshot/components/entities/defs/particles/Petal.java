package com.flickshot.components.entities.defs.particles;

import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.entities.defs.VisibleEntity;
import com.flickshot.components.entities.defs.enemies.Flower;
import com.flickshot.components.graphics.Sprite;
import com.flickshot.components.physics.Physics;
import com.flickshot.geometry.Transformation;
import com.flickshot.geometry.Vector2d;
import com.flickshot.scene.Updater.UpdateEvent;
import com.flickshot.util.MutableDouble;

public class Petal extends MovingParticle{
	public static String ENTITY_NAME = "petal";
	
	private final Transformation stx;
	
	final double time = 2;
	
	double t;
	
	public Petal(){
		stx = new Transformation(new Vector2d(),new Vector2d(48,48),new MutableDouble());
		sprite = new Sprite("petal_pink",stx);
		setArtist(sprite);
		sprite.z=1;
	}
	
	public void init(double x, double y){
		super.init(x,y);
		bounds = Physics.getScene(Physics.getSceneIndex("main"));
		sprite.alpha=1;
		t=time;
	}
	
	public void update(UpdateEvent evt){
		super.update(evt);
		t-=evt.getDelta();
		sprite.alpha = (float)(t/time);
		if(t<0)kill();
	}
	
	private static final Vector2d temp = new Vector2d();
	public static void spawnFlower(Flower f){
		final int num = 12;
		temp.set(0,24);
		for(int i = 0; i<num; i++){
			double theta = ((Math.PI*2)/12)*i;
			temp.setDir(theta);
			Petal p =(Petal)Entities.newInstance(ENTITY_NAME,temp.x+f.collider.tx.translation.x,temp.y+f.collider.tx.translation.y);
			p.orientation = theta;
			p.velocity.add(temp.x*3,temp.y*3);
			p.sprite.setTexture("petal_"+f.getColor());
		}
	}
	
	public static final EntityStateFactory getFactory(){
		return new EntityStateFactory(){
			@Override
			public EntityState construct() {
				return new Petal();
			}

			@Override
			public Class<? extends EntityState> getType() {
				return Petal.class;
			}
		};
	}
	
}
