package com.flickshot.components.graphics.program;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

import static android.opengl.GLES20.*;

public class GLProgram {
	private static final String varGroup = "[_a-zA-Z0-9]";
	private static final Pattern uniformPattern = Pattern.compile("uniform\\s+("+varGroup+"+\\s+"+varGroup+"+);");
	private static final Pattern attributePattern = Pattern.compile("attribute\\s+("+varGroup+"+\\s+"+varGroup+"+);");
	
	private final HashMap<String,GLUniform> uniforms = new HashMap<String,GLUniform>();
	private final HashMap<String,GLAttribute> attributes = new HashMap<String,GLAttribute>();
	private final GLAttribute[] attributesArray;
	
	public final String name;
	public final int handle;
	public final int vertexShader;
	public final int fragmentShader;
	
	private boolean destroyed = false;
	
	public GLProgram(String name,InputStream vertexShaderStream, InputStream fragmentShaderStream) throws IOException{
		this.name = name;
		handle = glCreateProgram();
		
		HashSet<String> uniformDeclarations = new HashSet<String>();
		HashSet<String> attributeDeclarations = new HashSet<String>();
		
		vertexShader = loadShader(vertexShaderStream,GL_VERTEX_SHADER,uniformDeclarations,attributeDeclarations);
		fragmentShader = loadShader(fragmentShaderStream,GL_FRAGMENT_SHADER,uniformDeclarations,attributeDeclarations);
		
		glAttachShader(handle,vertexShader);
		glAttachShader(handle,fragmentShader);
		glLinkProgram(handle);
		glUseProgram(handle);
		
		attributesArray = new GLAttribute[attributeDeclarations.size()];
		
		int i = 0;
		for(String u:uniformDeclarations){
			GLUniform unif = new GLUniform(name,u,handle);
			uniforms.put(unif.name,unif);
			Log.d("GLProgram",""+unif);
		}
		i = 0;
		for(String a:attributeDeclarations){
			GLAttribute attr = new GLAttribute(name,a,handle);
			attributesArray[i++] = attr;
			attributes.put(attr.name,attr);
			Log.d("GLProgram",""+attr);
		}
	}
	
	private static int loadShader(InputStream stream,int type,HashSet<String> uniformDeclarations,HashSet<String> attributeDeclarations) throws IOException{
		int shaderHandle = glCreateShader(type);
		byte[] buffer = new byte[stream.available()];
		stream.read(buffer);
		String shaderString = new String(buffer);
		
		Matcher uniformMatcher = uniformPattern.matcher(shaderString);
		Matcher attributeMatcher = attributePattern.matcher(shaderString);
		while(uniformMatcher.find()){
			uniformDeclarations.add(uniformMatcher.group(1));
		}
		while(attributeMatcher.find()){
			attributeDeclarations.add(attributeMatcher.group(1));
		}
		
		glShaderSource(shaderHandle,shaderString);
		glCompileShader(shaderHandle);
		
		int[] params = new int[1];
		glGetShaderiv(shaderHandle, GL_COMPILE_STATUS,params,0);
		if(params[0]==GL_FALSE){
			throw new Error("Shader compilation failed:"+glGetShaderInfoLog(shaderHandle));
		}
		
		return shaderHandle;
	}
	
	public void useEnableAll(){
		if(destroyed) throw new IllegalStateException("has been destroyed");
		glUseProgram(handle);
		for(int i = 0; i<attributesArray.length; i++){
			attributesArray[i].enableArray();
		}
	}
	
	public void use(){
		if(destroyed) throw new IllegalStateException("has been destroyed");
		glUseProgram(handle);
	}
	
	public void disableAll(){
		if(destroyed) throw new IllegalStateException("has been destroyed");
		for(int i = 0; i<attributesArray.length; i++){
			attributesArray[i].disableArray();
		}
	}	

	public GLUniform getUniform(String name){
		return uniforms.get(name);
	}
	
	public GLAttribute getAttribute(String name){
		return attributes.get(name);
	}
	
	public void destroy(){
		glDeleteProgram(handle);
		glDeleteShader(vertexShader);
		glDeleteProgram(fragmentShader);
		for(int i = 0; i<attributesArray.length; i++){
			attributesArray[i].disableArray();
			attributesArray[i]=null;
		}
		uniforms.clear();
		attributes.clear();
	}
}
