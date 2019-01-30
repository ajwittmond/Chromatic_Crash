package com.flickshot.components.entities.defs.gui;

import java.util.ArrayList;

import android.view.MotionEvent;
import android.view.View;

import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.entities.defs.VisibleEntity;
import com.flickshot.components.graphics.Artist;
import com.flickshot.components.graphics.Renderer2d;
import com.flickshot.components.graphics.Sprite;
import com.flickshot.geometry.Square;

public class Window extends GuiElement{
	public static final String ENTITY_NAME = "window";
	
	private boolean opening = true;
	private boolean closing = false;
	
	protected final double t = 0.25;
	protected double dt = 0;
	
	private final ArrayList<WindowListener> listeners = new ArrayList<WindowListener>();
	
	public float borderSize;
	
	public float tileWidth,tileHeight;
	
	public Window(){
		setArtist(new Artist(){
			@Override
			public boolean isOnScreen(double screenX, double screenY,
					double screenWidth, double screenHeight) {
				return Square.boxCollision(getX(),getY(),getWidth(),getHeight(),screenX,screenY,screenWidth,screenHeight);
			}
			
			@Override
			public void draw(double delta, Renderer2d renderer) {
				if(opening){
					for(GuiElement child: children)child.setVisible(false);
					dt+=delta/t;
					if(dt>=1){
						dt=1;
						opening = false;
					}
				}else if(closing){
					for(GuiElement child: children)child.setVisible(false);
					dt-=delta/t;
					if(dt<=0){
						dt=0;
						kill();
					}
				}else{
					for(GuiElement child: children)child.setVisible(true);
				}
				renderer.translate(getCX(),getCY(),z);
				renderer.scale(dt,dt);
				drawWindow(delta,renderer);
			}
		});
	}
	
	@Override
	public void init(double x, double y){
		super.init(x,y);
		opening=true;
		closing=false;
		dt=0;
	}
	
	@Override
	public void destroy(){
		super.destroy();
		for(WindowListener l: listeners)l.onClose();
		listeners.clear();
	}
	
	@Override
	public void unload(){
		super.unload();
		listeners.clear();
	}
	
	public void addListener(WindowListener listener){
		listeners.add(listener);
	}
	
	protected void drawWindow(double delta, Renderer2d r){
		r.push();
			r.setDrawMode(Renderer2d.FILL);
			r.color(0,0,0,1);
			r.scale(width,height);
			r.shape(Renderer2d.SQUARE);
		r.pop();
		r.setDrawMode(Renderer2d.STROKE);
		r.lineWidth(4);
		r.color(1,1,1,1);
		r.scale(width-32,height-32);
		r.shape(Renderer2d.SQUARE);
	}
	
	public void close(){
		closing = true;
	}
	
	
	public static EntityStateFactory getFactory(){
		return new EntityStateFactory(){
			@Override
			public EntityState construct() {
				return new Window();
			}
			
			@Override
			public Class<? extends EntityState> getType() {
				return Window.class;
			}
		};
	}
	
	public static interface WindowListener{
		public void onClose();
	}

}
