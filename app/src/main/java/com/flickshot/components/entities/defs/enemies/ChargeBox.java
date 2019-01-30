package com.flickshot.components.entities.defs.enemies;

import java.util.ArrayList;

import android.util.Log;

import com.flickshot.assets.AssetLibrary;
import com.flickshot.assets.sfx.Sound;
import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.entities.defs.PuckState;
import com.flickshot.components.entities.defs.enemies.Bomb.BombConfig;
import com.flickshot.components.entities.defs.enemies.Bomb.Factory;
import com.flickshot.components.entities.defs.enemies.blocks.Block;
import com.flickshot.components.entities.defs.fx.BoxFragment;
import com.flickshot.components.graphics.Artist;
import com.flickshot.components.graphics.Renderer2d;
import com.flickshot.components.graphics.Sprite;
import com.flickshot.components.physics.Collider;
import com.flickshot.components.physics.Manifold;
import com.flickshot.components.physics.PhysShape;
import com.flickshot.components.physics.Physics;
import com.flickshot.components.physics.Scene;
import com.flickshot.components.physics.shapes.Polygon;
import com.flickshot.config.Config;
import com.flickshot.geometry.Box;
import com.flickshot.geometry.Transformation;
import com.flickshot.geometry.Vector2d;
import com.flickshot.scene.Updater.UpdateEvent;
import com.flickshot.util.MiscLib;
import com.flickshot.components.physics.Bounds;

public class ChargeBox extends Enemy{
	public static final String ENTITY_NAME = "chargeBox";
	public static final String EDITOR_SPRITE = "box_{COLOR};128;128;0";
	private Sprite sprite;
	private double width = 128;
	
	private final double chargeSpeed = 5000;
	private final double waitTime = 1;
	private final double pauseTime = 0.5;
	private final double aDeccel = Math.PI*2;
	private final double aFix = Math.PI*2;
	private final double deccel = 500;
	
	private final Vector2d chargeDir = new Vector2d();
	
	private double currTheta;
	
	private boolean charging;
	private boolean chargeHit;
	
	private double waitTimer;
	private boolean pausing = false;
	private double pauseTimer;
	
	private final int damage = 10;

    private Sound charge = new Sound("charge");
	
	public ChargeBox(){
		super();
		collider.tx.scale.set(width,width);
		collider.resetMassData();
		maxHealth = 3;
		killForce = 40000000;
		setPoints(30);
		sprite = new Sprite("box_red",collider.tx);
		setArtist(new Artist(){

			@Override
			public boolean isOnScreen(double screenX, double screenY,
					double screenWidth, double screenHeight) {
				return sprite.isOnScreen(screenX,screenY,screenWidth,screenHeight);
			}
			
			private final Vector2d v = new Vector2d();
			@Override
			public void draw(double delta, Renderer2d renderer) {
				if(charging || pausing){
					int clones = 10;
					double dist = (256.0*(1-(pauseTimer/pauseTime)))/clones;
					Vector2d temp = collider.tx.translation;
					collider.tx.translation = v;
					for(int i = clones; i >0; i--){
						v.set(temp.x - (chargeDir.x*dist*i),temp.y - (chargeDir.y*dist*i));
						sprite.alpha = 1.0f-((float)i/(float)clones);
						sprite.draw(delta,renderer);
					}
					collider.tx.translation = temp;
				}
				sprite.alpha = 1;
				sprite.draw(delta,renderer);
			}
			
		});
		collisionSound = new Sound("wood_wack");
        deathSound = new Sound("electric_smash");
        damageSound = new Sound("crate_break");
	}
	
	@Override
	public void init(double x, double y){
		super.init(x,y);
		waitTimer = waitTime;
		randColor();
		sprite.setTexture("box_"+color.name);
		sprite.setTint(0,0,0);
		sprite.tintWeight = 0;
		pauseTimer = pauseTime;
		charging = false;
		pausing = false;
		chargeHit = false;
        damageSound.setVolume(0.5f,0.5f);
        deathSound.setVolume(0.15f,0.15f);
        charge.setVolume(0.5f,0.5f);
	}
	
	public void configure(Config c){
		super.configure(c);
		sprite.setTexture("box_"+color.name);
	}
	
	@Override
	public void update(UpdateEvent evt){
		super.update(evt);
		if(charging == false){
			waitTimer-=evt.getDelta();
			if(waitTimer<=0){
				boolean still = true;
				if(collider.angularVelocity!=0){//spining
					collider.angularDeccelerate(aDeccel,evt.getDelta());
					still=false;
				}else if(collider.tx.theta.val%(Math.PI/2)!=0){//askew
					still=false;
					double d = aFix*evt.getDelta();
					double down = Math.floor(collider.tx.theta.val/(Math.PI/2))*(Math.PI/2);
					double up = Math.ceil(collider.tx.theta.val/(Math.PI/2))*(Math.PI/2);
					double target = (Math.abs(collider.tx.theta.val-down)<Math.abs(collider.tx.theta.val-up)) 
							? down : up;
					if(Math.abs(collider.tx.theta.val-target)<d*2){
						still = true;
						currTheta = target;
						collider.tx.theta.val=target;
					}else{
						if(target<collider.tx.theta.val){
							collider.tx.theta.val-=d;
						}else{
							collider.tx.theta.val+=d;
						}
					}
				}
				if(collider.velocity.getMag()!=0){
					collider.deccelerate(deccel,evt.getDelta());
					still=false;
				}
				if(still && !charging){
					if(!pausing){
						Scene s = Physics.getScene(0);
						Box b = collider.getBounds();
						double minDist = width*2;
						boolean set;
						//find random direction that is not toward a wall
						do{
							int c = (int)(Math.random()*4.0);
							set=true;
							switch(c){
								case 0: 
									if(((s.x+s.width)-(b.getX()+b.getWidth()))>minDist)
										chargeDir.set(1,0);
									else 
										set=false;  
									break;
								case 1: 
									if(((s.y+s.height)-(b.getY()+b.getHeight()))>minDist)
										chargeDir.set(0,1);
									else 
										set=false;  
									break; 
								case 2: 
									if((b.getX()-s.x)>minDist)
										chargeDir.set(-1,0);
									else 
										set=false;
								case 3:
									if((b.getY()-s.y)>minDist)
										chargeDir.set(0,-1);
									else 
										set=false;  
									break; 
							}
						}while(!set);
						pausing = true;
                        charge.play();
					}else{
						pauseTimer-=evt.getDelta();
						if(pauseTimer<=0){
		//					PuckState state = ((PuckState)Entities.getEntity(PuckState.class).getState(0));
		//					double difx = state.collider.tx.translation.x - collider.tx.translation.x;
		//					double dify = state.collider.tx.translation.y - collider.tx.translation.y;
		//					chargeDir.set((difx>dify)?difx:0,(difx>dify)?0:dify);
		//					chargeDir.normalize();
							charging = true;
						}
					}
				}else{
					pauseTimer = pauseTime;
                    if(pausing) {
                        pausing = false;
                        charge.stop();
                    }
				}
			}
			sprite.tintWeight=1.0f - ((float)health/(float)maxHealth);
		}else{
			sprite.tintWeight=0;
			collider.tx.theta.val = currTheta;
			collider.angularVelocity = 0;
			collider.velocity.set(chargeDir);
			collider.velocity.mul(chargeSpeed);
		}
	}
	
	@Override
	public void destroy(){
		super.destroy();
		BoxFragment.spawn(collider,width,width);
        charge.stop();
	}
	

	@Override
	protected ArrayList<PhysShape> getShapes() {
		return shapeAsList(new Polygon(0,2,4,new double[]{
			 0.5,  0.5,
			-0.5,  0.5,
			-0.5, -0.5,
			 0.5, -0.5
		},0,0));
	}
	
	@Override
	protected void onCollision(Manifold m){
		if(!charging){
			super.onCollision(m);
		}else{
			Collider other = (m.a==collider) ? m.b : m.a;
			if(other.getShape(0) instanceof Bounds ){
				charging = false;
				pausing = false;
				chargeHit = false;
				waitTimer = waitTime;
			}else{
				boolean chargeNormal = false;	
				if(m.a==collider){
					chargeNormal = Math.abs(m.normal.x-chargeDir.x)<0.1 && Math.abs(m.normal.y-chargeDir.y)<0.1;
				}else{
					chargeNormal = Math.abs(m.normal.x+chargeDir.x)<0.1 && Math.abs(m.normal.y+chargeDir.y)<0.1;
				}
				if(chargeNormal){
					if(other.state instanceof Enemy){
						if(other.state instanceof Bomb){
							((Bomb)other.state).explode();
						}else{
							((Enemy)other.state).doDamage(1,m);
							if(((Enemy)other.state).health>0){
								charging = false;
								pausing = false;
								waitTimer = waitTime;
							}
						}
						if(other.state instanceof Block || other.state instanceof Splitter){
							charging = false;
							pausing = false;
							chargeHit = false;
							waitTimer = waitTime;
						}
					}else if(other.state instanceof PuckState && !chargeHit){
						((PuckState)other.state).stun();
						((PuckState)other.state).damage(damage);
						chargeHit = true;
					}
				}
			}
			doCollisionSound(Math.sqrt(m.forceSquared));
		}
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
	
	public static final EntityStateFactory getFactory(){
		return new Factory();
	}
	
	public static class Factory extends EntityStateFactory{
		@Override
		public EntityState construct() {
			return new ChargeBox();
		}

		@Override
		public Class<? extends EntityState> getType() {
			return ChargeBox.class;
		}
		
		@Override
		public Config getConfig(){
			return new ChargeBoxConfig();
		}
		
		@Override
		public void getAssets(ArrayList<String[]> assets){
			assets.add(new String[]{"texture","box_red"});
			assets.add(new String[]{"texture","box_green"});
			assets.add(new String[]{"texture","box_pink"});
			assets.add(new String[]{"sound","wood_wack"});
            assets.add(new String[]{"sound","charge"});
            assets.add(new String[]{"sound","crate_break"});
            assets.add(new String[]{"sound","electric_smash"});
		}
	}
	
	public static class ChargeBoxConfig extends EnemyConfig{
		public ChargeBoxConfig(){}
	}

}
