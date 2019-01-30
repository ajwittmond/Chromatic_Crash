package com.flickshot.components.entities.defs.spawners;

import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.entities.defs.enemies.Enemy;
import com.flickshot.config.BoxConfig;
import com.flickshot.config.Config;
import com.flickshot.geometry.Square;

/**
 * Created by Alex on 4/9/2015.
 */
public class RandomSpawner extends Spawner{
    public static final String ENTITY_NAME = "RandomSpawner";

    public Square box = new Square();
    public double minSpeed;
    public double maxSpeed;

    @Override
    public void spawn() {
        Enemy e = (Enemy) Entities.newInstance(enemy,box.x + (Math.random() * box.width), box.y + (Math.random() * box.height));
        e.configure(configs[current]);
    }

    @Override
    public void configure(Config c){
        super.configure(c);
        box = ((RandomSpawnerConfig)c).box.toSquare();
        minSpeed = ((RandomSpawnerConfig) c).minSpeed;
        maxSpeed = ((RandomSpawnerConfig) c).maxSpeed;
        for(int i = 0; i<configs.length; i++){
            double mag = minSpeed + (Math.random()*(maxSpeed-minSpeed));
            double theta = Math.PI * 2 * Math.random();
            configs[i].velocity.x = Math.cos(theta) * mag;
            configs[i].velocity.y = Math.sin(theta) * mag;
        }
    }

    public static final EntityStateFactory getFactory() {
        return new Factory();
    }

    public static class Factory extends EntityStateFactory{

        @Override
        public EntityState construct() {
            return new RandomSpawner();
        }

        @Override
        public Class<? extends EntityState> getType() {
            return RandomSpawner.class;
        }

        @Override
        public Config getConfig(){
            return new RandomSpawnerConfig();
        }
    }

    public static class RandomSpawnerConfig extends SpawnerConfig{
        public BoxConfig box;

        public double minSpeed;
        public double maxSpeed;
    }
}
