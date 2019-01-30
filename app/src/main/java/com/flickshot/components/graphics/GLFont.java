package com.flickshot.components.graphics;

import java.nio.FloatBuffer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import static android.util.Log.*;

/**
 * This object stores information about a particular font
 * @author Alex
 *
 */
public class GLFont {
	private static final int ASCII_START=32;
	private static final int ASCII_END=126;
	
	private static final int NUM_OF_MODIFIERS=3;
	
	public final static int FONT_SIZE_MIN = 6;
	public final static int FONT_SIZE_MAX = 180;
	
	private final float[] texX;
	private final float[] charWidths;
	
	private int startChar,endChar;
	
	public final Typeface typeface;
	
	int spriteWidth;
	int spriteHeight;
	
	public final FontData data;
	
	GLFont(String path,int fontSize){
		int chars = ((ASCII_END-ASCII_START)+1);
		texX = new float[chars];
		charWidths = new float[chars];
		typeface = Typeface.createFromFile(path);
		data = initFont(typeface,fontSize,ASCII_START,ASCII_END);
	}
	
	GLFont(Typeface tf, int fontSize){
		int chars = ((ASCII_END-ASCII_START)+1);
		texX = new float[chars];
		charWidths = new float[chars];
		typeface = tf;
		data = initFont(tf,fontSize,ASCII_START,ASCII_END);
	}
	
	private FontData initFont(final Typeface tf,final int fontSize,final int startChar,final int endChar){//TODO fix text being truncated
		
		this.startChar = startChar;
		this.endChar = endChar;
		final char[] characterSet = getChars((char)startChar,(char)endChar);
		
		Paint paint = new Paint();
		paint.setAntiAlias( true );
		paint.setTextSize(fontSize);
		paint.setColor(0xFFFFFFFF);
		paint.setTypeface( tf );

		// get font metrics
		Paint.FontMetrics fm = paint.getFontMetrics();
		float ascent = fm.ascent;
		float descent = fm.descent;
		float top = fm.top;
		float bottom = fm.bottom;
		float leading = fm.leading;
		float xHeight;
		
		int fontHeight = (int)Math.round(Math.ceil(Math.abs(bottom)+Math.abs(top)));//??is this the correct size??
		
		paint.getTextWidths(characterSet,0,characterSet.length,charWidths);
		Rect bounds = new Rect();
		paint.getTextBounds(characterSet,0,characterSet.length,bounds);
		
		xHeight = bounds.top/2;
		
		float width = 0;
		for(float w: charWidths)width+=w+1;
		
		Bitmap bmp = Bitmap.createBitmap(getPow2(bounds.width()),getPow2(bounds.height()),Bitmap.Config.ALPHA_8);
		Canvas canvas = new Canvas( bmp );
		bmp.eraseColor(0x00000000);
		
		paint.setTextAlign(Align.LEFT);
		
		float baseLine = (bmp.getHeight()/2)-xHeight;
		
		float xpos = 0;
		for(int i = 0; i<characterSet.length; i++){
			canvas.drawText(characterSet,i,1,xpos,baseLine,paint);//??is this the correct y position??
			xpos+=charWidths[i]+1;
		}
		
		xpos = 0.0f;
		for(int i = 0; i<characterSet.length; i++){
			float w = charWidths[i];
			texX[i]=xpos;
			xpos+=w+1;
		}
		
		spriteWidth = bmp.getWidth();
		spriteHeight = bmp.getHeight();
		
		return new FontData(loadTexture(bmp),ascent,descent,top,bottom,leading,xHeight);
	}
	
	private char[] getChars(char startChar, char endChar){
		char[] out = new char[(endChar-startChar)+1];
		int i = 0;
		while(startChar<=endChar)out[i++]=startChar++;
		return out;
	}
	
	private int loadTexture(Bitmap bmp){
		int[] handle = new int[1];
		GLES20.glGenTextures(1, handle, 0);
		
		if(handle[0] == 0) throw new IllegalStateException("could not load texture");
			
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,handle[0]);
		
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE );
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE );
        
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);
		
        bmp.recycle();
		
		return handle[0];
	}
	
	private static int getPow2(int x){
		if(x<1)return 0;
		int i = (isPower2(x))?0:1;
		for(;x!=1;i++)x/=2;
		x = 2;
		for(;i>1;i--)x*=2;
		return x;
	}
	
	private static boolean isPower2(int x){
		int i = 1;
		while(i<x)i*=2;
		return i==x;
	}
	
	public final int getCharacterIndex(char c){
		c-=startChar;
		if(c<0 || c>charWidths.length) c = 0;
		return c;
	}
	
	/**
	 * Sets the texture and vertex coordinates of the passed character in this font
	 * @param c
	 * @param coords
	 * @param offsetc
	 * @param stepc
	 * @param texCoords
	 * @param offsett
	 * @param stept
	 * @param xoffset
	 * @param yoffset
	 * @return the width to move the cursor
	 */
	public final float setCoordsSquare(char c,float[] coords,int offsetc, int stepc, float[] texCoords, int offsett, int stept,float xoffset, float yoffset){
		int i = getCharacterIndex(c);
		float width = charWidths[i];
		float x = texX[i];
		yoffset-=spriteHeight/2;//draw from center of y axis
		Primitives2d.setQuad(coords,offsetc,stepc,xoffset,yoffset,xoffset+width,yoffset+spriteHeight);
		Primitives2d.setQuad(texCoords,offsett,stept,x/spriteWidth,1,(x+width)/spriteWidth,0);//y axis is inverted
		return width;
	}
	
	/**
	 * Sets the texture and vertex coordinates of the passed character in this font
	 * @param c
	 * @param coords
	 * @param offsetc
	 * @param stepc
	 * @param texCoords
	 * @param offsett
	 * @param stept
	 * @param xoffset
	 * @param yoffset
	 * @return the width to move the cursor
	 */
	public final float setCoordsSquare(char c,FloatBuffer coords,int offsetc, int stepc, FloatBuffer texCoords, int offsett, int stept,float xoffset, float yoffset){
		int i = getCharacterIndex(c);
		float width = charWidths[i];
		float x = texX[i];
		Primitives2d.setQuad(coords,offsetc,stepc,xoffset,yoffset,xoffset+width,yoffset+spriteHeight);
		Primitives2d.setQuad(texCoords,offsett,stept,x/spriteWidth,1,(x+width)/spriteWidth,0);//y axis is inverted
		return width;
	}
	
	public final float getWidth(CharSequence c,int offset, int size){
		float width = 0;
		for(int i = offset; i<offset+size; i++)width+=charWidths[getCharacterIndex(c.charAt(i))];
		return width;
	}
	
	public final float getWidth(char[] c,int offset, int size){
		float width = 0;
		for(int i = offset; i<offset+size; i++)width+=charWidths[getCharacterIndex(c[i])];
		return width;
	}
	
	public final float getWidth(char c){
		return charWidths[getCharacterIndex(c)];
	}
	
	/**
	 * for sake of reducing dependency on the andriod libraries
	 */
	public static final class FontData{
		public final int handle;
		public final float ascent;
		public final float descent;
		public final float leading;
		public final float top;
		public final float bottom;
		public final float height;
		public final float xHeight;
		
		private FontData(int handle, float ascent, float descent,  float top, float bottom, float leading,float xHeight){
			this.handle = handle;
			this.ascent = ascent;
			this.descent = descent;
			this.leading = leading;
			this.top = top;
			this.bottom = bottom;
			this.xHeight = xHeight;
			height = (float)(Math.ceil(Math.abs(bottom)+Math.abs(top)));
		}
	}
}
