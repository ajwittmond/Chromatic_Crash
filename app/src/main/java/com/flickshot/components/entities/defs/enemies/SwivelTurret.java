package com.flickshot.components.entities.defs.enemies;

import java.util.ArrayList;

import com.flickshot.assets.sfx.Sound;
import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.entities.defs.PuckState;
import com.flickshot.components.graphics.Artist;
import com.flickshot.components.graphics.Renderer2d;
import com.flickshot.components.graphics.Sprite;
import com.flickshot.components.physics.Manifold;
import com.flickshot.components.physics.MassData;
import com.flickshot.components.physics.PhysShape;
import com.flickshot.components.physics.shapes.Circle;
import com.flickshot.components.physics.shapes.Polygon;
import com.flickshot.config.Config;
import com.flickshot.geometry.Transformation;
import com.flickshot.geometry.Vector2d;
import com.flickshot.scene.Updater.UpdateEvent;
import com.flickshot.util.MutableDouble;

public class SwivelTurret extends Turret{
	public static String ENTITY_NAME = "swivel_turret";
	public static final String EDITOR_SPRITE = "swivel_turret_{COLOR};125;100;0";

	final Sprite sprite;
	
	double turretSpeed = Math.PI*2;
	
	double turretTheta = 0;
	
	public SwivelTurret(){
		super();
        maxHealth = 10;
        killForce = 20000000;
        setPoints(30);
		collider.tx.scale.set(64,64);
		collider.massData = new MassData(Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY);
		sprite = new Sprite("swivel_turret_red",new Transformation(new Vector2d(),new Vector2d(125,100),new MutableDouble()));
		sprite.setTint(0,0,0);
		setArtist(new Artist(){

			@Override
			public boolean isOnScreen(double screenX, double screenY,
					double screenWidth, double screenHeight) {
				return true;
			}

			@Override
			public void draw(double delta, Renderer2d renderer) {
				renderer.translate(collider.tx.translation.x, collider.tx.translation.y, -0.1);
				renderer.rotate(turretTheta);
				renderer.translate(62.5-50,0,0);
				sprite.draw(delta,renderer);
			}
			
		});
        collisionSound = new Sound("wood_wack");
	}
	
	private final Vector2d puckVec = new Vector2d();
	public void update(UpdateEvent evt){

        sprite.tintWeight = 1.0f-(float)health/(float)maxHealth;

		PuckState puck = (PuckState)Entities.getEntity(PuckState.class).getState(0);
		if(puck==null)return;
		puckVec.set(puck.collider.tx.translation);
		puckVec.sub(collider.tx.translation);
		
		turretTheta %= Math.PI*2;
		if(turretTheta<0) turretTheta += (Math.PI*2);
		
		double ptheta = puckVec.getDir();
		ptheta %= Math.PI*2;
		if(ptheta<0) ptheta +=(Math.PI*2);
		
		double tDif = ptheta-turretTheta;
		if(Math.abs(tDif)>Math.abs((Math.PI*2)-Math.abs(tDif))){
			tDif = Math.abs((Math.PI*2)-Math.abs(tDif))*-(tDif/Math.abs(tDif));
		}
		double ad = turretSpeed * evt.getDelta();
		if(Math.abs(tDif)<ad*2){
			turretTheta = ptheta;
		}else if(ad!=0){
			turretTheta += ad*(tDif/Math.abs(tDif));
		}

        super.update(evt);
	}

    @Override
    public void fire() {
        puckVec.setMag(150);
        Enemy projectile =(Enemy)Entities.getEntity(this.projectile).newInstance(
                collider.tx.translation.x+Math.cos(turretTheta)*75,collider.tx.translation.y +Math.sin(turretTheta)*75);
        projectile.collider.velocity.setMag(fireSpeed);
        projectile.collider.velocity.setDir(turretTheta);
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

    @Override
	protected ArrayList<PhysShape> getShapes() {
		return shapeAsList(new Circle(0,0,50));
	}

    @Override
    public void configure(Config c){
        super.configure(c);
        sprite.setTexture("swivel_turret_"+color.name);
    }

	public static final EntityStateFactory getFactory(){
		return new Factory();
	}
	
	public static class Factory extends EntityStateFactory{
		@Override
		public EntityState construct() {
			return new SwivelTurret();
		}

		@Override
		public Class<? extends EntityState> getType() {
			return SwivelTurret.class;
		}
		
		@Override
		public Config getConfig(){
			return new Turret.TurretConfig();
		}
		
		@Override
		public void getAssets(ArrayList<String[]> assets){
			assets.add(new String[]{"texture","swivel_turret_red"});
            assets.add(new String[]{"texture","swivel_turret_green"});
            assets.add(new String[]{"texture","swivel_turret_pink"});
			assets.add(new String[]{"texture","circle_small"});
            assets.add(new String[]{"sound","wood_wack"});
            assets.add(new String[]{"sound","cannon"});
            assets.add(new String[]{"sound","concrete_step"});
            assets.add(new String[]{"sound","concrete_slap"});
		}
	}
}
