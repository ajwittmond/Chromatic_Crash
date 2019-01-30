package com.flickshot.config;

import java.util.HashMap;

import com.flickshot.geometry.Transformation;
import com.flickshot.util.MutableDouble;

public class TransformationConfig extends Config{
	public Vector2dConfig scale;
	public Vector2dConfig translation;
	public double orientation;
	
	public TransformationConfig(){}
	
	public Transformation toTransformation(){
		return new Transformation(scale.toVector(),translation.toVector(),new MutableDouble(orientation));
	}
	
	@Override
	public void setValue(String text) {}

	@Override
	public void getAliases(HashMap<String, String> map) {
		map.put("theta","orientation");
		map.put("position","translation");
		map.put("dimensions","scale");
	}
}
