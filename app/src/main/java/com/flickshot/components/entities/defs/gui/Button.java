package com.flickshot.components.entities.defs.gui;

import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.graphics.Artist;
import com.flickshot.components.graphics.Renderer2d;
import com.flickshot.components.input.TouchEvent;
import com.flickshot.geometry.Square;
import com.flickshot.scene.Updater.UpdateEvent;
import com.flickshot.util.Action;

public class Button extends GuiElement{
	public static final String ENTITY_NAME = "Button";
	private Action action;
	
	public String text;
	
	float rb=0.8f,gb=0.8f,bb=0.8f,ab=1;
	float rborder=0.8f,gborder=0.8f,bborder=0.8f,aborder=1;
	double borderWidth = 0;
	float rt=0,gt=0,bt=0,at=1;
	public float textSize = 64;
	
	public Button(){
		setArtist(new Artist(){
			@Override
			public boolean isOnScreen(double screenX, double screenY,
					double screenWidth, double screenHeight) {
				return Square.boxCollision(getX(),getY(),getWidth(),getHeight(),screenX,screenY,screenWidth,screenHeight);
			}

			@Override
			public void draw(double delta, Renderer2d renderer) {
				drawButton(delta,renderer);
			}
			
		});
	}
	
	static final int[] yalign = new int[]{Renderer2d.TOP,Renderer2d.CENTER,Renderer2d.BOTTOM};
	static final int[] xalign = new int[]{Renderer2d.LEFT,Renderer2d.CENTER,Renderer2d.RIGHT};
	double alignDt = 0;
	@Override
	public void update(UpdateEvent evt){
		super.update(evt);
		alignDt= (alignDt+evt.getDelta())%3;
	}
	
	@Override
	public void init(double x, double y){
		super.init(x,y);
		textSize = 64;
		rb=0.8f;gb=0.8f;bb=0.8f;ab=1;
		rt=0;gt=0;bt=0;at=1;
	}
	
	@Override
	public void unload(){
		super.unload();
	}
	
	public void setColor(float r, float g, float b, float a){
		rb = r; gb = g; bb =b; ab = a;
	}

	public void setTextColor(float r, float g, float b, float a){
		rt = r; gt = g; bt = b; at =a;
	}
	
	public void setBorderColor(float r, float g, float b, float a){
		rborder = r; gborder = g; bborder =b; aborder = a;
	}
	
	public void setBorderWidth(double w){
		borderWidth = w;
	}

	@Override
	public void onUp(TouchEvent evt) {
		if(action!=null) action.doAction();
	}
	
	public final void setAction(Action action){
		this.action = action;
	}
	
	public void doAction(){
		if(action!=null)action.doAction();
	}
	
	protected void drawButton(double delta, Renderer2d renderer){
		renderer.setDrawMode(Renderer2d.FILL);
		renderer.translate(getX()+getWidth()/2,getY()+getHeight()/2,z);
		renderer.color(rborder,gborder,bborder,aborder);
		renderer.shape(Renderer2d.SQUARE,getWidth(),getHeight());
		renderer.color(rb,gb,bb,ab);
		renderer.shape(Renderer2d.SQUARE,getWidth()-borderWidth,getHeight()-borderWidth);
		
		if(text!=null && !text.equals("")){
			renderer.color(rt,gt,bt,at);
			double scale = textSize/renderer.fontSize();
            double twidth = renderer.textWidth(text);
            if(twidth*scale>getWidth()){
                scale = getWidth()/twidth;
            }
			renderer.scale(scale,scale);
			renderer.align(Renderer2d.CENTER,Renderer2d.CENTER);
			renderer.text(text);
		}
	}

	public static EntityStateFactory getFactory(){
		return new EntityStateFactory(){
			@Override
			public EntityState construct() {
				return new Button();
			}

			@Override
			public Class<? extends EntityState> getType() {
				return Button.class;
			}
		};
	}
}
