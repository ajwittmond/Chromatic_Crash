package com.flickshot.components.graphics;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

class Primitives2d {
	static final FloatBuffer B_LINE;
	static final FloatBuffer B_TRIANGLE;
	
	static final FloatBuffer B_SQUARE_MIN;
	static final FloatBuffer B_CIRCLE_MIN;
	static final FloatBuffer B_CIRCLE_FAN_MIN;
	
	static final FloatBuffer B_SQUARE_TRIANGLES;
	static final FloatBuffer B_CIRCLE_TRIANGLES;
	static final FloatBuffer B_CIRCLE_FAN_TRIANGLES;
	
	static final FloatBuffer B_SQUARE_LINES;
	static final FloatBuffer B_TRIANGLE_LINES;
	static final FloatBuffer B_CIRCLE_LINES;
	static final FloatBuffer B_CIRCLE_FAN_LINES;
	
	static final float[] A_LINE;
	static final float[] A_TRIANGLE;
	
	static final float[] A_SQUARE_MIN;
	static final float[] A_CIRCLE_MIN;
	static final float[] A_CIRCLE_FAN_MIN;
	
	static final float[] A_SQUARE_TRIANGLES;
	static final float[] A_CIRCLE_TRIANGLES;
	static final float[] A_CIRCLE_FAN_TRIANGLES;
	
	static final float[] A_SQUARE_LINES;
	static final float[] A_TRIANGLE_LINES;
	static final float[] A_CIRCLE_LINES;
	static final float[] A_CIRCLE_FAN_LINES;
	
	static final int CIRCLE_VERTS = 32;
	
	static{
		final int square_verts = 4;
		final int triangle_verts = 3;
		final int line_verts = 2;
		
		final int vert_size = 8;//2 4 byte floats
		final int triangle_size = vert_size*triangle_verts;
		final int line_size = vert_size*line_verts;
		
		final int box_min_size = vert_size*square_verts;
		final int circle_min_size = vert_size*CIRCLE_VERTS;
		final int circle_fan_min_size = circle_min_size;
		
		final int box_triangles_size = triangle_size*(square_verts-2);
		final int circle_triangles_size = triangle_size*(CIRCLE_VERTS-2);
		final int circle_fan_triangles_size = triangle_size*(CIRCLE_VERTS-1);
		
		final int box_lines_size = line_size*square_verts;
		final int triangle_line_size = line_size*triangle_verts;
		final int circle_lines_size = line_size*CIRCLE_VERTS;
		final int circle_fan_lines_size = line_size*CIRCLE_VERTS;
		
		final int buffer_size = triangle_size+line_size+box_min_size+circle_min_size+
				circle_fan_min_size+box_triangles_size+circle_triangles_size+
				circle_fan_triangles_size+box_lines_size+triangle_line_size+circle_lines_size+
				circle_fan_lines_size;
		
		final FloatBuffer buffer = ByteBuffer.allocateDirect(buffer_size)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer();
		buffer.rewind();
		
		A_LINE = new float[]{
				0.0f,	0.0f,
				1.0f,	1.0f
		};
		A_TRIANGLE = new float[]{
				0.0f,	0.5f,
				-0.5f,	-0.5f,
				0.5f,	-0.5f
		};
		A_SQUARE_MIN = new float[]{
				0.5f, 	0.5f,
				-0.5f, 	0.5f,
				-0.5f, 	-0.5f,
				0.5f, 	-0.5f
		};
		A_CIRCLE_MIN = fillCircle(new float[CIRCLE_VERTS*2],0.5f,0,CIRCLE_VERTS);
		A_CIRCLE_FAN_MIN = fillCircleFan(new float[CIRCLE_VERTS*2],0.5f,0,CIRCLE_VERTS);

		B_LINE = getPrimitiveBuffer(buffer, A_LINE);
		B_TRIANGLE = getPrimitiveBuffer(buffer, A_TRIANGLE);
		
		B_SQUARE_MIN = getPrimitiveBuffer(buffer, A_SQUARE_MIN);
		B_CIRCLE_MIN = getPrimitiveBuffer(buffer, A_CIRCLE_MIN);
		B_CIRCLE_FAN_MIN = getPrimitiveBuffer(buffer, A_CIRCLE_FAN_MIN);
		
		B_SQUARE_TRIANGLES = getPrimitiveBuffer(buffer, (A_SQUARE_TRIANGLES=getTriangles(A_SQUARE_MIN)));
		B_CIRCLE_TRIANGLES = getPrimitiveBuffer(buffer, (A_CIRCLE_TRIANGLES=getTriangles(A_CIRCLE_MIN)));
		B_CIRCLE_FAN_TRIANGLES = getPrimitiveBuffer(buffer, (A_CIRCLE_FAN_TRIANGLES=getTriangles(A_CIRCLE_FAN_MIN)));
		
		B_TRIANGLE_LINES = getPrimitiveBuffer(buffer, (A_TRIANGLE_LINES=getLines(A_TRIANGLE)));
		B_SQUARE_LINES = getPrimitiveBuffer(buffer, (A_SQUARE_LINES=getLines(A_SQUARE_MIN)));
		B_CIRCLE_LINES = getPrimitiveBuffer(buffer, (A_CIRCLE_LINES=getLines(A_CIRCLE_MIN)));
		B_CIRCLE_FAN_LINES = getPrimitiveBuffer(buffer, (A_CIRCLE_FAN_LINES=getLines(A_CIRCLE_FAN_MIN)));
	}
	
	private static FloatBuffer getPrimitiveBuffer(FloatBuffer buffer,float[] values){
		buffer.mark();
		int p = buffer.put(values).position();
		buffer.reset();
		FloatBuffer out = buffer.slice();
		out.limit(values.length);
		buffer.position(p);
		return out.asReadOnlyBuffer();
	}
	
	/**
	 * assume convex polygon
	 * @param verts
	 * @return
	 */
	public static float[] getTriangles(float[] verts){
		int count = verts.length/2;
		float[] out = new float[(count-2)*6];
		int i = 0;
		
		for(int j = 1; j<count-1;j++){
			int p = j*2;
			out[i++] = verts[0];
			out[i++] = verts[1];
			out[i++] = verts[p++];
			out[i++] = verts[p++];
			out[i++] = verts[p++];
			out[i++] = verts[p++];
		}
		
		return out;
	}
	
	public static float[] getLines(float[] verts){
		float[] out = new float[verts.length*2];
		int i = 0;
		int j = 0;
		for(; j<verts.length-2; j+=2){
			out[i++] = verts[j];
			out[i++] = verts[j+1];
			out[i++] = verts[j+2];
			out[i++] = verts[j+3];
		}
		out[i++]=verts[j];
		out[i++]=verts[j+1];
		out[i++]=verts[0];
		out[i++]=verts[1];
		return out;
	}
	
	public static float[] fillCircle(float[] out,float radius, int offset,int verts){
		double theta = (Math.PI*2)/verts;
		double c = Math.cos(theta), s=Math.sin(theta);
		
		out[offset++]=radius;
		out[offset++]=0.0f;
		
		for(;verts>1;verts--){
			double x = out[offset-2],y = out[offset-1];
			out[offset++] = (float)(c*x - s*y);
			out[offset++] = (float)(s*x + c*y);
		}
		
		return out;
	}
	
	public static float[] fillCircleFan(float[] out,float radius, int offset,int verts){
		verts-=2;
		double theta = (Math.PI*2)/verts;
		double c = Math.cos(theta), s=Math.sin(theta);
		
		out[offset++]=0.0f;
		out[offset++]=0.0f;
		out[offset++]=radius;
		out[offset++]=0.0f;
		
		for(;verts>1;verts--){
			double x = out[offset-2],y = out[offset-1];
			out[offset++] = (float)(c*x - s*y);
			out[offset++] = (float)(s*x + c*y);
		}
		

		out[offset++]=radius;
		out[offset++]=0.0f;
		
		return out;
	}
	
	public static float[] setQuad(float[] coords, int offset, int step, float left, float bottom,float right, float top){
		setPoint(coords,offset,right,top);
		offset+=step;
		setPoint(coords,offset,left,top);
		offset+=step;
		setPoint(coords,offset,left,bottom);
		offset+=step;
		setPoint(coords,offset,right,bottom);
		return coords;
	}
	
	public static FloatBuffer setQuad(FloatBuffer coords, int offset, int step, float left, float bottom,float right, float top){
		setPoint(coords,offset,right,top);
		offset+=step;
		setPoint(coords,offset,left,top);
		offset+=step;
		setPoint(coords,offset,left,bottom);
		offset+=step;
		setPoint(coords,offset,right,bottom);
		return coords;
	}
	
	public static float[] setQuadTriangles(float[] coords, int offset, int step, float left, float bottom,float right, float top){
		setPoint(coords,offset,right,top);
		offset+=step;
		setPoint(coords,offset,left,top);
		offset+=step;
		setPoint(coords,offset,left,bottom);
		offset+=step;
		
		setPoint(coords,offset,right,top);
		offset+=step;
		setPoint(coords,offset,left,bottom);
		offset+=step;
		setPoint(coords,offset,right,bottom);
		return coords;
	}
	
	public static FloatBuffer setQuadTriangles(FloatBuffer coords, int offset, int step, float left, float bottom,float right, float top){
		setPoint(coords,offset,right,top);
		offset+=step;
		setPoint(coords,offset,left,top);
		offset+=step;
		setPoint(coords,offset,left,bottom);
		offset+=step;
		
		setPoint(coords,offset,right,top);
		offset+=step;
		setPoint(coords,offset,left,bottom);
		offset+=step;
		setPoint(coords,offset,right,bottom);
		return coords;
	}
	
	public static void setPoint(float[] coords,int offset, float x, float y){
		coords[offset++] = x;
		coords[offset] = y;
	}
	
	public static void setPoint(FloatBuffer fb,int offset, float x, float y){
		fb.put(offset++,x);
		fb.put(offset++,y);
	}
	
	private Primitives2d(){}
}
