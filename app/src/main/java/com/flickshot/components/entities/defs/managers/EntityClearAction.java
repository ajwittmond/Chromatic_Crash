package com.flickshot.components.entities.defs.managers;

import com.flickshot.components.entities.CommonEntity;
import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.defs.enemies.Enemy;
import com.flickshot.components.entities.defs.enemies.InvulnerableBouncer;
import com.flickshot.components.timeline.Timelines;
import com.flickshot.config.Config;
import com.flickshot.scene.Updater;

import java.util.HashMap;

/**
 * Created by Alex on 3/25/2015.
 */
public abstract class EntityClearAction extends CommonEntity {

    String entity;

    public abstract void doAction();

    public void init(double x, double y){
        super.init(x,y);
        entity=null;
    }

    public void update(Updater.UpdateEvent evt){
        super.update(evt);
        int num;
        if(entity==null || entity.equals("") || entity.equalsIgnoreCase("null")){
            num = Entities.getStateCount(Enemy.class);
            num -=Entities.getStateCount(InvulnerableBouncer.class);
        }else{
            num = Entities.getStateCount(entity);
            if (Entities.getEntity(entity).stateType.isAssignableFrom(InvulnerableBouncer.class))
                num -= Entities.getStateCount(InvulnerableBouncer.class);
        }
        if(num<=0){
            doAction();
        }
    }

    @Override
    public void configure(Config c){
        if(c instanceof EntityClearConfig)
            entity = ((EntityClearConfig)c).entity;
    }

    public static class EntityClearConfig extends Config {
        public String entity;
        @Override
        public void setValue(String text) {
            // TODO Auto-generated method stub

        }

        @Override
        public void getAliases(HashMap<String, String> map) {
            // TODO Auto-generated method stub

        }



    }
}
