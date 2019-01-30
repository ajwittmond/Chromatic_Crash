package com.flickshot.components.entities.defs.managers;

import com.flickshot.components.entities.CommonEntity;
import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.entities.defs.enemies.Enemy;
import com.flickshot.components.timeline.Timeline.TimelineListener;
import com.flickshot.components.timeline.Timelines;
import com.flickshot.scene.Updater.UpdateEvent;

public class GotoNext extends CommonEntity{
	public static String ENTITY_NAME = "GotoNext";
	
	TimelineListener l = new TimelineListener(){

		@Override
		public void onEvent(double time) {
			kill();
		}
		
	};
	
	boolean first;
	
	public GotoNext(){}
	
	public void init(double x, double y){
		super.init(x,y);
		first = true;
	}
	
	public void update(UpdateEvent evt){
		if(first){
			Timelines.get("default").addListener(l);
			first = false;
		}
		if(Entities.getStateCount(Enemy.class)<=0){
			Timelines.get("default").gotoNext();
			kill();
		}
	}
	
	public void destroy(){
		Timelines.get("default").removeListener(l);
	}
	

	public static EntityStateFactory getFactory(){
		return new EntityStateFactory(){

			@Override
			public EntityState construct() {
				return new GotoNext();
			}

			@Override
			public Class<? extends EntityState> getType() {
				return GotoNext.class;
			}
			
		};
	}
}
