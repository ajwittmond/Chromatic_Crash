package com.flickshot.components.entities.defs.enemies.blocks;

import java.util.ArrayList;

import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.entities.defs.enemies.blocks.Block.BlockConfig;
import com.flickshot.components.entities.defs.enemies.blocks.Block.Factory;
import com.flickshot.config.Config;

public class LongBlock extends Block{
	public static final String ENTITY_NAME = "block_long";
	public static final String EDITOR_SPRITE = "block_long_{COLOR};200;100;0";
	
	public LongBlock(){
		super();
		maxHealth = 20;
		setPoints(50);
		collider.tx.scale.set(200,100);
		spriteName="block_long_";
	}
	

	
	public static EntityStateFactory getFactory(){
		return new Factory();
	}
	
	public static class Factory extends EntityStateFactory{
		@Override
		public EntityState construct() {
			return new LongBlock();
		}

		@Override
		public Class<? extends EntityState> getType() {
			return LongBlock.class;
		}
		
		@Override
		public Config getConfig(){
			return new BlockConfig();
		}
		
		@Override
		public void getAssets(ArrayList<String[]> assets){
			assets.add(new String[]{"texture","block_long_red"});
			assets.add(new String[]{"texture","block_long_green"});
			assets.add(new String[]{"texture","block_long_pink"});
            assets.add(new String[]{"texture","flare"});
            assets.add(new String[]{"sound","crate_break"});
            assets.add(new String[]{"sound","concrete_slap"});
		}
	}
	
}
