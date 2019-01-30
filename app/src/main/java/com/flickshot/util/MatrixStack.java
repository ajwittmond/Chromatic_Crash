package com.flickshot.util;

import java.util.Stack;

import android.util.Log;

public class MatrixStack {
	
	private float[] stack = new float[8*16];
	private int position = 0;
	private float[] current = new float[16];
	
	public MatrixStack(){	 
	}
	
	public void push(){
		if(position*16>=stack.length) resize();
		
		int start = position*16;
		for(int i = 0; i<16; i++){
			stack[i+start] = current[i];
		}
		
		position++;
	}
	
	public final void pop(){
		if(position<=0){
			throw new IllegalStateException("MatrixStack: to many calls to pop or to few calls to push");
		}
		
		int start = 16*(position-1);
		for(int i = 0; i<16; i++){
			current[i] = stack[start+i];
		}
		
		position--;
	}
	
	public final float[] current(){
		return current;
	}
	
	public final float[] current(float[] f, int offset){
		if(f.length-offset<16)throw new ArrayIndexOutOfBoundsException("MatrixStack.current: array passed is not big enough");
		System.arraycopy(current,0,f,offset,16);
		return f;
	}
	
	public final float[] current(float[] f){
		return current(f,0);
	}
	
	public final int size(){
		return position;
	}
	
	public final int capacity(){
		return stack.length/16;
	}
	
	public final void clear(){
		position = 0;
	}
	
	private final void resize(){
		int size = stack.length/16;
		int newSize = size*2;
		float[] temp = stack;
		stack = new float[newSize*16];
		System.arraycopy(temp,0,stack,0,temp.length);
		Log.e("MatrixStack", String.format("stack overflow: resizing from a capacity of %d to one of %d",size,newSize));
	}
}
