package com.flickshot.geometry;

public class Square implements Box{
	public double x,y,width,height;
	
	public Square(){
	}
	
	public Square(double x,double y,double width,double height){
		this.x = x;
		this.y = y;
		this.height = height;
		this.width = width;
	}
	
	public boolean collision(Square s){
		return boxCollision(getX(),getY(),getWidth(),getHeight(),s.getX(),s.getY(),s.getWidth(),s.getHeight());
	}
	
	public boolean collision(Box b){
		return boxCollision(this,b);
	}
	
	public boolean collision(double x, double y){
		return x>getX() && x<getX()+getWidth() && y>getY() && y<getY()+getHeight();
	}
	
	public boolean collision(double x, double y, double width, double height){
		return  boxCollision(getX(),getY(),getWidth(),getHeight(),x,y,width,height);
	}
	
	public void set(double x, double y, double width, double height){
		this.x = x;
		this.y = y;
		this.height = height;
		this.width = width;
	}

	@Override
	public double getWidth() {
		return width;
	}

	@Override
	public void setWidth(double width) {
		this.width = width;
	}

	@Override
	public double getHeight() {
		return height;
	}

	@Override
	public void setHeight(double height) {
		this.height = height;
	}

	@Override
	public double getX() {
		return this.x;
	}

	@Override
	public void setX(double x) {
		this.x =x;
	}

	@Override
	public double getY(){
		return this.y;
	}

	@Override
	public void setY(double y) {
		this.y = y;
	}
	
	
	public double getCX(){
		return x+(width/2);
	}
	
	public void setCX(double cx){
		this.x = cx-(width/2);
	}
	
	public double getCY(){
		return y+(height/2);
	}
	
	public void setCY(double cy){
		y = cy-(height/2);
	}
	
	public static boolean boxContains(Box b, double x, double y){
		return x>b.getX() && x<b.getX()+b.getWidth() && y>b.getY() && y<b.getY()+b.getHeight();
	}
	
	public static boolean boxContains(double bx, double by, double bw, double bh, double x, double y){
		return x>bx && x<bx+bw && y>by && y<by+bh;
	}
	
	public static boolean boxCollision(Box a, Box b){
		return (a.getX()+a.getWidth() >b.getX() ) && (a.getX() <b.getX()+b.getWidth()) && (a.getY()+ a.getHeight() > b.getY()) && (a.getY() < b.getY() + b.getHeight());
	}
	
	public static boolean boxCollision(double x1,double y1,double width1,double height1,double x2,double y2,double width2,double height2){
		return (x1+width1 >x2 ) && (x1 <x2+width2) && (y1+ height1 > y2) && (y1 < y2 + height2);
	}
	
}
