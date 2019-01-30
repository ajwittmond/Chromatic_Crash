package com.flickshot.components.entities.defs;

import android.util.Log;

import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.Entity;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.graphics.Artist;
import com.flickshot.components.graphics.DrawLib;
import com.flickshot.components.physics.Collider;
import com.flickshot.components.physics.Manifold;
import com.flickshot.components.physics.PhysMaterial;
import com.flickshot.components.physics.shapes.Polygon;
import com.flickshot.geometry.Box;
import com.flickshot.geometry.Transformation;
import com.flickshot.geometry.Vector2d;
import com.flickshot.scene.Updater.UpdateEvent;
import com.flickshot.util.MatrixStack;

public class BasicTriangleEnemy extends EntityState{
	public static final String ENTITY_NAME = "BasicTriangleEnemy";
	
	Polygon polygon = new Polygon(0,2,3,new double[]{
			 0,  64,
				-64,  -64,
				64, -64,
			},0,0);
	public Collider collider;
	Artist sprite;
	
	private Vector2d dimensions = new Vector2d(64,64);
	
	public boolean alive = true;
	
	BasicTriangleEnemy(){
		collider = new Collider(0,0,polygon,new PhysMaterial(1,0.2,0.9,0.8),this,true,true){
			@Override
			public void onCollision(Manifold m){
			}
		};
		collider.dragMul = 0.002; 
		Transformation stx = new Transformation(collider.tx.translation,dimensions,collider.tx.theta);
		
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
		alive = true;
		collider.tx.translation.set(x,y);
		collider.velocity.set(0,0);
		collider.angularVelocity = 0;
		collider.bind();
		sprite.bind();
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
		sprite.unbind();
		collider.unbind();
	}

	@Override
	public void unload() {
		sprite.unbind();
		collider.unbind();
	}
	
	public static final EntityStateFactory getFactory(){
		return new EntityStateFactory(){
			@Override
			public EntityState construct() {
				return new BasicTriangleEnemy();
			}

			@Override
			public Class<? extends EntityState> getType() {
				return BasicTriangleEnemy.class;
			}
		};
	}
}
