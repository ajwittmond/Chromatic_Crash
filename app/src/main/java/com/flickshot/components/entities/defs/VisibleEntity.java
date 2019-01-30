package com.flickshot.components.entities.defs;

import android.util.Log;

import com.flickshot.components.entities.CommonEntity;
import com.flickshot.components.graphics.Artist;

public abstract class VisibleEntity extends CommonEntity{
    Artist artist;
	
	public final void setArtist(Artist newArtist){
		if(this.artist!=null){
			boolean bound = artist.bound();
			if(bound){
				artist.unbind();
				if(!newArtist.bound()) newArtist.bind();
			}else{
				if(newArtist.bound()) newArtist.unbind();
			}
		}
		artist = newArtist;
	}
	
	@Override
	public void init(double x, double y){
		super.init(x,y);
		artist.bind();
	}
	
	public void setVisible(boolean visible){
		artist.setVisible(visible);
	}
	
	@Override
	public void destroy(){
		artist.unbind();
	}
	
	@Override
	public void unload(){
		artist.unbind();
	}
	
}
