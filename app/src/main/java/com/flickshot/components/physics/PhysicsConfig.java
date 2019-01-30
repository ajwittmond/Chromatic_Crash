package com.flickshot.components.physics;

import java.util.HashMap;

import com.flickshot.config.Config;

public class PhysicsConfig extends Config{
	public SceneConfig[] scenes;
	
	@Override
	public void setValue(String text) {}

	@Override
	public void getAliases(HashMap<String, String> map) {
		map.put("scene","scenes");
	}

}
