package com.flickshot.components.entities;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;

import android.util.Log;

import com.flickshot.GameView;
import com.flickshot.components.Component;
import com.flickshot.config.Config;
import com.flickshot.scene.Scene;
import com.flickshot.scene.Updatable;
import com.flickshot.scene.UpdateQueue;
import com.flickshot.scene.Updater.UpdateEvent;

import dalvik.system.DexFile;

public final class Entities extends Component{
	
	private static Entities current;
	
	private final static HashMap<String,Entity> nameMap = new HashMap<String,Entity>();
	private final static HashMap<Class<?>,EntityTree> nodeMap = new HashMap<Class<?>,EntityTree>();
	private static Entity[] entities = new Entity[8];
	private static int position=0;
	
	private final static EntityTree root = new EntityTree(EntityState.class);
	
	
	public static Entities create(){
		if(current!=null){
			throw new IllegalStateException();
		}
		current = new Entities();
		findDefs();
		UpdateQueue.add(current);
		Scene.addSceneListener(new Scene.SceneListener(){
			@Override
			public void onStart() {}

			@Override
			public void onEnd() {
				clearStates();
			}
		});
		return current;
	}
	
	private static void findDefs(){
		try{
			DexFile df = new DexFile(GameView.getMain().getPackageCodePath());
			Enumeration<String> iter = df.entries();
			while(iter.hasMoreElements()){
				String name = iter.nextElement();
				if(name.startsWith("com.flickshot.components.entities.defs")){
					Class<?> clazz = Class.forName(name);
					if(!Modifier.isAbstract(clazz.getModifiers()) && EntityState.class.isAssignableFrom(clazz)){
						Log.d("Entities","loading entity state "+name);
						String ename;
						try{
							ename = (String)clazz.getField("ENTITY_NAME").get(null);
						}catch(Exception ex){
							throw new IllegalStateException("failed to find entity name: "+name,ex);
						}
						try{
							add(ename,new Entity((EntityStateFactory)clazz.getMethod("getFactory").invoke(null)));
						}catch(Exception ex){
							throw new IllegalStateException("failed to get entity factory: "+name,ex);
						}
					}
				}
			}
			root.addToNodeMap(nodeMap);
		}catch(Exception ex){
			throw new IllegalStateException("failed to load components",ex);
		}
	}
	
	public static Entities getCurrentEntities(){	
		return current;
	}
	
	@Override
	public void preUpdate(UpdateEvent evt) {
		for(int i = 0; i<position; i++){
			entities[i].preUpdate(evt);
		}
	}

	@Override
	public void update(UpdateEvent evt) {
		for(int i = 0; i<position; i++){
			entities[i].update(evt);
		}
	}

	@Override
	public void postUpdate(UpdateEvent evt) {
		for(int i = 0; i<position; i++){
			entities[i].postUpdate(evt);
		}
	}
	
	public static final void add(String name,Entity e){
		nameMap.put(name,e);
		nodeMap.put(e.stateType,root.add(e));
		if(position>=entities.length){
			Entity[] temp = entities;
			entities = new Entity[temp.length+8];
			System.arraycopy(temp, 0, entities, 0, temp.length);
		}
		entities[position++]=e;
	}
	
	public static final void clearStates(){
		for(int i = 0; i<position; i++){
			entities[i].clear();
		}
	}
	
	public static final Entity getEntity(String name){
		Entity e = nameMap.get(name);
        if(e==null){
            throw new IllegalStateException("Entity \""+name+"\" not found");
        }
        return e;
	}
	
	public static final Entity getEntity(Class entityState){
		return nodeMap.get(entityState).value;
	}
	
	public static final EntityState newInstance(String name, double x, double y){
		return nameMap.get(name).newInstance(x,y);
	}
	
	public static final EntityState newInstance(Class entityState, double x, double y){
		return nodeMap.get(entityState).value.newInstance(x,y);
	}
	
	public static final String[][] getAssetsForEntity(String name){
		return getEntity(name).assets;
	}
	
	public static final Config getEntityConfig(String name){
		return getEntity(name).factory.getConfig();
	}
	
	public static final Collection<EntityState> getStates(Class<? extends EntityState> type,Collection<EntityState> c){
		EntityTree t = nodeMap.get(type);
		if(t!=null){
			t.getStates(c);
		}else{
			throw new IllegalStateException("cannot find type "+type);
		}
		return c;
	}
	
	public static final int getStateCount(Class<? extends EntityState> type){
		EntityTree t = nodeMap.get(type);
		if(t!=null){
			return t.getCount();
		}else{
			throw new IllegalStateException("cannot find type "+type);
		}
	}
	
	public static final int getStateCount(String entity){
		EntityTree t = nodeMap.get(nameMap.get(entity).factory.getType());
		if(t!=null){
			return t.getCount();
		}else{
			throw new IllegalStateException("cannot find type for "+entity);
		}
	}
	
	public void reset(){
		
	}
}
