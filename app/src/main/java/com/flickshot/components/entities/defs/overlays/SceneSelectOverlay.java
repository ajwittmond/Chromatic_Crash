package com.flickshot.components.entities.defs.overlays;

import java.util.ArrayList;
import java.util.HashMap;

import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.entities.defs.VisibleEntity;
import com.flickshot.components.input.TouchEvent;
import com.flickshot.components.input.TouchListener;
import com.flickshot.components.input.TouchManager;
import com.flickshot.components.graphics.Artist;
import com.flickshot.components.graphics.Graphics;
import com.flickshot.components.graphics.Renderer2d;
import com.flickshot.components.graphics.Screen;
import com.flickshot.config.Config;
import com.flickshot.scene.Scene;
import com.flickshot.scene.Updater.UpdateEvent;

public class SceneSelectOverlay extends VisibleEntity{
	public static final String ENTITY_NAME = "SceneSelectOverlay";
	
	private SceneButtonCol[] cols; 
	private TouchListener t;
	
	double col;
	
	private double colCountHeight=64;
	private double boxHeight=256;
	
	private boolean grabbed;

	public SceneSelectOverlay(){
		setArtist(new Artist(){

			@Override
			public boolean isOnScreen(double screenX, double screenY,
					double screenWidth, double screenHeight) {
				return true;
			}

			@Override
			public void draw(double delta, Renderer2d renderer) {
				Screen s = Graphics.getCurrentScene().screen;
				renderer.align(Renderer2d.CENTER,Renderer2d.CENTER);
				renderer.translate(s.getX(),s.getY()+s.getHeight());
				renderer.setDrawMode(Renderer2d.FILL);
				//draw columns
				renderer.push();
					renderer.translate(0,-colCountHeight);
					double t = col-Math.floor(col);
					if(t==0){
						cols[(int)Math.floor(col)].draw(delta,renderer);
					}else{
						double w = s.getWidth()*(1.0-t);
						renderer.translate(w-s.getWidth(),0);
						cols[(int)Math.floor(col)].draw(delta,renderer);
						renderer.translate(s.getWidth(),0);
						cols[(int)Math.ceil(col)].draw(delta,renderer);
					}
				renderer.pop();
				
				//draw col count
				double bwidth = s.getWidth()/cols.length;
				renderer.translate(bwidth/2,-colCountHeight/2);
				for(int i = 0;i<cols.length; i++){
					renderer.push();
						renderer.color(1,0.6,0.9);
						renderer.shape(Renderer2d.SQUARE,bwidth,colCountHeight);
						renderer.color(0.6,0.9,1);
						renderer.shape(Renderer2d.SQUARE,bwidth-16,colCountHeight-16);
						renderer.color(0,0,0);
						double scale = (colCountHeight*(3.0/4.0))/renderer.textHeight();
						renderer.scale(scale,scale);
						renderer.text(""+i);
					renderer.pop();
					renderer.translate(bwidth,0);
				}
				
				
			}
			
		});
		t= new TouchListener(){
			
			boolean moving;
			
			SceneButtonCol grabbedCol;
			
			double dx,dy;
			double px,py;
			
			@Override
			public void onDown(TouchEvent evt) {
				Screen s = Graphics.getCurrentScene().screen;
				px = TouchManager.x()-s.getX();
				py = TouchManager.y()-s.getY();
				double t = col-Math.floor(col);
				grabbed = true;
				if(t==0){
					grabbedCol = cols[(int)Math.floor(col)];
				}else{
					double width = Graphics.getCurrentScene().screen.getWidth()*(1.0-t);
					if(px<width){
						grabbedCol = cols[(int)Math.floor(col)];
					}else{
						grabbedCol = cols[(int)Math.ceil(col)];
					}
				}
			}

			@Override
			public void onMove(TouchEvent evt) {
				Screen s = Graphics.getCurrentScene().screen;
				dx += (TouchManager.x()-s.getX())-px;
				dy += (TouchManager.y()-s.getY())-py;
				px = TouchManager.x()-s.getX();
				py = TouchManager.y()-s.getY();
				if(!moving){
					moving = Math.abs(dx)>32 || Math.abs(dy)>32;
				}
				if(moving){
					col-=dx/s.getWidth();
					if(col>cols.length-1)
						col = cols.length-1;
					if(col<0)
						col = 0;
					dx = 0;
					grabbedCol.move(dy);
					dy=0;
				}
			}

			@Override
			public void onUp(TouchEvent evt) {
				Screen s = Graphics.getCurrentScene().screen;
				grabbed = false;
				if(moving==false && col-Math.floor(col)==0){
					grabbedCol.click(TouchManager.y()-s.getY());
				}
				moving = false;
			}
			
		};
	}
	
	@Override
	public void init(double x, double y){
		super.init(x,y);
		TouchManager.add(t);
		TouchManager.setScreen(Graphics.getCurrentScene().screen);

	}
	
	@Override
	public void update(UpdateEvent evt){
		super.update(evt);
		if(!grabbed){
			double t = col-Math.floor(col);
			if(t!=0){
				if(t>0.5){
					t+=evt.getDelta()*0.5;
				}else{
					t-=evt.getDelta()*0.5;
				}
			}
			if(t>1){
				col = Math.ceil(col);
			}else if(t<0){
				col = Math.floor(col);
			}else{
				col = Math.floor(col)+t;
			}
		}
	}
	
	@Override
	public void destroy(){
		super.destroy();
		TouchManager.remove(t);
	}
	
	public void configure(Config c){
		SceneSelectConfig sc = (SceneSelectConfig)c;
		cols = new SceneButtonCol[sc.rows.length];
		for(int i = 0; i<cols.length; i++){
			SceneButton first = new SceneButton(sc.rows[i].scenes[0]);
			SceneButton curr = first;
			for(int j = 1; j<sc.rows[i].scenes.length; j++){
				SceneButton temp = new SceneButton(sc.rows[i].scenes[j]);
				curr.next = temp;
				curr = temp;
			}
			cols[i] = new SceneButtonCol(first,sc.rows[i].scenes.length);
		}
	}
	
	public static final EntityStateFactory getFactory(){
		return new Factory();
	}
	
	public static class Factory extends EntityStateFactory{
		@Override
		public EntityState construct() {
			return new SceneSelectOverlay();
		}

		@Override
		public Class<? extends EntityState> getType() {
			return SceneSelectOverlay.class;
		}
		
		@Override
		public Config getConfig(){
			return new SceneSelectConfig();
		}
		
		@Override
		public void getAssets(ArrayList<String[]> assets){
		}
	}
	
	public static final class SceneSelectConfig extends Config{
		public RowConfig[] rows = new RowConfig[0];
		
		
		@Override
		public void setValue(String text) {
		}

		@Override
		public void getAliases(HashMap<String, String> map) {
		}
		
	}
	
	public static final class RowConfig extends Config{
		public SceneConfig[] scenes = new SceneConfig[0];
		
		
		@Override
		public void setValue(String text) {
		}

		@Override
		public void getAliases(HashMap<String, String> map) {
		}
		
	}
	
	public static final class SceneConfig extends Config{
		public String name;
		public String id;
		public String description;
		
		@Override
		public void setValue(String text) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void getAliases(HashMap<String, String> map) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	private class SceneButton{
		String name;
		String id;
		String description;
		
		SceneButton next;
		
		public SceneButton(SceneConfig c){
			name = c.name;
			id = c.id;
			description = c.description;
		}
		
		public void draw(double delta, Renderer2d r){
			r.translate(0,-boxHeight/2);
			Screen s = Graphics.getCurrentScene().screen;
			r.push();
				r.color(1,0.6,0.9);
				r.shape(Renderer2d.SQUARE,s.getWidth(),boxHeight);
				r.color(0.6,0.9,1);
				r.shape(Renderer2d.SQUARE,s.getWidth()-16,boxHeight-16);
				r.color(0,0,0);
				double scale = 64/r.textHeight();
				r.scale(scale,scale);
				r.text(""+name);
			r.pop();
			r.translate(0,-boxHeight/2);
			if(next!=null)next.draw(delta,r);
		}
	}
	
	private class SceneButtonCol{
		SceneButton top;

		final int size;
		double y;
		
		SceneButtonCol(SceneButton top,int size){
			this.top = top;
			this.size = size;
		}
		
		public void move(double dy){
			y += dy;
			if(y<0) y=0;
			double max = 
					Math.max((boxHeight*size)-
							Graphics.getCurrentScene().screen.getHeight(),0);
			if(y>max) y = max;
		}
		
		public void click(double y){
			if(alive()){
				Screen s = Graphics.getCurrentScene().screen;
				
				y = (s.getHeight()-y)-colCountHeight;
				
				if(y>0){
					y+=this.y;
					SceneButton curr = top;
						for(int i = 0; i<Math.floor(y/boxHeight) && curr!=null;i++){
							curr = curr.next;
						}
						if(curr!=null){
							Scene.newScene(curr.id);
							kill();
						}
				}
			}
		}
		
		
		public void draw(double delta, Renderer2d r){
			Screen s = Graphics.getCurrentScene().screen;
			r.push();
				r.translate(s.getWidth()/2,y);
				top.draw(delta,r);
			r.pop();
		}
	}
}
