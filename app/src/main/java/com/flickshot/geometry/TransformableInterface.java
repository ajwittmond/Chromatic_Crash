package com.flickshot.geometry;

import com.flickshot.util.MutableDouble;


public interface TransformableInterface {
	public Transformation getTransformation();
	
	public void setTransformation(Transformation t);
	
	public void setTransformation(Vector2d translation,Vector2d scale,MutableDouble theta);
	
	public void setTransformation(double x, double y, double w, double h, double theta);
}
