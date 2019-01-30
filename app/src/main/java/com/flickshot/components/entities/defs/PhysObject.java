package com.flickshot.components.entities.defs;

import java.util.ArrayList;

import com.flickshot.components.physics.Collider;
import com.flickshot.components.physics.Manifold;
import com.flickshot.components.physics.PhysMaterial;
import com.flickshot.components.physics.PhysShape;
import com.flickshot.geometry.Transformation;
import com.flickshot.geometry.Vector2d;

public abstract class PhysObject extends VisibleEntity{
	public final Collider collider;
	
	public PhysObject(){
		collider = new Collider(0,0,getShapes(),getMaterial(),this,true,true){
			@Override 
			public void onCollision(Manifold m){
				doOnCollision(m);
			}
			
			@Override
			public boolean doImpulse(Manifold m){
				return doDoImpulse(m);
			}

            @Override
            public boolean isStill(){
                return getIsStill();
            }

            @Override
            public Vector2d getVelocity(PhysShape shape){
                return doGetVelocity(shape);
            }
		};
	}
	
	public Collider getCollider(){
		return collider;
	}
	
	@Override
	public void init(double x,double y){
		super.init(x,y);
		collider.tx.translation.set(x,y);
		collider.tx.theta.val = 0;
		collider.velocity.set(0, 0);
		collider.angularVelocity = 0;
		collider.force.set(0, 0);
		collider.bind();
	}
	
	@Override
	public void destroy(){
		super.destroy();
		collider.unbind();
	}
	
	@Override
	public void unload(){
		super.destroy();
		collider.unbind();
	}
	
	public final void setVelocity(double x, double y){
		collider.velocity.set(x, y);
	}
	
	public final void setAngularVelocity(double a){
		collider.angularVelocity = a;
	}
	
	public final void setPosition(double x, double y){
		collider.tx.translation.set(x,y);
	}
	
	public final void set(double x, double y, double vx, double vy, double va){
		setPosition(x,y);
		setVelocity(vx,vy);
		setAngularVelocity(va);
	}
	
	private void doOnCollision(Manifold m){
		onCollision(m);
	}
	
	private boolean doDoImpulse(Manifold m){
		return doImpulse(m);
	}

    private boolean getIsStill(){ return isStill(); }

    private Vector2d doGetVelocity(PhysShape shape){
        return getVelocity(shape);
    }

	protected void onCollision(Manifold m){
		
	}

    protected Vector2d getVelocity(PhysShape shape){
        return collider.velocity;
    }

    protected boolean isStill(){
        return collider.velocity.getMagSquared()<=collider.minSpeed*collider.minSpeed
                && collider.angularVelocity <= collider.minAngularSpeed;
    }
	
	protected boolean doImpulse(Manifold m){
		return true;
	}
	
	protected final ArrayList<PhysShape> shapeAsList(PhysShape shape){
		ArrayList<PhysShape> shapes = new ArrayList<PhysShape>();
		shapes.add(shape);
		return shapes;
	}
	
	protected PhysMaterial getMaterial(){
		return new PhysMaterial(1,0.2,0.9,0.8);
	}
	
	protected abstract ArrayList<PhysShape> getShapes();
}
