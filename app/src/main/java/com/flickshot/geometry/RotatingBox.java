package com.flickshot.geometry;

public interface RotatingBox extends Box{
	public double getTheta();
	public void setTheta(double theta);
	
	public double getBoxWidth();
	public void setBoxWidth(double boxWidth);
	
	public double getBoxHeight();
	public void setBoxHeight(double boxHeight);
}
