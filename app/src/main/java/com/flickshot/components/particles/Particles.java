package com.flickshot.components.particles;

import java.util.ArrayList;
import java.util.HashMap;

import com.flickshot.components.Component;
import com.flickshot.components.graphics.Renderer2d;
import com.flickshot.components.graphics.Sprite;
import com.flickshot.geometry.Transformation;
import com.flickshot.scene.Updater.UpdateEvent;

public class Particles extends Component{
	private static final HashMap<String,ParticleSystem> particleIds = new HashMap<String,ParticleSystem>();
	private static final ArrayList<ParticleSystem> particleSystems = new ArrayList<ParticleSystem>();
	
	
	public static ParticleSystem createSystem(String id,ParticleType type, int initSize){
		if(particleIds.containsKey(id)){
			throw new IllegalStateException("system with that id already exists");
		}
		ParticleSystem ps = new ParticleSystem(type,initSize);
		particleIds.put(id,ps);
		particleSystems.add(ps);
		return ps;
	}
	
	public static ParticleSystem getSystem(String id){
		return particleIds.get(id);
	}
	
	public static boolean hasSystem(String id){
		return particleIds.containsKey(id);
	}
	
	public static void removeSystem(String id){
		particleSystems.remove(particleIds.remove(id));
	}
	
	public static void update(double delta){
		for(int i = 0;i<particleSystems.size(); i++){
			particleSystems.get(i).update(delta);
		}
	}
	
	static final Transformation tx = new Transformation();
	static final Sprite sprite = new Sprite("",tx);
	public static void draw(Renderer2d r){
//		for(int i = 0;i<particleSystems.size(); i++){
//			ParticleSystem ps = particleSystems.get(i);
//			sprite.tintWeight = ps.type.tintWeight;
//			sprite.z = ps.type.z;
//			r.blendMode((ps.type.additive)?Renderer2d.ADDITIVE:Renderer2d.TRANSPARENCY);
//			sprite.setTexture(ps.type.texture);
//			for(int j = 0; j<ps.size; j++){
//				ParticleSystem.Particle p = ps.particles[j];
//				sprite.setTint(p.r,p.g,p.b);
//				sprite.alpha = p.a;
//				tx.theta.val = p.orientation;
//				if(ps.type.orientationRelative)tx.theta.val+=p.dir;
//				p.scale = Math.max(0,p.scale);
//				tx.scale.set(ps.type.width*p.scale,ps.type.height*p.scale);
//				tx.translation.set(p.x,p.y);
//				r.draw(sprite);
//			}
//		}

        for(int i = 0;i<particleSystems.size(); i++) {
			ParticleSystem ps = particleSystems.get(i);
            if(ps.size>0)
			    r.draw(ps);
        }
	}

    public static final void clearAll(){
        for(ParticleSystem p : particleSystems){
            p.clear();
        }
    }

	public static final void create(){
	} 
	
	@Override
	public void preUpdate(UpdateEvent evt) {
	}

	@Override
	public void update(UpdateEvent evt) {
	}

	@Override
	public void postUpdate(UpdateEvent evt) {
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}

}
