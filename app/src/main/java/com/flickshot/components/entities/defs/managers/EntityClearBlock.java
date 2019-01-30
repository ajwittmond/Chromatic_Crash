package com.flickshot.components.entities.defs.managers;

import java.util.HashMap;

import com.flickshot.components.entities.CommonEntity;
import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.entities.defs.enemies.InvulnerableBouncer;
import com.flickshot.components.entities.defs.managers.Jump.Factory;
import com.flickshot.components.entities.defs.managers.Jump.JumpConfig;
import com.flickshot.components.timeline.Timelines;
import com.flickshot.config.Config;
import com.flickshot.scene.Updater.UpdateEvent;
import com.flickshot.components.entities.defs.enemies.Enemy;

public class EntityClearBlock extends EntityClearAction{
	public static final String ENTITY_NAME = "EntityClearBlock";

	public void init(double x, double y){
		super.init(x,y);
		Timelines.get("default").setPaused(true);
	}

	public void doAction(){
        Timelines.get("default").setPaused(false);
        kill();
    }

	
	public static class Factory extends EntityStateFactory{

		@Override
		public EntityState construct() {
			return new EntityClearBlock();
		}

		@Override
		public Class<? extends EntityState> getType() {
			return EntityClearBlock.class;
		}
		
		@Override
		public Config getConfig(){
			return new EntityClearConfig();
		}
		
	}
	
	public static EntityStateFactory getFactory(){
		return new Factory();
	}
}
