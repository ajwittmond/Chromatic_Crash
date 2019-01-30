package com.flickshot.components.entities.defs.spawners;

import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.entities.defs.enemies.Enemy;
import com.flickshot.components.physics.Physics;
import com.flickshot.config.Config;
import com.flickshot.geometry.Box;

/**
 * Created by Alex on 4/9/2015.
 */
public class CornersSpawner extends Spawner {
    public static String ENTITY_NAME = "CornersSpawner";

    private int corner;

    private double padX,padY;

    private boolean alternate;
    private boolean clockwise;

    @Override
    public void spawn() {
        doSpawn();
        if(!alternate){
            for(int i = 0; i<3 && current<num; i++){
                current++;
                doSpawn();
            }
        }
    }

    private void doSpawn(){
        Box b = Physics.getScene(0);
        Enemy e = null;
        switch(corner){
            case 0:
                e = (Enemy)Entities.newInstance(enemy,b.getX()+padX,b.getY()+padY);
                break;
            case 1:
                e = (Enemy)Entities.newInstance(enemy,b.getX()+b.getWidth()-padX,b.getY()+padY);
                break;
            case 2:
                e = (Enemy)Entities.newInstance(enemy,b.getX()+b.getWidth()-padX,b.getY()+b.getHeight()-padY);
                break;
            case 3:
                e = (Enemy)Entities.newInstance(enemy,b.getX()+padX,b.getY()+b.getHeight()-padY);
                break;
        }
        e.configure(configs[current]);
        if(clockwise)
            corner++;
        else
            corner--;
        if(corner<0)
            corner = 4+corner;
        else if(corner>=4)
            corner-=4;
    }

    @Override
    public void configure(Config c){
        super.configure(c);
        CornersSpawnerConfig config = (CornersSpawnerConfig)c;

        corner = config.startCorner;
        padX = config.padX;
        padY = config.padY;
        alternate = config.alternate;
        clockwise = config.clockwise;

        int co = corner;

        for(int i = 0; i<configs.length; i++){
            double dir = config.orientation;
            switch(co){
                case 1:
                    dir+=Math.PI/2;
                    break;
                case 2:
                    dir+=Math.PI;
                    break;
                case 3:
                    dir+=Math.PI*(3.0/2.0);
                    break;
            }

            configs[i].velocity.x = Math.cos(dir)*config.speed;
            configs[i].velocity.y = Math.sin(dir)*config.speed;

            if(clockwise)
                co++;
            else
                co--;
            if(co<0)
                co = 4+co;
            else if(co>=4)
                co-=4;
        }
    }

    public static final EntityStateFactory getFactory() {
        return new Factory();
    }

    public static class Factory extends EntityStateFactory{

        @Override
        public EntityState construct() {
            return new CornersSpawner();
        }

        @Override
        public Class<? extends EntityState> getType() {
            return CornersSpawner.class;
        }

        @Override
        public Config getConfig(){
            return new CornersSpawnerConfig();
        }
    }

    public static class CornersSpawnerConfig extends SpawnerConfig{
        public double speed;
        public double orientation;
        public boolean rotate;

        public double padX;
        public double padY;

        public int startCorner;

        public boolean clockwise;

        public boolean alternate;
    }
}