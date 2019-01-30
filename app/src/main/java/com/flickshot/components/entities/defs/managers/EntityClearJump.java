package com.flickshot.components.entities.defs.managers;

import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.entities.defs.managers.Jump.Factory;
import com.flickshot.components.entities.defs.managers.Jump.JumpConfig;
import com.flickshot.config.Config;
import com.flickshot.scene.Updater.UpdateEvent;

public class EntityClearJump extends Jump{
	public static final String ENTITY_NAME = "entity_clear_jump";
	
	String entity;

	public void update(UpdateEvent evt){
		if(Entities.getStateCount(entity)<=0){
			super.update(evt);
		}
	}
	
	public void configure(Config c){
		super.configure(c);
		entity = ((EntityClearJumpConfig)c).entity;
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
			return new EntityClearJumpConfig();
		}
		
	}
	
	public static EntityStateFactory getFactory(){
		return new Factory();
	}
	
	public static class EntityClearJumpConfig extends JumpConfig{
		public String entity;
	}
}
