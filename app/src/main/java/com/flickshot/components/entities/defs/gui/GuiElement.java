package com.flickshot.components.entities.defs.gui;

import java.util.ArrayList;


import com.flickshot.components.entities.defs.VisibleEntity;
import com.flickshot.components.input.TouchEvent;
import com.flickshot.components.input.TouchListener;
import com.flickshot.components.input.TouchManager;
import com.flickshot.geometry.Box;
import com.flickshot.geometry.Square;

public abstract class GuiElement extends VisibleEntity implements Box{
	private final TouchListener tl;
	
	private boolean touched = false;
	
	private GuiElement parent = null;
	
	private boolean hasInput = true;
	
	protected final ArrayList<GuiElement> children = new ArrayList<GuiElement>();
	
	double xRelative,yRelative,width,height;
	
	public double z = 0;
	
	public GuiElement(){
		tl = new TouchListener(){

			@Override
			public void onDown(TouchEvent evt) {
				if(Square.boxContains(getThis(),TouchManager.x(),TouchManager.y())){
					doOnTouch(evt);
				}
			}

			@Override
			public void onMove(TouchEvent evt) {
				if(touched){
					doOnMove(evt);
					touched = Square.boxContains(getThis(),TouchManager.x(),TouchManager.y());
				}
			}

			@Override
			public void onUp(TouchEvent evt) {
				if(touched){
					doOnUp(evt);
					touched = false;
				}
			}
			
		};
	}
	
	private GuiElement getThis(){
		return this;
	}
	
	public void setRecievesInput(boolean input){
		if(parent==null){
			if(hasInput && !input){
				TouchManager.remove(tl);
			}else if(!hasInput && input){
				TouchManager.add(tl);
			}
		}
		hasInput = input;
	}
	
	public void setAsChild(GuiElement parent){
		TouchManager.remove(tl);
		this.parent = parent;
		parent.children.add(this);
		z = parent.z-1;
	}
	
	public void removeFromParent(){
		if(parent!=null){
			parent.children.remove(this);
			parent = null;
			TouchManager.add(tl);
		}
	}
	
	public void addChild(GuiElement child){
		child.setAsChild(this);
	}
	
	public void removeChild(GuiElement child){
		if(children.contains(child))child.removeFromParent();
	}
	
	public final boolean touched(){
		return touched;
	}
	
	@Override
	public void init(double x,double y){
		super.init(x,y);
		TouchManager.add(tl);
	}
	
	@Override
	public void destroy(){
		super.destroy();
		if(parent==null)TouchManager.remove(tl);
		parent = null;
		touched = false;
		while(!children.isEmpty())children.remove(0).kill();
		children.clear();
	}
	
	@Override
	public void unload(){
		super.unload();
		if(parent==null)TouchManager.remove(tl);
		parent = null;
		touched = false;
		children.clear();
	}
	
	public double getX(){
		if(parent==null){
			return xRelative;
		}else{
			return xRelative+parent.getX();
		}
	}
	
	public void setX(double x){
		if(parent==null){
			xRelative = x;
		}else{
			xRelative = x - parent.getX();
		}
	}
	
	
	public double getY(){
		if(parent==null){
			return yRelative;
		}else{
			return yRelative+parent.getY();
		}
	}
	public void setY(double y){
		if(parent==null){
			yRelative = y;
		}else{
			yRelative = y - parent.getY();
		}
	}
	
	public double getWidth(){
		return width;
	}
	
	public void setWidth(double width){
		this.width = width;
	}
	
	public double getHeight(){
		return height;
	}
	
	public void setHeight(double height){
		this.height = height;
	}
	
	public double getCX(){
		return getX()+width/2;
	}
	
	public void setCX(double cx){
		setX(cx-width/2);
	}
	
	public double getCY(){
		return getY()+height/2;
	}
	
	public void setCY(double cy){
		setY(cy-height/2);
	}
	
	public double getXRelative(){
		return xRelative;
	}
	
	public void setXRelative(double x){
		xRelative = x;
	}
	
	public double getYRelative(){
		return yRelative;
	}
	
	public void setYRelative(double y){
		yRelative = y;
	}
	
	public double getCXRelative(){
		return xRelative+width/2;
	}
	
	public void setCXRelative(double cx){
		this.xRelative = cx-width/2;
	}
	
	public double getCYRelative(){
		return yRelative+height/2;
	}
	
	public void setCYRelative(double cy){
		this.yRelative = cy-height/2;
	}
	
	public void setPosition(double x, double y){
		setX(x);setY(y);
	}
	
	public void setPositionRelative(double x, double y){
		xRelative = x; yRelative = y;
	}
	
	public void setDimensions(double w, double h){
		width = w; height = h;
	}
	
	
	private void doOnTouch(TouchEvent evt){
		if(hasInput){
			if(Square.boxContains(getThis(),TouchManager.x(),TouchManager.y())){
				touched = true;
				onTouch(evt);
				for(GuiElement child: children){
					child.doOnTouch(evt);
				}
			}
		}
	}
	
	private void doOnMove(TouchEvent evt){
		if(hasInput){
			if(touched){
				onMove(evt);
				touched = Square.boxContains(getThis(),TouchManager.x(),TouchManager.y());
				for(GuiElement child: children){
					child.doOnMove(evt);
				}
			}
		}
	}
	
	private void doOnUp(TouchEvent evt){
		if(hasInput){
			if(touched){
				onUp(evt);
				touched = false;
				for(GuiElement child: children){
					child.doOnUp(evt);
				}
			}
		}
	}
	/**
	 * called on touch event in bounds
	 */
	public void onTouch(TouchEvent evt){};
	
	/**
	 * called if previously touched
	 */
	public void onMove(TouchEvent evt){};
	
	/**
	 * called if previously touched
	 */
	public void onUp(TouchEvent evt){};
}
