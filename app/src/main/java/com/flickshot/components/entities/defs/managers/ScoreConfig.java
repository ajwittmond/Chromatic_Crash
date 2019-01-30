package com.flickshot.components.entities.defs.managers;

import java.util.HashMap;

import com.flickshot.config.Config;

public class ScoreConfig extends Config{
	public boolean multiHitComboEnabled;
	public boolean colorComboEnabled;
	
	public int multiComboBonus = 10;
	public int colorComboBonus = 10;
	
	public int bronze=10;
	public int silver=2000;
	public int gold=3000;

    public boolean infiniteMode = false;
	
	@Override
	public void setValue(String text) {
		
	}

	@Override
	public void getAliases(HashMap<String, String> map) {
		
	}

}
