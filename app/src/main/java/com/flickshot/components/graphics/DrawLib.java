package com.flickshot.components.graphics;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import com.android.texample2.GLText;
import com.flickshot.GameView;
import com.flickshot.components.graphics.program.GLProgram;
import com.flickshot.components.physics.shapes.Polygon;
import com.flickshot.geometry.Transformation;
import com.flickshot.util.MatrixStack;

public class DrawLib {
	
	private static GLProgram shape_program;
	public static GLProgram texture_program;
	
	public static FloatBuffer box_coords;
	private static FloatBuffer triangle_coords;
	private static FloatBuffer circle_coords;
	private static FloatBuffer line_coords;
	private static FloatBuffer orientable_circle_coords;
	
	
	public static float line_width=2.0f;
	
	static{
		ByteBuffer bb = ByteBuffer.allocateDirect(12*4);
		bb.order(ByteOrder.nativeOrder());
		
		box_coords = bb.asFloatBuffer();
		box_coords.put(new float[]{
				0.5f, 0.5f, 0.0f,
				-0.5f, 0.5f, 0.0f,
				-0.5f, -0.5f, 0.0f,
				0.5f, -0.5f, 0.0f
		});
		box_coords.position(0);
		
		bb = ByteBuffer.allocateDirect(12*3);
		bb.order(ByteOrder.nativeOrder());
		
		triangle_coords = bb.asFloatBuffer();
		triangle_coords.put(new float[]{
			0.0f, 0.5f, 1.0f,
			-0.5f, -0.5f, 1.0f,
			0.5f, -0.5f, 1.0f
		});
		triangle_coords.position(0);
		
		final int circleSize = 32;
		bb = ByteBuffer.allocateDirect(12*circleSize);
		bb.order(ByteOrder.nativeOrder());
		
		float[] circle = new float[circleSize*3];
		double theta = Math.PI * (2.0/circleSize);
		for(int i = 0;i<circle.length; i+=3){
			circle[i] = (float)(Math.cos(theta*i/3)*0.5);
			circle[i+1] = (float)(Math.sin(theta*i/3)*0.5);
			circle[i+2] = 0;
		}
		circle_coords = bb.asFloatBuffer();
		circle_coords.put(circle);
		circle_coords.position(0);
		
		bb = ByteBuffer.allocateDirect(12*circleSize);
		bb.order(ByteOrder.nativeOrder());
		
		circle = new float[circleSize*3];
		theta = Math.PI * (2.0/(circleSize-2));//has point in center and is drawn with line strip in stead of line loop
		circle[0]=0;
		circle[1]=0;
		circle[2]=0;
		for(int i = 3;i<circle.length; i+=3){
			circle[i] = (float)(Math.cos(theta*(i-3)/3)*0.5);
			circle[i+1] = (float)(Math.sin(theta*(i-3)/3)*0.5);
			circle[i+2] = 0;
		}
		orientable_circle_coords = bb.asFloatBuffer();
		orientable_circle_coords.put(circle);
		orientable_circle_coords.position(0);
		
		bb = ByteBuffer.allocateDirect(12*2);
		bb.order(ByteOrder.nativeOrder());
		line_coords = bb.asFloatBuffer();
		line_coords.position(0);
	}
	
	public static void init(Resources resources) throws IOException{
		shape_program = new GLProgram("shape",resources.getAssets().open("sprite_shape.vs"),resources.getAssets().open("sprite_shape.fs"));
		texture_program = new GLProgram("shape",resources.getAssets().open("sprite_text.vs"),resources.getAssets().open("sprite_text.fs"));
	}
	
	
	public static int loadTexture(final Context context, final int resourceId){
		int[] texture = new int[1];
		GLES20.glGenTextures(1, texture, 0);
		
		if(texture[0]!=0){
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inScaled = false; // prevents autoscaling when the bitmap is loaded in
			
			final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
			
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
			
			GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,0,bitmap,0);
			
			bitmap.recycle();
			
			return texture[0];
		}else{
			throw new RuntimeException("failed to generate texture");
		}
	}
	
	public static void deleteTexture(int texture){
		GLES20.glDeleteTextures(1,new int[]{texture},0);
	}
	
	private static final void fillPrimitive(MatrixStack mvMatrix, MatrixStack pvMatrix, float x, float y, float z, float width, float height,float theta, float r,float g, float b, float a,FloatBuffer verts, int numOfVerts){
		mvMatrix.push();
			Matrix.translateM(mvMatrix.current(), 0, x, y, z);
			Matrix.scaleM(mvMatrix.current(), 0, width, height, 1);
			Matrix.rotateM(mvMatrix.current(), 0, theta, 0, 0, 1);
			
			shape_program.useEnableAll();
			shape_program.getAttribute("aVertexPosition").setAttributePointer(3,GLES20.GL_FLOAT,false,12,verts);
			shape_program.getUniform("uColor").set4(r,g,b,a);
			shape_program.getUniform("uMVMatrix").setMatrix4(1,false, mvMatrix.current(), 0);
			shape_program.getUniform("uPMatrix").setMatrix4(1,false, pvMatrix.current(), 0);
		mvMatrix.pop();
		
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN,0,numOfVerts);
		shape_program.disableAll();
	}
	
	private static final void strokePrimitive(MatrixStack mvMatrix, MatrixStack pvMatrix, float x, float y, float z, float width, float height,float theta, float r,float g, float b, float a,FloatBuffer verts, int numOfVerts){
		mvMatrix.push();
			Matrix.translateM(mvMatrix.current(), 0, x, y, z);
			Matrix.scaleM(mvMatrix.current(), 0, width, height, 1);
			Matrix.rotateM(mvMatrix.current(), 0, theta, 0, 0, 1);
			
			shape_program.useEnableAll();
			shape_program.getAttribute("aVertexPosition").setAttributePointer(3,GLES20.GL_FLOAT,false,12,verts);
			shape_program.getUniform("uColor").set4(r,g,b,a);
			shape_program.getUniform("uMVMatrix").setMatrix4(1,false, mvMatrix.current(), 0);
			shape_program.getUniform("uPMatrix").setMatrix4(1,false, pvMatrix.current(), 0);
		mvMatrix.pop();
		
		GLES20.glLineWidth(line_width);
		GLES20.glDrawArrays(GLES20.GL_LINE_LOOP,0,numOfVerts);
		shape_program.disableAll();
	}
	
	public static void fillTriangle(MatrixStack mvMatrix, MatrixStack pvMatrix, float x, float y, float z, float width, float height,float theta, float r,float g, float b, float a){
		fillPrimitive(mvMatrix,pvMatrix,x,y,z,width,height,theta,r,g,b,a,triangle_coords,3);
	}
	
	public static void fillSquare(MatrixStack mvMatrix, MatrixStack pvMatrix, float x, float y, float z, float width, float height,float theta, float r,float g, float b, float a){
		fillPrimitive(mvMatrix,pvMatrix,x,y,z,width,height,theta,r,g,b,a,box_coords,4);
	}
	
	public static void fillCircle(MatrixStack mvMatrix, MatrixStack pvMatrix, float x, float y, float z, float width, float height,float theta, float r,float g, float b, float a){
		fillPrimitive(mvMatrix,pvMatrix,x,y,z,width,height,theta,r,g,b,a,circle_coords,32);
	}
	
	public static void strokeLine(MatrixStack mvMatrix, MatrixStack pvMatrix,float x1, float y1, float z1, float x2, float y2, float z2, float r,float g, float b, float a){
		mvMatrix.push();
			line_coords.position(0);
			line_coords.put(x1);
			line_coords.put(y1);
			line_coords.put(z1);
			line_coords.put(x2);
			line_coords.put(y2);
			line_coords.put(z2);
			line_coords.position(0);
			
			shape_program.useEnableAll();
			shape_program.getAttribute("aVertexPosition").setAttributePointer(3,GLES20.GL_FLOAT,false,12,line_coords);
			shape_program.getUniform("uColor").set4(r,g,b,a);
			shape_program.getUniform("uMVMatrix").setMatrix4(1,false, mvMatrix.current(), 0);
			shape_program.getUniform("uPMatrix").setMatrix4(1,false, pvMatrix.current(), 0);
		mvMatrix.pop();
		
		GLES20.glLineWidth(line_width);
		GLES20.glDrawArrays(GLES20.GL_LINE_LOOP,0,2);
		shape_program.disableAll();
	}
	
	public static void strokeTriangle(MatrixStack mvMatrix, MatrixStack pvMatrix, float x, float y, float z, float width, float height,float theta, float r,float g, float b, float a){
		strokePrimitive(mvMatrix,pvMatrix,x,y,z,width,height,theta,r,g,b,a,triangle_coords,3);
	}
	
	public static void strokeSquare(MatrixStack mvMatrix, MatrixStack pvMatrix, float x, float y, float z, float width, float height,float theta, float r,float g, float b, float a){
		strokePrimitive(mvMatrix,pvMatrix,x,y,z,width,height,theta,r,g,b,a,box_coords,4);
	}
	
	public static void strokeCircle(MatrixStack mvMatrix, MatrixStack pvMatrix, float x, float y, float z, float width, float height,float theta, float r,float g, float b, float a){
		strokePrimitive(mvMatrix,pvMatrix,x,y,z,width,height,theta,r,g,b,a,circle_coords,32);
	}
	
	public static void strokeOrientableCircle(MatrixStack mvMatrix, MatrixStack pvMatrix, float x, float y, float z, float width, float height,float theta, float r,float g, float b, float a){
		strokePrimitive(mvMatrix,pvMatrix,x,y,z,width,height,theta,r,g,b,a,orientable_circle_coords,32);
	}
	
	public static final FloatBuffer sprite_coords;
	
	static{
		ByteBuffer bb = ByteBuffer.allocateDirect(8*4);
		bb.order(ByteOrder.nativeOrder());
		
		sprite_coords = bb.asFloatBuffer();
		sprite_coords.put(new float[]{
				0.0f, 0.0f,
				1.0f, 0.0f, 
				1.0f, 1.0f,
				0.0f, 1.0f
		});
		sprite_coords.position(0);
	}
	
	public static void drawSprite(MatrixStack mvMatrix, MatrixStack pvMatrix,int texture,float[] textureCoords, float xOffset,float yOffset, float x, float y, float z, float width, float height,float theta, float r, float g, float b,float tintWeight, float alpha){
		mvMatrix.push();
			Matrix.translateM(mvMatrix.current(), 0, x, y, z);
			Matrix.scaleM(mvMatrix.current(), 0, width, height, 1);
			Matrix.rotateM(mvMatrix.current(), 0, theta, 0, 0, 1);
			
			GLES20.glEnable(GLES20.GL_BLEND);
			GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA,GLES20.GL_ONE_MINUS_SRC_ALPHA);
			
			texture_program.useEnableAll();
			texture_program.getAttribute("aVertexPosition").setAttributePointer(3,GLES20.GL_FLOAT,false,12,box_coords);
			texture_program.getAttribute("aTextureCoord").setAttributePointer(2,GLES20.GL_FLOAT,false,8,sprite_coords);
			texture_program.getUniform("uTint").set3(r,g,b);
			texture_program.getUniform("uTintWeight").set1(tintWeight);
			texture_program.getUniform("uAlpha").set1(alpha);
			
			GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
			texture_program.getUniform("uSampler").set1(0);
			
			texture_program.getUniform("uMVMatrix").setMatrix4(1,false, mvMatrix.current(), 0);
			texture_program.getUniform("uPMatrix").setMatrix4(1,false, pvMatrix.current(), 0);
		mvMatrix.pop();
		
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN,0,4);
		texture_program.disableAll();
	}
	
	public static final void drawWireframe(MatrixStack mvMatrix, MatrixStack pvMatrix,Transformation t,Polygon polygon,float r,float g, float b, float a){
		mvMatrix.push();
			t.transform(mvMatrix);
			
			shape_program.useEnableAll();
			shape_program.getAttribute("aVertexPosition").setAttributePointer(2,GLES20.GL_FLOAT,false,8,polygon.getDrawBuffer());
			shape_program.getUniform("uColor").set4(r,g,b,a);
			shape_program.getUniform("uMVMatrix").setMatrix4(1,false, mvMatrix.current(), 0);
			shape_program.getUniform("uPMatrix").setMatrix4(1,false, pvMatrix.current(), 0);
		mvMatrix.pop();
		
		GLES20.glLineWidth(line_width);
		GLES20.glDrawArrays(GLES20.GL_LINE_LOOP,0,polygon.getNumOfVerts());
		shape_program.disableAll();
	}
}
