package com.flickshot.components.entities;

import android.util.Log;

import com.flickshot.scene.Updater.UpdateEvent;

public abstract class CommonEntity extends EntityState{
	
	protected boolean active;
	protected boolean alive;
	
	@Override
	public void init(double x,double y){
		active = true;
		alive = true;
	}
	
	@Override
	public boolean active(){
		return active;
	}
	
	@Override
	public boolean alive(){
		return alive;
	}
	
	@Override
	public void preUpdate(UpdateEvent evt) {
		
	}

	@Override
	public void update(UpdateEvent evt) {
		
	}

	@Override
	public void postUpdate(UpdateEvent evt) {
		
	}
	
	@Override
	public void destroy(){
		
	}
	
	@Override
	public void unload(){
		
	}
	
	public void kill(){
		alive = false;
	}
	
	public void setActive(boolean active){
		this.active = active;
	}
}
