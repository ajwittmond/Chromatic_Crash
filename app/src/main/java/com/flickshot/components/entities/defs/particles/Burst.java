package com.flickshot.components.entities.defs.particles;

import com.flickshot.components.entities.defs.VisibleEntity;
import com.flickshot.components.graphics.Sprite;
import com.flickshot.geometry.Transformation;
import com.flickshot.geometry.Vector2d;
import com.flickshot.util.MutableDouble;

public abstract class Burst extends VisibleEntity{
	private final Sprite sprite;
	
	public double time = 1;
	private double t;
	private double maxScale = 2;
	
	private double initWidth,initHeight;
	
	public Burst(){
		super();
		setArtist(sprite = new Sprite("flare",new Transformation(new Vector2d(),new Vector2d(32,32),new MutableDouble(0))));
		sprite.z=1;
	}
}
