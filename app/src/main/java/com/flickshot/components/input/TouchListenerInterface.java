package com.flickshot.components.input;

import com.flickshot.util.LinkedNodeInterface;

import android.view.MotionEvent;
import android.view.View;

public interface TouchListenerInterface extends LinkedNodeInterface<TouchListenerInterface>{
	public void onDown(TouchEvent evt);
	public void onMove(TouchEvent evt);
	public void onUp(TouchEvent evt);
} 
