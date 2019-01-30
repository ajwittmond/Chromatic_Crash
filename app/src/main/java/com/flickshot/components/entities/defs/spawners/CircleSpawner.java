package com.flickshot.components.entities.defs.spawners;

import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.entities.defs.enemies.Enemy;
import com.flickshot.config.Config;

/**
 * Created by Alex on 4/9/2015.
 */
public class CircleSpawner extends Spawner{
    public static String ENTITY_NAME = "CircleSpawner";

    private double theta;
    private double step;
    private double radius;

    @Override
    public void spawn() {
        Enemy e = (Enemy)Entities.newInstance(enemy,x+ (Math.cos(theta)*radius),y+ (Math.sin(theta)*radius));
        e.configure(configs[current]);
        theta+=step;
    }

    @Override
    public void configure(Config c){
        super.configure(c);
        CircleSpawnerConfig config = (CircleSpawnerConfig)c;
        radius = config.radius;
        double tt = config.startTheta;
        theta = tt;
        step = (Math.PI*2.0*config.rotations)/configs.length;
        if(config.clockWise)
            step = -step;
        for(int i = 0; i<configs.length; i++){
            if(config.rotate)
                configs[i].orientation = tt+config.orientation;
            else
                configs[i].orientation = config.orientation;
            configs[i].velocity.x = Math.cos(tt)*config.speed;
            configs[i].velocity.y = Math.sin(tt)*config.speed;
            tt+=step;
        }
    }

    public static final EntityStateFactory getFactory() {
        return new Factory();
    }



    public static class Factory extends EntityStateFactory{

        @Override
        public EntityState construct() {
            return new CircleSpawner();
        }

        @Override
        public Class<? extends EntityState> getType() {
            return CircleSpawner.class;
        }

        @Override
        public Config getConfig(){
            return new CircleSpawnerConfig();
        }
    }

    public static class CircleSpawnerConfig extends SpawnerConfig{
        public double speed;
        public double orientation;
        public double radius;
        public double startTheta;
        public double rotations = 1;
        public boolean rotate;
        public boolean clockWise;
    }
}
