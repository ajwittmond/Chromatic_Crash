package com.flickshot.components.entities.defs.enemies;

import java.util.ArrayList;

import com.flickshot.assets.sfx.Sound;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.entities.defs.PuckState;
import com.flickshot.components.entities.defs.enemies.Bouncer.BouncerConfig;
import com.flickshot.components.entities.defs.enemies.Bouncer.Factory;
import com.flickshot.components.physics.Collider;
import com.flickshot.components.physics.Manifold;
import com.flickshot.config.Config;

public class InvulnerableBouncer extends Bouncer{
	public static final String ENTITY_NAME = "InvulnerableBouncer";
	public static final String EDITOR_SPRITE = "bouncer_invulnerable;256;64;0";

    Sound ping = new Sound("ping");

	public InvulnerableBouncer(){
        super();
		killForce = Double.POSITIVE_INFINITY;
        maxHealth = Integer.MAX_VALUE;
	}
	
	public void init(double x, double y){
		super.init(x,y);
		sprite.setTexture("bouncer_invulnerable");
        damageSound.setVolume(0, 0);
        deathSound.setVolume(0,0);
        ping.setVolume(0.5f,0.5f);
	}
	
	public void configure(Config c){
		super.configure(c);
		sprite.setTexture("bouncer_invulnerable");
        wr = 0;
        wg = 0;
        wb = 0;
	}

    public void onCollision(Manifold m){
        super.onCollision(m);
        Collider other = (m.a==collider) ? m.b : m.a;
        if(other.state instanceof PuckState && Math.sqrt(m.forceSquared)>20000000){
            ping.play();
        }
    }

	public static final EntityStateFactory getFactory(){
		return new Factory();
	}
	
	public static class Factory extends EntityStateFactory{
		@Override
		public EntityState construct() {
			return new InvulnerableBouncer();
		}

		@Override
		public Class<? extends EntityState> getType() {
			return InvulnerableBouncer.class;
		}
		
		@Override
		public Config getConfig(){
			return new BouncerConfig();
		}
		
		@Override
		public void getAssets(ArrayList<String[]> assets){
			assets.add(new String[]{"texture","bouncer_invulnerable"});
            assets.add(new String[]{"texture","wave"});
            assets.add(new String[]{"sound","pulse"});
            assets.add(new String[]{"sound","metal_hit"});
            assets.add(new String[]{"sound","metal_impact"});
            assets.add(new String[]{"sound","ping"});
		}
	}
}
