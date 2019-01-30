package com.flickshot.geometry;

import com.flickshot.util.MutableDouble;


public class Transformable implements TransformableInterface{
	protected Transformation transformation;
	
	public Transformable(){
		transformation = new Transformation();
	}
	
	public Transformable(Transformation t){
		transformation = t;
	}
	
	public Transformable(Vector2d translation, Vector2d scale, MutableDouble theta){
		transformation = new Transformation(translation,scale,theta);
	}
	
	public Transformable(double x, double y, double w, double h, double theta){
		transformation = new Transformation(x,y,w,h,theta);
	}

	@Override
	public Transformation getTransformation() {
		return transformation;
	}

	@Override
	public void setTransformation(Transformation t) {
		transformation = t;
	}

	@Override
	public void setTransformation(Vector2d translation, Vector2d scale,
			MutableDouble theta) {
		transformation = new Transformation(translation,scale,theta);
	}

	@Override
	public void setTransformation(double x, double y, double w, double h,
			double theta) {
		transformation = new Transformation(x,y,w,h,theta);
	}
	
	public void setPosition(double x, double y){
		transformation.translation.set(x,y);
	}
	
	public void setPosition(Vector2d pos){
		transformation.translation.set(pos);
	}
	
	public void setDimensions(double w, double h){
		transformation.scale.set(w,h);
	}
	
	public void setDimensions(Vector2d s){
		transformation.scale.set(s);
	}
	
	public void setOrientation(double theta){
		transformation.theta.val = theta;
	}
}
