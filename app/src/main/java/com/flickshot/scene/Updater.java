package com.flickshot.scene;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import android.util.Log;
import android.view.Display;

import com.flickshot.GameView;

public class Updater extends Thread{
	private static final AtomicInteger UID = new AtomicInteger();
	private static final double CONVERSION = 1000000000.0;
	
	private static final int MAX_FRAMES = 500;
	private static final long MIN_TIME = (long)((1.0/MAX_FRAMES)*CONVERSION);
	
	private static final double maxDelta = 1.0/30.0;
	
	private final AtomicBoolean doUpdate = new AtomicBoolean(true);
	private final AtomicLong fps = new AtomicLong(0);
	private final AtomicLong filteredFps = new AtomicLong(0);
	
	private final Object freezeLock = new Object();
	
	public final AtomicBoolean frozen = new AtomicBoolean(false);
	public final AtomicBoolean paused = new AtomicBoolean(false);
	public final AtomicBoolean running = new AtomicBoolean(true);
	public final AtomicBoolean notify = new AtomicBoolean(false);
	
	public final AtomicLong filterStrength = new AtomicLong(200);
	
	private AtomicLong currTime = new AtomicLong(0);
	
	public Updater(){
		setName("Game Updater "+UID.getAndIncrement());
	}
	
	@Override
	public void run(){
		UpdateEvent event = new UpdateEvent(System.nanoTime());
		long refreshTime = (long)((1/GameView.getCurrent().refreshRate)*CONVERSION);
		long prevTime = System.nanoTime();
		long frameTime = 0;
		while(running.get()){
			long thisTime = System.nanoTime();
			long thisFrameTime = thisTime-prevTime;
			do{
				thisTime = System.nanoTime();
				thisFrameTime = thisTime-prevTime;
				synchronized(this){
					if(frozen.get()){
						notify();
						try {
							wait();
						} catch (InterruptedException e) {
						}
					}
				}
				if(thisFrameTime<MIN_TIME) Thread.yield();
			}while(thisFrameTime<MIN_TIME);

			frameTime += (thisFrameTime - frameTime) / filterStrength.get();
			prevTime = thisTime;
			
			double fps = CONVERSION/thisFrameTime;
			double filteredFps = CONVERSION/frameTime;
			double realDelta = thisFrameTime/CONVERSION;
			double delta = (paused.get()) ? 0 : realDelta;
			
			if(delta>maxDelta)delta=maxDelta;
			
			if(doUpdate.get()){
				event.set(delta, realDelta, fps, filteredFps, thisTime);
				update(event);
			}
			
			
//			long timeLeft = refreshTime - currTime.addAndGet(thisFrameTime);
//			if(timeLeft<=(frameTime/2)){
//				currTime.set(0);
//				freeze();
//			}

		}
	}
	
	public synchronized void freeze(){
		if(isAlive()){
			if(!frozen.get()){
				frozen.set(true);
				if(Thread.currentThread()!=this){
					try {
						wait();
					} catch (InterruptedException e) {
						throw new IllegalStateException(e);
					}
				}
			}
		}
	}
	
	public synchronized void unfreeze(){
		if(isAlive()){
			frozen.set(false);
			super.interrupt();
		}
	}
	
	public void kill(){
		if(running.get()){
			running.set(false);
		}
	}
	
	/**
	 * The update event passed by this method is only
	 * guaranteed to be valid until the method exits
	 * @param evt
	 */
	public void update(UpdateEvent evt){
		UpdateQueue.update(evt);
	}
	
	public static class UpdateEvent{
		private double delta;
		private double realDelta;//always set to something
		
		private double fps;
		private double filteredFps;
		
		private final long startTime;
		private long updateTime;
		
		
		private UpdateEvent(long startTime){
			this.startTime = startTime;
		}
		
		private void set(double delta, double realDelta, double fps, double filteredFps, long updateTime){
			this.delta = delta;
			this.realDelta = realDelta;
			this.fps = fps;
			this.filteredFps = filteredFps;
			this.updateTime = updateTime;
		}
		
		public final double getDelta(){
			return delta;
		}
		
		public final double getRealDelta(){
			return realDelta;
		}
		
		public final double getFps(){
			return fps;
		}
		
		public final double getFilteredFps(){
			return filteredFps;
		}
		
		public final long getStartTime(){
			return startTime;
		}
		
		public final long getUpdateTime(){
			return updateTime;
		}
		
		public String toString(){
			return "delta "+delta+" real delta "+realDelta+" fps "+fps+" filteredFps "+ filteredFps;
		}
	}
	
	public final double getFps(){
		return Double.longBitsToDouble(fps.get());
	}
	
	public final double getFilteredFps(){
		return Double.longBitsToDouble(filteredFps.get());
	}
}
