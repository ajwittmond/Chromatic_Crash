package com.flickshot.components.graphics;

import java.util.HashMap;

import com.flickshot.assets.AssetLibrary;
import com.flickshot.config.Config;

public class BackgroundConfig extends Config{
	public String texture="wavy_background";
	public float x=-300,y=-225,width=1800,height=1350;
	public boolean tiling=false;
	//<background texture='wavy_background' x='-300' y='-225' width='1800' height='1350' tiling='false'/>
	public final Background toBackground(){
		return new Background(texture,x,y,width,height,tiling);
	}

	@Override
	public void setValue(String text) {
	}

	@Override
	public void getAliases(HashMap<String, String> map) {
		map.put("w","width");
		map.put("h","height");
	}

}
