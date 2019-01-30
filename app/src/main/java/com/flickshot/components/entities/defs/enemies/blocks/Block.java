package com.flickshot.components.entities.defs.enemies.blocks;

import java.util.ArrayList;

import com.flickshot.assets.sfx.Sound;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.entities.defs.enemies.Enemy;
import com.flickshot.components.entities.defs.enemies.EnemyConfig;
import com.flickshot.components.entities.defs.enemies.ChargeBox.ChargeBoxConfig;
import com.flickshot.components.entities.defs.enemies.ChargeBox.Factory;
import com.flickshot.components.entities.defs.fx.BoxFragment;
import com.flickshot.components.entities.defs.fx.Explosion;
import com.flickshot.components.graphics.Sprite;
import com.flickshot.components.physics.Manifold;
import com.flickshot.components.physics.MassData;
import com.flickshot.components.physics.PhysMaterial;
import com.flickshot.components.physics.PhysShape;
import com.flickshot.components.physics.Physics;
import com.flickshot.components.physics.shapes.Polygon;
import com.flickshot.config.Config;
import com.flickshot.scene.Updater.UpdateEvent;

public class Block extends Enemy{
	public static final String ENTITY_NAME = "block";
	public static final String EDITOR_SPRITE = "block_{COLOR};100;100;0";
	
	private final static double width = 100;
	
	protected String spriteName = "block_";
	
	Sprite sprite;
	
	public Block(){
		super();
		maxHealth = 10;
		killForce = 20000000;
		setPoints(25);
		collider.tx.scale.set(width, width);
		collider.massData = new MassData(Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY);
        setArtist(sprite = new Sprite(sprite + "red", collider.tx));
		sprite.setTint(0,0,0);
        deathSound = new Sound("crate_break");
        damageSound = new Sound("concrete_slap");
	}

    public void init(double x, double y){
        super.init(x,y);
        deathSound.setVolume(0.8f,0.8f);
//        damageSound.setVolume(0.5f,0.5f);
    }

	public void update(UpdateEvent evt){
		super.update(evt);
		sprite.tintWeight = 1.0f-(float)health/(float)maxHealth;
	}

	public void destroy(){
		super.destroy();
		BoxFragment.spawn(collider,collider.tx.scale.x,collider.tx.scale.y);
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
	
	protected PhysMaterial getMaterial(){
		return new PhysMaterial(1,0,0,0);
	}
	
	public static EntityStateFactory getFactory(){
		return new Factory();
	}
	
	public void configure(Config c){
		super.configure(c);
		sprite.setTexture(spriteName+color.name);
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
	
	public static class Factory extends EntityStateFactory{
		@Override
		public EntityState construct() {
			return new Block();
		}

		@Override
		public Class<? extends EntityState> getType() {
			return Block.class;
		}
		
		@Override
		public Config getConfig(){
			return new BlockConfig();
		}
		
		@Override
		public void getAssets(ArrayList<String[]> assets){
			assets.add(new String[]{"texture","block_red"});
			assets.add(new String[]{"texture","block_green"});
			assets.add(new String[]{"texture","block_pink"});
            assets.add(new String[]{"texture","flare"});
            assets.add(new String[]{"sound","concrete_slap"});
            assets.add(new String[]{"sound","crate_break"});
		}
	}
	
	public static class BlockConfig extends EnemyConfig{
		public BlockConfig(){}
	}

}
