package com.flickshot.components.entities.defs;

import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.scene.Updater.UpdateEvent;

public class BasicEnemySpawner extends EntityState{
	public static final String ENTITY_NAME = "BasicSpawner";
	double x,y;
	double t = 0;

	int max = 20;
	@Override
	public void preUpdate(UpdateEvent evt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(UpdateEvent evt) {
		t+= evt.getDelta();
		if(t>4){
			t=0;
			if(getSum()<max){
				int n = (int)Math.round(Math.floor(Math.random()*3.0));
				//n=0;
				if(n==0)
					Entities.getEntity("BasicEnemy").newInstance(x,y);
				else if(n==1)
					Entities.getEntity("BasicBoxEnemy").newInstance(x,y);
				else if(n==2)
					Entities.getEntity("BasicTriangleEnemy").newInstance(x,y);
				else
					throw new IllegalStateException();
			}
		}
	}
	
	private int getSum(){
		return Entities.getEntity("BasicEnemy").getSize()+Entities.getEntity("BasicTriangleEnemy").getSize()+Entities.getEntity("BasicBoxEnemy").getSize();
	}

	@Override
	public void postUpdate(UpdateEvent evt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init(double x, double y) {
		this.x = x;
		this.y = y;
		
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
	}
	
	public void unload(){
	}

	
	public static final EntityStateFactory getFactory(){
		return new EntityStateFactory(){
			@Override
			public EntityState construct() {
				return new BasicEnemySpawner();
			}

			@Override
			public Class<? extends EntityState> getType() {
				return BasicEnemySpawner.class;
			}
		};
	}
}
