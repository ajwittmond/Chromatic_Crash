package com.flickshot.components.graphics;

import java.util.HashMap;

import com.flickshot.config.Config;
import com.flickshot.config.Vector2dConfig;

public class ScreenConfig extends Config{
	public Vector2dConfig position = new Vector2dConfig();
	public double scale = 1;
	public double orientation = 0;
	public boolean useCenter=true;
	
	public ScreenConfig(){
		position.x = 600;
		position.y = 450;
	}
	
	public Screen toScreen(){
		return new Screen(position.x,position.y,scale,orientation,useCenter);
	}
	
	@Override
	public void setValue(String text) {
	}
	@Override
	public void getAliases(HashMap<String, String> map) {
		map.put("theta","orientation");
	}
}
