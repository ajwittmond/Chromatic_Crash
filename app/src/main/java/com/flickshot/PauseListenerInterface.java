package com.flickshot;

import com.flickshot.util.LinkedNodeInterface;

public interface PauseListenerInterface extends LinkedNodeInterface<PauseListenerInterface>{
	public void onPause();
	public void onUnpause();
}
