package com.flickshot.assets.music;

import java.io.IOException;

import com.flickshot.FlickShot;
import com.flickshot.GameView;

import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.TrackInfo;
import android.util.Log;

/**
 * This class serves to abstract out audio functionality for large sound files
 * @author Alex
 *
 */
public class Song {
	private final MediaPlayer player;
	private float left=1,right=1;
	public final String id;
	
	public Song(String id,String src) throws IllegalArgumentException, IllegalStateException, IOException{
		player = MediaPlayer.create(GameView.getMain(),GameView.getMain().getResources().getIdentifier(src,"raw","com.flickshot"));
		player.setVolume((float)FlickShot.options.musicVolume,(float)FlickShot.options.musicVolume);
		player.setLooping(true);
		this.id = id;
	}
	
	public void play(){
		player.start();
	}
	
	public void pause(){
		player.pause();
	}
	
	public void stop(){
		player.reset();
	}
	
	public void setVolume(float left, float right){
		this.left = Math.max(0,Math.min(left*(float)FlickShot.options.musicVolume,1));
		this.right = Math.max(0,Math.min(right*(float)FlickShot.options.musicVolume,1));
		player.setVolume(this.left,this.right);
	}
	
	public float getLeftVolume(){
		return left;
	}
	
	public float getRightVolume(){
		return right;
	}
	
	public boolean isPlaying(){
		return player.isPlaying();
	}
	
	public boolean isLooping(){
		return player.isLooping();
	}
	
	public void setLooping(boolean looping){
		player.setLooping(looping);
	}
	
	public void release(){
		player.release();
	}
	
	public int length(){
		return player.getDuration();
	}
	
	public int position(){
		return player.getCurrentPosition();
	}
	
	public void seek(int position){
		player.seekTo(position);
	}
	
}
