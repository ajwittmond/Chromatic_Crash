package com.flickshot.components.entities.defs.enemies;

import java.util.ArrayList;

import com.flickshot.components.entities.CommonEntity;
import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.physics.Physics;
import com.flickshot.components.physics.Scene;
import com.flickshot.scene.Updater.UpdateEvent;

public class AreaSpawner extends CommonEntity{
	public static final String ENTITY_NAME = "AreaSpawner";

	public String entity = "flower";
	public double time = 2;
	
	private double dt = time;
	
	@Override
	public void update(UpdateEvent evt){
		dt-=evt.getDelta();
		if(dt<=0){
			Scene scene = Physics.getScene(0);
			Entities.getEntity(entity).newInstance(scene.x+scene.width*Math.random(),scene.y+scene.height*Math.random());
			dt=time;
		}
	}
	
	public static final EntityStateFactory getFactory(){
		return new EntityStateFactory(){
			@Override
			public EntityState construct() {
				return new AreaSpawner();
			}

			@Override
			public Class<? extends EntityState> getType() {
				return AreaSpawner.class;
			}
			
			@Override
			public void getAssets(ArrayList<String[]> assets){
				assets.add(new String[]{"texture","flower_pink"});
				assets.add(new String[]{"texture","flower_green"});
				assets.add(new String[]{"texture","flower_red"});
			}
		};
	}
}
