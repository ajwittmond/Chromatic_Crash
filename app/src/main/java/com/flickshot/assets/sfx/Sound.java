package com.flickshot.assets.sfx;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.flickshot.FlickShot;
import com.flickshot.GameView;
import com.flickshot.assets.AssetLibrary;

import android.media.SoundPool;

/**
 * This class provides abstraction for dealing with sound effects
 * @author Alex
 *
 */
public class Sound {
	private static final BlockingQueue<SoundEvent> soundQueue = new LinkedBlockingQueue<SoundEvent>();
	private static final ArrayList<SoundEvent> eventPool = new ArrayList<SoundEvent>();
	private static final int MIN_TIME = 1000000000;
	
	private static final int PLAY = 0;
	private static final int PAUSE = 1;
	private static final int STOP = 2;
	private static final int RESUME = 3;
	private static final int VOLUME = 4;
	private static final int LOOPING = 5;
	
	private static final Thread soundThread = new Thread(){
		public void run(){
			while(true){
				SoundEvent evt;
				try {
					evt = soundQueue.take();
					switch(evt.action){
						case PLAY:
							if(evt.s.streamId!=0)evt.pool.stop(evt.s.streamId);
							evt.s.streamId = evt.pool.play(
                                    evt.poolId,
                                    evt.left*(float)FlickShot.options.soundFXVolume,
                                    evt.right*(float)FlickShot.options.soundFXVolume,
                                    1,evt.looping ? -1 : 0,1);
							break;
						case PAUSE:
							if(evt.s.streamId!=0)evt.pool.pause(evt.s.streamId);
							break;
						case STOP:
							if(evt.s.streamId!=0)evt.pool.stop(evt.s.streamId);
							break;
						case RESUME:
							if(evt.s.streamId!=0)evt.pool.resume(evt.s.streamId);
							break;
						case VOLUME:
							if(evt.s.streamId!=0)evt.pool.setVolume(
                                    evt.s.streamId,
                                    evt.left*(float)FlickShot.options.soundFXVolume,
                                    evt.right*(float)FlickShot.options.soundFXVolume);
							break;
						case LOOPING:
							if(evt.s.streamId!=0)evt.pool.setLoop(evt.s.streamId,(evt.looping) ? 1:-1);
							break;
							
					}
					synchronized(eventPool){
						eventPool.add(evt);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	};
	
	static{
		soundThread.setDaemon(true);
		soundThread.start();
	}
	
	private static void queueEvent(int action, Sound s){
		SoundEvent evt;
		synchronized(eventPool){
			if(!eventPool.isEmpty()){
				evt = eventPool.remove(eventPool.size()-1);
			}else{
				evt = new SoundEvent();
			}
		}
		SoundData d = (SoundData)AssetLibrary.get("sound",s.id);
		if(d==null)throw new IllegalStateException("sound "+s.id+" not loaded");
		evt.poolId = d.poolId;
		evt.action = action;
		evt.left = s.left;
		evt.right = s.right;
		evt.looping = s.looping;
		evt.pool = d.pool;
		evt.time = System.nanoTime();
		evt.s = s;
		try {
			if(!soundQueue.contains(evt))soundQueue.put(evt);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public final String id;
	
	
	private float left=1,right=1;
	private boolean looping;
	private int streamId = 0;
	
	public Sound(String id){
		this.id = id;
	}
	
	public void setLooping(boolean looping){
		this.looping = looping;
		queueEvent(LOOPING,this);
	}
	
	public void setVolume(float left, float right){
		this.left = Math.max(0,Math.min(left,1));
		this.right = Math.max(0,Math.min(right,1));
		queueEvent(VOLUME,this);
	}
	
	public float getLeftVolume(){
		return left;
	}
	
	public float getRightVolume(){
		return right;
	}
	
	public void play(){
		queueEvent(PLAY,this);
	}
	
	public void pause(){
		queueEvent(PAUSE,this);
	}
	
	public void stop(){
		queueEvent(STOP,this);
	}
	
	public void resume(){
		queueEvent(RESUME,this);
	}
	
	private static class SoundEvent{
		SoundPool pool;
		int poolId;
		int action;
		boolean looping;
		float left,right;
		long time;
		
		Sound s;
		
		public boolean equals(Object o){
			if(o instanceof SoundEvent){
				SoundEvent evt = (SoundEvent)o;
				if(pool.equals(evt.pool) && poolId==evt.poolId && action == evt.action && looping == evt.looping){
					return Math.abs(time-evt.time)<MIN_TIME;
				}
			}
			return false;
		}
	}
	
}
