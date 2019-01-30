package com.flickshot.components.entities.defs.enemies;

import java.util.ArrayList;

import com.flickshot.assets.sfx.Sound;
import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.entities.defs.particles.Petal;
import com.flickshot.components.graphics.Sprite;
import com.flickshot.components.particles.ParticleSystem;
import com.flickshot.components.particles.ParticleType;
import com.flickshot.components.particles.Particles;
import com.flickshot.components.physics.PhysShape;
import com.flickshot.components.physics.Physics;
import com.flickshot.components.physics.shapes.Circle;
import com.flickshot.config.Config;
import com.flickshot.geometry.Transformation;
import com.flickshot.geometry.Vector2d;
import com.flickshot.scene.Updater.UpdateEvent;

public class Flower extends Enemy{
	public static final String ENTITY_NAME = "flower";
	public static final String EDITOR_SPRITE = "flower_{COLOR};96;96;0";

    public static final ParticleType petalRedType;
    public static final ParticleSystem petalRed;

    public static final ParticleType petalPinkType;
    public static final ParticleSystem petalPink;

    public static final ParticleType petalGreenType;
    public static final ParticleSystem petalGreen;
    static{
        petalRedType = new ParticleType();
        petalRedType.life = 2;
        petalRedType.z = 1;
        petalRedType.tintWeight = 0;
        petalRedType.alpha(1, 0.5f, 0);
        petalRedType.dimensions(1, 1);
        petalRedType.scale(0, 0);
        petalRedType.texture = "petal_red";

        petalRed= Particles.createSystem("petalRed", petalRedType, 64);

        petalGreenType = new ParticleType();
        petalGreenType.life = 2;
        petalGreenType.z = 1;
        petalGreenType.tintWeight = 0;
        petalGreenType.alpha(1, 0.5f, 0);
        petalGreenType.dimensions(1, 1);
        petalGreenType.scale(0, 0);
        petalGreenType.texture = "petal_green";

        petalGreen= Particles.createSystem("petalGreen", petalGreenType, 64);

        petalPinkType = new ParticleType();
        petalPinkType.life = 2;
        petalPinkType.z = 1;
        petalPinkType.tintWeight = 0;
        petalPinkType.alpha(1, 0.5f, 0);
        petalPinkType.dimensions(1, 1);
        petalPinkType.scale(0, 0);
        petalPinkType.texture = "petal_pink";

        petalPink= Particles.createSystem("petalPink", petalPinkType, 64);
    }

	private final Vector2d dimensions = new Vector2d(96,96);
	
	private final Sprite redSprite;
	private final Sprite greenSprite;
	private final Sprite pinkSprite;
	
	private Sprite sprite;
	
	private double startTime;
	private double time;
	
	Flower(){
		super();
		final Transformation stx = new Transformation(collider.tx.translation,dimensions,collider.tx.theta);
		redSprite =new Sprite("flower_red",stx);
		greenSprite = new Sprite("flower_green",stx);
		pinkSprite = new Sprite("flower_pink",stx);

		randColor();
		setSprite();
		killForce = 10000000;
		deathSound = new Sound("woosh");
	}
	
	private void setSprite(){
		switch(color){
			case RED:
				setArtist(sprite = redSprite);
				break;
			case GREEN:
				setArtist(sprite = greenSprite);
				break;
			case PINK:
				setArtist(sprite = pinkSprite);
				break;
			default:
				throw new IllegalStateException();
		}
		sprite.setTint(0,0,0);
	}
	
	protected ArrayList<PhysShape> getShapes(){
		Circle c = new Circle(0,0,40);
		return shapeAsList(c);
	}
	
	@Override
	public void init(double x, double y) {
		super.init(x,y);
		randColor();
		setSprite();
		collider.dragMul = 0.002;
		collider.velocity.randDir(300);
		time = startTime = 10;
        petalRedType.bounds = Physics.getScene(0);
        petalGreenType.bounds = Physics.getScene(0);
        petalPinkType.bounds = Physics.getScene(0);
    }
	
	public void update(UpdateEvent evt){
		super.update(evt);
		time -= evt.getDelta();
		sprite.tintWeight = (float)(1-(time/startTime));
		if(time<=0) kill();
	}
	
	@Override
	public void destroy(){
		super.destroy();
		spawnFlower(this);
	}

    private static final Vector2d temp = new Vector2d();
    public static void spawnFlower(Flower f){
        final int num = 12;
        temp.set(0,24);
        ParticleSystem part = null;
        switch(f.color){
            case GREEN:
                part = petalGreen;
                break;
            case RED:
                part = petalRed;
                break;
            case PINK:
                part = petalPink;
                break;
        }
        for(int i = 0; i<num; i++){
            double theta = ((Math.PI*2)/12)*i;
            temp.setDir(theta);
            part.create(1, temp.x + f.collider.tx.translation.x, temp.y + f.collider.tx.translation.y, 64, theta, theta, 64);
        }
    }

	@Override
	public void configure(Config c){
		super.configure(c);
		setSprite();
		if(c instanceof FlowerConfig){
			time = startTime =  ((FlowerConfig) c).time;
		}
	}

	public static final EntityStateFactory getFactory(){
		return new Factory();
	}
	
	public static class Factory extends EntityStateFactory{
		@Override
		public EntityState construct() {
			return new Flower();
		}

		@Override
		public Class<? extends EntityState> getType() {
			return Flower.class;
		}
		
		@Override
		public Config getConfig(){
			return new FlowerConfig();
		}
		
		@Override
		public void getAssets(ArrayList<String[]> assets){
			assets.add(new String[]{"texture","flower_red"});
			assets.add(new String[]{"texture","flower_green"});
			assets.add(new String[]{"texture","flower_pink"});
			assets.add(new String[]{"texture","petal_pink"});
			assets.add(new String[]{"texture","petal_green"});
			assets.add(new String[]{"texture","petal_red"});
			assets.add(new String[]{"sound","woosh"});
		}
	}
	
	public static class FlowerConfig extends EnemyConfig{
		public double time = 10;
		public FlowerConfig(){}
	}
}
