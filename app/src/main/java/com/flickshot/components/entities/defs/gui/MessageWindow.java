package com.flickshot.components.entities.defs.gui;

import java.util.ArrayList;
import java.util.HashMap;

import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.Entity;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.graphics.Graphics;
import com.flickshot.components.graphics.Screen;
import com.flickshot.config.Config;
import com.flickshot.scene.Scene;
import com.flickshot.scene.Updater.UpdateEvent;
import com.flickshot.util.Action;

public class MessageWindow  extends Window{
	public static final String ENTITY_NAME = "MessageWindow";
	
	private String text;
	private Label label;
	
	public void init(double x, double y){
		super.init(x,y);
		Screen screen = Graphics.getCurrentScene().screen;
		
		width = screen.getWidth()*2/3;
		height = screen.getHeight()*2/3;
		
		setDimensions(width,height);
		setCX(screen.getCX());
		setCY(screen.getCY());
		z=-98;
		
		Button ok = (Button)Entities.getEntity("Button").newInstance(x,y);
		label = (Label)Entities.getEntity("Label").newInstance(0,0);
		
		final double margin = 64;
		final double itemHeight = (height - (margin*3))/2;
		double buttonWidth = (width-margin*2);
		
		ok.setPosition(margin,margin);
		ok.setDimensions(buttonWidth,itemHeight);
		ok.setAction(new Action(){
			public void doAction(){
				close();
			}
		});
		ok.text = "ok";
		ok.textSize=128;
		ok.setColor(1,1,1,1);
		ok.setTextColor(0,0,0,1);
		
		label.text = "Pause";
		label.setPosition(margin,margin+margin+itemHeight);
		label.setDimensions(width - (margin*2), itemHeight);
		label.textSize=128;
		label.setColor(1,1,1,1);
		
		addChild(ok);
		addChild(label);
		
		Scene.getCurrent().updater.paused.set(true);
	}
	
	@Override
	public void destroy(){
		super.destroy();
		label = null;
		Scene.getCurrent().updater.paused.set(false);
	}
	
	@Override
	public void unload(){
		super.unload();
		label = null;
	}
	
	public void update(UpdateEvent evt){
		Scene.getCurrent().updater.paused.set(true);
	}
	
	@Override
	public void configure(Config c){
		setText(((MessageConfig)c).message);
	}
	
	public void setText(String text){
		this.text = text;
		if(label!=null){
			label.text = text;
		}
	}
	
	public static final EntityStateFactory getFactory(){
		return new Factory();
	}
	
	public static class Factory extends EntityStateFactory{
		@Override
		public EntityState construct() {
			return new MessageWindow();
		}

		@Override
		public Class<? extends EntityState> getType() {
			return MessageWindow.class;
		}
		
		@Override
		public Config getConfig(){
			return new MessageConfig();
		}
	}
	
	public static class MessageConfig extends Config{
		public String message = "message text";
		@Override
		public void setValue(String text) {
		}

		@Override
		public void getAliases(HashMap<String, String> map) {
		}
		
	}
}
