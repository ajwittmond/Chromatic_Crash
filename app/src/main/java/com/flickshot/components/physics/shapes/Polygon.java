package com.flickshot.components.physics.shapes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collection;

import android.util.Log;

import com.flickshot.components.physics.PhysShape;
import com.flickshot.geometry.Box;
import com.flickshot.geometry.Matrix2d;
import com.flickshot.geometry.RotatingBox;
import com.flickshot.geometry.RotatingSquare;
import com.flickshot.geometry.Transformation;
import com.flickshot.geometry.Vector2d;

public class Polygon implements PhysShape{
	private static final Vector2d misc = new Vector2d();
	
	private final FloatBuffer drawBuffer;
	private final double[] vertices;//this array stores the untransformed vertices
	private final double[] normals;
	private double cx,cy;
	
	public Polygon(int offset,int size,int number,double[] verts,double x, double y){
		if(number<3 || offset<0 || size<1)throw new IllegalArgumentException();
		
		cx = x;
		cy = y;
		
		vertices = new double[number*2];
		normals = new double[number*2];
		
		//set vertices
		for(int i = 0; i<number; i++){
			int ia = i*2;
			int ib = offset+(size*i);
			vertices[ib] = verts[ib];
			vertices[ib+1] = verts[ib+1];
		}
		
		checkOrder();
		
//		//get correct cm
//		double xsum=0,ysum=0;
//		for(int i = 0; i<vertices.length;i+=2){
//			xsum+=vertices[i];
//			ysum+=vertices[i+1];
//		}
//		xsum/=(double)(vertices.length/2);
//		ysum/=(double)(vertices.length/2);
//		
//		//translate to new origin
//		for(int i = 0; i<vertices.length;i+=2){
//			vertices[i]-=xsum-cx;
//			vertices[i+1]-=ysum-cy;
//		}
		
		//set float buffer
		ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length*8);
		bb.order(ByteOrder.nativeOrder());
		drawBuffer =bb.asFloatBuffer();
		
		for(double a : vertices) drawBuffer.put((float)a);
		drawBuffer.position(0);
		
		setNormals();
	}
	
	private void setNormals(){
		for(int i = 0; i<vertices.length; i+=2){
			double u = vertices[(i+2)%vertices.length]-vertices[i];
			double v = vertices[(i+3)%vertices.length]-vertices[i+1];
			double mag = Vector2d.mag(u,v);
			normals[i] = v/mag;
			normals[i+1] = -(u/mag);
		}
	}
	
	//makes sure verts are in CCW order
	private void checkOrder(){
		double a = getArea();
		if(a<0){//CC order
			for(int i = 0; i<vertices.length/2; i++){
				int ia = i*2;
				int ib = 2*((vertices.length/2)-1-i);
				double x = vertices[ia];
				double y = vertices[ia+1];
				vertices[ia] = vertices[ib];
				vertices[ia+1] = vertices[ib+1];
				vertices[ib]=x;
				vertices[ib+1]=y;
			}
		}
	}
	
	public double getCX(){
		return cx;
	}
	
	public double getCY(){
		return cx;
	}
	
	public Vector2d getSupport(Vector2d axis,Vector2d out){
		double best = Double.NEGATIVE_INFINITY;
		for(int i = 0; i<vertices.length; i+=2){
			double p = axis.dot(vertices[i],vertices[i+1]);
			if(p>best){
				best = p;
				out.set(vertices[i],vertices[i+1]);
			}
		}
		return out;
	}
	
	public Vector2d getSupport(double ax, double ay,Vector2d out){
		double best = Double.NEGATIVE_INFINITY;
		for(int i = 0; i<vertices.length; i+=2){
			double p = Vector2d.dot(ax,ay,vertices[i],vertices[i+1]);
			if(p>best){
				best = p;
				out.set(vertices[i],vertices[i+1]);
			}
		}
		return out;
	}
	
	public void getTransformedValues(Transformation t,Collection<Double> verts,Collection<Double> norms){
		double c = Math.cos(t.theta.val),s=Math.sin(t.theta.val);
		double u = (vertices[0] * t.scale.x);
		double v = (vertices[1] * t.scale.y);
		double fx = (u*c - v*s)+t.translation.x;
		double fy = (u*s + v*c)+t.translation.y;
		double prevx = fx;
		double prevy = fy;
		
		verts.add(fx);
		verts.add(fy);
		
		for(int i = 2; i<vertices.length; i+=2){
			u = (vertices[i] * t.scale.x);
			v = (vertices[i+1] * t.scale.y);
			double x = (u*c - v*s)+t.translation.x;
			double y = (u*s + v*c)+t.translation.y;
			verts.add(x);
			verts.add(y);
			double nx = y-prevy;
			double ny = -(x-prevx);
			double m = Vector2d.mag(nx,ny);
			norms.add(nx/m);
			norms.add(ny/m);
			prevx = x;
			prevy = y;
		}
		
		double nx = fy-prevy;
		double ny = -(fx-prevx);
		double m = Vector2d.mag(nx,ny);
		norms.add(nx/m);
		norms.add(ny/m);
	}
	
	public int getTransformedValues(Transformation t, double[] verts, double[] norms){
		double c = Math.cos(t.theta.val),s=Math.sin(t.theta.val);
		double u = (vertices[0] * t.scale.x);
		double v = (vertices[1] * t.scale.y);
		double fx = (u*c - v*s)+t.translation.x;
		double fy = (u*s + v*c)+t.translation.y;
		double prevx = fx;
		double prevy = fy;
		int i = 0;
		verts[i++] = fx;
		verts[i++] = fy;
		
		for(; i<vertices.length; i+=2){
			u = (vertices[i] * t.scale.x);
			v = (vertices[i+1] * t.scale.y);
			double x = (u*c - v*s)+t.translation.x;
			double y = (u*s + v*c)+t.translation.y;
			verts[i] = x;
			verts[i+1] = y;
			double nx = y-prevy;
			double ny = -(x-prevx);
			double m = Vector2d.mag(nx,ny);
			norms[i-2] = nx/m;
			norms[i-1] = ny/m;
			prevx = x;
			prevy = y;
		}
		
		double nx = fy-prevy;
		double ny = -(fx-prevx);
		double m = Vector2d.mag(nx,ny);
		norms[i-2] = nx/m;
		norms[i-1] = ny/m;
		return i;
	}

    public void setVertex(int i, double x, double y){
        vertices[i*2] = x;
        vertices[i*2+1] = y;
    }
	
	public int getTransformedValues(Matrix2d m,double[] verts, double[] norms){
		m.transform(0,2,verts,0,2,vertices,vertices.length/2);
		for(int i = 0; i<vertices.length; i+=2){
			double u = verts[(i+2)%vertices.length]-verts[i];
			double v = verts[(i+3)%vertices.length]-verts[i+1];
			double mag = Vector2d.mag(u,v);
			norms[i]=v/mag;
			norms[i+1]=-(u/mag);
		}
		return vertices.length;
	}
	
	public Vector2d getPoint(int i,Vector2d v){
		i*=2;
		if(v==null){
			return new Vector2d(vertices[i],vertices[i+1]);
		}else{
			v.set(vertices[i],vertices[i+1]);
			return v;
		}
	}
	
	public double[] getPoint(int i,int offset,double[] v){
		i*=2;
		v[offset] = vertices[i];
		v[offset+1] = vertices[i+1];
		return v;
	}
	
	public double[] getVertices(int offset,int step,double[] v){
		for(int i = 0; i<vertices.length/2; i++){
			int ia = i*2;
			int ib = offset+(i*step);
			v[ib] = vertices[ia];
			v[ib+1] = vertices[ia+1];
		}
		return v;
	}
	
	public <T extends Collection<Double>> T getVertices(T c){
		for(int i = 0; i<vertices.length; i++) c.add(vertices[i]);
		return c;
	}
	
	public Vector2d getNormal(int i,Vector2d v){
		i*=2;
		if(v==null){
			return new Vector2d(normals[i],normals[i+1]);
		}else{
			v.set(normals[i],normals[i+1]);
			return v;
		}
	}
	
	public double[] getNormal(int i,int offset,double[] v){
		i*=2;
		v[offset] = normals[i];
		v[offset+1] = normals[i+1];
		return v;
	}
	
	public double[] getNormals(int offset,int step,double[] v){
		for(int i = 0; i<normals.length/2; i++){
			int ia = i*2;
			int ib = offset+(i*step);
			v[ib] = normals[ia];
			v[ib+1] = normals[ia+1];
		}
		return v;
	}
	
	public <T extends Collection<Double>> T getNormals(T c){
		for(int i = 0; i<normals.length; i++) c.add(normals[i]);
		return c;
	}
	
	public final FloatBuffer getDrawBuffer(){
		return drawBuffer;
	}
	
	public int getNumOfVerts(){
		return vertices.length/2;
	}

	
	private static final ArrayList<Double> vertA = new ArrayList<Double>();
	private static final ArrayList<Double> normA = new ArrayList<Double>();
	@Override
	public double getMass(double density,Transformation tx) {
		return (Double.isInfinite(density)) ? Double.POSITIVE_INFINITY : getArea(tx)*density;
	}
	
	@Override
	public double getInertia(double density,Transformation tx) {
		if(Double.isInfinite(density)) return Double.POSITIVE_INFINITY;
		double mass = getMass(density,tx);
		double inertia = 0;
		for(int i = 0; i<vertA.size()-2;i+=2){
			double b = Vector2d.dist(0,0,vertA.get(i),vertA.get(i+1));
			double u = Vector2d.dot(vertA.get(i),vertA.get(i+1),vertA.get(i+2),vertA.get(i+3))/Vector2d.dot(vertA.get(i),vertA.get(i+1),vertA.get(i),vertA.get(i+1));
			double h = Vector2d.dist(vertA.get(i+2),vertA.get(i+3),vertA.get(i)*u,vertA.get(i+1)*u);
			double a = b - Vector2d.mag(vertA.get(i)*u,vertA.get(i+1)*u);
			
			double d = Vector2d.dist(0,0,(vertA.get(i)+vertA.get(i+2))/3,(vertA.get(i+1)+vertA.get(i+3))/3);
			
			double in = (b*b*b*h)-(b*b*h*a)+(b*h*a*a)+(b*h*h*h);
			in/=36.0;
			
			inertia += in + mass*d*d;
		}
		return inertia;
	}
	
	public final double getArea(Transformation tx){
		vertA.clear();
		normA.clear();
		getTransformedValues(tx,vertA,normA);
		double a = 0;
		for(int i = 0; i<vertA.size(); i+=2){
			a+= Vector2d.cross(vertA.get(i),vertA.get(i+1),vertA.get((i+2)%vertA.size()),vertA.get((i+3)%vertA.size()));
		}
		return a;
	}

	public final double getArea(){
		double a = 0;
		for(int i = 0; i<vertices.length; i+=2){
			a+= Vector2d.cross(i,vertices,(i+2)%vertices.length,vertices);
		}
		return a;
	}
	
	@Override
	public Box calcBounds(Matrix2d m, Box out) {
		misc.set(vertices[0],vertices[1]);
		m.transform(misc);
		double minx=misc.x;
		double miny=misc.y;
		double maxx=misc.x;
		double maxy=misc.y;
		for(int i = 2; i<vertices.length; i+=2){
			misc.set(vertices[i],vertices[i+1]);
			m.transform(misc);
			if(misc.x<minx)minx=misc.x;
			if(misc.x>maxx)maxx=misc.x;
			if(misc.y<miny)miny=misc.y;
			if(misc.y>maxy)maxy=misc.y;
		}
		out.setX(minx);
		out.setY(miny);
		out.setWidth(maxx-minx);
		out.setHeight(maxy-miny);
		return null;
	}
	
	@Override
	public Vector2d getCOM(Vector2d out){
		out.set(cx,cy);
		return out;
	}
	
	@Override
	public boolean pointIsInside(double x, double y){
		for(int i = 0; i<vertices.length-2; i+=2)
			if(!check(x,y,vertices[i],vertices[i+1],vertices[i+2],vertices[i+3]))
				return false;
		if(!check(x,y,vertices[vertices.length-2],vertices[vertices.length-1],vertices[1],vertices[2]))
			return false;
		return true;
	}
	
	private boolean check(double x , double y, double px1, double py1, double px2, double py2){
		px1-=x;
		py1-=y;
		px2-=x;
		py2-=y;
		return ((px2*py1)-(px1*py2))>0;
	}
}
