package com.flickshot.components.input;

import android.view.MotionEvent;
import android.view.View;

public class TouchEvent {
	public MotionEvent evt;
	public View v;
	
	
	TouchEvent set(MotionEvent evt, View v){
		this.evt = evt;
		this.v = v;
		return this;
	}
}
