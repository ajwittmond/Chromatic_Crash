package com.flickshot.components.entities.defs;

import java.util.Arrays;

import android.hardware.SensorManager;
import android.opengl.Matrix;

import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.graphics.Artist;
import com.flickshot.components.graphics.DrawLib;
import com.flickshot.components.graphics.Renderer2d;
import com.flickshot.components.physics.PhysShape;
import com.flickshot.components.physics.Collider;
import com.flickshot.components.physics.Manifold;
import com.flickshot.components.physics.PhysMaterial;
import com.flickshot.components.physics.Physics;
import com.flickshot.components.physics.shapes.Circle;
import com.flickshot.components.physics.shapes.Polygon;
import com.flickshot.geometry.Box;
import com.flickshot.geometry.Transformation;
import com.flickshot.geometry.Vector2d;
import com.flickshot.scene.Updater.UpdateEvent;
import com.flickshot.util.MatrixStack;

import java.util.ArrayList;

public class ManifestState extends EntityState{
	public static final String ENTITY_NAME = "Manifest";
	public Artist artist;
	public Collider collider;
	public static Circle a = new Circle(0,64,48);
	public static Circle b = new Circle(0,-64,48);
	public static Circle c = new Circle(64,0,48);
	public static Circle d = new Circle(-64,0,48);
	
	ManifestState(){
		collider = new Collider(0,0,new ArrayList<PhysShape>(Arrays.asList(new PhysShape[]{a,b,c,d})),new PhysMaterial(1,0.2,0.9,0.8),this,true,true);
		
		
		final Vector2d vec = new Vector2d();
		artist = new Artist(){

			@Override
			public boolean isOnScreen(double screenX, double screenY,
					double screenWidth, double screenHeight) {
				return true;
			}

			@Override
			public void draw(double delta, Renderer2d renderer) {
				Transformation tx = collider.getTransformation();
				renderer.transform(tx);
				drawCirc(renderer,a);
				drawCirc(renderer,b);
				drawCirc(renderer,c);
				drawCirc(renderer,d);
			}
			
			private void drawCirc(Renderer2d renderer ,Circle c ){
				renderer.push();
					renderer.translate(c.getCX(),c.getCY());
					renderer.scale(c.radius*2,c.radius*2);
					renderer.color(0f,0f,0f,1f);
					renderer.setDrawMode(Renderer2d.FILL);
					renderer.shape(Renderer2d.ELLIPSE);
					renderer.color(1f,1f,1f,1f);
					renderer.setDrawMode(Renderer2d.STROKE);
					renderer.shape(Renderer2d.ORIENTABLE_ELLIPSE);
				renderer.pop();
			}
			
		};
	}
	
	@Override
	public void preUpdate(UpdateEvent evt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(UpdateEvent evt) {
		if(!Double.isInfinite(collider.massData.mass))collider.addGravity();
	}

	@Override
	public void postUpdate(UpdateEvent evt) {
		// TODO Auto-generated method stub
		
	}
	
	private boolean collided = false;
	
	@Override
	public void init(double x, double y) {
		collider.tx.translation.set(x,y);
		collider.dragMul = 0.002;
		collider.bind();
		artist.bind();
	}

	@Override
	public boolean active() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean alive() {
		// TODO Auto-generated method stub
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
				return new ManifestState();
			}

			@Override
			public Class<? extends EntityState> getType() {
				return ManifestState.class;
			}
		};
	}
}
