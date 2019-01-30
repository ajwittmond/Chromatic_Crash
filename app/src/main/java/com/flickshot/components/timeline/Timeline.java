package com.flickshot.components.timeline;

import java.util.ArrayList;

import com.flickshot.util.Action;

public class Timeline {
	private final ArrayList<TimelineListener> listeners = new ArrayList<TimelineListener>();
	
	TimelineEvent first = new TimelineEvent(0);
	
	TimelineEvent next = first;
    TimelineEvent prev = null;
	
	boolean paused;
	double time = 0;
	
	final Object id;
	
	public Timeline(Object id){
		this.id = id;
		next = first;
	}
	
	void update(double delta){
		if(!paused && next!=null){
			time+=delta;
			while(next!=null && time>=next.time){
				next.doAction();
				for(TimelineListener l: listeners)l.onEvent(next.time);
                prev = next;
				next = next.next;
			}
		}
	}
	
	public void seek(double t){
		time = t;
		next = first;
		while(next.time<time)
			next = next.next;
	}
	
	public void gotoNext(){
		if(next!=null){
			time = next.time;
		}
	}
	
	public boolean paused(){
		return paused;
	}
	
	public void setPaused(boolean paused){
		this.paused = paused;
	}
	
	
	public void addAction(double time,Action action){	
		for(TimelineEvent curr = first;curr!=null;curr = curr.next){
			if(curr.time==time){
				curr.actions.add(action);
				break;
			}else if(curr.next == null || curr.next.time>time){
				TimelineEvent temp = curr.next;
				curr.next = new TimelineEvent(time);
				curr.next.actions.add(action);
				curr.next.next = temp;
				break;
			}
		}

        if(time!=0 && prev!=null){
            next = prev.next;
        }
	}

    public double getTime(){
        return time;
    }

	public void addListener(TimelineListener l){
		listeners.add(l);
	}
	
	public void removeListener(TimelineListener l){
		listeners.remove(l);
	}
	
	private class TimelineEvent{
		final double time;
		TimelineEvent next;
		TimelineEvent prev;
		final ArrayList<Action> actions = new  ArrayList<Action>();
		
		TimelineEvent(double time){
			this.time = time;
		}
		
		void doAction(){
			for(int i = 0; i<actions.size(); i++)
				actions.get(i).doAction();
		}
		
	}
	
	public static interface TimelineListener{
		public void onEvent(double time);
	}
}
