package com.flickshot.components.physics;

import android.util.Log;

public class MassData {
	public double mass;
	public double invMass;
	
	public double inertia;
	public double invInertia;
	
	public MassData(){}
	
	public MassData(double mass, double inertia){
		set(mass,inertia);
	}
	
	public void set(double mass, double inertia){
		this.mass = mass;
		this.inertia = inertia;
		this.invMass = 1/mass;
		this.invInertia = 1/inertia;
	}
	
	@Override
	public String toString(){
		return "massdata{mass:"+mass+" invMass:"+invMass+" inertia:"+inertia+" invInertia:"+invInertia+" }";
	}
}
