package com.flickshot.components.physics;

import com.flickshot.geometry.Box;
import com.flickshot.geometry.Matrix2d;
import com.flickshot.geometry.Transformation;
import com.flickshot.geometry.Vector2d;

public interface PhysShape{
	public double getMass(double density,Transformation tx);
	
	public double getInertia(double density,Transformation tx);
	
	public Box calcBounds(Matrix2d matrix, Box out);
	
	public Vector2d getCOM(Vector2d out);
	
	public boolean pointIsInside(double x, double y);
}
