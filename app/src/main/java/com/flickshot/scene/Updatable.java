package com.flickshot.scene;

import com.flickshot.scene.Updater.UpdateEvent;
import com.flickshot.util.LinkedNode;
import com.flickshot.util.LinkedNodeInterface;

public abstract class Updatable extends LinkedNode<Updatable> implements LinkedNodeInterface<Updatable> {

	public abstract void preUpdate(UpdateEvent evt);
	public abstract void update(UpdateEvent evt);
	public abstract void postUpdate(UpdateEvent evt);
}
