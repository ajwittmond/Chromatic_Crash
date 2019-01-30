package com.flickshot.components.entities.defs.fx;

import java.util.ArrayList;

import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.entities.defs.PhysObject;
import com.flickshot.components.entities.defs.enemies.ChargeBox;
import com.flickshot.components.physics.Collider;
import com.flickshot.components.physics.PhysShape;
import com.flickshot.components.physics.shapes.Polygon;
import com.flickshot.components.graphics.Artist;
import com.flickshot.components.graphics.Renderer2d;
import com.flickshot.geometry.Transformation;
import com.flickshot.scene.Updater.UpdateEvent;

public class BoxFragment extends PhysObject{
	public static final String ENTITY_NAME = "charge_box_frag";
	
	private static final double WIDTH = 32;
	
	final double life = 2;
	double l;
	
	public BoxFragment(){
		setArtist(new Artist(){

			@Override
			public boolean isOnScreen(double screenX, double screenY,
					double screenWidth, double screenHeight) {
				return true;
			}

			@Override
			public void draw(double delta, Renderer2d renderer) {
				renderer.color(0,0,0,Math.pow(l/life,2));
				renderer.setDrawMode(Renderer2d.FILL);
				renderer.translate(collider.tx.translation.x,collider.tx.translation.y);
				renderer.scale(WIDTH,WIDTH);
				renderer.rotate(collider.tx.theta.val);
				renderer.shape(Renderer2d.SQUARE);
			}
			
		});
	}
	
	public void init(double x, double y){
		super.init(x,y);
		l = life;
	}
	
	public void update(UpdateEvent evt){
		super.update(evt);
		l-=evt.getDelta();
		if(l<=0 || evt.getFilteredFps()<200)kill();
	}
	
	
	@Override
	protected ArrayList<PhysShape> getShapes() {
		double w = WIDTH/2;
		return shapeAsList(new Polygon(0,2,4,new double[]{
				 w,  w,
				-w,  w,
				-w, -w,
				 w, -w
			},0,0));
	}
	
	public static void spawn(Collider collider,double width, double height){
		double theta = collider.tx.theta.val;
		double c = Math.cos(theta);
		double s = Math.sin(theta);
		for(double u = (WIDTH/2)-(width/2); u<width/2; u+=WIDTH){
			for(double v = (WIDTH/2)-(height/2); v<height/2; v+=WIDTH){
				double x = (c*u) - (s*v);
				double y = (s*u) + (c*v);
				BoxFragment f = (BoxFragment)
						Entities.newInstance(BoxFragment.class,
								x+collider.tx.translation.x,y+collider.tx.translation.y);
				f.collider.tx.theta.val = theta;
				f.collider.velocity.set(collider.velocity);
			}
		}
	}
	
	public static final EntityStateFactory getFactory(){
		return new EntityStateFactory(){
			@Override
			public EntityState construct() {
				return new BoxFragment();
			}

			@Override
			public Class<? extends EntityState> getType() {
				return BoxFragment.class;
			}
		};
	}
}
