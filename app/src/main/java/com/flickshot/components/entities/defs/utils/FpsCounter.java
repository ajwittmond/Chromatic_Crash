package com.flickshot.components.entities.defs.utils;

import com.android.texample2.GLText;
import com.flickshot.GameView;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.entities.defs.BoxState;
import com.flickshot.components.graphics.Artist;
import com.flickshot.components.graphics.Renderer2d;
import com.flickshot.geometry.Box;
import com.flickshot.scene.UpdateQueue;
import com.flickshot.scene.Updater.UpdateEvent;
import com.flickshot.util.MatrixStack;

public class FpsCounter extends EntityState {
	public static final String ENTITY_NAME = "FpsCounter";
	public boolean alive = true;
	double lastFps;
	
	double x,y;

	Artist artist;
	
	FpsCounter(){
		artist = new Artist(){

			@Override
			public boolean isOnScreen(double screenX, double screenY,
					double screenWidth, double screenHeight) {
				// TODO Auto-generated method stub
				return true;
			}

			@Override
			public void draw(double delta, Renderer2d renderer) {
				renderer.font("default");
				Box s = getScreen();
				renderer.color(1f,1f,1f,1f);
				renderer.translate(s.getX()+x,s.getY()+y,-50);
				renderer.scale(2,2);
				renderer.text("fps1: "+(Math.round(lastFps*100)/100.0)+"  fps2: "+(Math.round(1/delta*100)/100.0));
			}
			
		};
	}
	
	@Override
	public void preUpdate(UpdateEvent evt) {
		lastFps = evt.getFilteredFps();
	}

	@Override
	public void update(UpdateEvent evt) {
	}

	@Override
	public void postUpdate(UpdateEvent evt) {
	}

	@Override
	public void init(double x, double y) {
		alive = false;
		this.x =x;
		this.y =y;
		artist.bind();
	}

	@Override
	public boolean active() {
		return true;
	}

	@Override
	public boolean alive() {
		return alive;
	}

	@Override
	public void destroy() {
		artist.unbind();
	}
	
	@Override
	public void unload() {
		artist.unbind();
	}
	
	public static final EntityStateFactory getFactory(){
		return new EntityStateFactory(){
			@Override
			public EntityState construct() {
				return new FpsCounter();
			}

			@Override
			public Class<? extends EntityState> getType() {
				return FpsCounter.class;
			}
		};
	}
	
}
