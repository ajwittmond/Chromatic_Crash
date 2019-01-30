package com.flickshot.components.graphics;

import android.util.DisplayMetrics;
import android.util.Log;

import com.flickshot.GameView;
import com.flickshot.components.particles.Particles;
import com.flickshot.util.LinkedNodeList;

public class Scene {
	
	Background background;
	private final LinkedNodeList<Artist> artists;
	
	public final Screen screen;
	public final Renderer2d renderer;
	
	public float tintR=1,tintG=1,tintB=1,tintA=0;
	
	double iconversion;
	
	boolean destroyed = false;
	
	public Scene(Renderer2d renderer,Background background,Screen screen){
		this.renderer = renderer;
		this.background = background;
		this.screen = screen;
		artists = new LinkedNodeList<Artist>();
	}
	
	final void transferArtists(Scene s,String id){
		Artist head = s.artists.getHead();
		for(;head!=null;head=head.next){
			artists.add(head);
			head.scene = this;
			head.sceneId = id;
		}
	}
	
	final void start(){
		renderer.start();
	}
	
	final void draw(double delta){
		double 	x=screen.getX(),y=screen.getY(),
				width=screen.getWidth(),height=screen.getHeight();
		
		renderer.setProjection(screen, 100, -100);
		renderer.identity();
		
		if(background!=null){
			renderer.background(background,screen);;
		}
		Artist head = artists.getHead();
		for(;head!=null;head=head.next){
			if(head.isOnScreen(x,y,width,height)){
				renderer.push();
					head.draw(delta,renderer);
				renderer.pop();
			}
		}
	}
	
	final void finish(double delta){
		Particles.update(delta);
		Particles.draw(renderer);
		renderer.finish();
		renderer.addTint(tintR,tintG,tintB,tintA);
	}
	
	public final void add(Artist a){
		artists.add(a);
	}
	
	public final void remove(Artist a){
		artists.remove(a);
	}
	
	final void setIConversion(int width,int height){
		DisplayMetrics dm = new DisplayMetrics();
		GameView.getMain().getWindowManager().getDefaultDisplay().getMetrics(dm);
	    iconversion = screen.getWidth()/((double)width/(double)dm.densityDpi);
	    Log.e("scene","screen width "+ screen.getWidth() +" pixel width " + width + "screen height "
                + screen.getHeight() +" pixel height" + height + " iconversion "+iconversion);
	}
}
