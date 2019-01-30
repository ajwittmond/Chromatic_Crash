package com.flickshot.components.entities.defs.gui;

import java.util.ArrayList;

import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.Entity;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.entities.defs.fx.Transition;
import com.flickshot.components.entities.defs.managers.InfiniteModeManager;
import com.flickshot.components.entities.defs.managers.TimeAttackManager;
import com.flickshot.components.graphics.Graphics;
import com.flickshot.components.graphics.Renderer2d;
import com.flickshot.components.graphics.Screen;
import com.flickshot.components.graphics.Sprite;
import com.flickshot.components.input.TouchEvent;
import com.flickshot.components.input.TouchManager;
import com.flickshot.geometry.Vector2d;
import com.flickshot.scene.Scene;
import com.flickshot.util.Action;
import com.flickshot.util.MiscLib;

public class PauseWindow extends Window{
	public static final String ENTITY_NAME = "PauseWindow";

    private Sprite menuSprite = new Sprite("pause_menu");
	
	public void init(double x, double y){
		super.init(x,y);
		Screen screen = Graphics.getCurrentScene().screen;

		height = screen.getHeight()*5.0/6.0;
		width = height;
		setDimensions(width,height);
		setCX(screen.getCX());
		setCY(screen.getCY());
        z=-98;

        menuSprite.setBoxWidth(width);
        menuSprite.setBoxHeight(width);
        menuSprite.setCX(0);
        menuSprite.setCY(0);
		menuSprite.z = -1;

		final double margin = 64;
		final double itemHeight = (height - (margin*3))/2;
		double buttonWidth = (width-margin*4)/3;
		
		this.addListener(new WindowListener(){

			@Override
			public void onClose() {
				Scene.getCurrent().updater.paused.set(false);
                TimeAttackManager tm = (TimeAttackManager)Entities.getEntity(TimeAttackManager.class).getState(0);
                if(tm==null){
                    InfiniteModeManager im = (InfiniteModeManager)Entities.getEntity(InfiniteModeManager.class).getState(0);
                    if(im!=null){
                        im.levelSong.setVolume(1,1);
                    }
                }else{
                    tm.levelSong.setVolume(1,1);
                }
			}
			
		});
		
		Scene.getCurrent().updater.paused.set(true);
	}

    @Override
    protected void drawWindow(double delta, Renderer2d r){
        Screen screen = Graphics.getCurrentScene().screen;
        r.push();
            r.scale(1 / dt, 1 / dt);
            r.color(0, 0, 0, dt*0.5);
            r.setDrawMode(Renderer2d.FILL);
            r.shape(Renderer2d.SQUARE,screen.getWidth()*2,screen.getHeight()*2);
        r.pop();
        menuSprite.draw(delta,r);
    }

    @Override
    public void onTouch(TouchEvent evt){
        if(Vector2d.dist(TouchManager.x(), TouchManager.y(), getCX(), getCY())<=getWidth()){
            double theta = Math.atan2(TouchManager.y()-getCY(),TouchManager.x()-getCX());
            if(theta<0)
                theta +=Math.PI*2;
            if(theta<Math.PI){
                close();
            }else if(theta<Math.PI+(Math.PI/2)){
                Transition t = (Transition)Entities.newInstance(Transition.class,0,0);
                t.target = Scene.getCurrent().id;
            }else{
                Scene.newScene("scene_select");
            }
        }
    }
	
	public static final EntityStateFactory getFactory(){
		return new EntityStateFactory(){
			@Override
			public EntityState construct() {
				return new PauseWindow();
			}

			@Override
			public Class<? extends EntityState> getType() {
				return PauseWindow.class;
			}
			
			@Override
			public void getAssets(ArrayList<String[]> assets){
				assets.add(new String[]{"texture","pause_button"});
			}
		};
	}
}
