package com.flickshot.components.timers;

import com.flickshot.GameView;
import com.flickshot.components.ComponentState;
import com.flickshot.util.LinkedNode;


public abstract class Timer extends LinkedNode<Timer> implements ComponentState{
	
	public final double initialDelay;
	public final double period;
	public final int times;
	public final boolean hasLimit;
	
	private double t;
	private int rep=0;
	
	public Timer(double initialDelay){
		this.initialDelay = initialDelay;
		this.period = 0;
		this.times = 1;
		this.hasLimit = true;
		t = initialDelay;
	}
	
	public Timer(double initialDelay,double period,boolean continuous){
		this.initialDelay = initialDelay;
		this.period = period;
		this.times = 1;
		this.hasLimit = continuous;
		t = initialDelay;
	}
	
	public Timer(double initialDelay,double period,int times){
		this.initialDelay = initialDelay;
		this.period = period;
		this.times = times;
		this.hasLimit = true;
		rep=times;
		t = initialDelay;
	}
	
	final void update(double delta){
		t-=delta;
		if(t<=0){
			action();
			t=t+period;
			if(hasLimit){
				rep--;
				if(rep<=0){
					unbind();
				}
			}
		}
	}
	
	public final void bind(){
		Timers.add(this);
	}
	
	public final void unbind(){
		Timers.remove(this);
	}
	
	public abstract void action();
}
