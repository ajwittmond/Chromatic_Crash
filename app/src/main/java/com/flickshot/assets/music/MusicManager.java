package com.flickshot.assets.music;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import android.media.MediaPlayer;
import android.util.Log;

import com.flickshot.assets.Asset;
import com.flickshot.assets.AssetManager;

public class MusicManager extends AssetManager{
	private static final String NAME = "music";
	
	private static final HashSet<Asset> assets = new HashSet<Asset>();
	private static final HashMap<String,Song> songs = new HashMap<String,Song>();
	
	@Override
	public void swap(Collection<Asset> c) {
		final ArrayList<Asset> rids = new ArrayList<Asset>();
		for(Asset curr: assets){
			if(curr.global || c.contains(curr)) continue;
			rids.add(curr);
		}
		for(Asset asset : rids){
			unload(asset);
		}
		load(c);
	}

	@Override
	public void load(Asset asset) {
		try{
			songs.put(asset.id,new Song(asset.id,asset.fileId));
			assets.add(asset);
			Log.d("MusicManager","song "+asset+" loaded");
		}catch(Exception ex){
			throw new IllegalStateException("failed to load song:"+asset,ex);
		}
	}

	@Override
	public void load(Collection<Asset> c) {
		for(Asset a: c){
			if(!assets.contains(a)) load(a);
		}
	}

	@Override
	public void unload(Asset asset) {
		assets.remove(asset);
		Song s = songs.remove(asset.id);
		s.release();
	}

	@Override
	public void unload(Collection<Asset> c) {
		for(Asset a: c){
			if(assets.contains(a)) unload(a);
		}
	}

	@Override
	public boolean contains(String id) {
		return songs.containsKey(id);
	}

	@Override
	public Object get(String id) {
		return songs.get(id);
	}

	@Override
	public String getName() {
		return NAME;
	}


    private static final ArrayList<Song> paused = new ArrayList<Song>();
    public void stopAll(){
        if(!paused.isEmpty())
            throw new IllegalStateException("resume not called");
        for(String s: songs.keySet()){
            if(songs.get(s).isPlaying()) {
                songs.get(s).pause();
                paused.add(songs.get(s));
            }
        }
    }

    public void resumeAll(){
        while(!paused.isEmpty()){
            ((Song)paused.remove(0)).play();
        }

    }
}
