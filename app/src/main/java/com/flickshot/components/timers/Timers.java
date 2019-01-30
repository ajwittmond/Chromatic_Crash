package com.flickshot.components.timers;

import com.flickshot.components.Component;
import com.flickshot.scene.Scene;
import com.flickshot.scene.UpdateQueue;
import com.flickshot.scene.Updater.UpdateEvent;
import com.flickshot.util.LinkedNodeList;

public class Timers extends Component{
	private static final LinkedNodeList<Timer> timers = new LinkedNodeList<Timer>();
	private static Timers current;
	
	private Timers(){}
	
	public static final void create(){
		if(current!=null){
			throw new IllegalStateException();
		}
		current = new Timers();
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
	
	
	public static final void add(Timer t){
		timers.add(t);
	}
	
	public static final boolean remove(Timer t){
		return timers.remove(t);
	}
	
	public static final void clear(){
		timers.clear();
	}

	@Override
	public void preUpdate(UpdateEvent evt) {
		Timer t = timers.getHead();
		while(t!=null){
			t.update(evt.getDelta());
			t = t.next();
		}
	}

	@Override
	public void update(UpdateEvent evt) {}

	@Override
	public void postUpdate(UpdateEvent evt) {}

	@Override
	public void reset() {
		timers.clear();
	}

}
