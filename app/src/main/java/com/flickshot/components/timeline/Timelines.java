package com.flickshot.components.timeline;

import java.util.ArrayList;
import java.util.HashMap;

import com.flickshot.components.Component;
import com.flickshot.components.timers.Timers;
import com.flickshot.scene.Scene;
import com.flickshot.scene.UpdateQueue;
import com.flickshot.scene.Updater.UpdateEvent;

public class Timelines extends Component{
	private static Timelines current;
	
	private static final ArrayList<Timeline> timelines = new ArrayList<Timeline>();
	private static final HashMap<Object,Timeline> ids = new HashMap<Object,Timeline>();
	
	public static final void create(){
		if(current!=null){
			throw new IllegalStateException();
		}
		current = new Timelines();
		UpdateQueue.add(current);
		Scene.addSceneListener(new Scene.SceneListener(){
			@Override
			public void onStart() {
			}

			@Override
			public void onEnd() {
				clear();
			}
			
		});
	}
	
	public static Timeline get(Object id){
		return ids.get(id);
	}
	
	public static Timeline remove(Timeline t){
		timelines.remove(t);
		return t;
	}
	
	public static void add(Timeline t){
		timelines.add(t);
		ids.put(t.id,t);
	}
	
	public static void clear(){
		timelines.clear();
		ids.clear();
	}
	
	private Timelines(){
		
	}
	
	@Override
	public void reset() {
		clear();
	}

	@Override
	public void preUpdate(UpdateEvent evt) {
	}

	@Override
	public void update(UpdateEvent evt) {
		for(int i = 0; i<timelines.size(); i++)
			timelines.get(i).update(evt.getDelta());
	}

	@Override
	public void postUpdate(UpdateEvent evt) {
	}
	

}
