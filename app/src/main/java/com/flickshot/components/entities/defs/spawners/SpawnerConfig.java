package com.flickshot.components.entities.defs.spawners;

import com.flickshot.components.entities.defs.enemies.Enemy;
import com.flickshot.components.entities.defs.enemies.EnemyConfig;
import com.flickshot.config.Config;

import java.util.HashMap;

/**
 * Created by Alex on 4/9/2015.
 */
public class SpawnerConfig extends Config {
    public String enemy = "flower";
    public double interval = 1;
    public int number = 1;

    public EnemyConfig.Color[] colors = new EnemyConfig.Color[]{EnemyConfig.Color.RANDOM};

    public boolean random;

    @Override
    public void setValue(String text) {
    }

    @Override
    public void getAliases(HashMap<String, String> map) {
    }
}
