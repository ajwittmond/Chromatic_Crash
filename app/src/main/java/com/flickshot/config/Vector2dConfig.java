package com.flickshot.config;

import java.util.HashMap;

import com.flickshot.geometry.Vector2d;

public class Vector2dConfig extends Config{
	public double x,y;
	
	public Vector2dConfig(){}
	
	public Vector2dConfig(double x, double y){
		this.x=x;
		this.y=y;
	}
	
	public final Vector2d toVector(){
		return new Vector2d(x,y);
	}
	
	@Override
	public void setValue(String text) {}

	@Override
	public void getAliases(HashMap<String, String> map) {
		map.put("width","x");
		map.put("height","y");
		map.put("w","x");
		map.put("h","y");
	}

}
