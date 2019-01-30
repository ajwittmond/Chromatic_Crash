package com.flickshot.components.physics;

import java.util.HashMap;

import com.flickshot.config.BoxConfig;
import com.flickshot.config.Config;
import com.flickshot.config.Vector2dConfig;

public class SceneConfig extends Config{
	public BoxConfig bounds = new BoxConfig();
	public Vector2dConfig gravity = new Vector2dConfig(0,-200);
	public String id = "main";
	public double margin = Scene.DEFAULT_MARGIN;
	public double elasticity = Scene.DEFAULT_bounds_ELASTICITY;
	public double staticFriction = Scene.DEFAULT_bounds_STATIC_FRICTION;
	public double dynamicFriction = Scene.DEFAULT_bounds_DYNAMIC_FRICTION;
	
	public SceneConfig(){
		bounds.width = 1200;
		bounds.height = 900;
	}
	
	@Override
	public void setValue(String text) {
		if(text!=null)id=text;
	}

	@Override
	public void getAliases(HashMap<String, String> map) {
	}

}
