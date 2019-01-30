package com.flickshot.assets.textures;

import java.util.HashMap;
import java.util.Set;

import com.flickshot.components.graphics.GLTexture;

/**
 * This class holds sprite data including information on the cell width and height
 * as well as the animation start and end frames
 * @author Alex
 *
 */
public class SpriteSheet {
	public final GLTexture texture;
	public final int cellWidth;
	public final int cellHeight;
	private final HashMap<String ,Animation> animations;
	
	SpriteSheet(GLTexture texture,int cellWidth, int cellHeight,HashMap<String,Animation> animations){
		this.texture = texture;
		this.cellWidth = cellWidth;
		this.cellHeight = cellHeight;
		this.animations = animations;
	}
	
	public Animation getAnimation(String name){
		return animations.get(name);
	}
	
	public Set<String> getAnimationNames(){
		return animations.keySet();
	}
	
	public boolean hasAnimation(String name){
		return animations.containsKey(name);
	}
	
	public int getNumberOfAnimation(){
		return animations.size();
	}
	
	public int getNumOfFrames(){
		return (texture.width/cellWidth)*(texture.height/cellHeight);
	}
	
	public static final class Animation{
		public final int startFrame;
		public final int endFrame;
		public final boolean looped;
		
		public Animation(int startFrame, int endFrame, boolean looped){
			this.startFrame = startFrame;
			this.endFrame = endFrame;
			this.looped = looped;
		}
		
	}
}
