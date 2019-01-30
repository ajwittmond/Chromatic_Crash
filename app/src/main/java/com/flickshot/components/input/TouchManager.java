package com.flickshot.components.input;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;

import com.flickshot.GameView;
import com.flickshot.geometry.Box;
import com.flickshot.geometry.Square;
import com.flickshot.scene.Scene;
import com.flickshot.scene.Updatable;
import com.flickshot.scene.UpdateQueue;
import com.flickshot.scene.Updater.UpdateEvent;
import com.flickshot.components.Component;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import java.util.Queue;
public class TouchManager extends Component implements OnTouchListener{
	
	private double x,y;
	
	private boolean down = false;
	
	private Box screen;
	
	private TouchListenerInterface listeners;
	private MotionEvent[] events = new MotionEvent[8];
	private View[] views = new View[8];
	private int pos = 0;
	private final Object lock = new Object();
	
	private final TouchEvent event = new TouchEvent();
	
	private TouchManager(){}
	
	@Override
	public final boolean onTouch(View v, MotionEvent event) {
		synchronized(lock){
			if(pos>=events.length){
				Log.e("TouchManager.onTouch","touch event buffers growing from "+events.length+" to "+(events.length+8));
				MotionEvent[] tempe = events;
				View[] tempv = views;
				events = new MotionEvent[tempe.length+8];
				views = new View[tempv.length+8];
				System.arraycopy(tempe, 0, events, 0, tempe.length);
				System.arraycopy(tempv, 0, views, 0, tempv.length);
//				if(events.length>16){
//					Map<Thread,StackTraceElement[]> stackTraces = Thread.getAllStackTraces();
//					Log.e("TouchManager.onTouch","printing stack traces");
//					for(Thread t : stackTraces.keySet()){
//						Log.e("TouchManager.onTouch","Thread"+t);
//						for(StackTraceElement e: stackTraces.get(t)){
//							Log.e("TouchManager.onTouch", e.getFileName()+" "+e.getMethodName()+" "+e.getLineNumber());
//						}
//					}
//				}
			}
			views[pos]=v; 
			events[pos]=event;
			pos++;
		}
		return true;
	}
	
	private final int[] coords = new int[2];
	
	private void setCoordinates(View v, MotionEvent event){
		if(screen != null){
			v.getLocationOnScreen(coords);
			double px = (double)(event.getX()-coords[0])/(double)v.getWidth();
			double py = 1.0 - ((double)(event.getY()-coords[1])/(double)v.getHeight());
			x = screen.getX() + screen.getWidth()*px;
			y = screen.getY() + screen.getHeight()*py;
		}else{
			x = event.getX();
			y = v.getHeight() - event.getY();
		}
	}

	private static TouchManager current;
	private static View view;
	
	public static final void create(){
		if(current!=null){
			throw new IllegalStateException();
		}
		current = new TouchManager();
		GameView.getCurrent().setOnTouchListener(current);
		setScreen(new Square(-1,-1,2,2));
		UpdateQueue.add(current);
	}
	
	public static final double x(){
		if(current == null){
			throw new IllegalStateException();
		}
		return current.x;
	}
	
	public static final double y(){
		if(current == null){
			throw new IllegalStateException();
		}
		return current.y;
	}
	
	public static final Box screen(){
		if(current == null){
			throw new IllegalStateException();
		}
		return current.screen;
	}
	
	public static final void setScreen(Box screen){
		if(current == null){
			throw new IllegalStateException();
		}
		current.screen = screen;
	}
	
	public static final boolean down(){
		if(current == null){
			throw new IllegalStateException();
		}
		return current.down;
	}
	
	public static final void add(TouchListenerInterface listener){
		if(current.listeners != null) current.listeners.push(listener);
		current.listeners = listener;
	}
	
	public static final void remove(TouchListenerInterface listener){
		listener.remove();
		if(current.listeners == listener){
			current.listeners = current.listeners.next();
		}
		listener.next(null);
		listener.prev(null);
	}

	@Override
	public void preUpdate(UpdateEvent evt) {
		synchronized(lock){
			for(int i = 0; i<pos; i++){
				MotionEvent event = events[i];
				View v = views[i];
				switch(event.getAction()){
					case MotionEvent.ACTION_DOWN :
						down = true;
						setCoordinates(v,event);
						TouchListenerInterface t = listeners;
						while(t!=null){
							t.onDown(this.event.set(event, v));
							t = t.next();
						}
						break;
					case MotionEvent.ACTION_MOVE :
						setCoordinates(v,event);
						t = listeners;
						while(t!=null){
							t.onMove(this.event.set(event, v));
							t = t.next();
						}
						break;
					case MotionEvent.ACTION_UP :
						setCoordinates(v,event);
						down = false;
						t = listeners;
						while(t!=null){
							t.onUp(this.event.set(event, v));
							t = t.next();
						}
						break;
				}
			}
			pos = 0;
		}
	}

	@Override
	public void update(UpdateEvent evt) {
	}

	@Override
	public void postUpdate(UpdateEvent evt) {
	}
	
	public void reset(){
	}
}
