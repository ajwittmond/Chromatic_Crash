package com.flickshot.components.entities.defs.managers;

import java.util.ArrayList;
import java.util.HashMap;

import com.flickshot.assets.AssetLibrary;
import com.flickshot.assets.music.Song;
import com.flickshot.assets.sfx.Sound;
import com.flickshot.components.entities.CommonEntity;
import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.Entity;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.entities.defs.PuckState;
import com.flickshot.components.entities.defs.gui.Bar;
import com.flickshot.components.entities.defs.gui.VisibleTimer;
import com.flickshot.components.entities.defs.overlays.EndOverlay;
import com.flickshot.components.entities.defs.pickups.Pickup;
import com.flickshot.components.graphics.Graphics;
import com.flickshot.components.graphics.Screen;
import com.flickshot.components.physics.Physics;
import com.flickshot.components.timeline.Timelines;
import com.flickshot.config.Config;
import com.flickshot.scene.Updater.UpdateEvent;
import com.flickshot.components.entities.defs.enemies.Enemy;

public class TimeAttackManager extends CommonEntity{
	public static final String ENTITY_NAME = "TimeAttackManager";
	
	boolean first;
	boolean finished=false;
	
	double timeLimit = 30;
	
	ScoreTracker scoreTracker;
	
	//Bar time;
	public VisibleTimer time;
    public double t;
	Bar score;
	Bar health;
	ScoreConfig sconfig;

	public Song levelSong;
	Sound outOfTime = new Sound("air_horn");
	
	double deathTimer;
	double endTimer;
	
	public void init(double x, double y){
		super.init(x,y);
		first=true;
		deathTimer=2;
		endTimer=3;
        outOfTime.setVolume(0.4f,0.4f);
		Entity scores = Entities.getEntity("scoreTracker");
		if(scores.numberOfInstances()>0){
			scoreTracker = (ScoreTracker)scores.getState(0);
		}else{
			scoreTracker = (ScoreTracker)scores.newInstance(0,0);
		}
		scoreTracker.setScore(0);
		levelSong=null;
        t = 0;
	}
	
	private double bwidth;
	
	
	private final ArrayList<EntityState> enemies = new ArrayList<EntityState>();
    private final ArrayList<EntityState> pickups = new ArrayList<EntityState>();
	@Override
	public void update(UpdateEvent evt) {
		if(first){
			first = false;
			if(levelSong!=null){
                levelSong.setVolume(1,1);
				levelSong.play();
			}
			Screen s = Graphics.getCurrentScene().screen;
			com.flickshot.components.physics.Scene ps = Physics.getScene(0);
			
			Entity bar = Entities.getEntity(Bar.class);
			
			bwidth = (s.getWidth()-ps.width)/4;
			
//			score = (Bar)bar.newInstance(0,0);
//			score.setPosition(s.getX()+(bwidth/2),ps.y);
//			score.setDimensions(bwidth,ps.height);
//			score.setColor(0,1,0,1);
//			score.setBorderColor(0,0.6f,0,1);
//			score.borderWidth=8;
//			score.drawBorder = true;
//			score.setValue(0);
			
//			time = (Bar)bar.newInstance(0,0);
//			time.setPosition((bwidth/2)+ps.x+ps.width,ps.y);
//			time.setDimensions(bwidth/3,ps.height);
//			time.setColor(0,1,0,1);
//			time.setBorderColor(0,0.6f,0,1);
//			time.drawBorder = true;
//			time.borderWidth=8;
//			time.setValue(1);
			
			time = (VisibleTimer)Entities.newInstance(VisibleTimer.class,
					ps.x+ps.width+bwidth,ps.getY()+ps.getHeight()-(bwidth/2));
			time.tx.scale.set(bwidth,bwidth);
			time.setTimer(timeLimit,timeLimit);
			
			health = (Bar)bar.newInstance(0,0);
			health.setPosition(ps.x+ps.width+(bwidth/2),ps.y);
			health.setDimensions(bwidth,ps.height-(bwidth+90));
			health.setColor(0,1,0,1);
			health.setBorderColor(0,0.6f,0,1);
			health.drawBorder = true;
			health.borderWidth=8;
			health.setValue(1);
			
		}else if(!finished){
            t+=evt.getDelta();
			health.setDimensions(bwidth, Physics.getScene(0).height-(bwidth+time.getTextHeight()+32));
			PuckState puck = (PuckState)Entities.getEntity(PuckState.class).getState(0);
			if(puck!=null){
				//time.setValue(time.getValue()-(evt.getDelta()/timeLimit));
				//float v = (float)time.getValue();
				//time.setColor(1*(1-v),v,0,1);
				//time.setBorderColor(0.6f*(1-v),v*0.6f,0,1);
//				score.setValue((double)scoreTracker.getScore()/(double)maxScore); 
				health.setValue((double)puck.getHealth()/(double)PuckState.START_HEALTH);
				float v = (float)health.getValue();
				health.setColor(1*(1-v),v,0,1);
				health.setBorderColor(0.6f*(1-v),v*0.6f,0,1);
				if(time.getTimeLeft()<=0){
                    levelSong.setVolume(0.2f,0.2f);
                    Timelines.get("default").setPaused(true);
					if(endTimer==3)outOfTime.play();
					enemies.clear();
                    pickups.clear();
					Entities.getStates(Enemy.class,enemies);
					for(EntityState e:enemies)((Enemy)e).kill();
                    Entities.getStates(Pickup.class,pickups);
                    for(EntityState e:pickups)((Pickup)e).kill();
					endTimer-=evt.getDelta();
					if(endTimer<=0)
						endLevel();
				}
			}else{
                if(levelSong.isPlaying())
                    levelSong.stop();
				health.setValue(0);
				deathTimer-=evt.getDelta();
				if(deathTimer<=0){
					lose(null);
				}
			}
		}
	}
	
	public void endLevel(){
		levelSong.setVolume(0.2f,0.2f);
		if(!finished){
			if(scoreTracker.getMedals()>0){
				win();
			}else{
				lose("Not Enough Points "+scoreTracker.getScore()+"/"+scoreTracker.getBronze());
			}
		}
	}
	
	@Override
	public void configure(Config config){
		TimeAttackConfig c = (TimeAttackConfig)config;
		timeLimit = c.timeLimit;
		if(!AssetLibrary.has("music",c.levelMusic)){
			AssetLibrary.loadAsset("music",c.levelMusic);
		}
		levelSong = (Song)AssetLibrary.get("music",c.levelMusic);
		scoreTracker.configure(c.scoreConfig);
	}
	
	private void win(){
//		MessageWindow window = (MessageWindow)Entities.getEntity("MessageWindow").newInstance(0,0);
//		window.setText("You Won! Points: "+scoreTracker.getScore());
//		window.addListener(new WindowListener(){
//			public void onClose(){
//				Scene.newScene("scene_select");
//			}
//		});
		Entities.newInstance(EndOverlay.class,0,0);
		finished = true;
	}
	
	private void lose(String message){
//		MessageWindow window = (MessageWindow)Entities.getEntity("MessageWindow").newInstance(0,0);
//		window.setText(message);
//		window.addListener(new WindowListener(){
//			public void onClose(){
//				Scene.newScene("scene_select");
//			}
//		});
		Entities.newInstance(EndOverlay.class,0,0);
		finished = true;
	}
 
	
	public static final EntityStateFactory getFactory(){
		return new Factory();
	}

	public static class Factory extends EntityStateFactory{
		@Override
		public EntityState construct() {
			return new TimeAttackManager();
		}

		@Override
		public Class<? extends EntityState> getType() {
			return TimeAttackManager.class;
		}
		
		@Override
		public Config getConfig(){
			return new TimeAttackConfig();
		}

        @Override
        public void getAssets(ArrayList<String[]> assets) {
            super.getAssets(assets);
            assets.add(new String[]{"texture","cancel_button"});
            assets.add(new String[]{"texture","restart"});
            assets.add(new String[]{"texture","flare"});
            assets.add(new String[]{"texture","spark"});
            assets.add(new String[]{"texture","next"});
            assets.add(new String[]{"texture","google_play_button"});
            assets.add(new String[]{"sound","blast"});
            assets.add(new String[]{"sound","air_horn"});
            assets.add(new String[]{"sound","trill_1"});
            assets.add(new String[]{"sound","trill_2"});
            assets.add(new String[]{"sound","trill_3"});
            assets.add(new String[]{"sound","trill_4"});
            assets.add(new String[]{"sound","end_flourish"});

        }
    }
	
	public static final class TimeAttackConfig extends Config{
		public double timeLimit = 30;
		public String levelMusic = "citric_wedge";
		public ScoreConfig scoreConfig = new ScoreConfig();
		
		public void TimeAttackConfig(){}
		
		@Override
		public void setValue(String text) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getAliases(HashMap<String, String> map) {
			// TODO Auto-generated method stub
			
		}
		
	}
}
