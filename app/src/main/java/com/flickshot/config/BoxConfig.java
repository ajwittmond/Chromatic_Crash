package com.flickshot.config;

import java.util.HashMap;

import com.flickshot.geometry.Square;

public class BoxConfig extends Config{
	public double x,y,width,height;
	
	public BoxConfig(){}
	
	public final Square toSquare(){
		return new Square(x,y,width,height);
	}
	
	@Override
	public void setValue(String text) {}
	
	@Override
	public void getAliases(HashMap<String, String> map) {
	}

}
