package com.flickshot.components.entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import android.util.Log;

class EntityTree {
	
	ArrayList<EntityTree> children = new ArrayList<EntityTree>();
	/**this value of this node*/
	final Class<? extends EntityState> type;
	Entity value;
	
	EntityTree(Class<? extends EntityState> type){
		this.type = type;
	}
	
	EntityTree(Class<? extends EntityState> type, Entity value){
		this.type = type;
		this.value = value;
	}
	
	/**
	 * traces recursively down a tree to get all entities with states that inherit from 
	 * this one's
	 * @param c
	 * @return
	 */
	Collection<EntityState> getStates(Collection<EntityState> c){
		if(value!=null){
			for(int i = 0; i < value.getSize(); i++){
				c.add(value.states[i]);
			}
			
		}
		for(EntityTree child: children){
			child.getStates(c);
		}
		return c;
	}
	
	int getCount(){
		int i = 0;
		if(value!=null)
			i+=value.getSize();
		for(EntityTree child: children){
			i+=child.getCount();
		}
		return i;
	}
	
	/**
	 * adds a type to this tree
	 * @param clazz
	 */
	EntityTree add(Entity entity){
		Class<? extends EntityState> clazz = entity.stateType;
		if(clazz.getSuperclass().equals(type)){
			for(EntityTree t: children){
				if(t.type.equals(clazz)){
					t.value = entity;
					return t;
				}
			}
			EntityTree child = new EntityTree(clazz,entity);
			children.add(child);
			return child;
		}else{
			for(EntityTree child: children){
				if(child.type.isAssignableFrom(clazz)){
					return child.add(entity);
				}
			}
			Class<? extends EntityState> next = getNextType(clazz);
			EntityTree child = new EntityTree(next);
			children.add(child);
			return child.add(entity);
		}
	}
	
	private Class<? extends EntityState> getNextType(Class<? extends EntityState> clazz){
		Class<?> current = clazz.getSuperclass();
		while(current!=null && !current.getSuperclass().equals(type)){
			current = current.getSuperclass();
		}
		return current.asSubclass(type);
	}
	
	void print(){
		Log.e("EntityTree", type+" "+children.size()+" "+children);
		for(EntityTree child:children)child.print();
	}
	
	void addToNodeMap(HashMap<Class<?>,EntityTree> nodeMap){
		nodeMap.put(type,this);
		for(EntityTree child: children)child.addToNodeMap(nodeMap);
	}
}
