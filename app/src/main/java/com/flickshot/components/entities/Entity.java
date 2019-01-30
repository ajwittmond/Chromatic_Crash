package com.flickshot.components.entities;

import java.util.ArrayList;
import java.util.HashMap;

import android.util.Log;

import com.flickshot.scene.Updatable;
import com.flickshot.scene.Updater.UpdateEvent;

public class Entity extends Updatable{
	private final HashMap<String,StringAction> actions = new HashMap<String,StringAction>();
	public final Class<? extends EntityState> stateType;
	public final String[][] assets;
	EntityState[] states; 
	public final EntityStateFactory factory;
	private int position = 0;
	
	public Entity(EntityStateFactory factory){
		this.factory = factory;
		states = new EntityState[16];
		stateType = factory.getType();
		
		ArrayList<String[]> asseta = new ArrayList<String[]>();
		factory.getAssets(asseta);
		assets = asseta.toArray(new String[asseta.size()][]);
	}
	
	public EntityState newInstance(double x, double y){
		if(states.length<=position){
			EntityState[] temp = new EntityState[states.length*2];
			System.arraycopy(states, 0, temp, 0, states.length);
			states = temp;
		}
		if(states[position]==null){
			states[position] = factory.construct();
		}
        position++;
		states[position-1].init(x,y);
		return states[position-1];
	}

	@Override
	public void preUpdate(UpdateEvent evt) {
		for(int i = 0; i<position; i++){
			if(states[i].active())states[i].preUpdate(evt);
		}
	}

	@Override
	public void update(UpdateEvent evt) {
		for(int i = 0; i<position; i++){
			if(states[i].active())states[i].update(evt);
		}
	}

	@Override
	public void postUpdate(UpdateEvent evt) {
		for(int i = 0; i<position; i++){
			if(states[i].active())states[i].postUpdate(evt);
		}
		for(int i = 0; i<position; i++){
			while(!states[i].alive() && i<position){
				states[i].destroy();
				position--;
				EntityState temp = states[i];
				if(i!=position){
					states[i] = states[position];
					states[position] = temp;
				}
			}
		}
	}
	
	public void clear(){
		ArrayList<EntityState> persistantStates = new ArrayList<EntityState>();
		for(int i = 0; i<position;i++){
			if(states[i].persistant)
				persistantStates.add(states[i]);
			else
				states[i].unload();
			states[i]=null;
		}
		position = 0;
		for(;position<persistantStates.size();position++){
			states[position] = persistantStates.get(position);
		}
	}
	
	public EntityState getState(int i){
		if(i<position)
			return states[i];
		else
			return null;
	}
	
	public int getSize(){
		return position;
	}
	
	public int numberOfInstances(){
		return position;
	}
	
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
}
