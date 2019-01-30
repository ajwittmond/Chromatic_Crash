package com.flickshot.components.entities.defs.managers;

import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.timeline.Timeline;
import com.flickshot.components.timeline.Timelines;
import com.flickshot.config.Config;

/**
 * Created by Alex on 3/25/2015.
 */
public class EntityClearGoToNext extends EntityClearAction{
    public static final String ENTITY_NAME = "EntityClearGoToNext";

    String entity;
    Timeline.TimelineListener tl = new Timeline.TimelineListener(){
        @Override
        public void onEvent(double time) {
            kill();
        }
    };


    public void init(double x, double y){
        super.init(x, y);
    }

    public void doAction(){
        Timelines.get("default").gotoNext();
        kill();
    }

    public void destroy(){
        super.destroy();
        Timelines.get("default").removeListener(tl);
    }

    public void unload(){
        super.unload();
        Timelines.get("default").removeListener(tl);
    }


    public static class Factory extends EntityStateFactory {

        @Override
        public EntityState construct() {
            return new EntityClearGoToNext();
        }

        @Override
        public Class<? extends EntityState> getType() {
            return EntityClearGoToNext.class;
        }

        @Override
        public Config getConfig(){
            return new EntityClearConfig();
        }

    }

    public static EntityStateFactory getFactory(){
        return new Factory();
    }
}
