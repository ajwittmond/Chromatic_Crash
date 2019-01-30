package com.flickshot.assets;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import com.flickshot.GameView;
import com.flickshot.R;

import dalvik.system.DexFile;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.util.Log;

public class AssetLibrary {
	private static final String ASSET_CONFIG_TAG = "AssetConfig";
	public static final String ID_ATTRIBUTE = "id";
	public static final String SRC_ATTRIBUTE = "src";
	
	private static final HashMap<String,HashMap<String,Asset>> ASSETS = new HashMap<String, HashMap<String,Asset>>();
	
	public static final HashMap<String,AssetManager> MANAGERS = new HashMap<String,AssetManager>();
	
	public static void init(Context ctx){
		try{
			DexFile df = new DexFile(ctx.getPackageCodePath());
			Enumeration<String> iter = df.entries();
			while(iter.hasMoreElements()){
				String name = iter.nextElement();
				if(name.startsWith("com.flickshot.assets") && !name.equals("com.flickshot.assets.AssetManager")){
					Class<?> clazz = Class.forName(name);
					if(AssetManager.class.isAssignableFrom(clazz)){
						Log.d("AssetLibrary","loading class "+name);
						AssetManager manager = clazz.asSubclass(AssetManager.class).getConstructor().newInstance();
						MANAGERS.put(manager.getName(),manager);
					}
				}
			}
		}catch(Exception ex){
			throw new IllegalStateException("failed to load asset managers",ex);
		}
		loadGlobals(GameView.getMain().getResources().getXml(R.xml.global_assets));
	}
	
	public static void addAssets(HashMap<String,ArrayList<Asset>> assets){
		for(String name:assets.keySet()){
			if(!MANAGERS.containsKey(name)) throw new IllegalStateException("invalid asset type");
			ArrayList<Asset> asseta = assets.get(name);
			if(!ASSETS.containsKey(name))ASSETS.put(name,new HashMap<String,Asset>());
			HashMap<String,Asset> assetm = ASSETS.get(name);
			for(Asset a:asseta){
				assetm.put(a.id,a);
			}
		}
	}
	
	public static Asset getAsset(String type,String id){
		return ASSETS.get(type).get(id);
	}
	
	public static void load(XmlResourceParser xrp){
		try{
			final HashMap<String,ArrayList<Asset>> assets = new HashMap<String,ArrayList<Asset>>();
			int event = xrp.next();
			while(event!=XmlResourceParser.START_TAG || !xrp.getName().equals(ASSET_CONFIG_TAG)){
				event = xrp.next();
			}
			while(event != XmlResourceParser.END_DOCUMENT && !(event == XmlResourceParser.END_TAG && xrp.getName().equals(ASSET_CONFIG_TAG))){
				if(xrp.getName().equals(ASSET_CONFIG_TAG)){
					event = xrp.next();
					continue;
				}
				if(event == XmlResourceParser.START_TAG){
					String name = xrp.getName();
					String content = xrp.getText();
					if(content==null) content="";
					String id = null;
					String source = "";
					int c = xrp.getAttributeCount();
					for(int i=0;i<c;i++){
						String n = xrp.getAttributeName(i);
						if(n.equals(ID_ATTRIBUTE)){
							id = xrp.getAttributeValue(i);
						}else if(n.equals(SRC_ATTRIBUTE)){
							source = xrp.getAttributeValue(i);
						}
					}
					if(id==null)throw new IllegalStateException("asset must specify id attribute: "+name);
					if(!assets.containsKey(name))assets.put(name,new ArrayList<Asset>());
					assets.get(name).add(new Asset(id,source,content,false));
				}
				event=xrp.next();
			}
			for(String name: MANAGERS.keySet()){
				if(assets.containsKey(name)) MANAGERS.get(name).swap(assets.get(name));
			}
		}catch(Exception ex){
			throw new IllegalStateException("failed to load assets",ex);
		}
	}
	
	public static void load(HashMap<String,Asset[]> assets){
		try{
			for(String manager: assets.keySet()){
				MANAGERS.get(manager).swap(Arrays.asList(assets.get(manager)));
			}
			for(String manager:MANAGERS.keySet()){
				if(!assets.containsKey(manager)){
					MANAGERS.get(manager).swap(Arrays.asList(new Asset[]{}));
				}
			}
		}catch(Exception ex){
			throw new IllegalStateException("failed to load assets",ex);
		}
	}
	
	public static void loadAsset(HashMap<String,ArrayList<Asset>> assets,XmlResourceParser xrp){
		String name = xrp.getName();
		Asset loaded = MANAGERS.get(name).loadAsset(xrp);
		if(!assets.containsKey(name))assets.put(name,new ArrayList<Asset>());
		assets.get(name).add(loaded);
		Log.d("AssetLibrary","loaded asset: "+name+" "+loaded);
	}
	
	
	public static void loadAsset(String type, String id){
		if(!MANAGERS.get(type).contains(id))MANAGERS.get(type).load(ASSETS.get(type).get(id));
	}
	
	private static void loadGlobals(XmlResourceParser xrp){
		try{
			final HashMap<String,ArrayList<Asset>> assets = new HashMap<String,ArrayList<Asset>>();
			int event = xrp.next();
			while(event!=XmlResourceParser.START_TAG || !xrp.getName().equals(ASSET_CONFIG_TAG)){
				event = xrp.next();
			}
			while(event != XmlResourceParser.END_DOCUMENT && !(event == XmlResourceParser.END_TAG && xrp.getName().equals(ASSET_CONFIG_TAG))){
				if(xrp.getName().equals(ASSET_CONFIG_TAG)){
					event = xrp.next();
					continue;
				}
				if(event == XmlResourceParser.START_TAG){
					String name = xrp.getName();
					String content = xrp.getText();
					if(content==null) content="";
					String id = null;
					String source = "";
					int c = xrp.getAttributeCount();
					for(int i=0;i<c;i++){
						String n = xrp.getAttributeName(i);
						if(n.equals(ID_ATTRIBUTE)){
							id = xrp.getAttributeValue(i);
						}else if(n.equals(SRC_ATTRIBUTE)){
							source = xrp.getAttributeValue(i);
						}
					}
					if(id==null)throw new IllegalStateException("asset must specify id attribute: "+name);
					if(!assets.containsKey(name))assets.put(name,new ArrayList<Asset>());
					assets.get(name).add(new Asset(id,source,content,true));
				}
				event=xrp.next();
			}
			for(String name: MANAGERS.keySet()){
				if(assets.containsKey(name)) MANAGERS.get(name).swap(assets.get(name));
			}
		}catch(Exception ex){
			throw new IllegalStateException("failed to load assets",ex);
		}
	}
	
	public static Object get(String manager,String id){
		return MANAGERS.get(manager).get(id);
	}
	
	public static boolean has(String manager,String id){
		if(MANAGERS.containsKey(manager)) 
			return MANAGERS.get(manager).contains(id); 
		else return false; 
	}

    public static AssetManager getManager(String name){
        return MANAGERS.get(name);
    }

}
