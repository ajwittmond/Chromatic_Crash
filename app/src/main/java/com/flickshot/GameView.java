package com.flickshot;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.flickshot.components.graphics.Graphics;
import com.flickshot.geometry.Coordinate;
import com.flickshot.scene.Scene;
import com.flickshot.util.LinkedNodeList;

public class GameView extends GLSurfaceView implements GLSurfaceView.Renderer{
	private static Coordinate follower;
	
	private static GameView current;
	private static FlickShot main;
	
	private static final LinkedNodeList<PauseListenerInterface> pauseListeners = new LinkedNodeList<PauseListenerInterface>();
	
	/*
	 * If this is true then the delta value of the update is set to zero which should halt all time-dependent functions in the game.
	 * However, the update still happens so non-time-dependent things should still function as normal
	 */
	public static final AtomicBoolean paused = new AtomicBoolean(false);
	/**
	 * If this is set to true the update does not happen
	 */
	public static final AtomicBoolean frozen = new AtomicBoolean(false);
	
	public static double actualDelta = 0;
	
	static void createDisplay(FlickShot context){
		new GameView(context);
	}
	
	public static FlickShot getMain(){
		return main;
	}
	
	public static void pause(){
		paused.set(true);
		PauseListenerInterface p = pauseListeners.getHead();
		while(p!=null){
			p.onPause();
			p = p.next();
		}
	}
	
	public static void unPause(){
		paused.set(false);
		PauseListenerInterface p = pauseListeners.getHead();
		while(p!=null){
			p.onUnpause();
			p = p.next();
		}
	}
	
	public static void addPauseListener(PauseListenerInterface p){
		pauseListeners.add(p);
	}
	
	public static void removePauseListener(PauseListenerInterface p){
		pauseListeners.remove(p);
	}
	
	public static GameView getCurrent(){
		return current;
	}
	
	public static Coordinate getFollower(){
		return follower;
	}
	
	public static void setFollower(Coordinate f){
		follower = f;
	}
	
	public final float refreshRate;
	
	GameView(FlickShot context){
		super(context);
		refreshRate = context.getWindowManager().getDefaultDisplay().getRefreshRate();
		Log.e("GameView","refreshRate="+refreshRate);
		
		main = context;
		setEGLContextClientVersion(2);
		setEGLConfigChooser(8,8,8,8,16,0);
		//setRenderer(new DisplayRenderer());
		setRenderer(this);
		current = this;
	}

	private long previousTime = 0;
	private boolean wasPaused = false;
	
	@Override
	public void onDrawFrame(GL10 arg0) {
		if(!(frozen.get())){
			if(Scene.check())return;
			com.flickshot.scene.Scene ctx = com.flickshot.scene.Scene.getCurrent();
			if(ctx!=null)ctx.updater.freeze();
				double delta = calcDelta();
				Graphics.doDraw(delta);
			if(ctx!=null)ctx.updater.unfreeze();
			Graphics.finish(delta);
		}
	}
	
	private double calcDelta(){
		long t = System.nanoTime();
		if(wasPaused && !paused.get()){
			previousTime = t;
			Log.d("DisplayRenderer","unpaused first frame");
			wasPaused = false;
		}
		actualDelta = (t-previousTime)/1000000000.0;
		double delta;
		if(paused.get()){
			delta = 0;
			wasPaused = true;
		}else{
			delta = actualDelta;
		}
		previousTime=t;
		return delta;
	}

	@Override
	public void onSurfaceChanged(GL10 arg0, int width, int height) {
		Graphics.onSurfaceChanged(width,height);
	}

	@Override
	public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {
		main.init();
        previousTime = System.nanoTime();
        Graphics.onSurfaceCreated();
	}
} 
