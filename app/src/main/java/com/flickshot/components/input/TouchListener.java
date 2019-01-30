package com.flickshot.components.input;

import com.flickshot.components.ComponentState;
import com.flickshot.util.LinkedNode;

public abstract class TouchListener extends LinkedNode<TouchListenerInterface> implements TouchListenerInterface,ComponentState{
	public final void bind(){
		TouchManager.add(this);
	}
	
	public final void unbind(){
		TouchManager.remove(this);
	}
}
