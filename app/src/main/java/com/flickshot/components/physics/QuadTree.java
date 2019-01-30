package com.flickshot.components.physics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.flickshot.geometry.Bounded;
import com.flickshot.geometry.Box;
import com.flickshot.geometry.collision.CollisionLib;

public class QuadTree<T extends Bounded>{
	public QuadTree<T> nw = null;
	public QuadTree<T> ne = null;
	public QuadTree<T> sw = null;
	public QuadTree<T> se = null;
	
	public double x,y,width,height;
	
	public final ArrayList<T> members = new ArrayList<T>();
	
	private int limit = 4;
	
	
	public QuadTree(double x, double y, double width, double height){
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public QuadTree(double x, double y, double width, double height, int limit){
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.limit = limit;
	}
	
	public final boolean add(T a){
		return add(a,a.getBounds());
	}
	
	private final boolean add(T a,Box bounds){
		if(check(bounds)){
			if(nw!=null){
				if(nw.add(a,bounds))return true;
				if(ne.add(a,bounds))return true;
				if(sw.add(a,bounds))return true;
				if(se.add(a,bounds))return true;
			}
			members.add(a);
			if(nw==null && members.size()>limit) split();
			return true;
		}
		return false;
	}
	
	public final Collection<T> get(Box a,Collection<T> c){
		if(CollisionLib.boxBox(a.getX(), a.getY(), a.getWidth(), a.getHeight(), x, y, width, height)){
			for(int i = 0; i<members.size(); i++){
				T b = members.get(i);
				if(CollisionLib.boxBox(a, b.getBounds())){
					c.add(b);
				}
			}
			if(nw!=null){
				nw.get(a, c);
				ne.get(a, c);
				sw.get(a, c);
				se.get(a, c);
			}
		}
		return c;
	}
	
	public final Collection<T> get(double bx,double by, double bwidth, double bheight,Collection<T> c){
		if(CollisionLib.boxBox(bx, by, bwidth, bheight, x, y, width, height)){
			for(int i = 0; i<members.size(); i++){
				T bd = members.get(i);
				Box b = bd.getBounds();
				if(CollisionLib.boxBox(bx, by, bwidth, bheight, b.getX(), b.getY(), b.getWidth(), b.getHeight())){
					c.add(bd);
				}
			}
			if(nw!=null){
				nw.get(bx, by, bwidth, bheight, c);
				ne.get(bx, by, bwidth, bheight, c);
				sw.get(bx, by, bwidth, bheight, c);
				se.get(bx, by, bwidth, bheight, c);
			}
		}
		return c;
	}
	
	public final void clear(){
		members.clear();
		if(nw!=null){
			nw.clear();
			ne.clear();
			sw.clear();
			se.clear();
		}
	}
	
	public final void deconstruct(){
		members.clear();
		nw=null;
		ne=null;
		sw=null;
		se=null;
	}
	
	private final boolean check(Box b){
		double x0 = b.getX();
		double y0 = b.getY();
		double width0 = b.getWidth();
		double height0 = b.getHeight();
		return x0>x && x0+width0<x+width && y0>y && y0+height0<y+height; 
	}
	
	private final void split(){
		double w = width/2;
		double h = height/2;
		nw = new QuadTree<T>(x,y+h,w,h,limit);
		ne = new QuadTree<T>(x+w,y+h,w,h,limit);
		sw = new QuadTree<T>(x,y,w,h,limit);
		se = new QuadTree<T>(x+w,y,w,h,limit);
		
		Iterator<T> iter = members.iterator();
		while(iter.hasNext()){
			T a = iter.next();
			if(nw.add(a))
				iter.remove();
			else if(ne.add(a))
				iter.remove();
			else if(sw.add(a))
				iter.remove();
			else if(se.add(a))
				iter.remove();
		}
	}
}
