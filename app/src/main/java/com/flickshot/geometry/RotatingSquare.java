package com.flickshot.geometry;

import com.flickshot.util.MutableDouble;

public class RotatingSquare extends Transformable implements RotatingBox,Box{
	protected double width,height;
	private double c = 0,s=1;
	
	public RotatingSquare(Transformation tx){
		super(tx);
		calcDimensions();
	}
	
	public RotatingSquare(double cx, double cy,double boxWidth, double boxHeight){
		super(cx,cy,boxWidth,boxHeight,0);
		width = boxWidth;
		height = boxHeight;
	}
	
	public RotatingSquare(double cx, double cy,double boxWidth, double boxHeight, double theta){
		super(cx,cy,boxWidth,boxHeight,theta);
		setTheta(theta);
	}
	
	public void set(double cx, double cy, double boxWidth, double boxHeight){
		transformation.translation.set(cx,cy);
		this.setBoxWidth(boxWidth);
		this.setBoxHeight(boxHeight);
	}
	
	public void set(double cx, double cy, double boxWidth, double boxHeight, double theta){
		transformation.set(cx,cy,boxWidth,boxHeight,theta);
		calcDimensions();
	}
	
	public final void calcDimensions(){
		double boxWidth = transformation.scale.x;
		double boxHeight = transformation.scale.y;
		if(transformation.theta.val == 0){
			width = boxWidth;
			height = boxHeight;
			return;
		}
		
		c = Math.cos(transformation.theta.val);
		s = Math.sin(transformation.theta.val);
		
		double x1= -boxWidth/2;
		double y = -boxHeight/2;
		double x2= boxWidth/2;
		
		double xf1 = s*x1 - c*y;
		double yf1 = c*x1 + s*x1;
		double xf2 = s*x2 - c*y;
		double yf2 = c*x1 + s*x2;
		
		width = 2.0f*Math.max(Math.abs(xf1), Math.abs(xf2));
		height = 2.0f*Math.max(Math.abs(yf1), Math.abs(yf2));
	}
	
	private void calcBoxDimensions(){
		throw  new UnsupportedOperationException();
	}
	
	@Override
	public double getWidth() {
		return width;
	}

	@Override
	public void setWidth(double width) {
		this.width = width;
		calcBoxDimensions();
	}

	@Override
	public double getHeight() {
		return height;
	}

	@Override
	public void setHeight(double height) {
		this.height = height;
		calcBoxDimensions();
	}

	@Override
	public double getX() {
		return transformation.translation.x-width/2;
	}

	@Override
	public void setX(double x) {
		transformation.translation.x += x-getX();
	}

	@Override
	public double getY() {
		return transformation.translation.y-height/2;
	}

	@Override
	public void setY(double y) {
		transformation.translation.y += y-getY();
	}

	@Override
	public double getCX() {
		return transformation.translation.x;
	}

	@Override
	public void setCX(double cx) {
		transformation.translation.x = cx;
	}

	@Override
	public double getCY() {
		return transformation.translation.y;
	}

	@Override
	public void setCY(double cy) {
		transformation.translation.y = cy;
	}

	@Override
	public double getTheta() {
		return transformation.theta.val;
	}

	@Override
	public void setTheta(double theta) {
		transformation.theta.val = theta;
		c = (double)Math.cos(theta);
		s = (double)Math.sin(theta);
		calcDimensions();
	}

	@Override
	public double getBoxWidth() {
		return transformation.scale.x;
	}

	@Override
	public void setBoxWidth(double boxWidth) {
		transformation.scale.x = boxWidth;
		calcDimensions();
	}

	@Override
	public double getBoxHeight() {
		return transformation.scale.y;
	}

	@Override
	public void setBoxHeight(double boxHeight) {
		transformation.scale.y = boxHeight;
		calcDimensions();
	}
	
	@Override
	public void setTransformation(Transformation t) {
		transformation = t;
		calcDimensions();
	}

	@Override
	public void setTransformation(Vector2d translation, Vector2d scale,
			MutableDouble theta) {
		transformation = new Transformation(translation,scale,theta);
		calcDimensions();
	}

	@Override
	public void setTransformation(double x, double y, double w, double h,
			double theta) {
		transformation = new Transformation(x,y,w,h,theta);
		calcDimensions();
	}
	
	@Override
	public void setDimensions(double w, double h){
		transformation.scale.set(w,h);
		calcDimensions();
	}
	
	@Override
	public void setDimensions(Vector2d s){
		transformation.scale.set(s);
		calcDimensions();
	}
	
	@Override
	public void setOrientation(double theta){
		transformation.theta.val = theta;
		calcDimensions();
	}
}
