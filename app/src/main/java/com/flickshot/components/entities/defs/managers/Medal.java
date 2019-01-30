package com.flickshot.components.entities.defs.managers;

import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.entities.defs.VisibleEntity;
import com.flickshot.components.graphics.Graphics;
import com.flickshot.components.graphics.Screen;
import com.flickshot.components.graphics.Sprite;
import com.flickshot.geometry.Transformation;
import com.flickshot.scene.Scene;
import com.flickshot.scene.Updater.UpdateEvent;

public class Medal extends VisibleEntity{
	public static final String ENTITY_NAME = "medal";
	
	Transformation tx = new Transformation();
	
	public Sprite sprite = new Sprite("",tx);
	
	private double initTime;
	private double t;
	private double finalX,finalY,initX,initY;
	private double initTheta;
	private double finalW,finalH,initH,initW;
	
	public Medal(){
		setArtist(sprite);
	}
	
	public void init(double x, double y){
		super.init(x,y);
		t=initTime=2;
		finalX =x;
		finalY =y;
		Screen s = Graphics.getCurrentScene().screen;
		initX = s.getCX();
		initY = s.getCY();
		initTheta=Math.PI*2*3;
	}
	
	public void update(UpdateEvent evt){
		super.update(evt);
		t= Math.max(0,t-evt.getRealDelta());
		double u = t/initTime;
		sprite.alpha = (float)(1.0-u);
		tx.translation.set(finalX+(u*(initX-finalX)),finalY+(u*(initY-finalY)));
		tx.scale.set(finalW+(u*(initW-finalW)),finalH+(u*(initH-finalH)));
		tx.theta.val = initTheta*u;
	}
	
	public static Medal create(String texture, double x, double y,double w, double h){
		Medal m = ((Medal)Entities.newInstance(Medal.class,x,y));
		m.sprite.setTexture(texture);
		m.initW = w*5;
		m.finalW = w;
		m.initH = h*5;
		m.finalH = h;
		return m;
	}
	
	public static final EntityStateFactory getFactory(){
		return new EntityStateFactory(){
			@Override
			public EntityState construct() {
				return new Medal();
			}

			@Override
			public Class<? extends EntityState> getType() {
				return Medal.class;
			}
		};
	}
}
