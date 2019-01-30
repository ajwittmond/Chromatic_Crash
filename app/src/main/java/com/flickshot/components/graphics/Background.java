package com.flickshot.components.graphics;

import com.flickshot.assets.AssetLibrary;
import com.flickshot.assets.textures.SpriteSheet;

public class Background {
	private final String textureName;
	private GLTexture texture;
	
	public final float x,y,width,height;
	public final boolean tiling;
	
	Background(String textureName,float x,float y,float width,float height,boolean tiling){
		this.textureName = textureName;
		this.x =x;
		this.y =y;
		this.width = width;
		this.height = height;
		this.tiling = tiling;
	}
	
	public final GLTexture getTexture(){
		if(texture==null || texture.isDeleted()){
			AssetLibrary.loadAsset("texture",textureName);
			SpriteSheet sheet = (SpriteSheet)AssetLibrary.get("texture",textureName);
			texture = sheet.texture;
			texture.setRepeating(tiling);
		}
		return texture;
	}
	
	@Override
	public String toString(){
		return "background{ handle:"+getTexture().handle+" texture:"+textureName+" x:"+x+" y:"+y+" width:"+width+" height:"+height+" tiling:"+tiling+"}";
	}
}
