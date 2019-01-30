package com.flickshot.components.entities;

import java.util.ArrayList;
import java.util.HashMap;

import com.flickshot.config.Config;

public abstract class EntityStateFactory {
	
	public abstract EntityState construct();
	
	public abstract Class<? extends EntityState> getType();
	
	public void getAssets(ArrayList<String[]> assets){
		
	}
	
	public Config getConfig(){
		return null;
	}
}
