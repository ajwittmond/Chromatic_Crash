package com.flickshot.components.entities.defs;

import com.flickshot.components.graphics.Sprite;
import com.flickshot.geometry.Transformation;
import com.flickshot.geometry.Vector2d;

public abstract class SpritePhysObject extends PhysObject{

	public final Sprite sprite;
	private final Vector2d dimensions = new Vector2d(64,64);
	
	public SpritePhysObject(){
		super();
		Transformation stx = new Transformation(collider.tx.translation,dimensions,collider.tx.theta);
		sprite = new Sprite(getTextureName(),stx);
		setArtist(sprite);
	}
	
	public final void setDimensions(double w, double h){
		dimensions.set(w,h);
	}
	
	protected abstract String getTextureName();
}
