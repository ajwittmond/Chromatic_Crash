package com.flickshot.components.entities.defs.enemies;

import java.util.HashMap;

import com.flickshot.components.entities.defs.enemies.Enemy.ColorType;
import com.flickshot.config.Config;
import com.flickshot.config.Vector2dConfig;

public class EnemyConfig extends Config{
	
	public static enum Color{
		RED(){
			@Override
			public ColorType getColor() {
				return ColorType.RED;
			}
		},
		GREEN(){
			@Override
			public ColorType getColor() {
				return ColorType.GREEN;
			}
		},
		PINK(){
			@Override
			public ColorType getColor() {
				return ColorType.PINK;
			}
		},
		RANDOM(){
			@Override
			public ColorType getColor() {
				ColorType color = ColorType.RED;
				double r = Math.random();
				if(r<=1.0/3.0){
					color = ColorType.PINK;	
				}else if(r<=2.0/3.0){
					color = ColorType.GREEN;
				}
				return color;
			}
		};
		public abstract ColorType getColor();
	}
	
	public Color color = Color.RANDOM;
	
	public Vector2dConfig velocity;
	public double angularVelocity;
	
	public double orientation;
	
	public EnemyConfig(){}
	
	@Override
	public void setValue(String text) {
	}

	@Override
	public void getAliases(HashMap<String, String> map) {
	}

}
