package com.flickshot.components.entities.defs.managers;

import com.flickshot.components.entities.CommonEntity;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.config.Config;

import java.util.HashMap;

/**
 *  This class is the manager for a survival type level where the player gets time for points
 *  and the score is how long they survive
 */
public class SurvivalManager extends CommonEntity {
    public static final String ENTITY_NAME = "SurvivalManager";


    public static final EntityStateFactory getFactory(){
        return new Factory();
    }

    public static class Factory extends EntityStateFactory {
        @Override
        public EntityState construct() {
            return new SurvivalManager();
        }

        @Override
        public Class<? extends EntityState> getType() {
            return SurvivalManager.class;
        }

        @Override
        public Config getConfig(){
            return new SurvivalConfig();
        }
    }

    public static final class SurvivalConfig extends Config{
        public double timeLimit = 30;
        String levelMusic = "having_a_good_time";
        public ScoreConfig scoreConfig = new ScoreConfig();

        public void TimeAttackConfig(){}

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
