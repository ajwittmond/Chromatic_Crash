package com.flickshot.components.entities.defs.spawners;

import com.flickshot.components.entities.CommonEntity;
import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.defs.enemies.Enemy;
import com.flickshot.components.entities.defs.enemies.EnemyConfig;
import com.flickshot.config.Config;
import com.flickshot.config.Vector2dConfig;
import com.flickshot.scene.Updater;

import java.util.ArrayList;

/**
 * Created by Alex on 4/9/2015.
 */
public abstract class Spawner extends CommonEntity{
    protected EnemyConfig[] configs;

    protected String enemy;
    protected int num;
    protected double interval;

    private double dt;
    protected int current;

    double x,y;

    @Override
    public void init(double x, double y){
        super.init(x,y);
        dt = 0;
        current = 0;
        this.x = x;
        this.y = y;
    }

    public void update(Updater.UpdateEvent evt){
        super.update(evt);
        dt -= evt.getDelta();
        if(dt<0){
            spawn();
            dt = interval;
            current++;
            if(current>=num)
                kill();
        }
    }

    public abstract void spawn();


    @Override
    public void configure(Config c){
        SpawnerConfig config = (SpawnerConfig)c;
        enemy = config.enemy;
        num = config.number;
        interval = config.interval;

        configs = new EnemyConfig[num];
        for(int i = 0; i<configs.length; i++){
            configs[i] = (EnemyConfig)Entities.getEntity(enemy).factory.getConfig();
            configs[i].velocity = new Vector2dConfig();
            configs[i].color = config.colors[i%config.colors.length];
        }
    }
}
