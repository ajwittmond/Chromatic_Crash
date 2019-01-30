package com.flickshot.components.entities.defs.gui;

import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.entities.defs.VisibleEntity;
import com.flickshot.components.graphics.Artist;
import com.flickshot.components.graphics.Renderer2d;
import com.flickshot.geometry.Box;
import com.flickshot.geometry.Square;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class Label extends GuiElement{
	public static final String ENTITY_NAME = "Label";
	
	public double textSize=64;
	public String text;
	
	public String font = "default";
	
	float r=0,g=0,b=0,a=1;
	
	int alignX = Renderer2d.CENTER;
	int alignY = Renderer2d.CENTER;
	
	public boolean autoScale = false;
	
	public Label(){
		setArtist(new Artist(){
			@Override
			public boolean isOnScreen(double screenX, double screenY,
					double screenWidth, double screenHeight) {
				return Square.boxCollision(getX(),getY(),getWidth(),getHeight(),screenX,screenY,screenWidth,screenHeight);
			}

			@Override
			public void draw(double delta, Renderer2d renderer) {
				renderer.font(font);
				renderer.translate(getCX(),getCY(),z);
				renderer.color(r,g,b,a);
				double scale = textSize/renderer.fontSize();
				double twidth = renderer.textWidth(text);
				double theight = renderer.textHeight();
				if(autoScale || theight*scale > height ||twidth*scale > width){
					scale = Math.min(width/twidth,height/theight);
				}
				renderer.scale(scale,scale);
				renderer.align(alignX,alignY);
				renderer.text(text);
			}
		});
		//setRecievesInput(false);
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
