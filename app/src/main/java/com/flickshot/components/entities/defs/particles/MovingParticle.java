package com.flickshot.components.entities.defs.particles;

import com.flickshot.components.entities.defs.VisibleEntity;
import com.flickshot.components.graphics.Sprite;
import com.flickshot.geometry.Box;
import com.flickshot.geometry.Transformable;
import com.flickshot.geometry.TransformableInterface;
import com.flickshot.geometry.Transformation;
import com.flickshot.geometry.Vector2d;
import com.flickshot.scene.Updater.UpdateEvent;

public abstract class MovingParticle extends VisibleEntity{
	protected Sprite sprite;
	protected Box bounds;
	
	public final Vector2d velocity = new Vector2d();
	public double angularVelocity;
	
	public final Vector2d position = new Vector2d();
	public double orientation;
	
	public void init(double x, double y){
		super.init(x,y);
		position.set(x,y);
		orientation = 0;
		angularVelocity = 0;
		velocity.x = 0;
		velocity.y =0;
		
		if(sprite!=null){
			sprite.setTheta(orientation);
			sprite.setCX(position.x);
			sprite.setCY(position.y);
		}
	}
	
	public void update(UpdateEvent evt){
		super.update(evt);
		
		double d = evt.getDelta();
		position.x+=velocity.x*d;
		position.y+=velocity.y*d;
		orientation+=angularVelocity*d;
		
		if(bounds!=null){
			double left = bounds.getX();
			double right = bounds.getX()+bounds.getWidth();
			double bottom = bounds.getY();
			double top = bounds.getY() + bounds.getHeight();
			if(position.x<left){
				position.x=left;
				if(velocity.x<0)velocity.x=-velocity.x;
			}
			if(position.x>right){
				position.x=right;
				if(velocity.x>0)velocity.x=-velocity.x;
			}
			if(position.y<bottom){
				position.y=bottom;
				if(velocity.y<0)velocity.y=-velocity.y;
			}
			if(position.y>top){
				position.y=top;
				if(velocity.y>0)velocity.y=-velocity.y;
			}
		}
		
		if(sprite!=null){
			sprite.setTheta(orientation);
			sprite.setCX(position.x);
			sprite.setCY(position.y);
		}
				
	}
}
