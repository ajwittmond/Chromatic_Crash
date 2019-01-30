package com.flickshot.components.graphics;

import com.flickshot.geometry.Box;
import com.flickshot.geometry.Vector2d;
import com.flickshot.util.MutableDouble;

import static com.flickshot.components.graphics.Graphics.*;

public class Screen implements Box{
	
	public Vector2d position;
	public MutableDouble scale;
	public MutableDouble orientation;
	
	/**
	 * whether position is the corner or the center
	 */
	public boolean useCenter;
	
	Screen(){
		position = new Vector2d();
		scale = new MutableDouble(1);
		orientation = new MutableDouble(0);
	}
	
	Screen(Vector2d position,MutableDouble scale,MutableDouble orientation,boolean useCenter){
		this.position = new Vector2d();
		this.scale = new MutableDouble(1);
		this.orientation = new MutableDouble(0);
		this.useCenter = useCenter;
	}
	
	Screen(double x, double y, double scale, double orientation,boolean useCenter){
		position = new Vector2d(x,y);
		this.scale = new MutableDouble(scale);
		this.orientation = new MutableDouble(orientation);
		this.useCenter = useCenter;
	}
	
	@Override
	public double getX() {
		return useCenter ? position.x  - (getWidth()/2) : position.x;
	}

	@Override
	public void setX(double x) {
		position.x = useCenter ? x + (getWidth()/2) : x ; 
	}

	@Override
	public double getY() {
		return useCenter ? position.y  - (getHeight()/2) : position.y;
	}

	@Override
	public void setY(double y) {
		position.y = useCenter ? y + (getWidth()/2) : y; 
	}

	@Override
	public double getWidth() {
		return scale.val * aspect_ratio * screen_height;
	}

	@Override
	public void setWidth(double width) {
		scale.val = width/(aspect_ratio * screen_height);
	}

	@Override
	public double getHeight() {
		return scale.val * screen_height;
	}

	@Override
	public void setHeight(double height) {
		scale.val = height/screen_height;
	}

	@Override
	public double getCX() {
		return useCenter ? position.x : position.x - (getWidth()/2);
	}

	@Override
	public void setCX(double cx) {
		position.x = useCenter ? cx  : cx + (getWidth()/2); 
	}

	@Override
	public double getCY() {
		return useCenter ? position.y : position.y  - (getHeight()/2) ;
	}

	@Override
	public void setCY(double y) {
		position.y = useCenter ? y  : y + (getWidth()/2); 
	}
}
