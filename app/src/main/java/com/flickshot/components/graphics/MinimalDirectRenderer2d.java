package com.flickshot.components.graphics;

import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.GL_LEQUAL;
import static android.opengl.GLES20.glDepthFunc;
import static android.opengl.GLES20.glDisable;
import static android.opengl.GLES20.glEnable;

import com.flickshot.components.graphics.DirectRenderer2d.RenderEvent;
import com.flickshot.geometry.Box;

/**
 * Exactly the same as DirectRenderer2d except that it does not log events that do
 * not need to be sorted before drawing.  It instead draws the immediately as they are
 * processed.  This is more memory efficient.
 * @author Alex
 *
 */
public class MinimalDirectRenderer2d extends DirectRenderer2d{
	MinimalDirectRenderer2d(){super();}
	
	@Override
	void background(Background b,Box s){
		super.background(b,s);
		glDisable(GL_DEPTH_TEST);
		
		drawBackground();
	
		glEnable(GL_DEPTH_TEST);
		glDepthFunc(GL_LEQUAL);
	}
	
	@Override
	public void addEvent(RenderEvent evt){
		if(evt.isSprite || evt.a<1){//has transparency
			transparentEvents.add(evt);
		}else{//is solid
			evt.render(program);
			eventPos--;//allows reuse immediately
		}
	}
}
