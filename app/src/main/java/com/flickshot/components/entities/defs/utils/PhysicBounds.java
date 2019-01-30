package com.flickshot.components.entities.defs.utils;

import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.graphics.Artist;
import com.flickshot.components.graphics.DrawLib;
import com.flickshot.components.graphics.Renderer2d;
import com.flickshot.components.physics.Physics;
import com.flickshot.components.physics.Scene;
import com.flickshot.scene.Updater.UpdateEvent;
import com.flickshot.util.MatrixStack;

public class PhysicBounds extends EntityState{
	public static final String ENTITY_NAME = "PhysicsBounds";
	public boolean alive = true;
	
	public static Artist artist;
	
	
	@Override
	public void preUpdate(UpdateEvent evt) {
	}

	@Override
	public void update(UpdateEvent evt) {
	}

	@Override
	public void postUpdate(UpdateEvent evt) {
	}

	@Override
	public void init(double x, double y) {
		alive = true; 
		if(artist == null){
			artist = new Artist(){

				@Override
				public boolean isOnScreen(double screenX, double screenY,
						double screenWidth, double screenHeight) {
					// TODO Auto-generated method stub
					return true;
				}

				@Override
				public void draw(double delta, Renderer2d renderer) {
					renderer.setDrawMode(Renderer2d.STROKE);
					renderer.color(1f,1f,1f,1f);
					renderer.setShape(Renderer2d.SQUARE);
					int n = Physics.sceneCount();
					for(int i =0;i<n;i++){
						Scene s = Physics.getScene(i);
						renderer.shape((float)(s.x+s.width/2), (float)(s.y+s.height/2),99, (float)(s.width), (float)(s.height));
					}
				}
				
				
				
			};
		}
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
				return new PhysicBounds();
			}

			@Override
			public Class<? extends EntityState> getType() {
				return PhysicBounds.class;
			}
		};
	}
}
