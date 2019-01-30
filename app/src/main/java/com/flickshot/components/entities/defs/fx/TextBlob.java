package com.flickshot.components.entities.defs.fx;

import android.util.Log;

import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.entities.defs.VisibleEntity;
import com.flickshot.geometry.Vector2d;
import com.flickshot.geometry.collision.CollisionLib;
import com.flickshot.scene.Updater.UpdateEvent;
import com.flickshot.components.graphics.Artist;
import com.flickshot.components.graphics.Graphics;
import com.flickshot.components.graphics.Renderer2d;
import com.flickshot.components.graphics.Screen;

public class TextBlob extends VisibleEntity{
	public static final String ENTITY_NAME = "textBlob";
	
	public double textSize1=64;
	public double textSize2=128;
	public String text;
	public String font = "default";
	
	public final Vector2d position = new Vector2d();
	public final double z = -1;
	
	public float r,g,b,a;
	
	public double time = 1;
	private double t = 0;
	
	public TextBlob(){
		setArtist(new Artist(){

			@Override
			public boolean isOnScreen(double screenX, double screenY,
					double screenWidth, double screenHeight) {
				return CollisionLib.pointBox(position.x,position.y,screenX,screenY,screenWidth,screenHeight);
			}

			@Override
			public void draw(double delta, Renderer2d renderer) {
				double u = t/time;
				renderer.font(font);
				renderer.translate(position.x,position.y,z);
				renderer.color(r,g,b,Math.sqrt(u)*a);
				double scale = (textSize1 + (textSize2-textSize1)*(1.0-Math.sqrt(u)))/renderer.fontSize();
				renderer.scale(scale,scale);
				renderer.align(Renderer2d.CENTER,Renderer2d.CENTER);
				renderer.text(text);
			}
			
		});
	}
	
	@Override
	public void init(double x, double y){
		super.init(x,y);
		position.set(x,y);
		t = time;
	}
	
	@Override
	public void update(UpdateEvent evt){
		super.update(evt);
		t -= evt.getRealDelta();
		if(t<=0){
			t=0;
			kill();
		}
	}
	
	public void setTime(double time){
		this.time = time;
		this.t = time;
	}
	
	public void setColor(float r,float g, float b, float a){
		this.r=r;this.g=g;this.b=b;this.a = a;
	}
	
	public void setSizes(double size1, double size2){
		this.textSize1 = size1;
		this.textSize2 = size2;
	}
	
	public static TextBlob create(String text, double x, double y,double size1, double size2, float r, float g, float b,float a){
		TextBlob t = (TextBlob)Entities.newInstance(TextBlob.class,x,y);
		t.text = text;
		t.setColor(r,g,b,a);
		t.setSizes(size1,size2);
		return t;
	}
	
	public static TextBlob screenMessage(String text, float r, float g, float b,float a){
		Screen screen = Graphics.getCurrentScene().screen;
		return TextBlob.create(text,screen.getCX(),screen.getCY(),512,1024,r,g,b,a);
	}
	
	public static final EntityStateFactory getFactory(){
		return new EntityStateFactory(){
			@Override
			public EntityState construct() {
				return new TextBlob();
			}

			@Override
			public Class<? extends EntityState> getType() {
				return TextBlob.class;
			}
		};
	}
}
