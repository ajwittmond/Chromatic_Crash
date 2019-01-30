package com.flickshot.components.entities.defs.enemies;

import com.flickshot.assets.sfx.Sound;
import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.defs.PhysObject;
import com.flickshot.components.entities.defs.PuckState;
import com.flickshot.components.entities.defs.managers.ScoreTracker;
import com.flickshot.components.particles.ParticleSystem;
import com.flickshot.components.particles.ParticleType;
import com.flickshot.components.particles.Particles;
import com.flickshot.components.physics.Collider;
import com.flickshot.components.physics.Manifold;
import com.flickshot.components.physics.Physics;
import com.flickshot.config.Config;
import com.flickshot.scene.Updater.UpdateEvent;

public abstract class Enemy extends PhysObject{
    public static final ParticleType dropType;
    public static final ParticleSystem drop;

    static{
        dropType = new ParticleType();
        dropType.life = 1;
        dropType.z = 1;
        dropType.color1(0, 0, 0, 0, 0, 0);
        dropType.oneColor();
        dropType.alpha(1, 1, 1);
        dropType.dimensions(1, 1);
        dropType.startScale(32, 90);
        dropType.scale(-32, 0);
        dropType.startSpeed(300, 600);
        dropType.startDir(0, Math.PI / 2);
        dropType.texture = "flare";

        drop = Particles.createSystem("drop", dropType, 256);
    }

	private int points = 15;
	public int maxHealth=1;
	public int health;
	protected double killForce = 10000000;
	private boolean killedByPlayer=false;
	
	protected Sound collisionSound;
	protected Sound deathSound;
	protected Sound damageSound;
	public static final double SOUND_THRESHOLD = 5000000;
	public static final double SOUND_MAX = 100000000;
	
	public static enum ColorType{
		GREEN("green",0,0.8f,0),
		PINK("pink",1f,0.18f,1f),
		RED("red",0.8f,0,0);
		
		public final String name;
		public final float r,g,b;
		
		ColorType(String name, float r, float g, float b){
			this.name = name;
			this.r = r;
			this.g = g;
			this.b =b;
		}
		
		@Override
		public String toString(){
			return name;
		}
	}
	
	protected ColorType color = ColorType.RED; 
	
	public Enemy() {
		super();
	}
	
	public double getKillForce(){
		return killForce;
	}
	
	@Override
	public void init(double x, double y){
		super.init(x,y);
		health=maxHealth;
		killedByPlayer=false;
        dropType.bounds = Physics.getScene(0);
	}
	
	@Override
	public void update(UpdateEvent evt){
		if(health<=0)kill();
	}
	
	@Override
	public void destroy(){
		super.destroy();
		if(deathSound!=null)
			deathSound.play();
	}
	
	protected void setPoints(int points){
		this.points = points;
	}
	
	public void setColor(ColorType c){
		color = c;
	}
	
	public ColorType getColor(){
		return color;
	}
	
	public int getPoints(){
		return points;
	}
	
	public void randColor(){
		color = ColorType.RED;
		double r = Math.random();
		if(r<=1.0/3.0){
			color = ColorType.PINK;	
		}else if(r<=2.0/3.0){
			color = ColorType.GREEN;
		}
	}
	
	public void configure(Config config){
		if(config instanceof EnemyConfig){
			EnemyConfig c = (EnemyConfig)config;
			color = c.color.getColor();
			collider.angularVelocity = c.angularVelocity;
			if(c.velocity!=null){
				collider.velocity.set(c.velocity.x,c.velocity.y);
			}
			collider.tx.theta.val = c.orientation;
		}
	}
	
	@Override
	protected void onCollision(Manifold m){
		double force = Math.sqrt(m.forceSquared);
		if(collisionSound!=null && force> SOUND_THRESHOLD ){
			float gain = (float)Math.min(1,(force-SOUND_THRESHOLD)/(SOUND_MAX-SOUND_THRESHOLD));
            gain*=gain;
            gain*=0.5;
			collisionSound.setVolume(gain,gain);
			collisionSound.play();
		}
		if(force>killForce){
			Collider other = (m.a==collider) ? m.b : m.a;
			if(other.state instanceof PuckState){
				PuckState state = (PuckState)other.state;
				if(!state.grabbed() && !state.stunned()){
					health-=(int)(force/killForce);
					if(health<=0){
						Object o = Entities.getEntity("scoreTracker").getState(0);
						if(o!=null){
							ScoreTracker s = (ScoreTracker)Entities.getEntity("scoreTracker").getState(0);
							s.addToScore(this);
						}
						killedByPlayer=true;
						kill();
					}
					onDamage(m);
				}
			}
		}
	}
	
	protected void onDamage(Manifold m){
		if(damageSound!=null)
            damageSound.play();
	}
	
	protected void doCollisionSound(double force){
		if(collisionSound!=null && force> SOUND_THRESHOLD ){
			float gain = (float)Math.min(1,(force-SOUND_THRESHOLD)/(SOUND_MAX-SOUND_THRESHOLD));
			gain*=gain;
			gain*=0.5;
			collisionSound.setVolume(gain,gain);
			collisionSound.play();
		}
	}
	
	public void doForceDamage(double force){
		if(force>killForce){
			health-=(int)(force/killForce);
		}
	}
	
	public void doDamage(int damage,Manifold m){
        health -= Math.max(0,damage);
        onDamage(m);
	}
	
	protected boolean wasKilledByPlayer(){
		return killedByPlayer;
	}

}
