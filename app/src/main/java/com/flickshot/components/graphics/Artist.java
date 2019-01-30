package com.flickshot.components.graphics;

import com.flickshot.geometry.Box;
import com.flickshot.util.LinkedNode;

public abstract class Artist extends LinkedNode<Artist> {
	String sceneId = "default";
	Scene scene = Graphics.getScene(sceneId);
	private boolean bound = false;
	
	public abstract boolean isOnScreen(double screenX,double screenY, double screenWidth, double screenHeight);
	
	public abstract void draw(double delta,Renderer2d renderer);
	
	public final void setScene(String id){
		if(bound)scene.remove(this);
		if((scene = Graphics.getScene(id))==null) throw new IllegalStateException("no scene: "+id);
		sceneId = id;
		if(bound)scene.add(this);
	}
	
	public final boolean bound(){
		return bound;
	}
	
	public final void bind(){
		if(!bound){
			if(scene.destroyed)setScene(sceneId);
			scene.add(this);
			bound = true;
		}
	}
	
	public final void unbind(){
		if(bound){
			scene.remove(this);
			bound=false;
		}
	}
	
	public final boolean isVisible(){
		return bound;
	}
	
	public final void setVisible(boolean visible){
		if(visible) bind(); else unbind();
	}
	
	protected Scene getScene(){
		return scene;
	}
	
	public Box getScreen(){
		return scene.screen;
	}
	
	public final double getIConversion(){
		return scene.iconversion;
	}
}
