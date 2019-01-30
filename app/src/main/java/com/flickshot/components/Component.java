package com.flickshot.components;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;

import android.util.Log;

import com.flickshot.GameView;
import com.flickshot.assets.AssetManager;
import com.flickshot.components.entities.Entities;
import com.flickshot.components.input.TouchManager;
import com.flickshot.components.physics.Physics;
import com.flickshot.components.timers.Timers;
import com.flickshot.config.Config;
import com.flickshot.config.Config.ConfigException;
import com.flickshot.scene.Updatable;

import dalvik.system.DexFile;

/**
 * This class loads, defines, and configure components.
 * The contract for components is that to be loaded it needs to be
 * in a package starting with com.flickshot.components.Component and have
 * a public static method called create.
 * 
 * configurable components need the following methods:
 * 		public static String getTag();
 * 		public static Config getConfig();
 * 		public static void configure(Config c);
 * 	
 * @author Alex
 *
 */
public abstract class Component extends Updatable{
	private final static HashMap<String, Configurable> configurableComponents = new HashMap<String, Configurable>();
	
	private final static String PACKAGE_NAME = Component.class.getPackage().getName();
	private final static String CLASS_NAME = PACKAGE_NAME + ".Component";
	
	/**
	 * Called when the context changes
	 */
	public abstract void reset();
	
	private static boolean initialized = false;
	public final static void init(){
		Log.d(CLASS_NAME,"loading components from package:"+PACKAGE_NAME);
		if(!initialized){
			initialized = true;
			try{
				DexFile df = new DexFile(GameView.getMain().getPackageCodePath());
				Enumeration<String> iter = df.entries();
				while(iter.hasMoreElements()){
					String name = iter.nextElement();
					if(name.startsWith(PACKAGE_NAME) && !name.equals(CLASS_NAME)){
						Class<?> clazz = Class.forName(name);
						if(Component.class.isAssignableFrom(clazz)){
							Log.d("Component","loading component "+name);
							configurableCheck(clazz,name);
							try{
								clazz.getMethod("create").invoke(null);
							}catch(Exception ex){
								throw new IllegalStateException("failed to call method create() for class:"+name,ex);
							}
						}
					}
				}
			}catch(Exception ex){
				throw new IllegalStateException("failed to load components",ex);
			} 
		}else{
			Log.e("Component", "second call to initComponents");
		}
	}
	
	public static final boolean isConfigurableTag(String tag){
		return configurableComponents.containsKey(tag);
	}
	
	public static final Config getConfig(String tag) throws ConfigException{
		Configurable c = configurableComponents.get(tag);
		try{
			return (Config)c.clazz.getMethod("getConfig").invoke(null);
		}catch(Exception ex){
			throw new ConfigException("failed to get config from component: "+tag,ex);
		}
	}
	
	public static final void configure(String tag, Config config) throws ConfigException{
		Configurable c = configurableComponents.get(tag);
		try{
			c.clazz.getMethod("configure",Config.class).invoke(null,config);
		}catch(Exception ex){
			throw new ConfigException("failed to configure: "+tag,ex);
		}
	}
	
	public static final void configure(String tag,XmlPullParser xrp) throws ConfigException{
		Configurable c = configurableComponents.get(tag);
		c.config.configure(tag,xrp);
		try{
			c.clazz.getMethod("configure",Config.class).invoke(null,c.config);
		}catch(Exception ex){
			throw new ConfigException("failed to configure: "+tag,ex);
		}
	}
	
	private static final void configurableCheck(Class<?> clazz,String name){
		try{
			if(!clazz.getMethod("getTag").getGenericReturnType().equals(String.class))return;
			if(!clazz.getMethod("getConfig").getGenericReturnType().equals(Config.class))return;
			clazz.getMethod("configure",Config.class);
			String tag = (String)clazz.getMethod("getTag").invoke(null);
			configurableComponents.put(tag,new Configurable(clazz,(Config)clazz.getMethod("getConfig").invoke(null)));
			Log.d("Component","loading configurable "+name);
		}catch(Exception ex){}
	}
	
	private static final class Configurable{
		Class<?> clazz;
		Config config;
		Configurable(Class<?> clazz, Config config){
			this.clazz = clazz;
			this.config = config;
		}
	}
}
