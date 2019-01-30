package com.flickshot.config;

import java.util.HashMap;

public class TestConfig extends Config{
	public char c;
	public boolean b;
	public TransformationConfig tx;
	public Vector2dConfig[] vecs;
	public Pet pet;
	public int[] i;
	public TestConfig[] recur;
	
	static enum Pet{
		dog,
		cat,
		bird,
		fish
	}
	
	@Override
	public void setValue(String text) {}

	@Override
	public void getAliases(HashMap<String, String> map) {}

}
