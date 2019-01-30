package com.flickshot.assets.sfx;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;

import com.flickshot.GameView;
import com.flickshot.assets.Asset;
import com.flickshot.assets.AssetManager;
import com.flickshot.assets.music.Song;

public class SFXManager extends AssetManager{
	public static final String NAME = "sound";
	
	private static final HashSet<Asset> assets = new HashSet<Asset>();
	private static final HashMap<String,SoundData> sounds = new HashMap<String,SoundData>();
	
	private static final int MAX_STREAMS = 24;
	private static final SoundPool pool = new SoundPool(MAX_STREAMS,AudioManager.STREAM_MUSIC,0);
	
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
			int id = pool.load(GameView.getMain(),GameView.getMain().getResources().getIdentifier(asset.fileId,"raw","com.flickshot"),1);
			sounds.put(asset.id,new SoundData(pool,id));
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
		SoundData s = sounds.remove(asset.id);
		s.pool.unload(s.poolId);
	}

	@Override
	public void unload(Collection<Asset> c) {
		for(Asset a: c){
			if(assets.contains(a)) unload(a);
		}
	}

	@Override
	public boolean contains(String id) {
		return sounds.containsKey(id);
	}

	@Override
	public Object get(String id) {
		return sounds.get(id);
	}

	@Override
	public String getName() {
		return NAME;
	}

    public void autoPause(){
        pool.autoPause();
    }

    public void autoResume(){
        pool.autoResume();
    }
}
