package com.flickshot.components.entities.defs.gui;

import java.util.ArrayList;
import java.util.HashMap;

import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.graphics.Graphics;
import com.flickshot.components.graphics.Screen;
import com.flickshot.config.Config;
import com.flickshot.scene.Scene;
import com.flickshot.util.Action;

public class DeathWindow extends Window{
	public static final String ENTITY_NAME = "DeathWindow";
	
	public void init(double x, double y){
		super.init(x,y);
		Screen screen = Graphics.getCurrentScene().screen;
		
		width = screen.getWidth()*2/3;
		height = screen.getHeight()*2/3;
		
		setDimensions(width,height);
		setCX(screen.getCX());
		setCY(screen.getCY());
		z=-98;
		
		Button quit = (Button)Entities.getEntity("Button").newInstance(x,y);
		Button restart = (Button)Entities.getEntity("Button").newInstance(x,y);
		Label label = (Label)Entities.getEntity("Label").newInstance(0,0);
		
		final double margin = 64;
		final double itemHeight = (height - (margin*3))/2;
		double buttonWidth = (width-margin*3)/2;
		
		quit.setPosition(margin,margin);
		quit.setDimensions(buttonWidth,itemHeight);
		quit.setAction(new Action(){
			public void doAction(){
				Scene.newScene("scene_select");
			}
		});
		quit.text = "quit";
		quit.textSize=128;
		quit.setColor(1,1,1,1);
		quit.setTextColor(0,0,0,1);
		
		restart.setPosition(margin+buttonWidth+margin,margin);
		restart.setDimensions(buttonWidth,itemHeight);
		restart.setAction(new Action(){
			public void doAction(){
				close();
			}
		});
		restart.text = "restart";
		restart.textSize=128;
		restart.setColor(1,1,1,1);
		restart.setTextColor(0,0,0,1);
		
		label.text = "You Died";
		label.setPosition(margin,margin+margin+itemHeight);
		label.setDimensions(width - (margin*2), itemHeight);
		label.textSize=128;
		label.setColor(1,1,1,1);
		
		addChild(quit);
		addChild(restart);
		addChild(label);
		
		Scene.getCurrent().updater.paused.set(true);
	}
	
	public static final EntityStateFactory getFactory(){
		return new EntityStateFactory(){
			@Override
			public EntityState construct() {
				return new DeathWindow();
			}

			@Override
			public Class<? extends EntityState> getType() {
				return DeathWindow.class;
			}
			
			@Override
			public void getAssets(ArrayList<String[]> assets){
			}
		};
	}
}
