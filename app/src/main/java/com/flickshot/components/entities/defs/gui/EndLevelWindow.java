package com.flickshot.components.entities.defs.gui;

import java.util.ArrayList;

import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;

public class EndLevelWindow extends Window{
	public static final String ENTITY_NAME = "DeathWindow";
	
	public static final EntityStateFactory getFactory(){
		return new EntityStateFactory(){
			@Override
			public EntityState construct() {
				return new EndLevelWindow();
			}

			@Override
			public Class<? extends EntityState> getType() {
				return EndLevelWindow.class;
			}
			
			@Override
			public void getAssets(ArrayList<String[]> assets){
			}
		};
	}
}
