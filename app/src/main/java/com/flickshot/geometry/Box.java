package com.flickshot.geometry;

public interface Box extends Coordinate{
	
	public double getWidth();
	public void setWidth(double width);
	
	public double getHeight();
	public void setHeight(double height);
	
	public double getCX();
	public void setCX(double cx);
	
	public double getCY();
	public void setCY(double cy);
}
