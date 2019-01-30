package com.flickshot.components.entities.defs.fx;

import java.util.ArrayList;

import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.Entity;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.entities.defs.PhysObject;
import com.flickshot.components.entities.defs.VisibleEntity;
import com.flickshot.components.entities.defs.enemies.EnemyConfig;
import com.flickshot.components.entities.defs.enemies.IceProjectile;
import com.flickshot.components.entities.defs.enemies.IceProjectile.Factory;
import com.flickshot.components.graphics.Artist;
import com.flickshot.components.graphics.Renderer2d;
import com.flickshot.components.input.TouchEvent;
import com.flickshot.components.input.TouchManager;
import com.flickshot.config.Config;
import com.flickshot.scene.Updater.UpdateEvent;
import com.flickshot.components.input.TouchListener;
import com.flickshot.components.physics.Collider;
import com.flickshot.geometry.Vector2d;

public class Ice extends VisibleEntity{
	public static final String ENTITY_NAME = "ice";
	
	int maxHealth=3;
	int health;
	
	public Collider target;
	
	private final Vector2d position = new Vector2d();
	private double theta;
	
	private boolean first;
	
	private double size;
	
	public Ice(){
		setArtist(new Artist(){

			@Override
			public boolean isOnScreen(double screenX, double screenY,
					double screenWidth, double screenHeight) {
				return true;
			}

			@Override
			public void draw(double delta, Renderer2d renderer) {
				renderer.setDrawMode(Renderer2d.FILL);
				renderer.color(0,0,1,0.5f);
				renderer.translate(target.tx.translation.x,target.tx.translation.y,-5);
				renderer.shape(Renderer2d.ELLIPSE,size,size);
			}
			
		});
		TouchManager.add(new TouchListener(){

			@Override
			public void onDown(TouchEvent evt) {
				if(Vector2d.dist(TouchManager.x(),TouchManager.y(),position.x,position.y)<size)
					health--;
			}

			@Override
			public void onMove(TouchEvent evt) {
			}

			@Override
			public void onUp(TouchEvent evt) {
			}
			
		});
	}
	
	public void init(double x, double y){
		super.init(x,y);
		first = true;
		health=maxHealth;
	}
	
	public void update(UpdateEvent evt){
		if(health<=0)kill();
		if(first){
			position.set(target.tx.translation);
			theta = target.tx.theta.val;
			first = false;
		}
		target.velocity.set(0,0);
		target.angularVelocity = 0;
		target.tx.translation.set(position);
		target.tx.theta.val = theta;
	}
	
	public static void create(Collider target){
		Ice ice = (Ice)Entities.newInstance(Ice.class,0,0);
		ice.target = target;
		ice.size = Math.max(target.getBounds().getWidth(),target.getBounds().getHeight());
	}
	
	public static final EntityStateFactory getFactory(){
		return new Factory();
	}
	
	public static class Factory extends EntityStateFactory{
		@Override
		public EntityState construct() {
			return new Ice();
		}

		@Override
		public Class<? extends EntityState> getType() {
			return Ice.class;
		}
		
		@Override
		public Config getConfig(){
			return null;
		}
		
		@Override
		public void getAssets(ArrayList<String[]> assets){
			assets.add(new String[]{"texture","circle_small"});
		}
	}
}
