package com.flickshot.components.entities.defs.spawners;

import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.entities.defs.enemies.Enemy;
import com.flickshot.config.BoxConfig;
import com.flickshot.config.Config;
import com.flickshot.geometry.Square;

/**
 * Created by Alex on 4/11/2015.
 */
public class BoxSpawner extends Spawner{
    public static final String ENTITY_NAME = "BoxSpawner";

    private Square box;

    private int n;
    private int s;
    private int w;
    private int e;

    int corner;
    int curr;
    @Override
    public void spawn() {
        Enemy en = null;
        switch(corner){
            case 0:
                en = (Enemy)Entities.newInstance(enemy,
                        box.x + (box.width * ((double)curr/(double)s)), box.y);
                if(curr>=s-1){
                    curr = 0;
                    corner++;
                }else{
                    curr++;
                }
                break;
            case 1:
                en = (Enemy)Entities.newInstance(enemy,
                        box.x + box.width, box.y + (box.height * ((double)curr/(double)e)));
                if(curr>=e-1){
                    curr = 0;
                    corner++;
                }else{
                    curr++;
                }
                break;
            case 2:
                en = (Enemy)Entities.newInstance(enemy,
                        box.x + (box.width * (1.0 - (double)curr/(double)n )), box.y + box.height);
                if(curr>=n-1){
                    curr = 0;
                    corner++;
                }else{
                    curr++;
                }
                break;
            case 3:
                en = (Enemy)Entities.newInstance(enemy,
                        box.x , box.y + (box.height * (1.0 - (double)curr/(double)w)));
                if(curr>=w-1){
                    curr = 0;
                    corner = 0;
                }else{
                    curr++;
                }
                break;
        }
        en.configure(configs[current]);
    }

    @Override
    public void configure(Config c){
        super.configure(c);
        BoxSpawnerConfig config = (BoxSpawnerConfig)c;

        box = config.box.toSquare();

        int sideNum = num/4;
        int rem = num%4;

        n = sideNum + Math.max(0,rem--);
        s = sideNum + Math.max(0,rem--);
        e = sideNum + Math.max(0,rem--);
        w = sideNum + Math.max(0,rem--);

        corner = config.startCorner;

        int co = corner;
        curr = 0;

        for(int i = 0; i<configs.length; i++){
            double dir = config.orientation;
            if(config.rotate){
               switch(co) {
                   case 0:
                       if(curr>=s-1){
                           curr = 0;
                           co++;
                       }else{
                           curr++;
                       }
                       break;
                   case 1:
                       dir+=Math.PI/2;
                       if(curr>=e-1){
                           curr = 0;
                           co++;
                       }else{
                           curr++;
                       }
                       break;
                   case 2:
                       dir+=Math.PI;
                       if(curr>=n-1){
                           curr = 0;
                           co++;
                       }else{
                           curr++;
                       }
                       break;
                   case 3:
                       dir+=Math.PI*(3.0/2.0);
                       if(curr>=w-1){
                           curr = 0;
                           co = 0;
                       }else{
                           curr++;
                       }
                       break;
               }
            }
            configs[i].velocity.x = Math.cos(dir)*config.speed;
            configs[i].velocity.y = Math.sin(dir)*config.speed;
        }
        curr = 0;
    }

    public static final EntityStateFactory getFactory() {
        return new Factory();
    }


    public static class Factory extends EntityStateFactory{

        @Override
        public EntityState construct() {
            return new BoxSpawner();
        }

        @Override
        public Class<? extends EntityState> getType() {
            return BoxSpawner.class;
        }

        @Override
        public Config getConfig(){
            return new BoxSpawnerConfig();
        }
    }

    public static class BoxSpawnerConfig extends SpawnerConfig{
        public BoxConfig box;

        public double orientation;
        public double speed;

        public int startCorner;

        public boolean rotate;
    }
}
