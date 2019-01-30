package com.flickshot.components.physics.shapes;

import android.util.Log;

import com.flickshot.components.physics.PhysShape;
import com.flickshot.geometry.Box;
import com.flickshot.geometry.Matrix2d;
import com.flickshot.geometry.Transformation;
import com.flickshot.geometry.Vector2d;
import com.flickshot.geometry.collision.CollisionLib;

public class Circle implements PhysShape,Box{

	public double radius;
	public Vector2d position;
	
	public Circle(){
		position = new Vector2d();
	}
	
	public Circle(Vector2d position, double radius){
		this.position = position;
		this.radius = radius;
	}
	
	public Circle(double x, double y, double radius){
		this.position = new  Vector2d(x,y);
		this.radius = radius;
	}
	
	public void set(double x, double y, double radius){
		position.set(x,y);
		this.radius = radius;
	}
	
	public void set(Vector2d position, double radius){
		position.set(position);
		this.radius = radius;
	}

	@Override
	public double getWidth() {
		return radius*2;
	}

	@Override
	public void setWidth(double width) {
		radius=width/2;
	}

	@Override
	public double getHeight() {
		return radius*2;
	}

	@Override
	public void setHeight(double height) {
		radius=height/2;
	}

	@Override
	public double getCX() {
		return position.x;
	}

	@Override
	public void setCX(double cx) {
		position.x=cx;
	}

	@Override
	public double getCY() {
		return position.y;
	}

	@Override
	public void setCY(double cy) {
		position.y = cy;
	}

	@Override
	public double getX() {
		return position.x-radius;
	}

	@Override
	public void setX(double x) {
		position.x = x+radius;
	}

	@Override
	public double getY() {
		return position.y - radius;
	}

	@Override
	public void setY(double y) {
		position.y = y+radius;
	}

	@Override
	public double getMass(double density,Transformation tx) {
		return getArea(tx)*density;
	}

	@Override
	public double getInertia(double density,Transformation tx) {
		double r = radius;
		r*=r;
		return (getMass(density,tx)*r)/2;
	}
	
	@Override
	public boolean pointIsInside(double x, double y){
		return CollisionLib.pointCircle(x,y,position.x,position.y,radius);
	}
	
	public double getArea(Transformation tx){
		return Math.PI*radius*tx.scale.x*radius*tx.scale.x;
	}

	private static final Vector2d misc = new Vector2d();
	@Override
	public Box calcBounds(Matrix2d m, Box out) {
		misc.set(position);
		m.transform(misc);
		out.setX(misc.x-radius);
		out.setY(misc.y-radius);
		out.setWidth(radius*2);
		out.setHeight(radius*2);
		return null;
	}
	
	@Override
	public Vector2d getCOM(Vector2d out){
		out.set(position);
		return out;
	}

}
