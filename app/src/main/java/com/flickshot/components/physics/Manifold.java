package com.flickshot.components.physics;

import static com.flickshot.geometry.collision.CollisionLib.pythag;

import java.util.ArrayList;

import android.util.Log;

import com.flickshot.components.physics.shapes.Circle;
import com.flickshot.geometry.Transformation;
import com.flickshot.geometry.Vector2d;

public class Manifold {
	private static final double SLOP = 0.01;
	private static final double CORRECTION = 0.8;
	
	public Collider a;
	public PhysShape sa;
	public Collider b;
	public PhysShape sb;
	public double penetration;
	public final Vector2d[] contacts = new Vector2d[]{new Vector2d(),new Vector2d()}; 
	public int contactCount = 0;
	public Vector2d normal = new Vector2d();
	
	public double forceSquared;
	
	Manifold(){}
	
	private static final Vector2d misc0 = new Vector2d();
	private static final Vector2d misc1 = new Vector2d();
	private static final Vector2d misc2 = new Vector2d();
	private static final Vector2d ra = new Vector2d();
	private static final Vector2d rb = new Vector2d();
	
	private Vector2d getRelativeVelocity(){
        Vector2d va = a.getVelocity(sa);
        Vector2d vb = b.getVelocity(sb);
		if(vb.hasNaN() || va.hasNaN() ||
				Double.isNaN(b.angularVelocity) || Double.isNaN(a.angularVelocity) ||
				ra.hasNaN() || rb.hasNaN())
			throw new IllegalStateException(""+a.shapes.get(0)+" "+b.shapes.get(0)+" "+va+" "+vb+" "+ra+" "+rb);
		misc0.set(vb);
		
		misc2.set(rb);
		misc2.cross2(b.angularVelocity);
		misc0.add(misc2);
		
		misc0.sub(va);
		
		misc2.set(ra);
		misc2.cross2(a.angularVelocity);
		misc0.sub(misc2);
		
		return misc0;
	}
	
	private double e;
	
	//checks for resting collision
	final void initialize(double delta){
		
		e = Math.max(a.material.elasticity,b.material.elasticity);
		
		for(int i = 0; i<contactCount; i++){
			Vector2d contactPoint = contacts[i];
			
			ra.set(contactPoint);
			ra.sub(a.getPosition());
			if(b.shapes.get(0) instanceof Bounds){
				rb.set(0,0);
			}else{
				rb.set(contactPoint);
				rb.sub(b.getPosition());
			}
			
//			Vector2d rv = getRelativeVelocity();
//			if(rv.getMagSquared() < Physics.GRAVITY.getMagSquared()*delta + 0.0001)
//				e=0;
		}
		
	}
	
	private double totalInvMass;
	private double invMassA,invMassB;
	
	final void applyImpulse(){
		if(!a.doImpulse(this) || !b.doImpulse(this))
			return;
		for(int i = 0; i<contactCount; i++){
			Vector2d contactPoint = contacts[i];
			invMassA = a.massData.invMass;
			invMassB = b.massData.invMass;
			totalInvMass = invMassA+invMassB;
			double invIA = a.massData.invInertia;
			double invIB = b.massData.invInertia;
			if(totalInvMass+invIA+invIB==0)return;
			//do impulse translational
			//:::::::::::::::::::::::::
			ra.set(contactPoint);
			ra.sub(a.getPosition());
			if(b.shapes.get(0) instanceof Bounds){
				rb.set(0,0);
			}else{
				rb.set(contactPoint);
				rb.sub(b.getPosition());
			}
			
			Vector2d rv = getRelativeVelocity();
			double contactVel = rv.dot(normal);
			
			if(contactVel > 0) {
				return;
			}
			
			
			double ifa = Math.pow(ra.cross(normal),2)*invIA;
			double ifb = Math.pow(rb.cross(normal),2)*invIB;
			
			double j = - (1.0 + e) * contactVel;
			j /= totalInvMass+ifa+ifb;
			j /= (double)contactCount;
			
			misc1.set(normal);
			misc1.mul(j);
			forceSquared = misc1.getMagSquared();
			
			b.applyImpulse(misc1,rb);
			misc1.neg();
			a.applyImpulse(misc1,ra);
			//::::::::::::::::::::
			
			//do friction impulse
			//:::::::::::::::::::::
			rv = getRelativeVelocity();
			//Log.e("physics",""+rv);
			//calculate tangent
			double d = rv.dot(normal);
			misc1.set(rv);
			misc1.sub(d*normal.x,d*normal.y); 
			misc1.normalize();
			Vector2d tangent = misc1;
			//Log.e("physics",tangent+"");
			
			//ifa = Math.pow(ra.cross(tangent),2)*invIA;
			//ifb = Math.pow(rb.cross(tangent),2)*invIB;
			
			//get friction magnitude
			double jt = (-rv.dot(tangent))/(totalInvMass+ifa+ifb);
			jt /= (double)contactCount;
			if(Math.abs(jt)<0.0001)return;
			
			//get coefficient of static friction
			double k = pythag(a.material.staticFriction,b.material.staticFriction);
			
			Vector2d friction = tangent;
			//clamp friction in accordance with Columb's law
			if(Math.abs(jt)< j * k){
				//static friction
				friction.mul(jt);
			}else{
				//dynamic friction
				//get coefficient of dynamic friction
				k = pythag(a.material.dynamicFriction,b.material.dynamicFriction);
				friction.mul(-j * k);
			}
			//apply impulse
			b.applyImpulse(friction,rb);
			friction.neg();
			a.applyImpulse(friction,ra);
			
		}
	}
	
	final void positionalCorrection(){
		if(totalInvMass==0 )return;
		if(penetration>SLOP){
			misc0.set(normal);
			misc0.mul((penetration/totalInvMass)*CORRECTION);
			a.translate(misc0.x*-invMassA,misc0.y*-invMassA);
			b.translate(misc0.x*invMassB,misc0.y*invMassB);
		}
	}
	
	final void fireEvent(){
		a.doOnCollision(this);
		b.doOnCollision(this);
	}
	
	public String toString(){
		return "manifold{ penetration:"+penetration+" normal:"+normal+
				" contactCount:"+contactCount+" contact0:"+contacts[0]+" contact1:"+contacts[1]+" }";
	}
}
