package com.flickshot.util.reflection;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

import com.flickshot.assets.AssetManager;

import dalvik.system.DexFile;

public class RUtil {
	private static final char ARRAY_START = '[';
	private static final char ARRAY_END = ']';
    private static final char ARGUMENT_DELIMITER = ',';
    private static final char ARGUMENT_START = '(';
    private static final char ARGUMENT_END = ')';
    
    private static final String NAME_PATTERN = "([a-zA-Z_$][a-zA-Z0-9_$\\.]*)";
    private static final String BOOLEAN_PATTERN = "([tT][rR][uU][eE])|([fF][aA][lL][sS][eE])"; 
    private static final String INTEGER_PATTERN = "(\\d+)|(0x[0-9a-fA-F]*)|(0b[01]*)";
    private static final String LONG_PATTERN = "((\\d+)|(0x[0-9a-fA-F]*)|(0b[01]*))[lL]";
    private static final String DOUBLE_PATTERN = "(\\d+\\.\\d*([eE]\\d+)?)[dD]?";
    private static final String FLOAT_PATTERN = "(\\d+\\.\\d*([eE]\\d+)?)[fF]";
    private static final String ENUM_PATTERN = NAME_PATTERN+"\\.([a-zA-Z_$][a-zA-Z0-9_$]*)";
    private static final String CLASS_PATTERN = "new "+NAME_PATTERN+"\\((.*)\\)";
    private static final String ARRAY_PATTERN = "new "+NAME_PATTERN+"\\[(.*)\\]";
    
    private static final Map<String,Class> primitiveTypes = new HashMap<String,Class>();
    
    static{
    	primitiveTypes.put("byte", Byte.TYPE);
    	primitiveTypes.put("Byte", Byte.TYPE);
    	primitiveTypes.put("short", Short.TYPE);
    	primitiveTypes.put("Short", Short.TYPE);
    	primitiveTypes.put("int", Integer.TYPE);
    	primitiveTypes.put("Integer", Integer.TYPE);
    	primitiveTypes.put("long", Long.TYPE);
    	primitiveTypes.put("Long", Long.TYPE);
    	primitiveTypes.put("float", Float.TYPE);
    	primitiveTypes.put("Float", Float.TYPE);
    	primitiveTypes.put("double", Double.TYPE);
    	primitiveTypes.put("Double", Double.TYPE);
    	primitiveTypes.put("boolean", Boolean.TYPE);
    	primitiveTypes.put("Boolean", Boolean.TYPE);
    	primitiveTypes.put("char", Character.TYPE);
    	primitiveTypes.put("Character", Character.TYPE);
    }
    		
    
    private RUtil(){}
    
    public static final Object parseValue(String value) throws ValueFormatException{
        return parseValue(value,getClass(value));
    }
    
    public static final Object parseValue(String value,Class<?> type) throws ValueFormatException{
        value  = value.trim();
        if(type.equals(String.class)){
            return value;
        }else if(type.isPrimitive()){
            return loadPrimitive(value,type);
        }else if(type.isArray()){
            return loadArray(value,type);
        }else if(type.isEnum()){
            return loadEnum(value,type);
        }else{
            return loadObject(value,type);
        }
    }

    public static final Object loadPrimitive(String value,Class<?> type) throws ValueFormatException{
        try{
            if(type.equals(Boolean.TYPE)){
                return Boolean.parseBoolean(value);
            }else if(type.equals(Byte.TYPE)){
                return Byte.parseByte(value);
            }else if(type.equals(Short.TYPE)){
                return Short.parseShort(value);
            }else if(type.equals(Integer.TYPE)){
                return Integer.parseInt(value);
            }else if(type.equals(Long.TYPE)){
                return Long.parseLong(value);
            }else if(type.equals(Float.TYPE)){
                return Float.parseFloat(value);
            }else if(type.equals(Double.TYPE)){
                return Double.parseDouble(value);
            }else if(type.equals(Character.TYPE)){
                return value.charAt(0);
            }else{
                throw new ValueFormatException("cannot find primitive type for: "+value);
            }
        }catch(Exception ex){
            throw new ValueFormatException("cannot parse primitive from: "+value,ex);
        }
    }
    
    public static final Object loadEnum(String value, Class<?> type) throws ValueFormatException{
        for(Object constant:type.getEnumConstants()){
            if(constant.toString().equalsIgnoreCase(value)){
                return constant;
            }
        }
        throw new ValueFormatException("failed to parse enum constant from: "+value);
    }

    public static final Object loadArray(String value,Class<?> type) throws ValueFormatException{
        try{
            Class<?> componentType = type.getComponentType();
            String[] values = getItems(value,ARRAY_START,ARRAY_END);
            Object array  = Array.newInstance(componentType,  values.length);
            for(int i = 0; i< values.length; i++){
                Array.set(array,i,parseValue(values[i],componentType));
            }
            return array;
        }catch(Exception ex){
            throw new ValueFormatException("failed to parse array from: "+value,ex);
        }
    }

    public static final Object loadObject(String value,Class<?> type) throws ValueFormatException{
        String[] args = getItems(value,ARGUMENT_START,ARGUMENT_END);
        Object[] arguments = new Object[args.length];
        Constructor[] constructors = type.getConstructors();
        for(Constructor c: constructors){
            Class[] types = c.getParameterTypes();
            if(types.length == args.length){
                try{
                    for(int i = 0; i<args.length; i++){
                        arguments[i] = parseValue(args[i],types[i]);
                    }
                    return c.newInstance(arguments);
                }catch(Exception ex){}
            }
        }
        throw new ValueFormatException("failed to parse object from: "+value);
    }
    
    private static final String[] getItems(String value,char start,char end){
    	int starti,endi;
        for(starti = 1; starti<(value.length()-1) && value.charAt(starti-1)!=start; starti++);
        for(endi = value.length()-1; endi>1 && value.charAt(endi)!=end; endi--);
        value = value.substring(starti,endi);
        
        final ArrayList<String> args = new ArrayList<String>();
        StringBuilder curr = new StringBuilder("");
        int c = 0;
        for(int i = 0;  i<value.length(); i++){
            char next = value.charAt(i);
            if(c<1 && next==ARGUMENT_DELIMITER){
                args.add(curr.toString());
                curr = new StringBuilder("");
                continue;
            }
            if(next==ARGUMENT_END || next==ARRAY_END){
                c--;
            }
            if(next==ARGUMENT_START || next==ARRAY_START){
                c++;
            }
            curr.append(next);
        }
        args.add(curr.toString());
        
        if(args.size()==1 && args.get(0).equals(""))args.clear();
        return args.toArray(new String[args.size()]);
    }
    
    public static class ValueFormatException extends Exception{
        ValueFormatException(String m){
            super(m);
        }
        
        ValueFormatException(Exception ex){
            super(ex);
        }
        
        ValueFormatException(String m,Exception ex){
            super(m,ex);
        }
    }
    
    public static final Class<?> getClass(String value) throws ValueFormatException{
        if(value.matches(BOOLEAN_PATTERN)){
            return Boolean.TYPE;
        }else if(value.matches(LONG_PATTERN)){
            return Long.TYPE;
        }else if(value.matches(INTEGER_PATTERN)){
            return Integer.TYPE;
        }else if(value.matches(FLOAT_PATTERN)){
            return Float.TYPE;
        }else if(value.matches(DOUBLE_PATTERN)){
            return Double.TYPE;
        }else if(value.matches(CLASS_PATTERN)){
            Pattern classPattern = Pattern.compile(CLASS_PATTERN);
            Matcher matcher = classPattern.matcher(value);
            String name = matcher.group(0);
            try {
                return Class.forName(name);
            } catch (ClassNotFoundException ex) {
                throw new ValueFormatException("failed to find class "+name+" for value "+value,ex);
            }
        }else if(value.matches(ENUM_PATTERN)){
            Pattern classPattern = Pattern.compile(ENUM_PATTERN);
            Matcher matcher = classPattern.matcher(value);
            String name = matcher.group(0);
            if(primitiveTypes.containsKey(name)){
            	return primitiveTypes.get(name);
            }else{
	            try {
	                return Class.forName(name);
	            } catch (ClassNotFoundException ex) {
	                throw new ValueFormatException("failed to find class "+name+" for value "+value,ex);
	            }
            }
        }else if(value.matches(ARRAY_PATTERN)){
        	Pattern classPattern = Pattern.compile(ENUM_PATTERN);
            Matcher matcher = classPattern.matcher(value);
            String name = matcher.group(0);
            
            Class<?> componentType;
            if(primitiveTypes.containsKey(name)){
            	componentType = primitiveTypes.get(name);
            }else{
	            try {
	                componentType = Class.forName(name);
	            } catch (ClassNotFoundException ex) {
	                throw new ValueFormatException("failed to find class "+name+" for value "+value,ex);
	            }
            }
            return Array.newInstance(componentType, 0).getClass();
        }else if(value.length()==1){
            return Character.TYPE;
        }
        return String.class;
    }
}
