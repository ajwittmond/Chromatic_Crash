package com.flickshot.components.entities.defs.managers;

import com.flickshot.components.entities.CommonEntity;
import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.entities.defs.enemies.Enemy;
import com.flickshot.components.entities.defs.enemies.InvulnerableBouncer;
import com.flickshot.components.entities.defs.fx.TextBlob;
import com.flickshot.components.entities.defs.spawners.Spawner;
import com.flickshot.components.graphics.Graphics;
import com.flickshot.components.graphics.Screen;
import com.flickshot.config.Config;
import com.flickshot.scene.Updater;

/**
 * Created by Alex on 4/20/2015.
 */
public class WaveEnd extends CommonEntity {
    public static final String ENTITY_NAME = "WaveEnd";

    double timer;
    boolean blobShown;

    public void init(double x, double y){
        super.init(x,y);
        timer = 1;
        blobShown = false;
    }

    public void update(Updater.UpdateEvent evt){
        int num;
        num = Entities.getStateCount(Enemy.class);
        num -=Entities.getStateCount(InvulnerableBouncer.class);
        num += Entities.getStateCount(Spawner.class);
        if(num<=0){
            if(!blobShown){
                Screen screen = Graphics.getCurrentScene().screen;
                TextBlob.create("Wave Complete!", screen.getCX(), screen.getCY(), 512, 1024, 1, 1, 1, 1);
                blobShown = true;
            }
            timer-=evt.getDelta();
            if(timer<=0){
                ((InfiniteModeManager)Entities.getEntity(InfiniteModeManager.class).getState(0))
                        .newWave();
                kill();
            }
        }
    }

    public static class Factory extends EntityStateFactory{

        @Override
        public EntityState construct() {
            return new WaveEnd();
        }

        @Override
        public Class<? extends EntityState> getType() {
            return WaveEnd.class;
        }

    }

    public static EntityStateFactory getFactory(){
        return new Factory();
    }
}
