package com.flickshot.components.entities.defs;

import android.util.Log;

import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.Entity;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.graphics.Artist;
import com.flickshot.components.graphics.DrawLib;
import com.flickshot.components.graphics.Renderer2d;
import com.flickshot.components.physics.Collider;
import com.flickshot.components.physics.Manifold;
import com.flickshot.components.physics.PhysMaterial;
import com.flickshot.components.physics.shapes.Circle;
import com.flickshot.components.physics.shapes.Polygon;
import com.flickshot.geometry.Box;
import com.flickshot.geometry.Transformation;
import com.flickshot.geometry.Vector2d;
import com.flickshot.scene.Updater.UpdateEvent;
import com.flickshot.util.MatrixStack;

public class BasicBoxEnemy extends EntityState{
	public static final String ENTITY_NAME = "BasicBoxEnemy";
	
	Polygon polygon = new Polygon(0,2,4,new double[]{
			 64,  64,
				-64,  64,
				-64, -64,
				 64, -64
			},0,0);
	public Collider collider;
	Artist artist;
	
	private Vector2d dimensions = new Vector2d(128,128);
	
	public boolean alive = true;
	
	BasicBoxEnemy(){
		collider = new Collider(0,0,polygon,new PhysMaterial(1,0.2,0.9,0.8),this,true,true){
			@Override
			public void onCollision(Manifold m){
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
				renderer.shape(Renderer2d.SQUARE);
				renderer.color(1f,1f,1f,1f);
				renderer.setDrawMode(Renderer2d.STROKE);
				renderer.shape(Renderer2d.SQUARE);
				
			}
			
		};
	}
	
	@Override
	public void preUpdate(UpdateEvent evt) {}

	
	@Override
	public void update(UpdateEvent evt) {
		Entity puck = Entities.getEntity("Puck");
		if(puck.getSize()>0){
			PuckState p = ((PuckState)puck.getState(0));
			double dir = collider.tx.translation.getDir(p.collider.tx.translation);
			collider.accelerateToSpeed(dir,1000,500);
			collider.angularDeccelerate(50,evt.getDelta());
		}
	}

	@Override
	public void postUpdate(UpdateEvent evt) {
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
		alive = true;
		collider.tx.translation.set(x,y);
		collider.velocity.set(0,0);
		collider.angularVelocity = 0;
		collider.bind();
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
				return new BasicBoxEnemy();
			}

			@Override
			public Class<? extends EntityState> getType() {
				return BasicBoxEnemy.class;
			}
		};
	}
}
