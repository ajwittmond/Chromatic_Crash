package com.flickshot.util;

import java.nio.Buffer;

public class ObjectBuffer<T extends Bufferable>{
	
	public ObjectBuffer(){
		
	}
	
	public Object array() {
		// TODO Auto-generated method stub
		return null;
	}

	public int arrayOffset() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public int capacity(){
		return 0;
	}
	
	public ObjectBuffer clear(){
		return this;
	}
	
	public ObjectBuffer flip(){
		return this;
	}
	
	public int limit(){
		return 0;
	}
	
	public ObjectBuffer limit(int limit){
		return this;
	}
	
	public ObjectBuffer mark(){
		return this;
	}
	
	public ObjectBuffer position(int position){
		return this;
	}
	
	public int position(){
		return 0;
	}
	
	public int remaining(){
		return 0;
	}
	
	public ObjectBuffer reset(){
		return this;
	}
	
	public ObjectBuffer rewind(){
		return this;
	}
	
	public boolean hasArray() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isDirect() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isReadOnly() {
		// TODO Auto-generated method stub
		return false;
	}

}
