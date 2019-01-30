package com.flickshot.components.entities.defs.gui;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import com.flickshot.assets.AssetLibrary;
import com.flickshot.assets.textures.SpriteSheet;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.entities.defs.VisibleEntity;
import com.flickshot.components.graphics.Artist;
import com.flickshot.components.graphics.Renderer2d;
import com.flickshot.components.graphics.DirectRenderer2d;
import com.flickshot.geometry.Transformation;
import com.flickshot.geometry.collision.CollisionLib;
import com.flickshot.scene.Updater.UpdateEvent;

public class VisibleTimer extends VisibleEntity{
	public static final String ENTITY_NAME = "visible_timer";
	
	private static final String TEXTURE = "blank_white";
	private static final int SIZE = 128;

	private final FloatBuffer vertices;
	private final FloatBuffer texture_coords;
	
	public final Transformation tx = new Transformation();
	
	private double startTime;
	private double timeLeft;
	
	private double textHeight;
	
	public boolean showText = true;
	
	public VisibleTimer(){
		ByteBuffer temp = ByteBuffer.allocateDirect(SIZE*4*2*2).order(ByteOrder.nativeOrder());
		vertices  = temp.asFloatBuffer();
		vertices.position(SIZE*2);
		texture_coords = vertices.slice();
		vertices.position(0).limit(SIZE*2);
		setArtist(new Artist(){

			@Override
			public boolean isOnScreen(double screenX, double screenY,
					double screenWidth, double screenHeight) {
				return CollisionLib.boxBox(screenX,screenY,screenWidth,screenHeight,
						tx.translation.x,tx.translation.y,tx.scale.x,tx.scale.y);
			}

			@Override
			public void draw(double delta, Renderer2d renderer) {
				if(!AssetLibrary.has("texture",TEXTURE)){
						AssetLibrary.loadAsset("texture",TEXTURE);
				}
				float u = (float)(timeLeft/startTime);
				if(u>0.5f){
					u = (u-0.5f)/0.5f;
					renderer.color(1-u,1,0);
				}else{
					u /=0.5f;
					renderer.color(1,u,0);
				}
				renderer.push();
					renderer.transform(tx);
					renderer.setDrawMode(Renderer2d.STROKE);
					renderer.lineWidth(4);
					renderer.shape(Renderer2d.ELLIPSE);
					int textureHandle = ((SpriteSheet)AssetLibrary.get("texture",TEXTURE)).texture.handle;
					setVerts();
					renderer.setDrawMode(Renderer2d.FILL);
					((DirectRenderer2d)renderer).vertices(textureHandle,vertices,texture_coords,SIZE,1);
				renderer.pop();
				if(showText){
					renderer.push();
						String time = getTimeString();
						double scale = tx.scale.x/renderer.textWidth(time);
						textHeight = scale*renderer.textHeight();
						renderer.translate(tx.translation.x,tx.translation.y-((tx.scale.y/2)+8));
						renderer.scale(scale,scale);
						renderer.align(Renderer2d.CENTER,Renderer2d.BOTTOM);
						renderer.setDrawMode(Renderer2d.FILL);
						renderer.text(time);
					renderer.pop();
				}
			}
			
		});
	}
	
	private void setVerts(){
		vertices.put(0,0).put(1,0);
		texture_coords.put(0,0.5f).put(1,0.5f);
		float x = 0;
		float y = 0.5f;
		float theta = (float)(Math.PI*2*(timeLeft/startTime));
		float c = (float)Math.cos(theta/(SIZE-2));
		float s = (float)Math.sin(theta/(SIZE-2));
		float u = x,v = y;
		x = (c*u) - (s*v);
		y = (s*u) + (c*v);
		for(int i = 1; i<SIZE; i++){
			u = x;v = y;
			x = (c*u) - (s*v);
			y = (s*u) + (c*v);
			vertices.put(i*2,x).put((i*2)+1,y);
			texture_coords.put(i*2,x+0.5f).put((i*2)+1,y+0.5f);
		}
		
		vertices.position(0);
		texture_coords.position(0);
	}
	
	public double getTextHeight(){
		return textHeight;
	}
	
	public void init(double x, double y){
		super.init(x,y);
		tx.translation.set(x,y);
		timeLeft = startTime = 60;
	}
	
	public void update(UpdateEvent evt){
		super.update(evt);
		timeLeft = Math.max(0,timeLeft-evt.getDelta());
	}
	
	public double getTimeLeft(){
		return timeLeft;
	}
	
	public double getStartTime(){
		return startTime;
	}
	
	public void setTimer(double startTime,double time){
		this.startTime = startTime;
		this.timeLeft = time;
	}
	
	public String getTimeString(){
		int minutes = (int)(timeLeft/60);
		double seconds = timeLeft%60;
		return String.format("%02d:%02.2f",minutes,seconds);
	}
	
	public static EntityStateFactory getFactory(){
		return new EntityStateFactory(){
			@Override
			public EntityState construct() {
				return new VisibleTimer();
			}

			@Override
			public Class<? extends EntityState> getType() {
				return VisibleTimer.class;
			}
		};
	}
}
