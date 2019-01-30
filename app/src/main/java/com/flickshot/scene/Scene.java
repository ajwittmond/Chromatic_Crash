package com.flickshot.scene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.flickshot.GameView;
import com.flickshot.assets.Asset;
import com.flickshot.assets.AssetLibrary;
import com.flickshot.components.Component;
import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.particles.Particles;
import com.flickshot.components.timeline.Timeline;
import com.flickshot.components.timeline.Timelines;
import com.flickshot.components.timers.Timers;
import com.flickshot.config.Config;
import com.flickshot.util.Action;
import com.flickshot.util.MutableInt;

import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.util.Log;

public class Scene {
	private final static String MANIFEST_CONFIG_TAG = "Manifest";
	
	private final static String CONFIG_ATTRIBUTE = "config";
	
	private final static String ASSETS_CONFIG_TAG = "Assets";
	private final static String SCENES_CONFIG_TAG = "Scenes";
	
	private final static String SCENE_CONFIG_TAG = "Scene";
	
	private final static String ENTITY_CONFIG_TAG = "config";
	private final static String X_ATTRIBUTE = "x";
	private final static String Y_ATTRIBUTE = "y";
	private final static String TIME_ATTRIBUTE = "t";
	
	private final static String ASSET_TAG = "asset";
	private final static String ASSET_TYPE_ATTRIBUTE = "type";
	private final static String ASSET_ID_ATTRIBUTE = "id";
	
	private static final HashMap<String, SceneConfig> scenes = new HashMap<String, SceneConfig>();
	private static final ArrayList<SceneListener> listeners = new ArrayList<SceneListener>();
	
	private static String defaultName;
	private static boolean initialized = false;
	
	private static Scene current;
	
	private static final AtomicBoolean hasNewScene= new AtomicBoolean();
	private static String newSceneId;
	private static SceneInitializer newSceneInitializer;
	
	public static final Scene getCurrent(){
		return current;
	}
	
	@SuppressWarnings("unchecked")
	public static final HashMap<String, SceneConfig> getConfigs(){
		HashMap<String, SceneConfig> out = new HashMap<String, SceneConfig>();
		for(String key:scenes.keySet())out.put(key,scenes.get(key));
		return out;
	}
	
	public static final void newScene(String id){
		newScene(id,null);
	}
	
	public static final void newScene(String id,SceneInitializer init){
		hasNewScene.set(true);
		newSceneId = id;
		newSceneInitializer = init;
	}
	
	public static final Scene create(String id,SceneInitializer init){
		if(current!=null)end();
		current = new Scene(id,init);
		return current;
	}
	
	private static final void end(){
        current.stop();
	}
	
	public static final String getDefaultName(){
		return defaultName;
	}
	
	public static final void addSceneListener(SceneListener ctx){
		listeners.add(ctx);
	}
	
	public static boolean check(){
		if(hasNewScene.get()){
			hasNewScene.set(false);
			current.end();
			while(current.updater.isAlive()){
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					throw new IllegalStateException(e);
				}
			}
            Particles.clearAll();
			for(SceneListener l: listeners)l.onEnd();
			current = new Scene(newSceneId);
			current.start();
			return true;
		}
		return false;
	}
	
	public static final void loadManifest(Resources r,XmlResourceParser xrp){
		MutableInt line = new MutableInt(0);
		try{
			HashMap<String,Integer> sceneIds=null; 
			HashMap<String,ArrayList<Asset>> assets=null;
			
			int event = xrp.next();
			while(event!=XmlResourceParser.START_TAG || !xrp.getName().equals(MANIFEST_CONFIG_TAG)){
				event = xrp.next();
			}
			while(event != XmlResourceParser.END_DOCUMENT && !(event == XmlResourceParser.END_TAG && xrp.getName().equals(MANIFEST_CONFIG_TAG))){
				if(xrp.getName().equals(MANIFEST_CONFIG_TAG)){
					event = xrp.next();
					continue;
				}
				if(event == XmlResourceParser.START_TAG){
					if(xrp.getName().equals(SCENES_CONFIG_TAG)){
						sceneIds = loadScenesManifest(line,r,xrp);
					}else if(xrp.getName().equals(ASSETS_CONFIG_TAG)){
						assets = loadAssetsManifest(line,xrp);
					}
				}
				event=xrp.next();
			}
			if(sceneIds==null) throw new IllegalStateException("failed to load scenes");
			if(assets==null) throw new IllegalStateException("failed to load assets");
			AssetLibrary.addAssets(assets);
			for(String name:sceneIds.keySet()){
				scenes.put(name,loadSceneConfig(r.getXml(sceneIds.get(name))));
			}
			Log.d("scene","manifest finished loading");
		}catch(Exception ex){
			throw new IllegalStateException("failed to load manifest. line:"+line.val,ex);
		}
	}
	
	private static HashMap<String,Integer> loadScenesManifest(MutableInt line,Resources r,XmlResourceParser xrp) throws XmlPullParserException, IOException{
		HashMap<String,Integer> scenes = new HashMap<String,Integer>();
		defaultName = xrp.getAttributeValue(null,"default");
		int event = xrp.next();
		while(!(event == XmlResourceParser.END_TAG && xrp.getName().equals(SCENES_CONFIG_TAG))){
			if(event == XmlResourceParser.START_TAG){
				line.val = xrp.getLineNumber();
				String name = xrp.getName();
				String config = xrp.getAttributeValue(null,CONFIG_ATTRIBUTE);
				if(config==null)throw new IllegalStateException("scene must specify config attribute: "+name+" "+line.val);
				Log.d("Scene","loaded scene config: "+name+" "+config);
				scenes.put(name,r.getIdentifier(config,"xml","com.flickshot"));
			}
			event = xrp.next();
		}
		return scenes;
	}
	
	private static HashMap<String,ArrayList<Asset>> loadAssetsManifest(MutableInt line,XmlResourceParser xrp) throws XmlPullParserException, IOException{
		HashMap<String,ArrayList<Asset>> assets= new HashMap<String,ArrayList<Asset>>();
		int event = xrp.next();
		while(!(event == XmlResourceParser.END_TAG && xrp.getName().equals(ASSETS_CONFIG_TAG))){
			if(event == XmlResourceParser.START_TAG){
				line.val = xrp.getLineNumber();
				AssetLibrary.loadAsset(assets,xrp);
			}
			event = xrp.next();
		}
		return assets;
	}
	
	private static SceneConfig loadSceneConfig(XmlResourceParser xrp){
		ArrayList<EntityConfig> entities = new ArrayList<EntityConfig>();
		HashMap<String,Config> componentConfigs = new HashMap<String,Config>();
		int line = 0;
		HashMap<String,HashSet<Asset>> assets=new HashMap<String,HashSet<Asset>>();
		try{
			
			int event = xrp.next();
			while(event!=XmlResourceParser.START_TAG || !xrp.getName().equals(SCENE_CONFIG_TAG)){
				event = xrp.next();
			}
			
			while(event != XmlResourceParser.END_DOCUMENT && !(event == XmlResourceParser.END_TAG && xrp.getName().equals(SCENE_CONFIG_TAG))){
				
				if(xrp.getName().equals(SCENE_CONFIG_TAG)){
					event = xrp.next();
					continue;
				}
				if(event == XmlResourceParser.START_TAG){
					line = xrp.getLineNumber();
					String name = xrp.getName();
					if(name.equalsIgnoreCase(ASSET_TAG)){
						String id = null;
						String type = null;
						int c = xrp.getAttributeCount();
						for(int i=0;i<c;i++){
							String n = xrp.getAttributeName(i);
							if(n.equalsIgnoreCase(ASSET_ID_ATTRIBUTE)){
								id = xrp.getAttributeValue(i);
							}else if(n.equalsIgnoreCase(ASSET_TYPE_ATTRIBUTE)){
								type = xrp.getAttributeValue(i);
							}
						}
						if(id==null || type==null){
							throw new IllegalStateException("asset invalid.  line:"+line);
						}else{
							if(!assets.containsKey(type))assets.put(type,new HashSet<Asset>());
							assets.get(type).add(AssetLibrary.getAsset(type,id));
						}
					}else if(Component.isConfigurableTag(name)){
						//load component config
						Config c = Component.getConfig(name);
						c.configure(name,xrp);
						componentConfigs.put(name,c);
					}else{
						//load entity
						double x = 0;
						double y = 0;
						double t = 0;
						int c = xrp.getAttributeCount();
						for(int i=0;i<c;i++){
							String n = xrp.getAttributeName(i);
							if(n.equalsIgnoreCase(X_ATTRIBUTE)){
								x = Double.parseDouble(xrp.getAttributeValue(i));
							}else if(n.equalsIgnoreCase(Y_ATTRIBUTE)){
								y = Double.parseDouble(xrp.getAttributeValue(i));
							}else if(n.equalsIgnoreCase(TIME_ATTRIBUTE)){
								t = Double.parseDouble(xrp.getAttributeValue(i));
							}
						}
						//load possible entity config
						Config cfg = null;
						event=xrp.next();
						while(event != XmlResourceParser.END_TAG && event != XmlResourceParser.START_TAG) event = xrp.next();
						if(event == XmlResourceParser.START_TAG && xrp.getName().equals(ENTITY_CONFIG_TAG)){
							line = xrp.getLineNumber();
							cfg = Entities.getEntityConfig(name);
							cfg.configure(ENTITY_CONFIG_TAG,xrp);
							for(;!(event==XmlPullParser.END_TAG && xrp.getName().equals(name));event=xrp.next());
						}
						entities.add(new EntityConfig(name,x,y,t,cfg));
						continue;
					}
				}
				event=xrp.next();
			}
		}catch(Exception ex){
			throw new IllegalStateException("failed to load scene config. line:"+line,ex);
		}
		//load asset set
		
		for(EntityConfig e:entities){
			if(Entities.getEntity(e.name)==null)
				throw new IllegalStateException("cannot find entity: "+e.name);
			String[][] ea = Entities.getAssetsForEntity(e.name);
			for(String[] asset: ea){
				if(!assets.containsKey(asset[0]))assets.put(asset[0],new HashSet<Asset>());
				assets.get(asset[0]).add(AssetLibrary.getAsset(asset[0],asset[1]));
			}
		}
		HashMap<String,Asset[]> assetm=new HashMap<String,Asset[]>();
		for(String type:assets.keySet()){
				assetm.put(type,assets.get(type).toArray(new Asset[assets.get(type).size()]));
		}
		return new SceneConfig(entities.toArray(new EntityConfig[entities.size()]),assetm,componentConfigs);
	}
	
	private static void loadScene(String id){
		if(!scenes.containsKey(id))throw new IllegalStateException("scene "+id+" is not defined");
		SceneConfig config = scenes.get(id);
		
		try{
			Log.d("Scene","loading "+id);
			//load assets
			AssetLibrary.load(config.assets);
			
			//configure componenets
			for(String component: config.componentConfigs.keySet()){
				Log.d("Scene","configured: "+component);
				Component.configure(component,config.componentConfigs.get(component));
			}
			
			//load entities
			Timeline t = new Timeline("default");
			for(final EntityConfig e: config.entities){
				try{
//					if(e.t == 0){
//						EntityState ent = Entities.getEntity(e.name).newInstance(e.x,e.y);
//						if(e.config!=null)ent.configure(e.config);
//					}else{
//						Timers.add(new com.flickshot.components.timers.Timer(ec.t){
//							@Override
//							public void action() {
//								EntityState ent = Entities.getEntity(ec.name).newInstance(ec.x,ec.y);
//								if(ec.config!=null)ent.configure(ec.config);
//							}
//						});
						t.addAction(e.t,new Action(){
							public void doAction(){
								EntityState ent = Entities.getEntity(e.name).newInstance(e.x,e.y);
								if(e.config!=null)ent.configure(e.config);
							}
						});
//					}
				}catch(Exception ex){
					throw new IllegalStateException("failed to load entity:"+e.name,ex);
				}
			}
			Timelines.add(t);
		}catch(Exception ex){
			throw new RuntimeException("failed to load scene",ex);
		}
	}
	
	private final SceneInitializer initializer;
	public final Updater updater = new Updater();
	public final String id;
	
	private Scene(String id){
		this.id = id;
		initializer = null;
		try{
			loadScene(id);
		}catch(Exception ex){
			throw new IllegalStateException("could not load scene",ex);
		}
	}
	
	private Scene(String id,SceneInitializer init){
		this.id = id;
		initializer = init;
		try{
			loadScene(id);
		}catch(Exception ex){
			throw new IllegalStateException("could not load scene",ex);
		}
	}
	
	public void start(){
		System.gc();
		Log.d("Scene","starting new scene");
		if(initializer!=null)initializer.init();
		for(SceneListener l: listeners)l.onStart();
		updater.start();
	}
	
	public void stop(){
		updater.kill();
	}
	
	public static interface SceneInitializer{
		public void init();
	}
	
	public static interface SceneListener{
		public void onStart();
		public void onEnd();
	}
	
	public static final class SceneConfig{
		final HashMap<String,Config> componentConfigs;
		final EntityConfig[] entities;
		final HashMap<String,Asset[]> assets;
		SceneConfig(EntityConfig[] entities,HashMap<String,Asset[]> assets,HashMap<String,Config> componentConfigs){
			this.entities = entities;
			this.assets = assets;
			this.componentConfigs = componentConfigs;
		}
	}
	
	private static final class EntityConfig{
		final String name;
		final double x,y,t;
		final Config config;
		EntityConfig(String name,double x,double y, double t,Config config){
			this.name = name;
			this.x=x;
			this.y=y;
			this.t=t;
			this.config = config;
		}
	}
}
