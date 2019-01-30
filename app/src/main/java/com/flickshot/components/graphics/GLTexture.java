package com.flickshot.components.graphics;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.opengl.GLUtils;
import android.util.Log;

import static android.opengl.GLES20.*;
import static com.flickshot.Globals.*;

public class GLTexture {
	private static final GLTexture[] BOUND_TEXTURES = new GLTexture[32];
	
	private static int[] TEXTURE_CONSTANTS = new int[]{
		GL_TEXTURE0,GL_TEXTURE1,GL_TEXTURE2,GL_TEXTURE3,
		GL_TEXTURE4,GL_TEXTURE5,GL_TEXTURE6,GL_TEXTURE7,
		GL_TEXTURE8,GL_TEXTURE9,GL_TEXTURE10,GL_TEXTURE11,
		GL_TEXTURE12,GL_TEXTURE13,GL_TEXTURE14,GL_TEXTURE15,
		GL_TEXTURE16,GL_TEXTURE17,GL_TEXTURE18,GL_TEXTURE19,
		GL_TEXTURE20,GL_TEXTURE21,GL_TEXTURE22,GL_TEXTURE23,
		GL_TEXTURE24,GL_TEXTURE25,GL_TEXTURE26,GL_TEXTURE27,
		GL_TEXTURE28,GL_TEXTURE29,GL_TEXTURE30,GL_TEXTURE31
	};
	
	/**
	 * the dimension of the bitmap used to create this texture
	 */
	public final int width,height;
	/**
	 * This dimensions in pixels of actual texture, should be in power of 2
	 */
	public final int realWidth,realHeight;
	
	public final int handle;
	public final int type;
	
	private int samplerIndex;
	private boolean deleted = false;
		
	public GLTexture(Bitmap bmp, boolean repeating){
		width = bmp.getWidth();
		height = bmp.getHeight();
		
		
		realWidth = getPow2(width);
		realHeight = getPow2(height);
		
		if(realWidth!=width || realHeight!=height){
			Log.e("gltexture",width+" "+height+" "+realWidth+" "+realHeight);
			//resize texture to have dimension of power of 2
			Bitmap temp = Bitmap.createBitmap(realWidth,realHeight,Bitmap.Config.ARGB_8888);
			Log.e("gltexture",temp.getWidth()+" "+temp.getHeight());
			temp.eraseColor(0x000000);
			Canvas canvas = new Canvas(temp);
			Paint paint = new Paint();
			canvas.drawBitmap(bmp,null,new RectF(0,0,width,height),paint);
			bmp.recycle();
			bmp=temp;
		}
		
		int[] texture = new int[1];
		glGenTextures(1, texture, 0);
		
		if(texture[0]!=0){
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inScaled = false; // prevents autoscaling when the bitmap is loaded in
			
			glBindTexture(GL_TEXTURE_2D, texture[0]);
			
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			
			if(!repeating){
				glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
				glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
			}
			
			GLUtils.texImage2D(GL_TEXTURE_2D,0,bmp,0);
			
			bmp.recycle();
			
			handle = texture[0];
			type = GL_TEXTURE_2D;
		}else{
			throw new RuntimeException("failed to generate texture");
		}
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
	
	public GLTexture(Resources resources,int resourceId){
		this(getBitmap(resources,resourceId),true);
	}
	
	public GLTexture(Resources resources, String filename){
		this(getBitmap(resources, resources.getIdentifier(filename,"drawable",PACKAGE_NAME)),true);
	}
	
	public GLTexture(Resources resources,int resourceId,boolean isRepeating){
		this(getBitmap(resources,resourceId),isRepeating);
	}
	
	public GLTexture(Resources resources, String filename,boolean isRepeating){
		this(getBitmap(resources, resources.getIdentifier(filename,"drawable",PACKAGE_NAME)),isRepeating);
	}
	
	public final void setRepeating(boolean repeating){
		glBindTexture(GL_TEXTURE_2D,handle);
		if(repeating){
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
		}else{
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		}
	}
	
	public final void bind(int i){
		if(i<0 || i>31) throw new IllegalStateException("illegal sampler index: "+i);
		if(deleted) throw new IllegalStateException("texture already deleted");
		samplerIndex = i;
		
		glActiveTexture(TEXTURE_CONSTANTS[samplerIndex]);
		glBindTexture(type,handle);
		
		BOUND_TEXTURES[i]=this;
	}
	
	/**
	 * only effects client side texture management, opengl es does not actually provide a function
	 * to unbind texture as there is no reason to
	 */
	public final void unbind(){
		if(BOUND_TEXTURES[samplerIndex]==this)BOUND_TEXTURES[samplerIndex]=null;
	}
	
	public final void setActive(){
		if(deleted) throw new IllegalStateException("texture already deleted");
		if(BOUND_TEXTURES[samplerIndex]==this){
			glActiveTexture(TEXTURE_CONSTANTS[samplerIndex]);
		}else
			throw new IllegalStateException("texture not bound");
	}
	
	public final int samplerIndex(){
		return samplerIndex;
	}
	
	public final void delete(){
		if(!deleted){
			unbind();
			glDeleteTextures(1,new int[]{handle},0);
			deleted = true;
		}
	}
	
	public final boolean isDeleted(){
		return deleted;
	}
	
	private static final Bitmap getBitmap(Resources resources, int resourceId){
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inScaled = false;
		return BitmapFactory.decodeResource(resources, resourceId, options);
	}
	
}
