package com.flickshot.components.physics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import android.util.Log;

import com.flickshot.components.ComponentState;
import com.flickshot.geometry.Matrix2d;
import com.flickshot.geometry.RotatingBox;
import com.flickshot.geometry.TransformableInterface;
import com.flickshot.geometry.Transformation;
import com.flickshot.geometry.Vector2d;
import com.flickshot.geometry.collision.CollisionLib;
import com.flickshot.util.LinkedNode;
import com.flickshot.util.MutableDouble;

public class Mover extends LinkedNode<Mover> implements ComponentState,TransformableInterface{
    private boolean isHiddenParent;
    private final Mover hiddenParent;

	protected Mover parent = null;
	protected final ArrayList<Mover> children = new ArrayList<Mover>();

    //this vector stores the childs position relative to the root
    private final Vector2d childVec = new Vector2d();

	public final Vector2d velocity = new Vector2d();
	public final Vector2d force = new Vector2d();
	
	public double angularVelocity = 0;
	public double torque = 0;
	
	public double dragMul = 0.002;
	
	public Transformation tx;
	public MassData massData;
	
	public double maxSpeed = Double.NaN;
	public double minSpeed = 0.1;
	
	public double maxAngularSpeed = Double.NaN;
	public double minAngularSpeed = 0.00001;
	
	public boolean doMove = true;

	private double targetSpeed = Double.NaN;
	private double targetAngularSpeed = Double.NaN;

    private Mover(boolean isHiddenParent){
        tx = new Transformation();
        massData = new MassData(1,1);
        this.isHiddenParent = true;
        hiddenParent = null;
    }

	public Mover(){
		tx = new Transformation();
		massData = new MassData(1,1);
        hiddenParent = new Mover(true);
        hiddenParent.children.add(this);
	}
	
	public Mover(Transformation tx,MassData massData){
		this.tx = tx;
		this.massData = massData;
        hiddenParent = new Mover();
        hiddenParent.children.add(this);
	}
	
	public void move(double delta){
		if(parent==null){
            if(isHiddenParent||children.isEmpty()) {
                double invMass = getInvMass();
                double invInertia = getInvMass();
                //translational
                double mag = velocity.getMag();

                //calculate drag
                double drag = velocity.getMagSquared() * dragMul;
                double dragDir = velocity.getDir() + Math.PI;
                force.add((drag * Math.cos(dragDir)), (drag * Math.sin(dragDir)));

                //integrate
                force.mul(invMass);//scale to mass

                double dx = dif(delta, velocity.x, force.x);
                double dy = dif(delta, velocity.y, force.y);

                velocity.set(velocity.x + force.x * delta, velocity.y + force.y * delta);

                if (!Double.isNaN(maxSpeed)) {
                    if (velocity.getMag() > maxSpeed) velocity.setMag(maxSpeed);
                    double dt = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
                    if (dt > maxSpeed * delta) {
                        dx = maxSpeed * dx / dt;
                        dy = maxSpeed * dy / dt;
                    }
                }

                if (doMove && (CollisionLib.pythag(dx, dy) >= (minSpeed * delta))) {
                    tx.translate(dx, dy);
                    for (int i = 0; i < children.size(); i++) {
                        children.get(i).tx.translate(dx, dy);
                    }
                }
                force.set(0, 0);
                //target check
                double speed = velocity.getMag();
                if (!Double.isNaN(targetSpeed)) {
                    if (Math.abs(speed - targetSpeed) <= Math.abs(speed - mag)) {
                        velocity.setMag(targetSpeed);
                    }
                }

                //angular
                mag = Math.abs(angularVelocity);

                //calculate drag
                drag = angularVelocity * angularVelocity * dragMul;
                if (angularVelocity > 0) drag = -drag;
                torque += drag;
                torque *= invInertia;

                //integrate
                double da = dif(delta, angularVelocity, torque);
                if (!Double.isNaN(maxAngularSpeed)) {
                    if (Math.abs(angularVelocity) > maxAngularSpeed)
                        angularVelocity = maxAngularSpeed * Math.signum(angularVelocity);
                    double mad = maxAngularSpeed * delta;
                    if (da > mad) da = mad * Math.signum(da);
                }

                if (doMove && Math.abs(da) >= minAngularSpeed * delta) {
                    tx.theta.val += da;
                }

                angularVelocity += torque * delta;
                torque = 0;


                //targetCheck
                if (!Double.isNaN(targetAngularSpeed)) {
                    if (Math.abs(speed - targetAngularSpeed) <= Math.abs(speed - mag)) {
                        angularVelocity = targetAngularSpeed * Math.signum(angularVelocity);
                    }
                }

                /*
                 * TODO: add code to move children
                 */
            }else{
                hiddenParent.move(delta);
            }
		}
	}

    public boolean isStill(){
        return velocity.getMagSquared()<=minSpeed*minSpeed && angularVelocity <= minAngularSpeed;
    }

	private static double dif(double delta,double v, double a){
		return delta*v + (delta*delta*0.5*a);
	}
	
	public final void moveTowards(double x, double y, double speed){
		x -= tx.translation.x;
		y -= tx.translation.y;
		double d = Math.sqrt(x*x + y*y);
		x/=d;
		y/=d;
		velocity.set(x*speed, y*speed);
	}
	
	public final void accelerateTowards(double x, double y, double speed){
		x -= tx.translation.x;
		y -= tx.translation.y;
		double d = Math.sqrt(x*x + y*y);
		x/=d;
		y/=d;
		force.set(x*speed, y*speed);
	}
	
	public final void deccelerate(double mag,double delta){
		mag*=delta;
		velocity.setMag(Math.max(0,velocity.getMag()-mag));
	}
	
	public final void accelerateToSpeed(double dir,double accel,  double target){
		double s = velocity.getMag();
		double mass = getMass();
		if(s>=target){
			double x = accel * Math.cos(dir);
			double y = accel * Math.sin(dir);
			double vd = (velocity.x * velocity.x) + (velocity.y * velocity.y);
			double u = (x*velocity.x + y*velocity.y)/vd;
			force.x = (x - (u * velocity.x))*mass;
			force.y = (y - (u * velocity.y))*mass;
			velocity.setMag(target);
		}else if(s<target){
			double x = accel * Math.cos(dir);
			double y = accel * Math.sin(dir);
			force.x = x*mass;
			force.y = y*mass;
		}
		targetSpeed = target;
	}
	
	public final void angularAccelerationToVelocity(double accel, double target){
		double s = Math.signum(target);
		if((target-angularVelocity)*s>0){
			torque = accel*getInertia()*Math.signum(target);
		}
		targetAngularSpeed = target;
	}
	
	public final void angularDeccelerate(double mag,double delta){
		angularVelocity = Math.max(0,Math.abs(angularVelocity)-(mag*delta))*Math.signum(angularVelocity);
	}
	
	public boolean isPartOf(Mover m){
		if(parent == null){
			for(Mover child: children){
				if(child.equals(m) || child.isPartOf(m)){
					return true;
				}
			}
			return false;
		}else{
			return parent.isPartOf(m);
		}
	}
	
	public void bind(){
		if(tx==null || massData==null)throw new IllegalStateException();
		Physics.add(this);
	}
	
	public void unbind(){
		Physics.remove(this);
	}
	
	@Override
	public Transformation getTransformation() {
		return tx;
	}

	@Override
	public void setTransformation(Transformation t) {
		tx = t;
	}

	@Override
	public void setTransformation(Vector2d translation, Vector2d scale,
			MutableDouble theta) {
		tx.set(translation,scale,theta);
	}

	@Override
	public void setTransformation(double x, double y, double w, double h,
			double theta) {
		tx.set(x,y,w,h,theta);
	}
	
	public Transformation getParentTransformation(){
		if(parent==null)return tx; else return parent.getParentTransformation();
	}
	
	public int numOfChildren(){
		return children.size();
	}
	
	public Mover getChild(int i){
		return children.get(i);
	}
	
	public void addChild(Mover m){
        if(children.isEmpty()){
            hiddenParent.tx.set(tx);
            hiddenParent.velocity.set(velocity);
            hiddenParent.angularVelocity = angularVelocity;
        }
		m.parent = this;
		children.add(m);
		m.velocity.set(0,0);
		m.force.set(0,0);
		m.angularVelocity = 0;
        Mover curr = this;
        while(curr.parent!=null)curr= curr.parent;
        curr.hiddenParent.setChildData();
	}

    private void setChildData(){
        //only called on hidden parent

    }


	private final Matrix2d matrix = new Matrix2d();
	public Matrix2d getTransformationMatrix(){
		matrix.set(tx);
		return matrix;
	}
	
	private final Vector2d position = new Vector2d();
	public Vector2d getPosition(){
		if(parent==null){
            return doGetPosition();
        }else{
            return parent.getPosition();
        }
	}

    private Vector2d doGetPosition() {
        int num = 1;
        position.set(tx.translation);
        for(Mover child: children){
            position.add(child.doGetPosition());
            num++;
        }
        position.mul(1.0/(double)num);
        return position;
    }

    public final void applyImpulse(Vector2d impulse, Vector2d contactVector){
        if(parent==null){
            if(doMove) {
                if (children.isEmpty()) {
                    velocity.add(impulse.x * massData.invMass, impulse.y * massData.invMass);
                    angularVelocity += massData.invInertia * contactVector.cross(impulse);
                } else {
                    hiddenParent.applyImpulse(impulse, contactVector);
                }
            }
        }else{
            ((Collider)parent).applyImpulse(impulse,contactVector);
        }
    }

    public boolean doImpulse(Manifold m){
        return true;
    }
	
	public double getMass(){
		double mass = massData.mass;
		for(int i = 0; i<children.size(); i++){
			mass+=children.get(i).massData.mass;
		}
		return mass;
	}
	
	public double getInvMass(){
		double imass = massData.invMass;
		for(int i = 0; i<children.size(); i++){
			imass+=children.get(i).massData.invMass;
		}
		return imass;
	}
	
	public double getInertia(){
		double inertia = massData.inertia;
		for(int i = 0; i<children.size(); i++){
			inertia+=children.get(i).massData.inertia;
		}
		return inertia;
	}
	
	public double getInvInertia(){
		double invInertia = massData.invInertia;
		for(int i = 0; i<children.size(); i++){
			invInertia+=children.get(i).massData.invInertia;
		}
		return invInertia;
	}
}