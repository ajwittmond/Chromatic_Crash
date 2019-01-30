package com.flickshot.components.graphics;

import java.util.HashMap;

import com.flickshot.config.Config;

public class SceneConfig extends Config{
	public String id = "default";
	public BackgroundConfig background;
	public ScreenConfig screen;
	
	@Override
	public void setValue(String text) {}

	@Override
	public void getAliases(HashMap<String, String> map) {
		map.put("name","id");
	}
	

}
