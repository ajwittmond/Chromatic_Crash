package com.flickshot.components.graphics;

import java.nio.FloatBuffer;

import com.flickshot.assets.AssetLibrary;
import com.flickshot.assets.textures.SpriteSheet;

public class VertexBuffer {
	public final String textureName;
	private GLTexture texture;
	private float[] vertexCoord;
	private float[] textureCoord;
	private float[] vertexColor;
	private float[] tintWeight;
	private int size;
	
	public VertexBuffer(String texture,int size,float[] vertexCoord,float[] textureCoord,float[] vertexColor,float[] tintWeight){
		textureName = texture;
		if((this.size = size)<=0)throw new IllegalArgumentException("illegal size: "+size);
		this.vertexCoord = vertexCoord;
		this.vertexColor = vertexColor;
		this.textureCoord = textureCoord;
		this.tintWeight = tintWeight;
		if(vertexCoord.length%2 != 0 || textureCoord.length%2 != 0 || vertexColor.length%4 != 0)
			throw new IllegalArgumentException("invalid array size");
	}
	
	public int size(){
		return size;
	}
	
	final int getTextureHandle(){
		if(texture==null || texture.isDeleted()){
			SpriteSheet sheet = ((SpriteSheet)AssetLibrary.get("texture",textureName));
			texture = sheet.texture;
		}
		return texture.handle;
	}
	
	public void setPosition(int i,float x, float y){
		if(i>size) throw new ArrayIndexOutOfBoundsException(""+i);
		i= (i*2)%vertexCoord.length;
		vertexCoord[i] = x;
		vertexCoord[i+1] = y;
	}
	
	public void setTextureCoord(int i, float u, float v){
		if(i>size) throw new ArrayIndexOutOfBoundsException(""+i);
		i= (i*2)%textureCoord.length;
		textureCoord[i] = u;
		textureCoord[i+1] = v;
	}
	
	public void setColor(int i,float r, float g, float b, float a){
		if(i>size) throw new ArrayIndexOutOfBoundsException(""+i);
		i= (i*4)%vertexColor.length;
		vertexColor[i] = r;
		vertexColor[i+1] = b;
		vertexColor[i+2] = g;
		vertexColor[i+3] = a;
	}
	
	public void setTintWeight(int i,float w){
		if(i>size) throw new ArrayIndexOutOfBoundsException(""+i);
		tintWeight[i%tintWeight.length] = w;
	}
}
