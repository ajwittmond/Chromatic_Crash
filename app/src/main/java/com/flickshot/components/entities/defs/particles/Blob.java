package com.flickshot.components.entities.defs.particles;

import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.entities.defs.VisibleEntity;
import com.flickshot.components.graphics.Sprite;
import com.flickshot.geometry.Transformation;
import com.flickshot.geometry.Vector2d;
import com.flickshot.scene.Updater.UpdateEvent;
import com.flickshot.util.MutableDouble;

public class Blob extends VisibleEntity{
	public static final String ENTITY_NAME = "blob";
	
	
	private final Sprite sprite;
	
	public final double time = 1;
	private double t;
	
	private double w1,h1,w2,h2;
	
	public Blob(){
		super();
		setArtist(sprite = new Sprite("blank_white",new Transformation(new Vector2d(),new Vector2d(32,32),new MutableDouble(0))));
		sprite.z=-1;
	}
	
	@Override
	public void init(double x, double y){
		super.init(x,y);
		sprite.setCX(x);
		sprite.setCY(y);
		w1 = sprite.getBoxWidth();
		h1 = sprite.getBoxHeight();
		w2 = w1;
		h2 = h1;
		sprite.alpha = 1;
		sprite.tintWeight = 0;
		t = time;
	}
	
	public void update(UpdateEvent evt){
		super.update(evt);
		t-=evt.getRealDelta();
		double u = t/time;
		sprite.alpha = (float)u;
		sprite.setBoxWidth(w1 + ((1-u)*(w2-w1)));
		sprite.setBoxHeight(h1 + ((1-u)*(h2-h1)));
		if(t<=0)
			kill();
	}
	
	public void setDimensions(double w1, double h1, double w2, double h2){
		this.w1= w1;
		this.h1 = h1;
		this.w2 = w2;
		this.h2 = h2;
	}
	
	public static void create(String texture,double x, double y, double w1, double h1, double w2, double h2){
		Blob blob = (Blob)Entities.newInstance(Blob.class,x,y);
		blob.setDimensions(w1,h1,w2,h2);
		blob.sprite.setTexture(texture);
	}
	
	public static final EntityStateFactory getFactory(){
		return new EntityStateFactory(){
			@Override
			public EntityState construct() {
				return new Blob();
			}

			@Override
			public Class<? extends EntityState> getType() {
				return Blob.class;
			}
		};
	}
}
