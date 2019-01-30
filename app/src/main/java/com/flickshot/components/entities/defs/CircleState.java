package com.flickshot.components.entities.defs;

import android.util.Log;

import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.graphics.Artist;
import com.flickshot.components.graphics.Renderer2d;
import com.flickshot.components.physics.Collider;
import com.flickshot.components.physics.Manifold;
import com.flickshot.components.physics.PhysMaterial;
import com.flickshot.components.physics.Physics;
import com.flickshot.components.physics.shapes.Circle;
import com.flickshot.geometry.Transformation;
import com.flickshot.geometry.Vector2d;
import com.flickshot.scene.Updater.UpdateEvent;

public class CircleState extends EntityState {
	public static final String ENTITY_NAME = "Circle";
	
	Circle circle = new Circle(0,0,32);
	public Collider collider;
	Artist artist;
	
	boolean collided;
	private Vector2d dimensions = new Vector2d(64,64);
	
	CircleState(){
		collider = new Collider(0,0,circle,new PhysMaterial(1,0.2,0.9,0.8),this,true,true){
			@Override
			public void onCollision(Manifold m){
				collided = true;
			}
		};
		collider.dragMul = 0.002; 
		final Transformation stx = new Transformation(collider.tx.translation,dimensions,collider.tx.theta);
		artist = new Artist(){

			@Override
			public boolean isOnScreen(double screenX, double screenY,
					double screenWidth, double screenHeight) {
				return true;
			}

			@Override
			public void draw(double delta, Renderer2d renderer) {
				renderer.color(0f,0f,0f,1f);
				renderer.setDrawMode(Renderer2d.FILL);
				renderer.transform(stx);
				renderer.shape(Renderer2d.ELLIPSE);
				renderer.color(1f,1f,1f,1f);
				renderer.setDrawMode(Renderer2d.STROKE);
				renderer.shape(Renderer2d.ORIENTABLE_ELLIPSE);
				
			}
			
		};
	}
	
	@Override
	public void preUpdate(UpdateEvent evt) {}

	@Override
	public void update(UpdateEvent evt) {
		collider.addGravity(); 
//		if(collided){
//			artist.setTint(1,0,0,1);
//			collided = false;
//		}else{
//			artist.setTint(1,1,1,1);
//		}
	}

	@Override
	public void postUpdate(UpdateEvent evt) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * arguments
	 * double cx
	 * double cy
	 * double radius
	 * double density
	 * double elasticity
	 */
	@Override
	public void init(double x, double y) {
		collider.tx.translation.set(x,y);
		collider.velocity.set(0,0);
		collider.bind();
		artist.bind();
	}

	@Override
	public boolean active() {
		return true;
	}

	@Override
	public boolean alive() {
		return true;
	}

	@Override
	public void destroy() {
		artist.unbind();
		collider.unbind();
	}

	@Override
	public void unload() {
		artist.unbind();
		collider.unbind();
	}
	
	public static final EntityStateFactory getFactory(){
		return new EntityStateFactory(){
			@Override
			public EntityState construct() {
				return new CircleState();
			}

			@Override
			public Class<? extends EntityState> getType() {
				return CircleState.class;
			}
		};
	}

}
