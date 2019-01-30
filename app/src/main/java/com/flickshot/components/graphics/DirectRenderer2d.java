package com.flickshot.components.graphics;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import com.flickshot.GameView;
import com.flickshot.assets.textures.SpriteSheet;
import com.flickshot.components.graphics.GLFont.FontData;
import com.flickshot.components.graphics.program.GLProgram;
import com.flickshot.components.particles.ParticleSystem;
import com.flickshot.components.particles.Particles;
import com.flickshot.components.physics.shapes.Circle;
import com.flickshot.components.physics.shapes.Polygon;
import com.flickshot.geometry.Box;
import com.flickshot.geometry.Matrix2d;
import com.flickshot.geometry.Transformation;
import com.flickshot.geometry.Vector2d;

import android.content.res.Resources;
import android.opengl.GLES20;
import android.util.Log;

import static com.flickshot.util.MiscLib.*;

import static android.opengl.GLES20.*;
import static android.opengl.Matrix.*;

/**
 * This is a simple renderer that does not do any batching, it simply logs all draw calls and then 
 * processes them individually.
 * @author Alex
 *
 */
public class DirectRenderer2d extends Renderer2d{
	
	private final ArrayList<RenderEvent> eventPool = new ArrayList<RenderEvent>();
	protected int eventPos = 0;
	
	protected final EventTree transparentEvents = new EventTree();
	
	protected Background background;
	protected Box screen;
	
	protected RenderEvent eventQueue = null;
	protected RenderEvent last;
	
	protected GLProgram program;
	
	public DirectRenderer2d(){
		super();
		float[] m = new float[16];
		setIdentityM(m,0);
		identityMatrix.put(m).position(0);
	}

	private RenderEvent getEvent(){
		if(eventPos>=eventPool.size())eventPool.add(new RenderEvent());
		return eventPool.get(eventPos++);
	}
	
	protected void addEvent(RenderEvent evt){
		if(evt.isSprite || evt.a<1){//has transparency
			transparentEvents.add(evt);
		}else{//is solid
			if(eventQueue==null){
				eventQueue = evt;
                evt.next=null;
				last=evt;
			}else{
				last.next = evt;
				last = evt;
				evt.next=null;
			}
		}
	}
	
	@Override
	protected void startFrame() {
		if(program==null){
			Resources resources = GameView.getMain().getResources();
			try{
				program = new GLProgram("shape",resources.getAssets().open("general.vs"),
						resources.getAssets().open("general.fs"));
			}catch(Exception ex){
				throw new IllegalStateException("failed to init shader",ex);
			}
		}
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	}
	
	@Override
	protected void finishFrame() {
		
		
		program.use();
		
		if(background!=null){
			glDisable(GL_DEPTH_TEST);
			drawBackground();
		
			
		}
		
		glEnable(GL_DEPTH_TEST);
		glDepthFunc(GL_LEQUAL);
		
		//draw without transparency or sorting
		for(;eventQueue!=null;eventQueue = eventQueue.next) {
            eventQueue.render(program);
        }
		
		//draw with transparency and sorting
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA);
		transparentEvents.render(program);
		
		
		program.disableAll();
		statePos = 0;
		eventPos = 0;
		states.get(0).reset();
		last=null;
		eventQueue=null;
		
		glFinish();
	}
	
	final FloatBuffer screenQuad = (FloatBuffer)ByteBuffer.allocateDirect(8*4)
			.order(ByteOrder.nativeOrder()).asFloatBuffer().put(new float[]{
					1.0f,  1.0f,
					-1.0f, 1.0f,
					-1.0f,-1.0f,
					1.0f, -1.0f
			}).position(0);
	final FloatBuffer backgroundTexCoords = ByteBuffer.allocateDirect(8*4)
			.order(ByteOrder.nativeOrder()).asFloatBuffer();
	final FloatBuffer identityMatrix = ByteBuffer.allocateDirect(16*4)
			.order(ByteOrder.nativeOrder()).asFloatBuffer();
	
	protected final void drawBackground(){
		GLTexture texture = background.getTexture();
		
		float width = background.width * ((float)texture.realWidth/(float)texture.width);
		float height = background.height * ((float)texture.realHeight/(float)texture.height);
		
		//Log.e("dr2d",""+screen.getX()+" "+screen.getY()+" "+screen.getWidth()+" "+screen.getHeight());
		float minx = (float)((screen.getX() - background.x)/width);
		float miny = (float)((screen.getY() - background.y)/height);
		float maxx = minx + (float)(screen.getWidth()/width);
		float maxy = miny + (float)(screen.getHeight()/height);
		
		//Log.e("dr2d",minx+" "+miny+" "+maxx+" "+maxy);
		Primitives2d.setQuad(backgroundTexCoords,0,2,minx,maxy,maxx,miny).position(0);//y axis is inverted
		
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D,texture.handle);
		program.getUniform("uSampler").set1(0);
		
		
		program.getUniform("uMVMatrix").setMatrix4(1,false,identityMatrix);
		program.getUniform("uPVMatrix").setMatrix4(1,false,identityMatrix);
		
		program.getAttribute("aVertexColor").set4(1,1,1,1);
		program.getAttribute("aTintWeight").set1(0);
		
		program.getAttribute("aTextureCoord").setAttributePointer(2,GL_FLOAT,false,8,backgroundTexCoords);

		program.getAttribute("aVertexPosition").setAttributePointer(2,GL_FLOAT,false,8,screenQuad);
		
		glDrawArrays(GL_TRIANGLE_FAN,0,4);
		
		background=null;
		screen=null;
	}
	
	@Override
	void background(Background b,Box screen) {
		background = b;
		this.screen = screen;
	}
	
	@Override
	public void draw(Sprite sprite) {
		RenderEvent evt = getEvent().set(states.get(statePos),sprite);
		addEvent(evt);
	}

    static final Sprite pSprite = new Sprite("");
    public void draw(ParticleSystem p){
        pSprite.setTexture(p.type.texture);
        RenderEvent evt = getEvent().set(states.get(statePos),pSprite);
        evt.p = p;
        addEvent(evt);
    }

	private static void setAlignmentStart(GLFont font,State s,CharSequence text,int offset, int length){
		float xstart = 0;
		float ystart = -font.spriteHeight/2;
		switch(s.xalignment){
			case LEFT:
				break;
			case CENTER: 
				xstart = -font.getWidth(text,offset,length)/2;break;
			case RIGHT:
				xstart = -font.getWidth(text,offset,length);break;
		}
		switch(s.yalignment){
			case TOP:
				ystart -= font.data.xHeight;break;
			case CENTER:
				break;
			case BOTTOM:
				ystart += font.data.xHeight;break;
		}
		translateM(s.mvMatrix,0,xstart,ystart,0);
	}
	
	private static void setAlignmentStart(GLFont font,State s,char[] text,int offset, int length){
		float xstart = 0;
		float ystart = -font.spriteHeight/2;
		switch(s.xalignment){
			case LEFT:
				break;
			case CENTER: 
				xstart = -font.getWidth(text,offset,length)/2;break;
			case RIGHT:
				xstart = -font.getWidth(text,offset,length);break;
		}
		switch(s.yalignment){
			case TOP:
				ystart -= font.data.xHeight;break;
			case CENTER:
				break;
			case BOTTOM:
				ystart += font.data.xHeight;break;
		}
		translateM(s.mvMatrix,0,xstart,ystart,0);
	}
	
	private void addChar(GLFont font,State s,char c){
		RenderEvent evt = getEvent();
		evt.set(s);
		evt.isSprite = true;
		evt.isText = true;
		evt.drawMode = FILL;
		evt.texture = font.data.handle;
		
		translateM(s.mvMatrix,0,font.setCoordsSquare(c,evt.textCoords,0,2,evt.textureCoords,0,2,0,0),0,0);
		addEvent(evt);
	}
	
	@Override
	public void text(CharSequence text) {
		push();
			GLFont font = getState().font;
			State s = getState();
			setAlignmentStart(font,s,text,0,text.length());
			for(int i = 0; i<text.length(); i++){addChar(font,s,text.charAt(i));}
		pop();
	}

	@Override
	public void text(char[] text) {
		push();
			GLFont font = getState().font;
			State s = getState();
			setAlignmentStart(font,s,text,0,text.length);
			for(int i = 0; i<text.length; i++)addChar(font,s,text[i]);
		pop();
	}

	@Override
	public void text(CharSequence text, int offset, int length) {
		push();
			GLFont font = getState().font;
			State s = getState();
			setAlignmentStart(font,s,text,offset,length);
			for(int i = offset; i<offset+length; i++)addChar(font,s,text.charAt(i));
		pop();
	}

	@Override
	public void text(char[] text, int offset, int length) {
		push();
			GLFont font = getState().font;
			State s = getState();
			setAlignmentStart(font,s,text,offset,length);
			for(int i = offset; i<offset+length; i++)addChar(font,s,text[i]);
		pop();
	}
	
	@Override
	public double textWidth(CharSequence text) {
		GLFont font = getState().font;
		return font.getWidth(text,0,text.length());
	}

	@Override
	public double textWidth(char[] text) {
		GLFont font = getState().font;
		return font.getWidth(text,0,text.length);
	}

	@Override
	public double textWidth(CharSequence text, int offset, int length) {
		GLFont font = getState().font;
		return font.getWidth(text,offset,length);
	}

	@Override
	public double textWidth(char[] text, int offset, int length) {
		GLFont font = getState().font;
		return font.getWidth(text,offset,length);
	}
	
	public double textHeight(){
		FontData data = getState().font.data;
		return data.bottom-data.top;
	}
	
	@Override
	public void line(float x1, float y1, float x2, float y2) {
		RenderEvent evt = getEvent();
		evt.set(states.get(statePos));
		evt.currentShape = LINE;
		translateM(evt.mvMatrix,0,x1,y1,0);
		scaleM(evt.mvMatrix,0,x2-x1,y2-y1,1);
		addEvent(evt);
	}
	
	@Override
	public void line(double x1, double y1, double x2, double y2) {
		RenderEvent evt = getEvent();
		evt.set(states.get(statePos));
		evt.currentShape = LINE;
		translateM(evt.mvMatrix,0,(float)x1,(float)y1,0);
		scaleM(evt.mvMatrix,0,(float)(x2-x1),(float)(y2-y1),1);
		addEvent(evt);
	}
	
	public void lines(int offset, int size, int num, float[] points){
		int end = offset+size*(num-1);
		for(int i = offset+size; i<end; i+=size*2){
			line(points[i],points[i+1],points[i+size],points[i+size+1]);
		}
	}
	
	@Override
	public void lineStrip(int offset, int size, int num, float[] points) {
		int end = offset+size*num;
		float px = points[offset];
		float py = points[offset+1];
		for(int i = offset+size; i<end; i+=size){
			line(px,py,points[i],points[i+1]);
			px = points[i];
			py = points[i+1];
		}
	}
	
	@Override
	public void lineLoop(int offset, int size, int num,float[] points) {
		int end = offset+size*num;
		float px = points[offset];
		float py = points[offset+1];
		float fx = px;
		float fy = py;
		for(int i = offset+size; i<end; i+=size){
			line(px,py,points[i],points[i+1]);
			px = points[i];
			py = points[i+1];
		}
		line(px,py,fx,fy);
	}
	
	@Override
	public void lineLoop(int offset, int size, int num,double[] points) {
		int end = offset+size*num;
		double px = points[offset];
		double py = points[offset+1];
		double fx = px;
		double fy = py;
		for(int i = offset+size; i<end; i+=size){
			line(px,py,points[i],points[i+1]);
			px = points[i];
			py = points[i+1];
		}
		line(px,py,fx,fy);
	}
	
	@Override
	public void shape(Matrix2d matrix, Circle circle) {
		push();
			transform(matrix);
			RenderEvent evt = getEvent();
			evt.set(states.get(statePos));
			translateM(evt.mvMatrix,0,(float)circle.position.x,(float)circle.position.y,0);
			float w = (float)(circle.radius*2);
			scaleM(evt.mvMatrix,0,w,w,1);
			evt.currentShape = ELLIPSE;
			addEvent(evt);
		pop();
	}
	
	private final Vector2d miscVec = new Vector2d();

	@Override
	public void shape(Matrix2d matrix, Polygon polygon) {
		push();
			transform(matrix);
			polygon.getCOM(miscVec);
			translate((float)miscVec.x,(float)miscVec.y);
			polygon.getPoint(0,miscVec);
			float px = (float)miscVec.x;
			float py = (float)miscVec.y;
			float fx = px;
			float fy = py;
			int end = polygon.getNumOfVerts();
			for(int i = 1; i<end; i++){
				polygon.getPoint(i,miscVec);
				float x = (float)miscVec.x;
				float y = (float)miscVec.y;
				line(px,py,x,y);
				px = x;
				py = y;
			}
			line(px,py,fx,fy);
			State s = states.get(statePos);
			s.drawMode = FILL;
			s.currentShape = ELLIPSE;
			scaleM(s.mvMatrix,0,8,8,1);
			RenderEvent evt = getEvent();
			evt.set(s);
			addEvent(evt);
		pop();
	}
	
	public void shape(){
		RenderEvent evt=getEvent();
		State s = states.get(statePos);
		evt.set(s);
		addEvent(evt);
	}
	
	@Override
	public void shape(float w, float h) {
		RenderEvent evt=getEvent();
		evt.set(states.get(statePos));
		scaleM(evt.mvMatrix,0,w,h,1);
		addEvent(evt);
	}
	
	@Override
	public void shape(float x, float y,float z, float w, float h) {
		RenderEvent evt=getEvent();
		State s = states.get(statePos);
		evt.set(s);
		translateM(evt.mvMatrix,0,x,y,z);
		scaleM(evt.mvMatrix,0,w,h,1);
		addEvent(evt);
	}

	@Override
	public void shape(double w, double h) {
		RenderEvent evt=getEvent();
		evt.set(states.get(statePos));
		scaleM(evt.mvMatrix,0,(float)w,(float)h,1);
		addEvent(evt);
	}
	
	@Override
	public void shape(double x, double y,double z, double w, double h) {
		RenderEvent evt=getEvent();
		State s = states.get(statePos);
		evt.set(s);
		translateM(evt.mvMatrix,0,(float)x,(float)y,(float)z);
		scaleM(evt.mvMatrix,0,(float)w,(float)h,1);
		addEvent(evt);
	}
	
	public void vertices(int texture,FloatBuffer vertices,FloatBuffer textureCoords,int size,float tintWeight){
		RenderEvent evt=getEvent();
		State s = states.get(statePos);
		evt.set(s);
		evt.texture = texture;
		evt.isSprite = true;
		evt.useTemp = true;
		evt.tempSize = size;
		evt.tempVerts = vertices;
		evt.tempTextureCoords = textureCoords;
		evt.tintWeight = tintWeight;
		addEvent(evt);
	}
	
	public static final float[] IDENTITY_MAT = new float[16];
	public static final FloatBuffer SCREEN_COORDS = ByteBuffer
			.allocateDirect(8*4).
			order(ByteOrder.nativeOrder()).
			asFloatBuffer().put(new float[]{
					-1.0f, 1.0f,
					-1.0f, -1.0f,
					1.0f, -1.0f,
					1.0f, 1.0f
			});
	
	static{
		setIdentityM(IDENTITY_MAT,0);
	}
	
	void addTint(float r, float g, float b, float a){
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		
		program.getUniform("uSampler").set1(0);
		
		
		program.getUniform("uMVMatrix").setMatrix4(1,false,IDENTITY_MAT,0);
		program.getUniform("uPVMatrix").setMatrix4(1,false,IDENTITY_MAT,0);
		
		program.getAttribute("aVertexColor").set4(r,g,b,a);
		program.getAttribute("aTintWeight").set1(1);
		
		program.getAttribute("aTextureCoord").disableArray().set2(0,0);

		program.getAttribute("aVertexPosition").setAttributePointer(2,GL_FLOAT,false,8,SCREEN_COORDS);
		
		glDrawArrays(GL_TRIANGLE_FAN,0,4);
	}
	
	protected static final class RenderEvent{
		final float[] pvMatrix = new float[16];
		final float[] mvMatrix = new float[16];
		
		float r=0,g=0,b=0,a=1;
		
		float lineWidth=1;
		
		int drawMode = 0;
		int blendMode = 0;
		int currentShape = 0;
		
		float tintWeight = 0;
		int texture=0;
		final FloatBuffer textureCoords;
		final FloatBuffer textCoords;
		
		boolean useTemp;
		FloatBuffer tempVerts;
		FloatBuffer tempTextureCoords;
		int tempSize;
		
		
		boolean isSprite = false;
		boolean isText = false;

        ParticleSystem p;

		RenderEvent next;
		
		RenderEvent(){
			ByteBuffer bb = ByteBuffer.allocateDirect(8*4*2).order(ByteOrder.nativeOrder());
			FloatBuffer fb = bb.asFloatBuffer();
			textureCoords = fb.slice();
			textureCoords.limit(8);
			fb.position(8);
			textCoords = fb.slice();
		}
		
		public RenderEvent set(State s){
            p=null;
			System.arraycopy(s.pvMatrix,0,pvMatrix,0,16);
			System.arraycopy(s.mvMatrix,0,mvMatrix,0,16);
			lineWidth = s.lineWidth;
			r=s.r;
			g=s.g;
			b=s.b;
			a=s.a;
			drawMode=s.drawMode;
			blendMode=s.blendMode;
			currentShape=s.currentShape;
			isSprite = false;
			isText = false;
			useTemp = false;
			tintWeight = 1;
			texture = default_texture;
			return this;
		}
		
		RenderEvent set(State s,Sprite sprite){
            p=null;
			isSprite = true;
			isText = false;
			useTemp = false;
			drawMode = FILL;
			blendMode = s.blendMode;
			System.arraycopy(s.pvMatrix,0,pvMatrix,0,16);
			System.arraycopy(s.mvMatrix,0,mvMatrix,0,16);
			r =sprite.r;
			b =sprite.b;
			g =sprite.g;
			a =sprite.alpha;
			tintWeight = sprite.tintWeight;
			
			SpriteSheet sheet = sprite.getSheet();
			texture = sheet.texture.handle;
			
			float tWidth = (float)sheet.cellWidth/(float)sheet.texture.realWidth;
			float tHeight = (float)sheet.cellHeight/(float)sheet.texture.realHeight;
			
			int frame = sprite.getFrame();
			int col = frame%(sheet.texture.width/sheet.cellWidth);
			int row = frame/(sheet.texture.width/sheet.cellWidth);
			
			float wOffset = tWidth*col;
			float hOffset = tHeight*row;
			
			//coords flipped
			textureCoords
				.put(0,tWidth+wOffset).put(1,0+hOffset)
				.put(2,0+wOffset).put(3,0+hOffset)
				.put(4,0+wOffset).put(5,tHeight+hOffset)
				.put(6,tWidth+wOffset).put(7,tHeight+hOffset);
			
			
			Transformation tx = sprite.getTransformation();
			translateM(mvMatrix,0,(float)tx.translation.x,(float)tx.translation.y,sprite.z);
			rotateM(mvMatrix,0,(float)(RAD_TO_DEG*sprite.getTheta()),0,0,1);
			scaleM(mvMatrix,0,(float)tx.scale.x,(float)tx.scale.y,1);
			return this;
		}
		
		void render(GLProgram program){
			if(useTemp){
				render(program,tempVerts,tempTextureCoords,tempSize);
				tempVerts = null;
				tempTextureCoords = null;
			}else if(isText){
				render(program,textCoords,textureCoords,4);
			}else if(p!=null){
                renderParticles(program);
            }else if(isSprite){
				render(program,Primitives2d.B_SQUARE_MIN,textureCoords,4);
			}else{
				tintWeight=1;
				switch(currentShape){
					case LINE:
						drawMode=STROKE;
						render(program,Primitives2d.B_LINE,null,Primitives2d.B_LINE.limit()/2);
						break;
					case TRIANGLE:
						render(program,Primitives2d.B_TRIANGLE,null,Primitives2d.B_TRIANGLE.limit()/2);
						break;
					case SQUARE:
						render(program,Primitives2d.B_SQUARE_MIN,null,Primitives2d.B_SQUARE_MIN.limit()/2);
						break;
					case ELLIPSE:
						render(program,Primitives2d.B_CIRCLE_MIN,null,Primitives2d.B_CIRCLE_MIN.limit()/2);
						break;
					case ORIENTABLE_ELLIPSE:
						FloatBuffer v = (drawMode==STROKE) ? Primitives2d.B_CIRCLE_FAN_MIN : Primitives2d.B_CIRCLE_MIN;
						render(program,v,null,v.limit()/2);
						break;
					default:
						throw new IllegalStateException("shape not defined");
				}
			}
			
		}
		
		private void render(GLProgram program,FloatBuffer verts,FloatBuffer textureCoords,int vertices){
			switch(blendMode){
				case TRANSPARENCY:
					GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA,GLES20.GL_ONE_MINUS_SRC_ALPHA);
					break;
				case ADDITIVE:
					GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA,GLES20.GL_ONE);
					break;
				case SUBTRACTIVE:
					GLES20.glBlendFunc(GLES20.GL_ONE,GLES20.GL_DST_ALPHA);
					break;
				default:
					throw new IllegalStateException();
			}
			
			glLineWidth(lineWidth);
			
			glActiveTexture(GL_TEXTURE0);
			glBindTexture(GL_TEXTURE_2D,texture);
			program.getUniform("uSampler").set1(0);
			
			program.getUniform("uMVMatrix").setMatrix4(1,false,mvMatrix,0);
			program.getUniform("uPVMatrix").setMatrix4(1,false,pvMatrix,0);
			
			program.getAttribute("aVertexColor").set4(r,g,b,a);
			program.getAttribute("aTintWeight").set1(tintWeight);
			
			if(!isSprite)
				program.getAttribute("aTextureCoord").disableArray().set2(0,0);
			else
				program.getAttribute("aTextureCoord").enableArray().setAttributePointer(2,GL_FLOAT,false,8,textureCoords);

			program.getAttribute("aVertexPosition").setAttributePointer(2,GL_FLOAT,false,8,verts);

			glDrawArrays((drawMode==FILL) ? GL_TRIANGLE_FAN : GL_LINE_LOOP,0,vertices);
		}

        private void renderParticles(GLProgram program){

            //shared attributes
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D,texture);
            program.getUniform("uSampler").set1(0);

            program.getUniform("uPVMatrix").setMatrix4(1,false,pvMatrix,0);

            program.getAttribute("aTintWeight").set1(p.type.tintWeight);

            program.getAttribute("aTextureCoord").enableArray().setAttributePointer(2,GL_FLOAT,false,8,textureCoords);
            program.getAttribute("aVertexPosition").setAttributePointer(2,GL_FLOAT,false,8,Primitives2d.B_SQUARE_MIN);

            //changing attribute
            for(int i = 0; i<p.size; i++){
                ParticleSystem.Particle part = p.particles[i];
                if(part.scale>0 && part.a >0) {
                    program.getAttribute("aVertexColor").set4(part.r, part.g, part.b, part.a);

                    //calculate transformation matrix
                    setIdentityM(mvMatrix, 0);
                    translateM(mvMatrix, 0, (float) part.x, (float) part.y, (float) p.type.z);
                    rotateM(mvMatrix, 0, (float) (RAD_TO_DEG * part.orientation), 0, 0, 1);
                    scaleM(mvMatrix, 0, (float) (p.type.width * part.scale), (float) (p.type.height * part.scale), 1);
                    program.getUniform("uMVMatrix").setMatrix4(1, false, mvMatrix, 0);

                    //draw
                    glDrawArrays(GL_TRIANGLE_FAN, 0, 4);
                }
            }


        }

		private void printVerts(FloatBuffer fb){
			for(int i = 0; i<fb.limit(); i+=2){
				Log.e("dr2d evt",fb.get(i)+" "+fb.get(i+1));
			}
		}
		
		float z(){
            if(p==null)
                return mvMatrix[14];
            else
                return p.type.z;
		}
	}
		
	protected static final class EventTree{
		private Node tree;
		
		void add(RenderEvent evt){
			float z = evt.z();
			if(tree==null)
				tree = getNode(z,evt);
			else
				add(tree,evt,z);
		}
		
		private void add(Node n,RenderEvent evt, float z){
			if(n.z == z){
				evt.next=null;
				n.tail.next = evt;
				n.tail = evt;
			}else if(n.z > z){
				if(n.left==null)
					n.left = getNode(z,evt);
				else
					add(n.left,evt,z);
			}else{
				if(n.right==null)
					n.right = getNode(z,evt);
				else
					add(n.right,evt,z);
			}
		}
		
		int getC(RenderEvent head){
			int i = 0;
			for(;head!=null;head=head.next)i++;
			return i;
		}
		
		void render(GLProgram program){
			render(tree,program);
			tree = null;
			nodeCount=0;
		}
		
		private void render(Node n,GLProgram program){
			if(n!=null){
				render(n.right,program);
				for(;n.head!=null;n.head=n.head.next)
					n.head.render(program);
				render(n.left,program);
			}
		}
		
		private final ArrayList<Node> nodePool = new ArrayList<Node>();
		int nodeCount;
		
		private Node getNode(float z,RenderEvent evt){
			if(nodePool.size()<=nodeCount)nodePool.add(new Node());
			Node n = nodePool.get(nodeCount++);
			n.head=null;
			n.left=null;
			n.right=null;
			n.z = z;
			evt.next=null;
			n.head=evt;
			n.tail=evt;
			return n;
		}
		
		static final class Node{
			float z;
			RenderEvent head;
			RenderEvent tail;
			Node left;
			Node right;
		}
	}
}
