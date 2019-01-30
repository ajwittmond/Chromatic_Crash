package com.flickshot.components.graphics;

import java.util.HashMap;

import com.flickshot.assets.AssetLibrary;
import com.flickshot.assets.textures.SpriteSheet;
import com.flickshot.assets.textures.SpriteSheet.Animation;
import com.flickshot.geometry.Box;
import com.flickshot.geometry.RotatingBox;
import com.flickshot.geometry.RotatingSquare;
import com.flickshot.geometry.TransformableInterface;
import com.flickshot.geometry.Transformation;
import com.flickshot.geometry.Vector2d;
import com.flickshot.geometry.collision.CollisionLib;
import com.flickshot.util.MutableDouble;

public class Sprite extends Artist implements RotatingBox,TransformableInterface {
	public static final double FRAME_RATE = 60;
	public static final double FRAME_TIME = 1/FRAME_RATE;
	
	private SpriteSheet sheet;
	private String sheetName;
	
	public float r=1,g=1,b=1;
	public float tintWeight = 0;
	public float alpha = 1;
	public float z;
	
	private Animation currentAnimation;
	private int currentFrame=0;
	private double currentFrameTime=0;
	
	private int sequencePos;
	
	private final RotatingSquare box;
	
	private boolean animating = false;
	
	
	public Sprite(String textureName){
		this.sheetName = textureName;
		box = new RotatingSquare(new Transformation());
	}
	
	public Sprite(String textureName, Transformation tx){
		this.sheetName = textureName;
		box = new RotatingSquare(tx);
	}
	
	public void setTint(float r, float g, float b, float a){
		this.r = r;
		this.g = g;
		this.b = b;
		this.alpha = a;
	}
	
	public void setTint(float r, float g, float b){
		this.r = r;
		this.g = g;
		this.b = b;
	}
	
	final SpriteSheet getSheet(){
		if(sheet==null || sheet.texture.isDeleted())
			sheet = (SpriteSheet)AssetLibrary.get("texture",sheetName);
		if(sheet==null)
			throw new IllegalStateException("could not find spriteSheet "+sheetName);
		return sheet;
	}
	
	public void setTexture(String name){
		sheetName = name;
		sheet = null;
		animating = false;
		currentFrame = 0;
		currentFrameTime = 0;
		sequencePos = 0;
		currentAnimation = null;
	}
	
	public String getTexture(){
		return sheetName;
	}
	
	
	public void setAnimation(String animation){
		if(sheet==null || sheet.texture.isDeleted())
			sheet = (SpriteSheet)AssetLibrary.get("texture",sheetName);
		if(sheet.hasAnimation(animation)){
			currentAnimation = sheet.getAnimation(animation);
			currentFrame = currentAnimation.startFrame;
		}else{
			throw new IllegalStateException("no such animation: "+animation);
		}
	}
	
	public void play(){
		if(currentAnimation==null)
			throw new IllegalStateException("attempt to play without setting animation");
		animating = true;
	}
	
	public void pause(){
		animating = false;
	}
	
	public boolean isAnimating(){
		return animating;
	}
	
	public void setFrame(int frame){
		currentFrame = frame;
		if(currentAnimation!=null && (frame>currentAnimation.endFrame || frame<currentAnimation.startFrame)){
			animating=false;
			currentAnimation=null;
		}
	}
	
	public int getFrame(){
		return currentFrame;
	}

	@Override
	public boolean isOnScreen(double x, double y, double width, double height) {
		return CollisionLib.boxBox(x,y,width,height,box.getX(),box.getY(),box.getWidth(),box.getHeight());
	}

	@Override
	public void draw(double delta, Renderer2d renderer) {
		update(delta);
		renderer.draw(this);
	}
	
	public void update(double delta){
		if(animating){
			currentFrameTime+=delta;
			while(currentFrameTime>FRAME_TIME){
				currentFrameTime-=FRAME_TIME;
				currentFrame++;
			}
			if(currentFrame>currentAnimation.endFrame){
				if(currentAnimation.looped){
					currentFrame-=currentAnimation.endFrame-currentAnimation.startFrame;
				}else{
					currentFrame=currentAnimation.endFrame;
					animating = false;
				}
			}
		}
	}
	
	public Transformation getTransformation(){
		return box.getTransformation();
	}
	
	@Override
	public double getWidth() {
		return box.getWidth();
	}

	@Override
	public void setWidth(double width) {
		box.setWidth(width);
	}

	@Override
	public double getHeight() {
		return box.getHeight();
	}

	@Override
	public void setHeight(double height) {
		box.setHeight(height);
	}

	@Override
	public double getCX() {
		return box.getCX();
	}

	@Override
	public void setCX(double cx) {
		box.setCX(cx);
	}

	@Override
	public double getCY() {
		return box.getCY();
	}

	@Override
	public void setCY(double y) {
		box.setCY(y);
	}

	@Override
	public double getX() {
		return box.getX();
	}

	@Override
	public void setX(double x) {
		box.setX(x);
	}

	@Override
	public double getY() {
		return box.getY();
	}

	@Override
	public void setY(double y) {
		box.setY(y);
	}

	@Override
	public double getTheta() {
		return box.getTheta();
	}

	@Override
	public void setTheta(double theta) {
		box.setTheta(theta);
	}

	@Override
	public double getBoxWidth() {
		return box.getBoxWidth();
	}

	@Override
	public void setBoxWidth(double boxWidth) {
		box.setBoxWidth(boxWidth);
	}

	@Override
	public double getBoxHeight() {
		return box.getBoxHeight();
	}

	@Override
	public void setBoxHeight(double boxHeight) {
		box.setBoxHeight(boxHeight);
	}

	@Override
	public void setTransformation(Transformation t) {
		box.setTransformation(t);
	}

	@Override
	public void setTransformation(Vector2d translation, Vector2d scale,
			MutableDouble theta) {
		box.setTransformation(translation,scale,theta);
	}

	@Override
	public void setTransformation(double x, double y, double w, double h,
			double theta) {
		box.setTransformation(x,y,w,h,theta);
	}
}
