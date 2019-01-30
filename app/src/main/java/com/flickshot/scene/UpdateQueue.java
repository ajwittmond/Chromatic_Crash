package com.flickshot.scene;

import com.flickshot.util.LinkedNodeList;

public class UpdateQueue {
	
	private UpdateQueue(){}
	
	private final static LinkedNodeList<Updatable> updatables = new LinkedNodeList<Updatable>();
	
	public static final void add(Updatable u){
		updatables.add(u);
	}
	
	public static final boolean remove(Updatable u){
		return updatables.remove(u);
	}
	
	public static final void clear(){
		updatables.clear();
	}
	
	public static final int size(){
		return updatables.size();
	}
	
	static final void update(Updater.UpdateEvent evt){
		Updatable head = updatables.getHead();
		Updatable curr = head;
		while(curr!=null){
			curr.preUpdate(evt);
			curr = curr.next();
		}
		curr = head;
		while(curr!=null){
			curr.update(evt);
			curr = curr.next();
		}
		curr = head;
		while(curr!=null){
			curr.postUpdate(evt);
			curr = curr.next();
		}
	}

}
