package com.flickshot.components.graphics.program;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static android.opengl.GLES20.*;

public class GLUniform {
	public final String program;
	public final int programHandle;
	public final String name;
	public final String type;
	public final int handle;
	
	GLUniform(String program,String declaration, int programHandle){
		String[] s = declaration.split(" ");
		if(s.length!=2)throw new IllegalStateException(declaration);
		this.program = program;
		this.programHandle = programHandle;
		name = s[1];
		type = s[0];
		this.handle = glGetUniformLocation(programHandle,name);
	}
	
	public String toString(){
		return program+": uniform "+type+" "+name;
	}
	
	public void set1(float x){
		glUniform1f(handle,x);
	}
	
	public void set1(int count,FloatBuffer fb){
		glUniform1fv(handle,count,fb);
	}
	
	public void set1(int count, float[] values, int offset){
		glUniform1fv(handle,count,values,offset);
	}
	
	public void set2(float x, float y){
		glUniform2f(handle,x,y);
	}
	
	public void set2(int count,FloatBuffer fb){
		glUniform2fv(handle,count,fb);
	}
	
	public void set2(int count, float[] values, int offset){
		glUniform2fv(handle,count,values,offset);
	}
	
	public void set3(float x, float y, float z){
		glUniform3f(handle,x,y,z);
	}
	
	public void set3(int count,FloatBuffer fb){
		glUniform3fv(handle,count,fb);
	}
	
	public void set3(int count, float[] values, int offset){
		glUniform3fv(handle,count,values,offset);
	}
	
	public void set4(float x, float y, float z, float w){
		glUniform4f(handle,x,y,z,w);
	}
	
	public void set4(int count,FloatBuffer fb){
		glUniform4fv(handle,count,fb);
	}
	
	public void set4(int count, float[] values, int offset){
		glUniform4fv(handle,count,values,offset);
	}
	
	public void set1(int x){
		glUniform1i(handle,x);
	}
	
	public void set1(int count,IntBuffer fb){
		glUniform1iv(handle,count,fb);
	}
	
	public void set1(int count, int[] values, int offset){
		glUniform1iv(handle,count,values,offset);
	}
	
	public void set2(int x, int y){
		glUniform2i(handle,x,y);
	}
	
	public void set2(int count,IntBuffer fb){
		glUniform2iv(handle,count,fb);
	}
	
	public void set2(int count, int[] values, int offset){
		glUniform2iv(handle,count,values,offset);
	}
	
	public void set3(int x, int y, int z){
		glUniform3i(handle,x,y,z);
	}
	
	public void set3(int count,IntBuffer fb){
		glUniform3iv(handle,count,fb);
	}
	
	public void set3(int count, int[] values, int offset){
		glUniform3iv(handle,count,values,offset);
	}
	
	public void set4(int x, int y, int z, int w){
		glUniform4i(handle,x,y,z,w);
	}
	
	public void set4(int count,IntBuffer fb){
		glUniform4iv(handle,count,fb);
	}
	
	public void set4(int count,int[] values, int offset){
		glUniform4iv(handle,count,values,offset);
	}
	
	public void setMatrix2(int count, boolean transpose, FloatBuffer fb){
		glUniformMatrix2fv(handle, count, transpose,fb);
	}
	
	public void setMatrix2(int count, boolean transpose, float[] values, int offset){
		glUniformMatrix2fv(handle, count, transpose,values, offset);
	}
	
	public void setMatrix3(int count, boolean transpose, FloatBuffer fb){
		glUniformMatrix3fv(handle, count, transpose,fb);
	}
	
	public void setMatrix3(int count, boolean transpose, float[] values, int offset){
		glUniformMatrix3fv(handle, count, transpose,values, offset);
	}
	
	public void setMatrix4(int count, boolean transpose, FloatBuffer fb){
		glUniformMatrix4fv(handle, count, transpose,fb);
	}
	
	public void setMatrix4(int count, boolean transpose, float[] values, int offset){
		glUniformMatrix4fv(handle, count, transpose,values, offset);
	}
}
