package com.flickshot.components.entities.defs.enemies;

import java.util.ArrayList;

import com.flickshot.assets.AssetLibrary;
import com.flickshot.assets.sfx.Sound;
import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.entities.defs.PuckState;
import com.flickshot.components.entities.defs.enemies.ChargeBox.ChargeBoxConfig;
import com.flickshot.components.entities.defs.enemies.ChargeBox.Factory;
import com.flickshot.components.entities.defs.enemies.blocks.Block;
import com.flickshot.components.entities.defs.fx.Explosion;
import com.flickshot.components.graphics.Artist;
import com.flickshot.components.graphics.Graphics;
import com.flickshot.components.graphics.Renderer2d;
import com.flickshot.components.graphics.Screen;
import com.flickshot.components.graphics.Sprite;
import com.flickshot.components.physics.Bounds;
import com.flickshot.components.physics.Collider;
import com.flickshot.components.physics.Manifold;
import com.flickshot.components.physics.PhysShape;
import com.flickshot.components.physics.Physics;
import com.flickshot.components.physics.shapes.Polygon;
import com.flickshot.config.Config;
import com.flickshot.geometry.Box;
import com.flickshot.geometry.Matrix2d;
import com.flickshot.geometry.Transformation;
import com.flickshot.geometry.Vector2d;
import com.flickshot.geometry.collision.CollisionLib;
import com.flickshot.scene.Updater.UpdateEvent;

public class Arrow extends Enemy{
	public static final String ENTITY_NAME = "arrow";
	public static final String EDITOR_SPRITE = "arrow_{COLOR};128;64;0";
	
	private final double deccel = 500;
	private final double aDeccel = Math.PI*8;
	private final double chargeSpeed = 5000;
	private final double aFix = Math.PI/2;

	private final int damage = 10;
	
	private final Vector2d chargeVec = new Vector2d();
	
	private boolean targeted;
	private boolean charging;
	
	private final double pauseTime=0.25;
	private double pauseTimer=0;
	
	private  double initPause;
	
	private final Sprite sprite;

	private boolean chargeHit=false;

    private Sound chargeSound = new Sound("arrow_charge");

	public Arrow(){
		killForce = Double.POSITIVE_INFINITY;
		setPoints(20);
		maxHealth = 2;
		killForce = 20000000;
		
		sprite = new Sprite("arrow_red",new Transformation(collider.tx.translation,new Vector2d(128,64),collider.tx.theta)){
			final Vector2d point = new Vector2d();
			final Vector2d temp = new Vector2d();
			public void draw(double delta, Renderer2d r){
				super.draw(delta,r);
				
				if((collider.velocity.getMag()==0 && collider.angularVelocity==0) || charging){
					temp.set(0,1);
					temp.setDir(collider.tx.theta.val);
					Box screen = Physics.getScene(0);
					Vector2d p = CollisionLib.lineRay(
							screen.getX(),screen.getY(),
							screen.getX()+screen.getWidth(),screen.getY(),
							collider.tx.translation.x,collider.tx.translation.y,
							collider.tx.translation.x+temp.x,collider.tx.translation.y+temp.y,
							point);
					if(p==null){
						p = CollisionLib.lineRay(
							screen.getX()+screen.getWidth(),screen.getY(),
							screen.getX()+screen.getWidth(),screen.getY()+screen.getHeight(),
							collider.tx.translation.x,collider.tx.translation.y,
							collider.tx.translation.x+temp.x,collider.tx.translation.y+temp.y,
							point);
					}
					if(p==null){
						p = CollisionLib.lineRay(
								screen.getX()+screen.getWidth(),screen.getY()+screen.getHeight(),
								screen.getX(),screen.getY()+screen.getHeight(),
								collider.tx.translation.x,collider.tx.translation.y,
								collider.tx.translation.x+temp.x,collider.tx.translation.y+temp.y,
								point);
					}
					if(p==null){
						p = CollisionLib.lineRay(
								screen.getX(),screen.getY()+screen.getHeight(),
								screen.getX(),screen.getY(),
								collider.tx.translation.x,collider.tx.translation.y,
								collider.tx.translation.x+temp.x,collider.tx.translation.y+temp.y,
								point);
					}
					if(p!=null){
						r.translate(0,0,0.1);
						r.lineWidth((targeted)?4:1);
						r.color(1,0,0,(targeted)?1:0.5);
						r.line(collider.tx.translation.x,collider.tx.translation.y,p.x,p.y);
					}
				}
			}
		};
		setArtist(sprite);
		collisionSound = new Sound("wood_wack");
        damageSound = new Sound("splat");
        deathSound = new Sound("messy_splat");
	}
	
	Polygon poly;
	@Override
	protected ArrayList<PhysShape> getShapes() {
		return shapeAsList(poly = new Polygon(0,2,3,new double[]{
			-64,  32,
			-64, -32,
			 64, 0
		},0,0));
	}
	
	public void init(double x, double y){
		super.init(x,y);
		randColor();
		sprite.setTint(0,0,0);
		sprite.tintWeight=0;
		sprite.setTexture("arrow_"+color.name);
		initPause = 1;
		charging = false;
		targeted = false;
		chargeHit = false;
        chargeSound.setVolume(0.1f,0.1f);
        deathSound.setVolume(0.1f,0.1f);
	}
	
	public void configure(Config c){
		super.configure(c);
		sprite.setTexture("arrow_"+color.name);
	}
	
	public void update(UpdateEvent evt){
		super.update(evt);
		collider.tx.setProperAngle();
        if(health>0)
            sprite.tintWeight = 1.0f - ((float)health/(float)maxHealth);
		initPause-=evt.getDelta();
		if(!targeted){
			PuckState puck = ((PuckState)Entities.getEntity(PuckState.class).getState(0));
			if(puck!=null){
				chargeVec.set(puck.collider.tx.translation);
				chargeVec.sub(collider.tx.translation);
				double ptheta = chargeVec.getDir();
				if(ptheta<0) ptheta = (Math.PI*2)+ptheta;
				boolean still=true;
				if(collider.velocity.getMag()!=0){
					collider.deccelerate(deccel,evt.getDelta());
					still = false;
				}
				if(collider.angularVelocity!=0){
					collider.angularDeccelerate(aDeccel,evt.getDelta());
					still = false;
				}else{
					double tDif = ptheta-collider.tx.theta.val;
					if(Math.abs(tDif)>Math.abs((Math.PI*2)-Math.abs(tDif))){
						tDif = Math.abs((Math.PI*2)-Math.abs(tDif))*-(tDif/Math.abs(tDif));
					}
					double ad = aFix * evt.getDelta();
					if(Math.abs(tDif)<ad*2){
						collider.tx.theta.val = ptheta;
					}else{
						still = false;
						collider.tx.theta.val += ad*(tDif/Math.abs(tDif));
					}
				}
				if(still && !targeted){
					chargeVec.setMag(chargeSpeed);
					targeted=true;
                    chargeSound.play();
				}
				pauseTimer = pauseTime;
			}
		}else if(!charging){
			collider.tx.theta.val = chargeVec.getDir();
			collider.velocity.set(0,0);
			collider.angularVelocity = 0;
			pauseTimer-=evt.getDelta();
			charging= pauseTimer<=0;
		}else{
			collider.tx.theta.val = chargeVec.getDir();
			collider.velocity.set(chargeVec);
			collider.angularVelocity = 0;
		}
		if((collider.velocity.getMag()==0 && collider.angularVelocity==0)  || charging){
			sprite.setFrame(1);
		}else{
			sprite.setFrame(0);
		}
	}
	
	public void destroy(){
		super.destroy();
		int particles = 10;
		for(int i = 0; i<particles; i++){
			double theta = Math.PI*2*Math.random();
			double x = collider.tx.translation.x;
			double y = collider.tx.translation.y;
            Explosion.flare.create(1,x,y,300,theta,0,64,color.r*(1.0f-sprite.tintWeight),color.g*(1.0f-sprite.tintWeight),color.b*(1.0f-sprite.tintWeight));

		}
	}
	
	@Override
	public void onDamage(Manifold m){
        super.onDamage(m);
        final int particles = 8;
        double vtheta = m.normal.getDir()+(Math.PI);
        for(int i = 0; i<particles; i++){
            double theta = vtheta+((Math.PI*Math.random())-(Math.PI/2));
            drop.create(
                    1,m.contacts[0].x,m.contacts[0].y,300,theta,0,32,
                    color.r*(1.0f-sprite.tintWeight),color.g*(1.0f-sprite.tintWeight),
                    color.b*(1.0f-sprite.tintWeight));
        }
	}
	
	@Override
	protected void onCollision(Manifold m){
		if(!charging){
			super.onCollision(m);
		}else{
			Collider other = (m.a==collider) ? m.b : m.a;
			if(other.getShape(0) instanceof Bounds || other.state instanceof Block || other.state instanceof Bouncer || other.state instanceof Splitter){
				charging = false;
                chargeSound.stop();
				targeted = false;
				chargeHit = false;
			}else if(other.state instanceof PuckState){
				if(!chargeHit){
					((PuckState)other.state).stun();
					((PuckState)other.state).damage(10);
				}
				chargeHit = true;
			}else if(other.state instanceof Bomb){
				((Bomb)other.state).explode();
				charging = false;
				targeted = false;
				chargeHit = false;
                chargeSound.stop();
			}
			doCollisionSound(Math.sqrt(m.forceSquared));
		}
	}
	
	public static final EntityStateFactory getFactory(){
		return new Factory();
	}
	
	public static class Factory extends EntityStateFactory{
		@Override
		public EntityState construct() {
			return new Arrow();
		}

		@Override
		public Class<? extends EntityState> getType() {
			return Arrow.class;
		}
		
		@Override
		public Config getConfig(){
			return new ArrowConfig();
		}
		
		@Override
		public void getAssets(ArrayList<String[]> assets){
			assets.add(new String[]{"texture","arrow_red"});
			assets.add(new String[]{"texture","arrow_green"});
			assets.add(new String[]{"texture","arrow_pink"});
			assets.add(new String[]{"sound","wood_wack"});
            assets.add(new String[]{"sound","arrow_charge"});
            assets.add(new String[]{"sound","splat"});
            assets.add(new String[]{"sound","messy_splat"});
		}
	}

	public static class ArrowConfig extends EnemyConfig{
		public ArrowConfig(){}
	}
}
