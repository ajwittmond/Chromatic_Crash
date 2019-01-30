package com.flickshot.components.entities.defs.managers;

import com.flickshot.components.entities.CommonEntity;
import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.entities.defs.enemies.Enemy;
import com.flickshot.scene.Updater.UpdateEvent;
import com.flickshot.components.entities.defs.enemies.InvulnerableBouncer;;

public class LevelEnd extends CommonEntity{
	public static String ENTITY_NAME = "LevelEnd";
	
	public LevelEnd(){}
	
	
	double endTimer = 1;
	public void init(double x, double y){
		super.init(x,y);
		endTimer =1;
	}
	
	public void update(UpdateEvent evt){
		alive = true;
		if(Entities.getStateCount(Enemy.class)-Entities.getStateCount(InvulnerableBouncer.class)<=0){
			endTimer-=evt.getDelta();
			if(endTimer<=0){
				TimeAttackManager m = (TimeAttackManager)Entities.getEntity(TimeAttackManager.class).getState(0);
				if(m!=null)m.endLevel();
				kill();
			}
		}
	}
	

	public static EntityStateFactory getFactory(){
		return new EntityStateFactory(){

			@Override
			public EntityState construct() {
				return new LevelEnd();
			}

			@Override
			public Class<? extends EntityState> getType() {
				return LevelEnd.class;
			}
			
		};
	}
}
