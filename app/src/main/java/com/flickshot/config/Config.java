package com.flickshot.config;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;

import android.util.Log;

import com.flickshot.util.reflection.RUtil;
import com.flickshot.util.reflection.RUtil.ValueFormatException;

public abstract class Config {
	public static final String VALUE_ATTRIBUTE = "value";
	protected Config parent;
	protected String tag = "<undefined>";
	
	
	public final Config getParent(){
		return parent;
	}
	
	public final Config getRoot(){
		if(parent == null){
			return this;
		}else if(parent.parent==null){
			return parent;
		}else{
			return parent.getRoot();
		}
	}
	
	public void findAndConfigure(String tag,XmlPullParser xrp) throws ConfigException{
		try{
			int event = xrp.getEventType();
			while(!(event==XmlPullParser.START_TAG && tag.equals(xrp.getName()))){
				if(event==XmlPullParser.END_DOCUMENT) throw new ConfigException("could not find tag: "+tag);
				event = xrp.next();
			}
		}catch(Exception ex){
			throw new ConfigException("Error looking for tag: "+tag+" at line: "+xrp.getLineNumber());
		}
		configure(null,tag,xrp);
	}
	
	public void configure(Config parent,String tag,XmlPullParser xrp) throws ConfigException{
		this.parent = parent;
		this.tag = tag;
		final HashMap<String,String> aliases = new HashMap<String,String>();
		getAliases(aliases);
		
		int line = xrp.getLineNumber();
		try{
			final HashMap<String,ConfigEntry> entries = new HashMap<String,ConfigEntry>();
			for(Field f: getClass().getFields()){
				if(!java.lang.reflect.Modifier.isStatic(f.getModifiers())){
					entries.put(f.getName(),new ConfigEntry(f));
				}
			}
			
			int event = xrp.getEventType();
			if(!(event == XmlPullParser.START_TAG || event==XmlPullParser.END_TAG) || !xrp.getName().equals(tag)) throw new ConfigException(this,"illegal starting conditions");
			
			String text = null;
			
			for(int i = xrp.getAttributeCount()-1; i>=0; i--){
				String name = xrp.getAttributeName(i);
				if(aliases.containsKey(name))name = aliases.get(name);
				if(name.equalsIgnoreCase(VALUE_ATTRIBUTE)){
					text = xrp.getAttributeValue(i);
				}else{
					ConfigEntry e = entries.get(name);
					if(e==null)throw new ConfigException("Config field is undefined for attribute: "+name);
					e.add(xrp.getAttributeValue(i));
				}
			}
			
			event = xrp.next();
			if(event==XmlPullParser.TEXT && text==null) text = xrp.getText().trim();
			
			setValue(text);
			
			line = xrp.getLineNumber();
			while(!(event==XmlPullParser.END_TAG && xrp.getName().equals(tag))){
				if(event==XmlPullParser.START_TAG ){
					String name = xrp.getName();
					String tg = name;
					if(aliases.containsKey(name))name = aliases.get(name);
					ConfigEntry e = entries.get(name);
					if(e==null)throw new ConfigException("Config field is undefined for tag: "+name);
					/**
					 * Handle primitives
					 */
					if(e.type.isPrimitive() || e.type.isEnum() || e.type.equals(String.class)){
						/**
						 * Self closing: value is attribute
						 */
						if(xrp.getAttributeCount()==1 && xrp.getAttributeName(0).equalsIgnoreCase(VALUE_ATTRIBUTE)){
							e.add(xrp.getAttributeValue(0));
						}
						/**
						 * value is in text
						 */
						else {
							event = xrp.next();
							String value = xrp.getText().trim();
							e.add(value);
						}
						event = xrp.next();
						while(!(event==XmlPullParser.END_TAG && xrp.getName().equals(name))){
							if(event==XmlPullParser.START_TAG || 
									(event==XmlPullParser.END_TAG && !xrp.getName().equals(name))){
								throw new ConfigException("illegal value inside primitive type: "+name);
							}
							event = xrp.next();
						}
					}
					/**
					 * type is configurable object
					 */
					else{
						Config c = e.type.asSubclass(Config.class).newInstance();
						c.configure(this,tg,xrp);
						e.add(c);
					}
				}
				event = xrp.next();
				line = xrp.getLineNumber();
			}
			
			//set all fields
			for(String id: entries.keySet()){
				entries.get(id).set(this);
			}
		}catch(Exception ex){
			throw new ConfigException(this,"error parsing file at line "+line,ex);
		}
	}
	
	public void configure(String tag,XmlPullParser xrp) throws ConfigException{
		configure(null,tag,xrp);
	}
	
	/**
	 * Sets the value of this configurable using the text content
	 * @param text
	 */
	public abstract void setValue(String text);
	
	/**
	 * Sets alternative names for fields.  The key is the alias and the value
	 * is the field name.
	 * @param map
	 */
	public abstract void getAliases(HashMap<String,String> map);
	
	public static class ConfigException extends Exception{

		/**
		 * 
		 */
		private static final long serialVersionUID = 2973939451364450561L;
		
		public ConfigException(Config conf,String message,Throwable cause){
			super(generateMessage(conf,message),cause);
		}
		
		public ConfigException(Config conf,String message){
			super(generateMessage(conf,message));
		}
		
		public ConfigException(String message){
			super(message);
		}
		
		public ConfigException(String message,Throwable cause){
			super(message,cause);
		}
		
		public ConfigException(Throwable cause){
			super(cause);
		}
		
		private static final String generateMessage(Config conf,String message){
			Config parent = conf.getRoot();
			return String.format("Failed to configure configurable %s with root %s: \n%s",conf.tag,(parent==null)?"null":parent.tag,message);
		}
		
	}
	
	private static final class ConfigEntry{
		final Field field;
		final boolean isArray;
		final Class<?> type;
		private final ArrayList<Object> values = new ArrayList<Object>();
		
		public ConfigEntry(Field f) throws ConfigException{
			field = f;
			Class t = f.getType();
			isArray = t.isArray();
			if(isArray){
				type = t.getComponentType();
				if(type.isArray())throw new ConfigException("multidimensional arrays are not supported: "+field.getName());
			}else{
				type = t;
			}
			if(!(type.isPrimitive() || type.isEnum() || type.equals(String.class) || Config.class.isAssignableFrom(type))) throw new ConfigException("field is not primitive or configurable: "+field.getName());
		}
		
		void add(Object o) throws ConfigException,ValueFormatException{
			if(type.isPrimitive()){
				o = RUtil.loadPrimitive((String)o,type);
			}else if(type.isEnum()){
				o = RUtil.loadEnum((String)o,type);
			}
			values.add(o);
			if(!isArray && values.size()>1)throw new ConfigException("multiple values for non array type: "+field.getName());
		}
		
		void set(Config c) throws IllegalAccessException, IllegalArgumentException{
			if(values.size()==0){
				Log.w("config","field not defined: "+field.getName());
				if(!isArray)return;
			}
			
			Object value;
			if(isArray){
				value = Array.newInstance(type,values.size());
				for(int i = 0; i<values.size(); i++){
					Array.set(value,i,values.get(i));
				}
			}else {
				if(values.size()==1){
					value = values.get(0);
				}else{
					throw new IllegalStateException();
				}
			}
			field.set(c,value);
		}
	}
	
	@Override
	public String toString(){
		return toString(0);
	}
	
	public String toString(int tabs){
		Field[] fields = getClass().getFields();
		String t = "";
		for(int i = 0; i<tabs; i++) t+="    ";
		String s = t+String.format("<%s>%n",tag);
		for(Field f: fields){
			if(!java.lang.reflect.Modifier.isStatic(f.getModifiers())){
				try{
					Class<?> type = f.getType();
					if(type.isArray()){
						type = type.getComponentType();
						Object o = f.get(this);
						int i = 0;
						while(true){
							try{
								s+=getFieldString(tabs+1,Array.get(o,i++),f.getName(),type);
							}catch(Exception ex){
								break;
							}
						}
					}else{
						s+=getFieldString(tabs+1,f.get(this),f.getName(),type);
					}
				}catch(IllegalAccessException ex){}
			}
		}
		s+=t+String.format("<%s/>%n",tag);
		return s;
	}
	
	private String getFieldString(int tabs,Object o,String name,Class<?> type){
		if(type.isPrimitive()||type.isEnum()||type.equals(String.class)){
			String t = "";
			for(int i = 0; i<tabs; i++) t+="    ";
			return String.format("%s<%s value=\"%s\"/>%n",t,name,o.toString());
		}else if(Config.class.isAssignableFrom(type)){
			Config c = (Config)o;
			return c.toString(tabs);
		}else{
			throw new IllegalStateException();
		}
	}
}
