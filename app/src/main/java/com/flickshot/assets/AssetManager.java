package com.flickshot.assets;

import java.util.Collection;

import android.content.res.XmlResourceParser;

/**
 * A singleton that holds and manages the loading of game assets, such has sound and texture data
 * @author Alex
 *
 */
public abstract class AssetManager{
	
	public abstract void swap(Collection<Asset> c);
	
	public abstract void load(Asset asset);
	
	public abstract void load(Collection<Asset> c);

	public abstract void unload(Asset asset);
	
	public abstract void unload(Collection<Asset> c);
	
	public abstract boolean contains(String id);
	
	public abstract Object get(String id);
	
	public abstract String getName();
	
	public Asset loadAsset(XmlResourceParser xrp){
		String content = xrp.getText();
		if(content==null) content="";
		String id = null;
		String source = "";
		int c = xrp.getAttributeCount();
		for(int i=0;i<c;i++){
			String n = xrp.getAttributeName(i);
			if(n.equals(AssetLibrary.ID_ATTRIBUTE)){
				id = xrp.getAttributeValue(i);
			}else if(n.equals(AssetLibrary.SRC_ATTRIBUTE)){
				source = xrp.getAttributeValue(i);
			}
		}
		if(id==null)throw new IllegalStateException("asset must specify id attribute: "+xrp.getName());
		return new Asset(id,source,content,false);
	}
	
}
