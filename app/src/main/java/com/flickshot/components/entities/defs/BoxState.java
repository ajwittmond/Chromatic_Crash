package com.flickshot.components.entities.defs;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.util.Log;

import com.flickshot.GameView;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.graphics.Artist;
import com.flickshot.components.graphics.DrawLib;
import com.flickshot.components.graphics.Renderer2d;
import com.flickshot.components.physics.Collider;
import com.flickshot.components.physics.Manifold;
import com.flickshot.components.physics.PhysMaterial;
import com.flickshot.components.physics.PhysShape;
import com.flickshot.components.physics.Physics;
import com.flickshot.components.physics.shapes.Polygon;
import com.flickshot.geometry.Box;
import com.flickshot.geometry.Transformation;
import com.flickshot.geometry.Vector2d;
import com.flickshot.scene.Updater.UpdateEvent;
import com.flickshot.util.MatrixStack;

public class BoxState extends PhysObject {
	public static final String ENTITY_NAME = "Box";
	public static Polygon polygon = new Polygon(0,2,4,new double[]{
		 64,  64,
		-64,  64,
		-64, -64,
		 64, -64
	},0,0);

	SensorManager manager;
	BoxState(){
		super();
		final Transformation stx = new Transformation(collider.tx.translation,new Vector2d(128,128),collider.tx.theta);
		setArtist( new Artist(){

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
			
		});
	}
	
	protected ArrayList<PhysShape> getShapes(){
		return shapeAsList(polygon);
	}

	@Override
	public void update(UpdateEvent evt) {
		if(!Double.isInfinite(collider.massData.mass))collider.addGravity();
	}
	
	private boolean collided = false;
	
	@Override
	public void init(double x, double y) {
		super.init(x,y);
		collider.dragMul = 0.002;
	}

	public static final EntityStateFactory getFactory(){
		return new EntityStateFactory(){
			@Override
			public EntityState construct() {
				return new BoxState();
			}

			@Override
			public Class<? extends EntityState> getType() {
				return BoxState.class;
			}
		};
	}
}
