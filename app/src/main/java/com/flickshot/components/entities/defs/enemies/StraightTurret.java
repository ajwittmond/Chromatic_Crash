package com.flickshot.components.entities.defs.enemies;

import com.flickshot.assets.sfx.Sound;
import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.graphics.Sprite;
import com.flickshot.components.physics.Manifold;
import com.flickshot.components.physics.MassData;
import com.flickshot.components.physics.PhysShape;
import com.flickshot.components.physics.shapes.Polygon;
import com.flickshot.config.Config;
import com.flickshot.geometry.Transformation;
import com.flickshot.geometry.Vector2d;
import com.flickshot.scene.Updater;

import java.util.ArrayList;

/**
 * Created by root on 2/20/15.
 */
public class StraightTurret extends Turret{
    public static String ENTITY_NAME = "str_turret";
    public static final String EDITOR_SPRITE = "turret_single_{COLOR};100;125;0";

    Sprite sprite;

    public StraightTurret(){
        maxHealth = 5;
        killForce = 20000000;
        setPoints(30);
        setArtist(sprite = new Sprite("turret_single_red", new Transformation(collider.tx.translation,
                new Vector2d(100, 125), collider.tx.theta)));
        sprite.setTint(0,0,0);
        collider.massData = new MassData(Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY);
        collisionSound = new Sound("wood_wack");
    }

    public void update(Updater.UpdateEvent evt){
        super.update(evt);
        sprite.tintWeight = 1.0f-(float)health/(float)maxHealth;
    }

    @Override
    public void fire() {
        Enemy projectile = (Enemy)Entities.getEntity(this.projectile).newInstance(
                collider.tx.translation.x+(-Math.sin(collider.tx.theta.val)*66),
                collider.tx.translation.y+(Math.cos(collider.tx.theta.val)*66)
            );
        projectile.collider.velocity.setMag(fireSpeed);
        projectile.collider.velocity.setDir(collider.tx.theta.val+(Math.PI/2));
    }

    @Override
    protected ArrayList<PhysShape> getShapes() {
        return shapeAsList(new Polygon(0,2,4,new double[]{
                50,  25,
                -50,  25,
                -50, -62.5,
                50, -62.5
        },0,0));
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
    public void configure(Config c){
        super.configure(c);
        sprite.setTexture("turret_single_"+color.name);
    }

    public static final EntityStateFactory getFactory(){
        return new Factory();
    }

    public static class Factory extends EntityStateFactory{
        @Override
        public EntityState construct() {
            return new StraightTurret();
        }

        @Override
        public Class<? extends EntityState> getType() {
            return StraightTurret.class;
        }

        @Override
        public Config getConfig(){
            return new Turret.TurretConfig();
        }

        @Override
        public void getAssets(ArrayList<String[]> assets){
            assets.add(new String[]{"texture","turret_single_red"});
            assets.add(new String[]{"texture","turret_single_green"});
            assets.add(new String[]{"texture","turret_single_pink"});
            assets.add(new String[]{"texture","circle_small"});
            assets.add(new String[]{"sound","wood_wack"});
            assets.add(new String[]{"sound","cannon"});
            assets.add(new String[]{"sound","concrete_step"});
            assets.add(new String[]{"sound","concrete_slap"});
        }
    }
}
