package com.flickshot.components.entities.defs.enemies;

import com.flickshot.assets.sfx.Sound;
import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.graphics.Renderer2d;
import com.flickshot.components.graphics.Sprite;
import com.flickshot.components.physics.Manifold;
import com.flickshot.components.physics.MassData;
import com.flickshot.components.physics.PhysShape;
import com.flickshot.components.physics.shapes.Polygon;
import com.flickshot.config.Config;
import com.flickshot.geometry.Transformation;
import com.flickshot.geometry.Vector2d;
import com.flickshot.scene.Updater;
import com.flickshot.util.MiscLib;

import java.util.ArrayList;

/**
 * Created by root on 2/20/15.
 */
public class SquareTurret extends Turret{
    public static String ENTITY_NAME = "square_turret";
    public static final String EDITOR_SPRITE = "turret_square_{COLOR};125;125;0";

    Sprite sprite;

    public SquareTurret(){
        maxHealth = 5;
        killForce = 20000000;
        setPoints(30);
        setArtist(sprite = new Sprite("turret_square_red", new Transformation(collider.tx.translation,
                new Vector2d(125, 125), collider.tx.theta)));
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
        double theta = collider.tx.theta.val;
        for(int i = 0; i<4; i++){
            Enemy projectile = (Enemy) Entities.getEntity(this.projectile).newInstance(
                    collider.tx.translation.x+(Math.cos(theta)*116),
                    collider.tx.translation.y+(Math.sin(theta)*116)
            );
            projectile.collider.velocity.setMag(fireSpeed);
            projectile.collider.velocity.setDir(theta);
            theta+=Math.PI/2;
        }

    }
    @Override
    protected ArrayList<PhysShape> getShapes() {
        //create octagon
        double[] verts = new double[16];
        double theta = MiscLib.DEG_TO_RAD*22.5;
        double width = 100;
        double radius = (width/2)/Math.cos(theta);
        for(int i = 0; i<16; i+=2){
            verts[i] = Math.cos(theta)*radius;
            verts[i+1] = Math.sin(theta)*radius;
            theta+=MiscLib.DEG_TO_RAD*45;
        }

        return shapeAsList(new Polygon(0,2,8,verts,0,0));
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
        sprite.setTexture("turret_square_"+color.name);
    }

    public static final EntityStateFactory getFactory(){
        return new Factory();
    }

    public static class Factory extends EntityStateFactory{
        @Override
        public EntityState construct() {
            return new SquareTurret();
        }

        @Override
        public Class<? extends EntityState> getType() {
            return SquareTurret.class;
        }

        @Override
        public Config getConfig(){
            return new Turret.TurretConfig();
        }

        @Override
        public void getAssets(ArrayList<String[]> assets){
            assets.add(new String[]{"texture","turret_square_red"});
            assets.add(new String[]{"texture","turret_square_green"});
            assets.add(new String[]{"texture","turret_square_pink"});
            assets.add(new String[]{"texture","circle_small"});
            assets.add(new String[]{"sound","wood_wack"});
            assets.add(new String[]{"sound","cannon"});
            assets.add(new String[]{"sound","concrete_step"});
            assets.add(new String[]{"sound","concrete_slap"});
        }
    }
}
