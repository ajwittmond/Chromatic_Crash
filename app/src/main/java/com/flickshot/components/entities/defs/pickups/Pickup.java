package com.flickshot.components.entities.defs.pickups;

import com.flickshot.components.entities.defs.PhysObject;
import com.flickshot.components.entities.defs.PuckState;
import com.flickshot.components.graphics.Sprite;
import com.flickshot.components.physics.Collider;
import com.flickshot.components.physics.Manifold;
import com.flickshot.config.Config;
import com.flickshot.scene.Updater.UpdateEvent;

import java.util.HashMap;

public abstract class Pickup extends PhysObject{
	Sprite sprite;
	
	double maxLife = 1;
	double life;
	
	
	public void init(double x, double y){
		super.init(x,y);
		life = maxLife;
		sprite.setTint(0,0,0);
	}
	
	public void update(UpdateEvent evt){
		super.update(evt);
		life -= evt.getDelta();
		sprite.tintWeight = 1.0f-(float)(life/maxLife);
		if(life<=0)
			kill();
	}
	
	
	public void onCollision(Manifold m){
		Collider other = (m.a==collider) ? m.b : m.a;
		if(other.state instanceof PuckState)
			pickupAction();
	}
	
	public final void pickupAction(){
		doPickupAction();
		kill();
	}

    @Override
    public void configure(Config c){
        if(c instanceof PickupConfig){
            life = maxLife = ((PickupConfig)c).life;
        }
    }
	
	protected abstract void doPickupAction();

    public static class PickupConfig extends Config {
        public int life = 15;
        @Override
        public void setValue(String text) {

        }

        @Override
        public void getAliases(HashMap<String, String> map) {

        }
    }
}
