package com.flickshot.components.physics;

import java.util.ArrayList;
import java.util.Arrays;

import android.graphics.drawable.shapes.Shape;
import android.util.Log;

import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.defs.CircleState;
import com.flickshot.components.physics.shapes.Circle;
import com.flickshot.components.physics.shapes.Polygon;
import com.flickshot.geometry.Bounded;
import com.flickshot.geometry.Box;
import com.flickshot.geometry.Matrix2d;
import com.flickshot.geometry.RotatingBox;
import com.flickshot.geometry.Square;
import com.flickshot.geometry.Transformation;
import com.flickshot.geometry.Vector2d;

public class Collider extends Mover implements Bounded{
	private final Square square = new Square();
	
	public EntityState state;
	
	public boolean doBoundsCheck = false;
	public boolean doPhysics = false;
	
	public PhysMaterial material;
	final ArrayList<PhysShape> shapes;

    private final ArrayList<Square> shapeBounds = new ArrayList<Square>();
	
	private Matrix2d current;
	
	int scene = 0;
	
	public boolean canRotate = true;
	
	public Collider(){
		shapes = new ArrayList<PhysShape>();
	}
	
	public Collider(double x, double y,PhysShape shape,PhysMaterial m,EntityState state){
		super(new Transformation(x,y,1,1,0),new MassData(shape.getMass(m.density,new Transformation(x,y,1,1,0)),shape.getInertia(m.density,new Transformation(x,y,1,1,0))));
		shapes = new ArrayList<PhysShape>();
		shapes.add(shape);
		material = m;
		this.state = state;
	}
	
	public Collider(double x, double y,PhysShape shape,PhysMaterial m,EntityState state,boolean doBoundsCheck, boolean doPhysics){
		super(new Transformation(x,y,1,1,0),new MassData(shape.getMass(m.density,new Transformation(x,y,1,1,0)),shape.getInertia(m.density,new Transformation(x,y,1,1,0))));
		shapes = new ArrayList<PhysShape>();
		shapes.add(shape);
		material = m;
		this.state = state;
		this.doBoundsCheck = doBoundsCheck;
		this.doPhysics = doPhysics;
	}
	
	public Collider(double x, double y,ArrayList<PhysShape> shapes,PhysMaterial m,EntityState state){
		super(new Transformation(x,y,1,1,0),generateMassData(shapes,null,m.density,new Transformation(x,y,1,1,0)));
		this.shapes = shapes;
		material = m;
		this.state = state;
	}
	
	public Collider(double x, double y,PhysShape[] shapes,PhysMaterial m,EntityState state){
		super(new Transformation(x,y,1,1,0),generateMassData(new ArrayList<PhysShape>(Arrays.asList(shapes)),null,m.density,new Transformation(x,y,1,1,0)));
		this.shapes = new ArrayList<PhysShape>(Arrays.asList(shapes));
		material = m;
		this.state = state;
	}
	
	
	public Collider(double x, double y,ArrayList<PhysShape> shapes,PhysMaterial m,EntityState state,boolean doBoundsCheck, boolean doPhysics){
		super(new Transformation(x,y,1,1,0),generateMassData(shapes,null,m.density,new Transformation(x,y,1,1,0)));
		this.shapes = shapes;
		material = m;
		this.state = state;
		this.doBoundsCheck = doBoundsCheck;
		this.doPhysics = doPhysics;
	}
	
	private final static Vector2d pos = new Vector2d();
	private final static MassData generateMassData(ArrayList<PhysShape> shapes,ArrayList<Mover> children,double density,Transformation tx){
		double inertia=0;
		double mass=0;
		
		for(int i = 0; i<shapes.size(); i++){
			mass+=shapes.get(i).getMass(density,tx);
		}
		if(children!=null){
			for(Mover child: children){
				if(child instanceof Collider){
					Collider c = (Collider)child;
					c.massData = generateMassData(c.shapes,c.children,c.material.density,c.tx);
					mass+=c.massData.mass;
				}
			}
		}
		
		for(int i = 0; i<shapes.size(); i++){
			inertia+=shapes.get(i).getInertia(density,tx) + mass*shapes.get(i).getCOM(pos).getMagSquared();
		}
		if(children!=null){
			for(Mover child: children){
				if(child instanceof Collider){
					Collider c = (Collider)child;
					//this is incorrect
					inertia+=c.massData.inertia;
				}
			}
		}
		
		return new MassData(mass,inertia);
	}
	
	public final Scene getScene(){
		return Physics.getScene(scene);
	}
	
	public final void addGravity(){
		force.add(getScene().gravity.x*massData.mass,getScene().gravity.y*massData.mass);
	}
	
	public void resetMassData(){
		if(parent==null || !(parent instanceof Collider))
			massData = generateMassData(shapes,children,material.density,tx);
		else
			((Collider)parent).resetMassData();
	}
	
	public void bind(){
		Physics.add(this);
	}
	
	public void unbind(){
		Physics.remove(this);
	}
	
	final void doOnCollision(Manifold m){
		if(parent!=null && parent instanceof Collider)
			((Collider)parent).doOnCollision(m);
		onCollision(m);
	}

    public Vector2d getVelocity(PhysShape shape){
        return velocity;
    }
	
	public void onCollision(Manifold m){}
	
	void initFrameData(){
		current = getTransformationMatrix();

        while(shapeBounds.size()<shapes.size()){
            shapeBounds.add(new Square());
        }

        Square s = shapeBounds.get(0);
		shapes.get(0).calcBounds(current,s);
		double minx=s.x;
		double miny=s.y;
		double maxx=s.x+s.width;
		double maxy=s.y+s.height;
		
		for(int i = 0; i<shapes.size(); i++){
            s = shapeBounds.get(i);
			shapes.get(i).calcBounds(current,s);
			if(s.x<minx)minx = s.x;
			if(s.y<miny)miny = s.y;
			if(s.x+s.width>maxx)maxx = s.x+s.width;
			if(s.y+s.height>maxy)maxy = s.y+s.height;
		}
		
		square.set(minx,miny,maxx-minx,maxy-miny);
	}
	
	Matrix2d getCurrent(){
		return current;
	}
	
	@Override
	public final Box getBounds(){
		return square;
	}

    public final Square getShapeBounds(int i){
        return shapeBounds.get(i);
    }
	
	public void translate(double x, double y){
		tx.translation.add(x,y);
	}
	
	public void rotate(double t){
		tx.theta.val+=t;
	}
	
	public Vector2d getPosition(){
		if(parent==null){
			return tx.translation;
		}else{
			return parent.getPosition();
		}
	}
	
	public int numOfShape(){
		return shapes.size();
	}
	
	public PhysShape getShape(int i){
		return shapes.get(i);
	}
	
	private static final Vector2d pvec = new Vector2d();
	
	public boolean pointIsInside(double x, double y){
		tx.invTransform(pvec.set(x,y));
		for(PhysShape s: shapes)
			if(s.pointIsInside(pvec.x,pvec.y))
				return true;
		return false;
	}
}
