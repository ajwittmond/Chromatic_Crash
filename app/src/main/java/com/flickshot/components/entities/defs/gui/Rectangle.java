package com.flickshot.components.entities.defs.gui;

import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.graphics.Artist;
import com.flickshot.components.graphics.Renderer2d;
import com.flickshot.geometry.Square;

import android.view.MotionEvent;
import android.view.View;

public class Rectangle extends GuiElement{
	public static final String ENTITY_NAME = "Rectangle";
	
	public float r=1,g=1,b=1,a=1;
	
	public int drawMode = Renderer2d.FILL;
	
	public Rectangle(){
		setArtist(new Artist(){
			@Override
			public boolean isOnScreen(double screenX, double screenY,
					double screenWidth, double screenHeight) {
				return Square.boxCollision(getX(),getY(),getWidth(),getHeight(),screenX,screenY,screenWidth,screenHeight);
			}

			@Override
			public void draw(double delta, Renderer2d renderer) {
				renderer.setDrawMode(drawMode);
				renderer.color(r,g,b,a);
				renderer.shape(Renderer2d.SQUARE,getX(),getY(),z,width,height);
			}
		});
	}
	
	public void setColor(float r,float g, float b, float a){
		this.r=r;this.g=g;this.b=b;this.a=a;
	}
	
	public static EntityStateFactory getFactory(){
		return new EntityStateFactory(){
			@Override
			public EntityState construct() {
				return new Label();
			}

			@Override
			public Class<? extends EntityState> getType() {
				return Label.class;
			}
		};
	}
}
