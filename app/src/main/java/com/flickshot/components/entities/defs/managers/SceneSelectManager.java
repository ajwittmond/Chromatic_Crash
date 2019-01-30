
package com.flickshot.components.entities.defs.managers;

import java.util.ArrayList;
import java.util.HashMap;

import android.util.Log;

import com.flickshot.assets.AssetLibrary;
import com.flickshot.assets.music.Song;
import com.flickshot.components.entities.CommonEntity;
import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.Entity;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.entities.defs.gui.Button;
import com.flickshot.components.graphics.Graphics;
import com.flickshot.components.graphics.Screen;
import com.flickshot.components.input.TouchManager;
import com.flickshot.scene.Scene;
import com.flickshot.scene.Scene.SceneConfig;
import com.flickshot.scene.Updater.UpdateEvent;

public class SceneSelectManager extends CommonEntity{
	public static final String ENTITY_NAME = "scene_select_manager";
	Song song;
	boolean first;
	
	ArrayList<Button> buttons = new ArrayList<Button>();
	
	public void init(double x, double y){
		super.init(x,y);
		first=true;
		song = (Song)AssetLibrary.get("music","scene_select");
		song.setVolume(1,1);
		song.play();
	}
	
	@Override
	public void update(UpdateEvent evt) {
		Screen screen = Graphics.getCurrentScene().screen;
		final double screenWidth = screen.getWidth();
		
		final double margin=64;
		final double w = 256, h = 128;
		
		if(first){
			first = false;
			TouchManager.setScreen(screen);
			
			double x = screen.getX()+margin, y = screen.getHeight()-(margin*2)-h;
			
			HashMap<String,SceneConfig> scenes = Scene.getConfigs();
			Entity buttons = Entities.getEntity("Button");
			for(String scene: scenes.keySet()){
				if(scene.equals("scene_select"))continue;
				Button current = (Button)buttons.newInstance(x,y);
				buttons.add(current);
				current.setPosition(x,y);
				current.setDimensions(w,h);
				x+=margin+w;
				if(x+32+w>screen.getX()+screen.getWidth()){
					x=screen.getX()+margin;
					y-=margin+h;
				}
				current.text = scene;
				final String sceneName = scene;
				current.setAction(new com.flickshot.util.Action(){
					@Override
					public void doAction() {
						Scene.newScene(sceneName);
					}
				});
			}

			Button quit = (Button)buttons.newInstance(x,y);
			buttons.add(quit);
			quit.setPosition(x,y);
			quit.text = "quit";
			quit.setDimensions(w,h);
			quit.setAction(new com.flickshot.util.Action(){
				@Override
				public void doAction() {
					System.exit(0);
				}
			});
		}else{
			
			double x = screen.getX()+margin, y = screen.getHeight()-(margin*2)-h;
			
			for(Button b: buttons){
				b.setPosition(x,y);
				x+=margin+w;
				if(x+32+w>screen.getX()+screen.getWidth()){
					x=screen.getX()+margin;
					y-=margin+h;
				}
			}
		}
	}
	
	public void unload(){
		song.pause();
	}
 
	
	public static final EntityStateFactory getFactory(){
		return new EntityStateFactory(){
			@Override
			public EntityState construct() {
				return new SceneSelectManager();
			}

			@Override
			public Class<? extends EntityState> getType() {
				return SceneSelectManager.class;
			}
			
			@Override
			public void getAssets(ArrayList<String[]> assets){
				assets.add(new String[]{"music","scene_select"});
			}
		};
	}
}
