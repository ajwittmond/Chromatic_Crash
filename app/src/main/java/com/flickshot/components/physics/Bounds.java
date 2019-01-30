package com.flickshot.components.physics;

import com.flickshot.geometry.Box;
import com.flickshot.geometry.Matrix2d;
import com.flickshot.geometry.Transformation;
import com.flickshot.geometry.Vector2d;

public class Bounds implements PhysShape{

	Bounds(){}

	@Override
	public double getMass(double density,Transformation tx) {
		return Double.POSITIVE_INFINITY;
	}

	@Override
	public double getInertia(double density,Transformation tx) {
		return Double.POSITIVE_INFINITY;
	}

	@Override
	public Box calcBounds(Matrix2d t, Box out) {
		return null;
	}

	@Override
	public Vector2d getCOM(Vector2d out) {
		return null;
	}
	
	
	@Override
	public boolean pointIsInside(double x, double y){
		throw new UnsupportedOperationException();
	}
	

}
