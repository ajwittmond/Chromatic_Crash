package com.flickshot.util;

import java.nio.ByteBuffer;

public class MiscLib {
	public static final double DEG_TO_RAD = Math.PI/180;
	public static final double RAD_TO_DEG = 180/Math.PI;
	
	public static final double sqr(double a){
		return a*a;
	}
	
	public static final float sqr(float a){
		return a*a;
	}
	
	public static final int sqr(int a){
		return a*a;
	}
	
	public static final long sqr(long a){
		return a*a;
	}
	
	public static final double bound(double a, double min, double max){
		return Math.min(max,Math.max(min,a));
	}
	
	public static final float bound(float a,float min, float max){
		return Math.min(max,Math.max(min,a));
	}
	
	public static final int bound(int a, int min, int max){
		return Math.min(max,Math.max(min,a));
	}
	
	public static final long bound(long a, long min, long max){
		return Math.min(max,Math.max(min,a));
	}
	
	private static final ByteBuffer buffer = ByteBuffer.allocate(8);
	
	public static final short getShort(byte a, byte b){
		buffer.put(0,a).put(1,b);
		return buffer.getShort(0);
	}
	
	public static final int getInt(byte a, byte b, byte c, byte d){
		buffer.put(0,a).put(1,b).put(2,c).put(3,d);
		return buffer.getInt(0);
	}
	
	public static final float getFloat(byte a, byte b, byte c, byte d){
		buffer.put(0,a).put(1,b).put(2,c).put(3,d);
		return buffer.getFloat(0);
	}
	
	public static final long getLong(byte a, byte b, byte c, byte d,byte e, byte f, byte g, byte h){
		buffer.put(0,a).put(1,b).put(2,c).put(3,d).put(4,e).put(5,f).put(6,g).put(7,h);
		return buffer.getLong(0);
	}
	
	public static final double getDouble(byte a, byte b, byte c, byte d,byte e, byte f, byte g, byte h){
		buffer.put(0,a).put(1,b).put(2,c).put(3,d).put(4,e).put(5,f).put(6,g).put(7,h);
		return buffer.getDouble(0);
	}
}
