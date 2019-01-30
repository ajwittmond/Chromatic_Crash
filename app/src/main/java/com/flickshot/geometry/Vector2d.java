package com.flickshot.geometry;

import java.util.List;

import com.flickshot.util.ListAssignable;

public class Vector2d implements ListAssignable<Double>{
	public double x,y;
	
	public Vector2d(){}
	
	public Vector2d(double x, double y){
		this.x = x;
		this.y =y;
	}
	
	public Vector2d set(double x, double y){
		this.x = x;
		this.y = y;
		return this;
	}
	
	public Vector2d set(Vector2d vec){
		x = vec.x;
		y = vec.y;
		return this;
	}
	
	public final void randDir(double mag){
		double theta = Math.random()*Math.PI*2;
		this.x = Math.cos(theta)*mag;
		this.y = Math.sin(theta)*mag;
	}
	
	public final void mul(double f){
		this.x*=f;
		this.y*=f;
	}
	
	public final void mul(double f, Vector2d out){
		out.x = x*f;
		out.y = y*f;
	}
	
	public final void add(Vector2d v){
		this.x+=v.x;
		this.y+=v.y;
	}
	
	public final void add(double x, double y){
		this.x+=x;
		this.y+=y;
	}
	
	public final void add(Vector2d v,Vector2d out){
		out.x = x+v.x;
		out.x = y+v.y;
	}
	
	public final void add(double x, double y, Vector2d out){
		out.x=this.x+x;
		out.y=this.y+y;
	}
	
	public final void sub(Vector2d v){
		this.x-=v.x;
		this.y-=v.y;
	}
	
	public final void sub(double x, double y){
		this.x-=x;
		this.y-=y;
	}
	
	public final void sub(Vector2d v,Vector2d out){
		out.x=x-v.x;
		out.y=y-v.y;
	}
	
	public final void sub(double x, double y,Vector2d out){
		out.x=this.x-x;
		out.y=this.y-y;
	}
	
	public final double dot(Vector2d v){
		return x*v.x + y*v.y;
	}
	
	public final double dot(double x, double y){
		return this.x*x + this.y*y;
	}
	
	public final void cross1(double a){
		double t = x;
		x = a*y;
		y = -a*t;
	}
	
	public final void cross2(double a){
		double t = x;
		x = -a*y;
		y = a*t;
	}
	
	public final double cross(Vector2d v){
		return (x*v.y) - (y*v.x); 
	}
	
	public final double cross(double vx, double vy){
		return (x*vy) - (y*vx);
	}
	
	public final void normalize(){
		double mag = getMag();
		if(mag!=0){
			this.x/=mag;
			this.y/=mag;
		}
	}
	
	public final double dist(Vector2d vec){
		return Math.sqrt(Math.pow(x-vec.x,2)+Math.pow(y-vec.y,2));
	}
	
	public final double distSquared(Vector2d vec){
		return Math.pow(x-vec.x,2)+Math.pow(y-vec.y,2);
	}
	
	public final double dist(double x, double y){
		return Math.sqrt(Math.pow(this.x-x,2)+Math.pow(this.y-y,2));
	}
	
	public final double distSquared(double x, double y){
		return Math.sqrt(Math.pow(this.x-x,2)+Math.pow(this.y-y,2));
	}
	
	public final double getDir(){
		return Math.atan2(y, x);
	}
	
	public final void setDir(double theta){
		double d=Math.sqrt(x*x + y*y);
		double c = Math.cos(theta),s=Math.sin(theta);
		x = d*c;
		y = d*s;
	}
	
	public final double getMag(){
		if(x==0 && y==0)return 0;
		return Math.sqrt(x*x + y*y);
	}
	
	public final double getMagSquared(){
		if(x==0 && y==0)return 0;
		return x*x + y*y;
	}
	
	public final void setMag(double l){
		if(x==0 && y==0){
			x=l;
		}else{
			double f = l/getMag();
			x*=f;
			y*=f;
		}
	}
	
	public final void project(Vector2d v){
		double u = v.dot(this)/v.dot(v);
		x = v.x*u;
		y = v.y*u;
	}
	
	public final void neg(){
		x = -x;
		y = -y;
	}
	
	public final void perp(){
		double temp = x;
		x = -y;
		y = temp;
	}
	
	public final void perp(Vector2d out){
		double temp = x;//in case out is this
		out.x = -y;
		out.y = temp;
	}
	
	public final double getDir(Vector2d vec){
		return Math.atan2(vec.y-y,vec.x-x);
	}
	
	public final boolean hasNaN(){
		return Double.isNaN(x) || Double.isNaN(y);
	}
	
	@Override
	public final void assign(int offset,List<Double> l){
		x = l.get(offset);
		y = l.get(offset+1);
	}
	
	@Override
	public final String toString(){
		return "vec2("+x+","+y+")";
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof Vector2d ){
			Vector2d v = (Vector2d)o;
			return v.x==x && v.y==y; // strict floating point comparison
		}else{
			return false;
		}
	}
	
	public static final double dot(Vector2d a, Vector2d b){
		return (a.x*b.x) + (a.y*b.y);
	}
	
	public static final double dot(double xa, double ya, double xb, double yb){
		return (xa*xb) + (ya*yb);
	}
	
	public static final double cross(double ax, double ay , double bx, double by){
		return (ax*by) - (ay*bx);
	}
	
	public final static double cross(int offseta, double[] a,int offsetb, double[] b){
		return (a[offseta]*b[offsetb+1]) - (a[offseta+1]*b[offsetb]);
	}
	
	public static final double dist(double x1, double y1, double x2, double y2){
		return Math.sqrt(Math.pow(x1-x2,2)+Math.pow(y1-y2,2));
	}
	
	public static final double dist(Vector2d a, Vector2d b){
		return Math.sqrt(Math.pow(a.x-b.x,2)+Math.pow(a.y-b.y,2));
	}
	
	public static final double distSquared(double x1, double y1, double x2, double y2){
		double a = x1-x2;
		double b = y1-y2;
		return (a*a)+(b*b);
	}
	
	public static final double distSquared(Vector2d a, Vector2d b){
		double u = a.x-b.x;
		double v = a.y-b.y;
		return (u*u)+(v*v);
	}
	
	public static final double mag(double x, double y){
		return Math.sqrt(x*x + y*y);
	}
	
	public static final double magSquared(double x, double y){
		return x*x + y*y;
	}
}
