package com.flickshot.components.graphics;

import java.util.HashMap;

import com.flickshot.config.Config;

public class GraphicsConfig extends Config{
	public SceneConfig[] scenes;
	public String currentScene = "default";
	
	public double screenHeight = 960;
	
	@Override
	public void setValue(String text) {}

	@Override
	public void getAliases(HashMap<String, String> map) {
		map.put("scene","scenes");
	}

}
