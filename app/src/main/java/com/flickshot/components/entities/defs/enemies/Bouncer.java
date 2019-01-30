package com.flickshot.components.entities.defs.enemies;

import java.util.ArrayList;

import com.flickshot.assets.sfx.Sound;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.entities.defs.PuckState;
import com.flickshot.components.entities.defs.enemies.ChargeBox.ChargeBoxConfig;
import com.flickshot.components.entities.defs.enemies.ChargeBox.Factory;
import com.flickshot.components.entities.defs.enemies.blocks.Block;
import com.flickshot.components.entities.defs.fx.BoxFragment;
import com.flickshot.components.graphics.Sprite;
import com.flickshot.components.particles.ParticleSystem;
import com.flickshot.components.particles.ParticleType;
import com.flickshot.components.particles.Particles;
import com.flickshot.components.physics.Bounds;
import com.flickshot.components.physics.Collider;
import com.flickshot.components.physics.Manifold;
import com.flickshot.components.physics.MassData;
import com.flickshot.components.physics.PhysMaterial;
import com.flickshot.components.physics.PhysShape;
import com.flickshot.components.physics.shapes.Polygon;
import com.flickshot.config.Config;
import com.flickshot.geometry.Transformation;
import com.flickshot.geometry.Vector2d;
import com.flickshot.scene.Updater.UpdateEvent;
import com.flickshot.util.MiscLib;

public class Bouncer extends Enemy{
    public static ParticleSystem wave;
    public static ParticleType waveType;

    static{
        waveType = new ParticleType();
        waveType.life = 0.5;
        waveType.z = 1;
        waveType.color1(0, 0, 0, 0, 0, 0);
        waveType.oneColor();
        waveType.alpha(1, 0.8f, 0);
        waveType.dimensions(1, 1);
        waveType.scale(2000, 0);
        waveType.texture = "wave";

        wave = Particles.createSystem("wave", waveType, 16);
    }

	public static final String ENTITY_NAME = "Bouncer";
	public static final String EDITOR_SPRITE = "bouncer_{COLOR};256;64;0";
	
	private final Vector2d startPoint = new Vector2d();
	private final Vector2d lineVec = new Vector2d(1,0);
	
	public double speed;
	public double direction;
	
	private static final int damage = 10;
	private static final double WIDTH = 256,HEIGHT=64;

    protected float wr,wg,wb;

	private Bouncer hit;
	private boolean bounced;
	Sprite sprite;

    Sound pulseSound = new Sound("pulse");

	public Bouncer(){
		maxHealth = 10;
		killForce = 20000000;
		setPoints(40);
		sprite = new Sprite("bouncer_red",
				new Transformation(collider.tx.translation,new Vector2d(WIDTH,HEIGHT),collider.tx.theta));
		sprite.setTint(0,0,0);
		setArtist(sprite);
		damageSound = new Sound("metal_hit");
        deathSound = new Sound("metal_impact");
	}
	
	public void init(double x, double y){
		super.init(x,y);
        damageSound.setVolume(0.5f,0.5f);
        deathSound.setVolume(0.8f,0.8f);
		startPoint.set(x,y);
		last=null;
		randColor();
		sprite.setTexture("bouncer_"+color.name);
	}
	
	@Override
	public PhysMaterial getMaterial(){
		return new PhysMaterial(100000000,0,0,0);
	}
	
	@Override
	protected ArrayList<PhysShape> getShapes() {
		double w = WIDTH/2;
		double h = HEIGHT/2;
		return shapeAsList(new Polygon(0,2,4,new double[]{
			 w,  h,
			-w,  h,
			-w, -h,
			 w, -h
		},0,0));
	}
	
	public void update(UpdateEvent evt){
		super.update(evt);
		bounced = false;
		//hit = null;
		collider.tx.theta.val = direction;
		collider.velocity.set(lineVec);
		collider.velocity.mul(speed);
//		collider.accelerateToSpeed(direction,2000,speed);
		collider.angularVelocity = 0;

		//project position onto line with start point
		collider.tx.translation.sub(startPoint);
		collider.tx.translation.project(lineVec);
		collider.tx.translation.add(startPoint);
		
		float u = (float)health/(float)maxHealth;
		sprite.tintWeight = 1.0f-u;
	}
	
	@Override
	public void destroy(){
		super.destroy();
		BoxFragment.spawn(collider,WIDTH,HEIGHT);
	}
	
	@Override
	public void configure(Config c){
		super.configure(c);
		speed = ((BouncerConfig)c).speed;
		direction = collider.tx.theta.val;
		if(direction<0)
			direction+=Math.PI*2;
		sprite.setTexture("bouncer_"+color.name);
		lineVec.setDir(direction);
        wr = color.r;
        wg = color.g;
        wb = color.b;
	}
	
	Collider last;
	
	final private Vector2d impulse = new Vector2d();
	final private Vector2d normal = new Vector2d();
	@Override
	public void onCollision(Manifold m){

		Collider other = (m.a==collider) ? m.b : m.a;
		
		if( other.state instanceof PuckState && 
				Math.abs((m.normal.getDir()-collider.tx.theta.val)%Math.PI)
				<Math.PI/4){
			((PuckState)other.state).damage(damage);
			((PuckState)other.state).stun();
			normal.set(m.normal);
			if(m.b==collider)normal.neg();
			impulse.set(normal);
			impulse.mul(10000000);
			other.applyImpulse(impulse,normal);
            wave.create(1,m.contacts[0].x,m.contacts[0].y,0,0,normal.getDir(),0,wr,wg,wb);
            pulseSound.play();
		}else if(
                other.state instanceof Enemy && !(other.state instanceof Bouncer) &&
				!(other.state instanceof Block) && !(other.state instanceof Splitter) &&
                Math.abs((m.normal.getDir()-collider.tx.theta.val)%Math.PI)
				<Math.PI/4
                ){
            if(other.state instanceof Bomb){
                ((Bomb)other.state).explode();
            }else {
                ((Enemy) other.state).doDamage(1, m);
            }
			normal.set(m.normal);
			if(m.b==collider)normal.neg();
			impulse.set(normal);
			impulse.mul(10000000);
			other.applyImpulse(impulse,normal);
            wave.create(1,m.contacts[0].x,m.contacts[0].y,0,0,normal.getDir(),0,wr,wg,wb);
            pulseSound.play();
		}else{
			super.onCollision(m);
	
			PhysShape s = other.getShape(0);
			if(!bounced && (other!=last && (s instanceof Bounds || other.state instanceof Block || other.state instanceof Splitter))){
				bounced = true;
				direction = getNextDir();
				last = other;
				speed = -speed;
				hit=null;
			}else if(!bounced && other.state instanceof Bouncer && 
					Math.abs(((Bouncer)other.state).direction-getNextDir())<0.0001 &&
					other.state != hit){
				bounced = true;
				Bouncer b =(Bouncer)other.state;
				
				b.direction = b.getNextDir();
				b.speed = -b.speed;
				b.hit = this;
				b.last = null;
				
				direction = getNextDir();
				speed = -speed;
				hit = b;
				
				last = null;
				
			}
		}
	}
	
	private double getNextDir(){
		double d = direction+Math.PI;
		if(d>Math.PI*2)
			d-=(Math.PI*2);
		return d;
	}
	
	@Override
	public void onDamage(Manifold m){
        super.onDamage(m);
        final int particles = 8;
        double vtheta = m.normal.getDir()+(Math.PI);
        for(int i = 0; i<particles; i++){
            double theta = vtheta+((Math.PI*Math.random())-(Math.PI/2));
            drop.create(1,m.contacts[0].x,m.contacts[0].y,300,theta,0,32);
        }
	}
	
	public static EntityStateFactory getFactory(){
		return new Factory();
	}
	
	public static class Factory extends EntityStateFactory{
		@Override
		public EntityState construct() {
			return new Bouncer();
		}

		@Override
		public Class<? extends EntityState> getType() {
			return Bouncer.class;
		}
		
		@Override
		public Config getConfig(){
			return new BouncerConfig();
		}
		
		@Override
		public void getAssets(ArrayList<String[]> assets){
			assets.add(new String[]{"texture","bouncer_red"});
			assets.add(new String[]{"texture","bouncer_green"});
            assets.add(new String[]{"texture","bouncer_pink"});
            assets.add(new String[]{"texture","wave"});
            assets.add(new String[]{"sound","pulse"});
            assets.add(new String[]{"sound","metal_hit"});
            assets.add(new String[]{"sound","metal_impact"});
		}
	}
	
	public static class BouncerConfig extends EnemyConfig{
		public double speed = 500;
		public BouncerConfig(){}
	}
}
