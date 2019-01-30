package com.flickshot.components.entities.defs.gui;

import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.graphics.Artist;
import com.flickshot.components.graphics.Renderer2d;
import com.flickshot.geometry.Square;

public class Bar extends GuiElement{
	public static final String ENTITY_NAME = "Bar";
	
	public boolean vertical=true;
	
	public float r=1,g=1,b=1,a=1;

	public boolean drawBorder = false;
	public float borderWidth = 2.0f;
	public float br=1,bg=1,bb=1,ba=1;
	
	private double value = 0;
	
	public Bar(){
		setArtist(new Artist(){
			@Override
			public boolean isOnScreen(double screenX, double screenY,
					double screenWidth, double screenHeight) {
				return Square.boxCollision(getX(),getY(),getWidth(),getHeight(),screenX,screenY,screenWidth,screenHeight);
			}

			@Override
			public void draw(double delta, Renderer2d renderer) {
				
				if(drawBorder){
					renderer.setDrawMode(Renderer2d.STROKE);
					renderer.lineWidth(borderWidth);
					renderer.color(br,bg,bb,ba);
					renderer.shape(Renderer2d.SQUARE,getCX(),getCY(),z,width,height);
				}
				
				renderer.setDrawMode(Renderer2d.FILL);
				renderer.color(r,g,b,a);
				if(vertical){
					double h = height*value;
					renderer.shape(Renderer2d.SQUARE,getCX(),getY()+(h/2),z,width,h);
				}else{
					double w = width*value;
					renderer.shape(Renderer2d.SQUARE,getX()+(w/2),getCY(),z,w,height);
				}
			}
		});
	}
	
	public final double getValue(){
		return value;
	}
	
	public final void setValue(double value){
		this.value = Math.min(Math.max(0,value),1);
	}
	
	public void setColor(float r,float g, float b, float a){
		this.r=r;this.g=g;this.b=b;this.a=a;
	}
	
	public void setBorderColor(float r,float g, float b, float a){
		this.br=r;this.bg=g;this.bb=b;this.ba=a;
	}
	
	public static EntityStateFactory getFactory(){
		return new EntityStateFactory(){
			@Override
			public EntityState construct() {
				return new Bar();
			}

			@Override
			public Class<? extends EntityState> getType() {
				return Bar.class;
			}
		};
	}
}
