package com.flickshot.components.entities.defs.gui;

import java.util.ArrayList;

import android.util.Log;

import com.flickshot.FlickShot;
import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.entities.defs.managers.InfiniteModeManager;
import com.flickshot.components.entities.defs.managers.TimeAttackManager;
import com.flickshot.components.entities.defs.overlays.EndOverlay;
import com.flickshot.components.graphics.Graphics;
import com.flickshot.components.graphics.Renderer2d;
import com.flickshot.components.graphics.Screen;
import com.flickshot.components.graphics.Sprite;
import com.flickshot.geometry.Transformation;
import com.flickshot.scene.Scene;
import com.flickshot.util.Action;

public class PauseButton extends Button{
	public static final String ENTITY_NAME = "PauseButton";
	
	Sprite sprite;
	FlickShot.HardwareButtonListener hb = new FlickShot.HardwareButtonListener() {
        @Override
        public void onBack() {
            super.onBack();
            doAction();
        }

        @Override
        public void onMenu() {
            super.onMenu();
            doAction();
        }

        @Override
        public void onHome() {
            super.onHome();

            if(!Scene.getCurrent().updater.paused.get()  && !(Entities.getStateCount(EndOverlay.class)>0)){
                Entities.getEntity("PauseWindow").newInstance(0,0);
                TimeAttackManager tm = (TimeAttackManager)Entities.getEntity(TimeAttackManager.class).getState(0);
                if(tm==null){
                    InfiniteModeManager im = (InfiniteModeManager)Entities.getEntity(InfiniteModeManager.class).getState(0);
                    if(im!=null){
                        im.levelSong.setVolume(0.2f,0.2f);
                    }
                }else{
                    tm.levelSong.setVolume(0.2f,0.2f);
                }
            }
        }
    };

	public PauseButton(){
		sprite = new Sprite("pause_button");
		sprite.alpha = 0.5f;
		width=128;
		height=128;
		xRelative = 0;
		yRelative = 0;
		z=-98;
		sprite.setCX(0);
		sprite.setCY(0);
		sprite.setBoxHeight(height);
		sprite.setBoxWidth(width);
		setArtist(sprite);
		setAction(new Action(){
			@Override
			public void doAction() {
				if(!Scene.getCurrent().updater.paused.get()  && !(Entities.getStateCount(EndOverlay.class)>0)){
					Entities.getEntity("PauseWindow").newInstance(0,0);
                    TimeAttackManager tm = (TimeAttackManager)Entities.getEntity(TimeAttackManager.class).getState(0);
                    if(tm==null){
                        InfiniteModeManager im = (InfiniteModeManager)Entities.getEntity(InfiniteModeManager.class).getState(0);
                        if(im!=null){
                            im.levelSong.setVolume(0.2f,0.2f);
                        }
                    }else{
                        tm.levelSong.setVolume(0.2f,0.2f);
                    }
				}else{
					PauseWindow w = (PauseWindow)Entities.getEntity(PauseWindow.class).getState(0);
					if(w!=null) w.close();
				}
			}
		});
	}
	
	public void init(double x, double y){
		super.init(x,y);
		Screen screen = Graphics.getCurrentScene().screen;
		sprite.setX(screen.getX());
		sprite.setY(screen.getY()+screen.getHeight()-height);
		setX(screen.getX());
		setY(screen.getY()+screen.getHeight()-height);
        FlickShot.addListener(hb);
	}

    @Override
    public void destroy(){
        super.destroy();
        FlickShot.removeListener(hb);
    }

    @Override
    public void unload(){
        super.unload();
        FlickShot.removeListener(hb);
    }
	
	public static final EntityStateFactory getFactory(){
		return new EntityStateFactory(){
			@Override
			public EntityState construct() {
				return new PauseButton();
			}

			@Override
			public Class<? extends EntityState> getType() {
				return PauseButton.class;
			}
			
			@Override
			public void getAssets(ArrayList<String[]> assets){
				assets.add(new String[]{"texture","pause_button"});
                assets.add(new String[]{"texture","pause_menu"});
			}
		};
	}
}
