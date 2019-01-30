package com.flickshot.components.graphics.program;

import java.nio.Buffer;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.*;

public class GLAttribute {
	public final String program;
	public final int programHandle;
	public final String name;
	public final String type;
	public final int handle;
	
	public boolean arrayEnabled = false;
	
	GLAttribute(String program,String declaration, int programHandle){
		String[] s = declaration.split(" ");
		if(s.length!=2)throw new IllegalStateException();
		this.program = program;
		this.programHandle = programHandle;
		name = s[1];
		type = s[0];
		handle = glGetAttribLocation(programHandle,name);
	}
	
	public String toString(){
		return program+": attribute "+type+" "+name;
	}
	
	public GLAttribute enableArray(){
		glEnableVertexAttribArray(handle);
		arrayEnabled = true;
		return this;
	}
	
	public GLAttribute disableArray(){
		glDisableVertexAttribArray(handle);
		arrayEnabled = false;
		return this;
	}
	
	public void setAttributePointer(int size,int type, boolean normalized, int stride,Buffer b){
		if(!arrayEnabled) enableArray();
		glVertexAttribPointer(handle,size,GL_FLOAT,normalized,stride,b);
	}
	
	public void set1(float f){
		if(arrayEnabled) disableArray();
		glVertexAttrib1f(handle,f);
	}
	
	public void set1(FloatBuffer fb){
		if(arrayEnabled) disableArray();
		glVertexAttrib1fv(handle,fb);
	}
	
	public void set1(float[] values, int offset){
		if(arrayEnabled) disableArray();
		glVertexAttrib1fv(handle,values,offset);
	}
	
	public void set2(float x, float y){
		if(arrayEnabled) disableArray();
		glVertexAttrib2f(handle,x,y);
	}
	
	public void set2(FloatBuffer fb){
		if(arrayEnabled) disableArray();
		glVertexAttrib2fv(handle,fb);
	}
	
	public void set2(float[] values, int offset){
		if(arrayEnabled) disableArray();
		glVertexAttrib2fv(handle,values,offset);
	}
	
	public void set3(float x, float y, float z){
		if(arrayEnabled) disableArray();
		glVertexAttrib3f(handle,x,y,z);
	}
	
	public void set3(FloatBuffer fb){
		if(arrayEnabled) disableArray();
		glVertexAttrib3fv(handle,fb);
	}
	
	public void set3(float[] values, int offset){
		if(arrayEnabled) disableArray();
		glVertexAttrib3fv(handle,values,offset);
	}
	
	public void set4(float x, float y, float z, float w){
		if(arrayEnabled) disableArray();
		glVertexAttrib4f(handle,x,y,z,w);
	}
	
	public void set4(FloatBuffer fb){
		if(arrayEnabled) disableArray();
		glVertexAttrib4fv(handle,fb);
	}
	
	public void set4(float[] values, int offset){
		if(arrayEnabled) disableArray();
		glVertexAttrib4fv(handle,values,offset);
	}
}
