package com.flickshot.components.entities.defs.managers;

import java.util.HashMap;

import com.flickshot.components.entities.CommonEntity;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.timeline.Timelines;
import com.flickshot.config.Config;
import com.flickshot.scene.Updater.UpdateEvent;

public class Jump extends CommonEntity{
	public static String ENTITY_NAME = "jump";
	
	double time;
	
	public void update(UpdateEvent evt){
		Timelines.get("default").seek(time);
		kill();
	}
	
	
	public void configure(Config c){
		this.time = ((JumpConfig)c).time;
	}
	
	public static class Factory extends EntityStateFactory{

		@Override
		public EntityState construct() {
			return new Jump();
		}

		@Override
		public Class<? extends EntityState> getType() {
			return Jump.class;
		}
		
		@Override
		public Config getConfig(){
			return new JumpConfig();
		}
		
	}
	
	public static EntityStateFactory getFactory(){
		return new Factory();
	}
	
	public static class JumpConfig extends Config{
		public double time;
		@Override
		public void setValue(String text) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getAliases(HashMap<String, String> map) {
			// TODO Auto-generated method stub
			
		}
		
		
		
	}
}
