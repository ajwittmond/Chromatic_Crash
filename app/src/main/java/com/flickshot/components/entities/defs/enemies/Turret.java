package com.flickshot.components.entities.defs.enemies;

import com.flickshot.assets.sfx.Sound;
import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.defs.fx.Explosion;
import com.flickshot.components.physics.Physics;
import com.flickshot.config.Config;
import com.flickshot.scene.Updater;

/**
 * Created by Alex on 3/10/2015.
 */
public abstract class Turret extends Enemy{
    protected double fireSpeed = 1000;
    protected double firePeriod=1;
    protected double fireTimer;
    protected String projectile;
    private static Sound fireSound = new Sound("cannon");

    public Turret(){
        deathSound = new Sound("concrete_step");
        damageSound = new Sound("concrete_slap");
    }

    public void init(double x, double y){
        super.init(x,y);
        fireTimer=firePeriod;
        deathSound.setVolume(1, 1);
//        damageSound.setVolume(0.5f,0.5f);
//        fireSound.setVolume(0.6f,0.6f);
        Explosion.flareType.bounds = Physics.getScene(0);
    }

    @Override
    public void update(Updater.UpdateEvent evt){
        super.update(evt);
        fireTimer-=evt.getDelta();
        if(fireTimer<=0){
            fireSound.play();
            fire();
            fireTimer = firePeriod;
        }
    }

    @Override
    public void destroy(){
        super.destroy();
        int particles = 14;
        for(int i = 0; i<particles; i++){
            double theta = Math.PI*2*Math.random();
            double dist = 30*Math.random();
            double x = collider.tx.translation.x + (dist*Math.cos(theta));
            double y = collider.tx.translation.y + (dist*Math.sin(theta));
            Explosion.flare.create(1,x,y,300,theta,0,64,0,0,0);
        }
    }

    @Override
    public void configure(Config c){
        super.configure(c);
        if(c instanceof TurretConfig){
            TurretConfig t = (TurretConfig)c;
            firePeriod = t.firePeriod;
            fireTimer = firePeriod;
            projectile = t.projectile;
        }else{
            throw new IllegalStateException();
        }
    }

    public abstract void fire();



    public static class TurretConfig extends EnemyConfig{
        public String projectile = "bullet";
        public double firePeriod = 1;


        public TurretConfig(){}
    }
}
