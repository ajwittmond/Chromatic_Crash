package com.flickshot.components.graphics;

import android.util.Log;

import com.flickshot.components.physics.shapes.Circle;
import com.flickshot.geometry.Transformation;

public class CircleArtist extends Artist{
	private Circle circle;
	private Transformation stx;
	public float r,g,b,a;
	
	
	public CircleArtist(Circle circle,Transformation stx){
		this.circle = circle;
		this.stx = stx;
	}
	
	@Override
	public boolean isOnScreen(double screenX, double screenY,
			double screenWidth, double screenHeight) {
		return true;
	}
	
	public void setColor(float r, float g, float b, float a){
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}
	
	@Override
	public void draw(double delta, Renderer2d renderer) {
		renderer.color(r,g,b,a);
		renderer.setDrawMode(Renderer2d.FILL);
		renderer.translate(stx.translation.x,stx.translation.y);
		renderer.scale(circle.radius*2,circle.radius*2);
		renderer.scale(stx.scale.x,stx.scale.y);
		renderer.rotate(stx.theta.val);
		renderer.shape(Renderer2d.ELLIPSE);
		renderer.color(1f,1f,1f,1f);
		renderer.setDrawMode(Renderer2d.STROKE);
		renderer.shape(Renderer2d.ORIENTABLE_ELLIPSE);
	}

}
