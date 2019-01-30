package com.flickshot.components.particles;

import com.flickshot.geometry.Box;

public final class ParticleType {
	public double life;
	public String texture="blank_white";
	public float tintWeight = 1;
	public float z;
	public float r11,g11,b11;
	public float r21,g21,b21;
	public float r12,g12,b12;
	public float r22,g22,b22;
	public float r13,g13,b13;
	public float r23,g23,b23;
	public float a1,a2,a3;
	public double dirMin;
	public double dirMax;
	public double dirInc;
	public double dirWiggle;
	public double speedMin;
	public double speedMax;
	public double speedInc;
	public double speedWiggle;
	public double width=1,height=1;
	public double scaleMin;
	public double scaleMax;
	public double scaleInc;
	public double scaleWiggle;
	public double orientationMin;
	public double orientationMax;
	public double orientationInc;
	public double orientationWiggle;
	public boolean additive;
	public boolean orientationRelative;
	public Box bounds;
	
	
	public void color1(float r1,float g1, float b1,float r2,float g2, float b2){
		r11 = r1;g11 = g1;b11 = b1;
		r21 = r2;g21 = g2;b21 = b2;
	}
	
	public void color2(float r1,float g1, float b1,float r2,float g2, float b2){
		r12 = r1;g12 = g1;b12 = b1;
		r22 = r2;g22 = g2;b22 = b2;
	}

	public void color3(float r1,float g1, float b1,float r2,float g2, float b2){
		r13 = r1;g13 = g1;b13 = b1;
		r23 = r2;g23 = g2;b23 = b2;
	}
	
	public void oneColor(){
		r12 = r11;g12 = g11;b12 = b11;
		r22 = r21;g22 = g21;b22 = b21;
		r13 = r11;g13 = g11;b13 = b11;
		r23 = r21;g23 = g21;b23 = b21;
	}
	
	public void twoColors(){
		r13 = r12;g13 = g12;b12 = b12;
		r23 = r22;g23 = g22;b22 = b22;
		
		r12 = (r11/2)+(r13/2);g12 = (g11/2)+(g13/2);b12 = (b11/2)+(b13/2);
		r22 = (r21/2)+(r23/2);g22 = (g21/2)+(g23/2);b22 = (b21/2)+(b23/2);
	}
	
	public void alpha(float a1,float a2,float a3){
		this.a1 = a1;
		this.a2 = a2;
		this.a3 = a3;
	}
	
	
	public void startDir(double min, double max){
		dirMin = min;dirMax = max;
	}
	
	public void dir(double inc, double wiggle){
		dirInc = inc; dirWiggle = wiggle;
	}
	
	public void startSpeed(double min, double max){
		speedMin = min;speedMax = max;
	}
	
	public void speed(double inc, double wiggle){
		speedInc = inc; speedWiggle = wiggle;
	}
	
	public void startScale(double min, double max){
		scaleMin = min;scaleMax = max;
	}
	
	public void scale(double inc, double wiggle){
		scaleInc = inc; scaleWiggle = wiggle;
	}
	
	public void startOrientation(double min, double max){
		orientationMin = min;orientationMax = max;
	}
	
	public void orientation(double inc, double wiggle){
		orientationInc = inc; orientationWiggle = wiggle;
	}
	
	public void dimensions(double width, double height){
		this.width = width;
		this.height = height;
	}
}

