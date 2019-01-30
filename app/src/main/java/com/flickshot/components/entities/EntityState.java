package com.flickshot.components.entities;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.flickshot.config.Config;
import com.flickshot.scene.Updatable;

public abstract class EntityState extends Updatable{
	private static final AtomicInteger NEXT_ID = new AtomicInteger();
	
	private final HashMap<String,StringAction> actions = new HashMap<String,StringAction>();
	
	public final int id;
	
	public boolean persistant;
	
	public EntityState(){
		id = NEXT_ID.getAndIncrement();
	}
	
	public abstract void init(double x,double y);
	
	public abstract boolean active();
	
	public abstract boolean alive();
	
	public abstract void destroy();
	
	public abstract void unload();
	
	public Object doString(String name,String args){
		StringAction action = actions.get(name);
		if(action!=null)
			return action.doAction(args);
		else
			return null;
	}
	
	public void setAction(String name,StringAction action){
		actions.put(name,action);
	}
	
	public void removeAction(String name){
		actions.remove(name);
	}
	
	public void configure(Config cfg){
		
	}
}
