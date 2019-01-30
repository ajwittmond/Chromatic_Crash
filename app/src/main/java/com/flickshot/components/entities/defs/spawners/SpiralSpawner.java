package com.flickshot.components.entities.defs.spawners;

import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.entities.defs.enemies.Enemy;
import com.flickshot.config.Config;

/**
 * Created by Alex on 4/9/2015.
 */
public class SpiralSpawner extends Spawner{
    public static final String ENTITY_NAME = "SpiralSpawner";

    private double theta;
    private double tStep;

    private double radius;
    private double rStep;

    @Override
    public void spawn() {
        Enemy e = (Enemy) Entities.newInstance(enemy, x + (Math.cos(theta) * radius), y + (Math.sin(theta) * radius));
        e.configure(configs[current]);
        theta += tStep;
        radius += rStep;
    }

    public void configure(Config c){
        super.configure(c);
        SpiralSpawnerConfig config = (SpiralSpawnerConfig)c;
        radius = 0;
        rStep = config.endRadius/(configs.length-1);

        double tt = config.startTheta;
        theta = tt;
        tStep = (Math.PI*2.0*config.rotations)/configs.length;
        if(config.clockWise)
            tStep = -tStep;
        for(int i = 0; i<configs.length; i++){
            if(config.rotate)
                configs[i].orientation = tt+config.orientation;
            else
                configs[i].orientation = config.orientation;
            configs[i].velocity.x = Math.cos(tt)*config.speed;
            configs[i].velocity.y = Math.sin(tt)*config.speed;
            tt+=tStep;
        }
    }

    public static final EntityStateFactory getFactory() {
        return new Factory();
    }


    public static class Factory extends EntityStateFactory{

        @Override
        public EntityState construct() {
            return new SpiralSpawner();
        }

        @Override
        public Class<? extends EntityState> getType() {
            return SpiralSpawner.class;
        }

        @Override
        public Config getConfig(){
            return new SpiralSpawnerConfig();
        }
    }

    public static class SpiralSpawnerConfig extends SpawnerConfig{
        public double speed;
        public double orientation;
        public double rotations;
        public double endRadius;
        public double startTheta;
        public boolean rotate;
        public boolean clockWise;
    }
}
