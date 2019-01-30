package com.flickshot.components.entities.defs.spawners;

import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.entities.defs.enemies.Enemy;
import com.flickshot.config.Config;
import com.flickshot.config.Vector2dConfig;
import com.flickshot.geometry.Vector2d;

/**
 * Created by Alex on 4/9/2015.
 */
public class LineSpawner extends Spawner{
    public static final String ENTITY_NAME = "LineSpawner";

    private Vector2d start;
    private Vector2d end;

    private double u;
    private double step;

    public void init(double x, double y){
        super.init(x,y);
        u = 0;
    }

    @Override
    public void spawn() {
        Enemy e = (Enemy) Entities.newInstance(enemy,
                start.x+((end.x-start.x)*u),start.y+((end.y-start.y)*u));
        e.configure(configs[current]);
        u+=step;
    }

    @Override
    public void configure(Config c){
        super.configure(c);

        LineSpawnerConfig config = (LineSpawnerConfig)c;

        step = 1.0/(configs.length-1);

        start = config.startPoint.toVector();
        end = config.endPoint.toVector();
        double theta = config.orientation;
        if(config.rotate) {
            theta = Math.atan2(end.y-start.y,end.x-start.x);
            if (config.useNormal)
                theta += Math.PI/2;
        }

        for(int i = 0; i<configs.length; i++){
            configs[i].orientation = theta;
            configs[i].velocity.x = Math.cos(theta)*config.speed;
            configs[i].velocity.y = Math.sin(theta)*config.speed;
        }
    }

    public static final EntityStateFactory getFactory() {
        return new Factory();
    }

    public static class Factory extends EntityStateFactory{

        @Override
        public EntityState construct() {
            return new LineSpawner();
        }

        @Override
        public Class<? extends EntityState> getType() {
            return LineSpawner.class;
        }

        @Override
        public Config getConfig(){
            return new LineSpawnerConfig();
        }
    }

    public static class LineSpawnerConfig extends SpawnerConfig{
        public Vector2dConfig startPoint;
        public Vector2dConfig endPoint;
        public double speed;
        public double orientation;
        public boolean rotate;
        public boolean useNormal;
    }
}
