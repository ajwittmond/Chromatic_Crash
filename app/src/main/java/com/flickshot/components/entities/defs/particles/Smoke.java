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

public class Smoke extends VisibleEntity{
	public final static String ENTITY_NAME = "smoke";
	
	private final Sprite sprite;
	

	public double time = 1;
	private double t;
	private double maxScale = 2;
	private double minScale = 1;
	
	private double initWidth,initHeight;
	
	public Smoke(){
		super();
		setArtist(sprite = new Sprite("flare",new Transformation(new Vector2d(),new Vector2d(32,32),new MutableDouble(0))));
		sprite.z=1;
	}
	
	@Override
	public void init(double x, double y){
		super.init(x,y);
		sprite.setCX(x);
		sprite.setCY(y);
		sprite.z=1;
		initWidth = sprite.getBoxWidth();
		initHeight = sprite.getBoxHeight();
		sprite.alpha = 1;
		sprite.tintWeight = 1;
		t = time;
	}
	
	public void update(UpdateEvent evt){
		super.update(evt);
		t-=evt.getDelta();
		double u = t/time;
		sprite.alpha = (float)u;
		double s = minScale + (maxScale*(1-u));
		sprite.setBoxWidth(initWidth * s);
		sprite.setBoxHeight(initHeight * s);
		if(t<=0)
			kill();
	}
	
	public void setDimensions(double w, double h){
		initWidth = w;
		initHeight = h;
		sprite.setBoxWidth(w);
		sprite.setBoxHeight(h);
	}
	
	public static void create(double x, double y, double w, double h, float r, float g, float b){
		Smoke s = (Smoke)Entities.newInstance(Smoke.class,x,y);
		s.setDimensions(w,h);
		s.sprite.setTint(r,g,b);
	}
	public static final EntityStateFactory getFactory(){
		return new EntityStateFactory(){
			@Override
			public EntityState construct() {
				return new Smoke();
			}

			@Override
			public Class<? extends EntityState> getType() {
				return Smoke.class;
			}
		};
	}
}
