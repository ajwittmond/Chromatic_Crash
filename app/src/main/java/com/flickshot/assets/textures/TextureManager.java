package com.flickshot.assets.textures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.util.Log;

import com.flickshot.GameView;
import com.flickshot.assets.Asset;
import com.flickshot.assets.AssetLibrary;
import com.flickshot.assets.AssetManager;
import com.flickshot.components.graphics.DrawLib;
import com.flickshot.components.graphics.GLTexture;

public class TextureManager extends AssetManager{
	private static final String NAME = "texture";
	private static final HashSet<Asset> assets = new HashSet<Asset>();
	private static final HashMap<Object,SpriteSheet> spriteSheets = new HashMap<Object,SpriteSheet>();
	private static final HashMap<Object,String> loaded = new HashMap<Object,String>();

	@Override
	public void swap(Collection<Asset> c) {
		final ArrayList<Asset> rids = new ArrayList<Asset>();
		for(Asset curr: assets){
			if(!(curr.global || c.contains(curr))){
				rids.add(curr);
			}
		}
		for(Asset asset : rids){
			unload(asset);
		}
		load(c);
	}

	@Override
	public void load(Asset asset) {
		TextureAsset tasset = (TextureAsset)asset;
		if(spriteSheets.containsKey(tasset.id)) throw new IllegalStateException("key already taken "+tasset);
		if(loaded.containsValue(tasset.fileId)) throw new IllegalStateException("texture already loaded "+tasset);
		Context main = GameView.getMain();
		try{
			GLTexture texture = new GLTexture(main.getResources(),asset.fileId);
			SpriteSheet sheet;
			if(tasset.data!=null){
				sheet = new SpriteSheet(texture,tasset.data.cellWidth,tasset.data.cellHeight,tasset.data.animations);
			}else{
				sheet = new SpriteSheet(texture,texture.width,texture.height,new HashMap<String,SpriteSheet.Animation>());
			}
			spriteSheets.put(tasset.id,sheet);
		}catch(Exception ex){
			throw new RuntimeException("failed to load: "+asset,ex);
		}
		loaded.put(asset.id,asset.fileId);
		assets.add(asset);
		Log.d("textures",asset+" loaded");
	}

	@Override
	public void load(Collection<Asset> c) {
		for(Asset a: c){
			if(!spriteSheets.containsKey(a.id)){
				load(a);
			}
		}
	}

	@Override
	public void unload(Asset asset) {
		if(!spriteSheets.containsKey(asset.id)) throw new IllegalStateException("texture not loaded");
		spriteSheets.remove(asset.id).texture.delete();
		loaded.remove(asset.id);
		assets.remove(asset);
		Log.d("textures",asset+" unloaded");
	}

	@Override
	public void unload(Collection<Asset> c) {
		for(Asset a: c){
			if(spriteSheets.containsKey(a.id)){
				unload(a);
			}
		}
	}

	@Override
	public Object get(String id) {
		return spriteSheets.get(id);
	}
	
	public boolean contains(String id){
		return spriteSheets.containsKey(id);
	}

	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	public Asset loadAsset(XmlResourceParser xrp){
		try {
			if(xrp.getAttributeCount()==2){
				return new TextureAsset(super.loadAsset(xrp));
			}
			String content = xrp.getText();
			if(content==null) content="";
			
			//load attributes
			String id = null;
			String source = "";
			String width = null;
			String height = null;
			int c = xrp.getAttributeCount();
			for(int i=0;i<c;i++){
				String n = xrp.getAttributeName(i);
				if(n.equals(AssetLibrary.ID_ATTRIBUTE)){
					id = xrp.getAttributeValue(i);
				}else if(n.equals(AssetLibrary.SRC_ATTRIBUTE)){
					source = xrp.getAttributeValue(i);
				}else if(n.equals("w")){
					width = xrp.getAttributeValue(i);
				}else if(n.equals("h")){
					height = xrp.getAttributeValue(i);
				}
			}
			if(id==null)throw new IllegalStateException("asset must specify id attribute: "+xrp.getName());
			if(width==null || height==null) throw new IllegalStateException("texture did not provide cell width and height "+id);
			//load animations
			HashMap<String,SpriteSheet.Animation> animations = new HashMap<String,SpriteSheet.Animation>();
			int event = xrp.getEventType();
			while(!(event==XmlPullParser.END_TAG && getName().equals(xrp.getName()))){
				if(event==XmlPullParser.START_TAG && xrp.getName().equals("cycle")){
					String cid = xrp.getAttributeValue(null,"id");
					String start = xrp.getAttributeValue(null,"start");
					String end = xrp.getAttributeValue(null,"end");
					String looped = xrp.getAttributeValue(null,"looped");
					if(cid==null||start==null||end==null||looped==null)
						throw new IllegalStateException("animation did not provide all necessary information");
					Log.e("textureManager","animation loaded "+cid);
					animations.put(cid,new SpriteSheet.Animation(Integer.parseInt(start),Integer.parseInt(end),looped.equalsIgnoreCase("true")));
				}
				event = xrp.next();
			}
			return new TextureAsset(id,source,content,false,new SpriteData(Integer.parseInt(width),Integer.parseInt(height),animations));
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
	
	class TextureAsset extends Asset{
		
		final SpriteData data;
		
		TextureAsset(String id,String fileId,String content,boolean global,SpriteData data){
			super(id,fileId,content,global);
			this.data = data;
		}
		
		TextureAsset(Asset asset){
			super(asset.id,asset.fileId,asset.content,asset.global);
			data=null;
		}
	}
	
	class SpriteData{
		final int cellWidth;
		final int cellHeight;
		final HashMap<String,SpriteSheet.Animation> animations;
		
		SpriteData(int cellWidth, int cellHeight, HashMap<String,SpriteSheet.Animation> animations){
			this.cellWidth = cellWidth;
			this.cellHeight = cellHeight;
			this.animations = animations;
		}
		
	}
}
