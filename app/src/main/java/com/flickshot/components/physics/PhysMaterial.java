package com.flickshot.components.physics;

public class PhysMaterial {
	public final double density;
	public final double elasticity;
	public final double staticFriction;
	public final double dynamicFriction;
	
	public PhysMaterial(double density, double elasticity, double staticFriction, double dynamicFriction){
		this.density = density;
		this.elasticity = elasticity;
		this.staticFriction = staticFriction;
		this.dynamicFriction = dynamicFriction;
	}
	
	@Override
	public String toString(){
		return "material{density:"+density+" elasticity:"+elasticity+" staticFriction:"+staticFriction+" dynamicFriction:"+dynamicFriction+"}";
	}
}
