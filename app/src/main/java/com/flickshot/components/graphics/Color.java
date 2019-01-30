package com.flickshot.components.graphics;

import java.nio.ByteBuffer;

public class Color {
	private ByteBuffer value = ByteBuffer.allocate(4);
	
	public Color(){
		value.putInt(0x000000ff);
	}
	
	public Color(byte r,byte g,byte b,byte a){
		value.put(0,r).put(0,g).put(0,b).put(0,a);
	}
	
	
	
}
