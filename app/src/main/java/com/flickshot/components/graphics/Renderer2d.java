package com.flickshot.components.graphics;

import static com.flickshot.util.MiscLib.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.opengl.GLUtils;
import android.util.Log;

import com.flickshot.GameView;
import com.flickshot.assets.AssetLibrary;
import com.flickshot.components.graphics.program.GLProgram;
import com.flickshot.components.particles.ParticleSystem;
import com.flickshot.components.physics.shapes.Circle;
import com.flickshot.components.physics.shapes.Polygon;
import com.flickshot.geometry.Box;
import com.flickshot.geometry.Matrix2d;
import com.flickshot.geometry.Square;
import com.flickshot.geometry.Transformation;
import com.flickshot.util.MatrixStack;

import static android.opengl.GLES20.*;
import static android.opengl.Matrix.*;

public abstract class Renderer2d {
	/**
	 * the different shape modes
	 */
	public static final int 
			ELLIPSE = 0, SQUARE = 1, TRIANGLE = 2,
			ORIENTABLE_ELLIPSE = 3, LINE = 4;
	
	public static final int
			TRANSPARENCY = 0,ADDITIVE = 1, SUBTRACTIVE = 2;
	
	/**
	 * the draw modes
	 */
	public static final int STROKE = 0, FILL = 1;
	
	/**
	 * text alingments
	 */
	public static final int LEFT = 0,BOTTOM=0, RIGHT = 1,TOP=1,CENTER = 3;
	
	//these need opengl to be initialized 
	protected static int default_texture;
	
	protected static GLFont default_font;
	
	protected static final HashMap<String,GLFont> fonts = new HashMap<String,GLFont>();
	
	private static boolean initialized = false;
	
	public static boolean initialized(){return initialized;}
	
	public static void init(){
		if(!initialized){
			//setup default texture
			Bitmap blank = Bitmap.createBitmap(2,2,Bitmap.Config.ARGB_8888);
			blank.setPixel(0,0,Color.argb(255,255,255,255));
			blank.setPixel(0,1,Color.argb(255,255,255,255));
			blank.setPixel(1,1,Color.argb(255,255,255,255));
			blank.setPixel(1,0,Color.argb(255,255,255,255));
			
			int[] texture = new int[1];
			glGenTextures(1, texture, 0);
			
			if(texture[0]!=0){
				
				glBindTexture(GL_TEXTURE_2D, texture[0]);
				
				glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
				glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
				
				glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S,GL_REPEAT);
				glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T,GL_REPEAT);
				
				GLUtils.texImage2D(GL_TEXTURE_2D,0,blank,0);
				
				blank.recycle();
				
				default_texture = texture[0];
			}else{
				throw new RuntimeException("failed to generate texture");
			}
			
			default_font = new GLFont(Typeface.DEFAULT,16);
			fonts.put("default",default_font);
			fonts.put("default italicized",new GLFont(Typeface.create(Typeface.DEFAULT,Typeface.ITALIC),64));
			fonts.put("default bold",new GLFont(Typeface.create(Typeface.DEFAULT,Typeface.BOLD),64));
			fonts.put("default bold italicized",new GLFont(Typeface.create(Typeface.DEFAULT,Typeface.ITALIC),64));
			initialized = true;
		}
	}
	
	private final float[] misc0 = new float[16];
	private final float[] misc1 = new float[16];
	
	protected final ArrayList<State> states = new ArrayList<State>();
	protected int statePos = 0;
	
	public Renderer2d(){
		if(!initialized)init();
		states.add(new State());
	}
	
	protected final State getState(){
		return states.get(statePos);
	}
	
	final void start(){
		for(int i = 0; i<states.size(); i++)
			states.get(i).reset();
		statePos = 0;
		startFrame();
	}
	
	final void finish(){
		finishFrame();
	}
	
	public final void setProjection(float[] matrix) {
		System.arraycopy(matrix,0,states.get(statePos).pvMatrix,0,16);
	}

	public final void setTransformation(float[] matrix) {
		System.arraycopy(matrix,0,states.get(statePos).mvMatrix,0,16);
	}

	public final float[] getProjection() {
		return states.get(statePos).pvMatrix;
	}

	public final float[] getTransformation() {
		return states.get(statePos).mvMatrix;
	}
	
	public final void setProjection(Screen screen,float near, float far){
		float[] pv = states.get(statePos).pvMatrix;
		double left = screen.getX();
		double bottom = screen.getY();
		orthoM(pv,0,(float)left,(float)(left+screen.getWidth()),(float)bottom,(float)(bottom+screen.getHeight()),near,far);
		if(screen.orientation.val!=0)rotateM(pv,0,(float)(RAD_TO_DEG*screen.orientation.val),0,0,1);
	}

	public final void setTransformation(Matrix2d matrix, double z) {
		float[] mv = states.get(statePos).mvMatrix;
		matrix.toGlMatrix(mv)[15] = (float)z;
	}
	
	public final void transform(Matrix2d matrix){
		float[] mv = states.get(statePos).mvMatrix;
		multiplyMM(misc1,0,matrix.toGlMatrix(misc0),0,mv,0);
		System.arraycopy(misc1,0,mv,0,16);
	}
	
	public final void transform(Transformation tx) {
		float[] mv = states.get(statePos).mvMatrix;
		translateM(mv,0,(float)tx.translation.x,(float)tx.translation.y,0);
		rotateM(mv,0,(float)(RAD_TO_DEG*tx.theta.val),0,0,1);
		scaleM(mv,0,(float)tx.scale.x,(float)tx.scale.y,1);
	}
	
	public final void ortho(Box b, float near, float far){
		ortho((float)b.getX(),(float)(b.getX()+b.getWidth()),(float)b.getY(),(float)(b.getY()+b.getHeight()),near,far);
	}
	
	public final void ortho(Box b, double near, double far){
		ortho(b.getX(),b.getX()+b.getWidth(),b.getY(),b.getY()+b.getHeight(),near,far);
	}
	
	public final void ortho(float left, float right, float bottom, float top, float near, float far){
		orthoM(states.get(statePos).pvMatrix,0,left,right,bottom,top,near,far);
	}
	
	public final void ortho(double left, double right, double bottom, double top, double near, double far){
		orthoM(states.get(statePos).pvMatrix,0,(float)left,(float)right,(float)bottom,(float)top,(float)near,(float)far);
	}

	public final void identity() {
		setIdentityM(states.get(statePos).mvMatrix,0);
	}

	public final void translate(float x, float y) {
		translateM(states.get(statePos).mvMatrix,0,x,y,0);
	}

	public final void translate(float x, float y, float z) {
		translateM(states.get(statePos).mvMatrix,0,x,y,z);
	}

	public final void translate(double x, double y) {
		translateM(states.get(statePos).mvMatrix,0,(float)x,(float)y,0);
	}

	public final void translate(double x, double y, double z) {
		translateM(states.get(statePos).mvMatrix,0,(float)x,(float)y,(float)z);
	}

	public final void scale(float w, float h) {
		scaleM(states.get(statePos).mvMatrix,0,w,h,1);
	}
	
	public final void scale(double w, double h) {
		scaleM(states.get(statePos).mvMatrix,0,(float)w,(float)h,1);
	}

	public final void rotate(float radians) {
		rotateM(states.get(statePos).mvMatrix,0,(float)(RAD_TO_DEG*radians),0,0,1);
	}

	public final void rotate(double radians) {
		rotateM(states.get(statePos).mvMatrix,0,(float)(RAD_TO_DEG*radians),0,0,1);
	}
	
	public final void push() {
		State current = states.get(statePos);
		statePos++;
		if(statePos>=states.size())states.add(new State());
		states.get(statePos).set(current);
	}

	public final void pop() {
		if(statePos<=0) {
			statePos = 0;
			throw new IllegalStateException("to many calls to pop or to few calls to push");
		}
		statePos--;
	}
	
	public final void loadFont(String id,Typeface tf,int size){
		fonts.put(id,new GLFont(tf,size));
	}
	
	public final void loadFont(String id,String path,int size){
		fonts.put(id,new GLFont(path,size));
	}
	
	public final void font(String id){
		if((states.get(statePos).font = fonts.get(id))==null)throw new IllegalArgumentException("font not defined: "+id);
	}
	
	protected final void font(GLFont font){
		if((states.get(statePos).font = font)==null)throw new IllegalArgumentException("no such font");
	}
	
	public final int fontSize(){
		return getState().font.spriteHeight;
	}
	
	public final void setShape(int shape) {
		if(!(shape>-1 && shape<5))throw new IllegalArgumentException("invalid shape: "+shape);
		states.get(statePos).currentShape = shape;
	}

	public final void setDrawMode(int mode) {
		if(!(mode>-1 && mode<2))throw new IllegalArgumentException("invalid draw mode: "+mode);
		states.get(statePos).drawMode = mode;
	}

	public final void color(Color c){
		
	}
	
	public final void color(float r, float g, float b) {
		states.get(statePos).setColor(bound(r,0,1),bound(g,0,1),bound(b,0,1),1);
	}

	public final void color(double r, double g, double b) {
		states.get(statePos).setColor((float)bound(r,0,1),(float)bound(g,0,1),(float)bound(b,0,1),1);
	}

	public final void color(float r, float g, float b, float a) {
		states.get(statePos).setColor(bound(r,0,1),bound(g,0,1),bound(b,0,1),bound(a,0,1));
	}

	public final void color(double r, double g, double b, double a) {
		states.get(statePos).setColor((float)bound(r,0,1),(float)bound(g,0,1),(float)bound(b,0,1),(float)bound(a,0,1));
	}
	
	/**
	 * color formated as bytes
	 * @param r
	 * @param g
	 * @param b
	 * @param a
	 */
	public final void color(byte r, byte g, byte b, byte a){
		color(normalize(r),normalize(g),normalize(b),normalize(a));
	}
	
	private float normalize(byte b){
		if(b==0){
			return 0;
		}else{
			return (float)b/255;
		}
	}
	
	private static final ByteBuffer bb = ByteBuffer.allocate(4);
	/**
	 * Color formated as single int
	 * @param c
	 */
	public final void color(int c){
		bb.position(0);
		bb.putInt(c);
		color(bb.get(0),bb.get(1),bb.get(2),bb.get(3));
	}
	
	public final void lineWidth(float width){
		states.get(statePos).lineWidth = Math.max(0,width);
	}
	
	public final void align(int x, int y){
		State s = getState();
		s.xalignment = bound(x,0,3);
		s.yalignment = bound(y,0,3);
	}
	
	public void shape(int shape){
		int curr = getState().currentShape;
		setShape(shape);
		shape();
		setShape(curr);
	}
	
	public void shape(int shape, float w, float h){
		int curr = getState().currentShape;
		setShape(shape);
		shape(w,h);
		setShape(curr);
		
	}
	
	public void shape(int shape,float x, float y,float z, float w, float h){
		int curr = getState().currentShape;
		setShape(shape);
		shape(x,y,z,w,h);
		setShape(curr);
	}
	
	public void shape(int shape, double w, double h){
		int curr = getState().currentShape;
		setShape(shape);
		shape(w,h);
		setShape(curr);
	}
	
	public void shape(int shape,double x, double y,double z, double w, double h){
		int curr = getState().currentShape;
		setShape(shape);
		shape(x,y,z,w,h);
		setShape(curr);
	}
	
	public void blendMode(int blendMode){
		if(blendMode<=3 || blendMode >=0)
			getState().blendMode = blendMode;
	}
	
	abstract void addTint(float r,float g, float b, float a);

	/**
	 * called to start the drawing process
	 */
	protected abstract void startFrame();
	
	/**
	 * finishes the drawing process
	 */
	protected abstract void finishFrame();
	
	public abstract void text(CharSequence text);
	
	public abstract void text(char[] text);
	
	public abstract void text(CharSequence text, int offset, int length);
	
	public abstract void text(char[] text, int offset, int length);
	
	public abstract double textWidth(CharSequence text);
	
	public abstract double textWidth(char[] text);
	
	public abstract double textWidth(CharSequence text, int offset, int length);
	
	public abstract double textWidth(char[] text, int offset, int length);
	
	public abstract double textHeight();
	
	abstract void background(Background b,Box screen);
	
	/**
	 * Draws the passed sprite
	 * @param sprite
	 */
	public abstract void draw(Sprite sprite);
	
	/**
	 * Draws a line between the passed coordinates
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 */
	public abstract void line(float x1, float y1, float x2, float y2);
	
	/**
	 * Draws a line between the passed coordinates
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 */
	public abstract void line(double x1, double y1, double x2, double y2);
	
	/**
	 * draws the passed lines
	 * @param offset starting point in the array
	 * @param size size of the array elements in array indices
	 * @param num the number of points to draw
	 * @param points an array containing a list of coordinates
	 */
	public abstract void lines(int offset, int size,int num,float[] points);
	
	/**
	 * Draws lines between all of the passed coordinates
	 * @param offset starting point in the array
	 * @param size size of the array elements in array indices
	 * @param num the number of points to draw
	 * @param points an array containing a list of coordinates
	 */
	public abstract void lineStrip(int offset, int size,int num,float[] points);
	
	/**
	 * Draws lines between all of the passed coordinates and 
	 * connects the first coordinate to the last
	 * @param offset starting point in the array
	 * @param size size of the array elements in array indices
	 * @param num the number of points to draw
	 * @param points an array containing a list of coordinates
	 */
	public abstract void lineLoop(int offset, int size,int num,float[] points);
	
	public abstract void lineLoop(int offset, int size,int num,double[] points);
	
	/**
	 * Draws the circle with the passed transformation
	 * @param matrix
	 * @param circle
	 */
	public abstract void shape(Matrix2d matrix,Circle circle);
	
	/**
	 * Draws the polygon with the passed transformation
	 * @param matrix
	 * @param polygon
	 */
	public abstract void shape(Matrix2d matrix,Polygon polygon);
	
	/**
	 * draws the current shape
	 */
	public abstract void shape();
	
	/**
	 * draws the current shape 
	 * @param w width of the shape
	 * @param h height of the shape
	 */
	public abstract void shape(float w, float h);
	
	/**
	 * draws the current shape
	 * @param x x coordinate
	 * @param y y coordinate
	 * @param z z coordinate
	 * @param w width
	 * @param h height
	 */
	public abstract void shape(float x, float y,float z, float w, float h);
	
	/**
	 * draws the current shape 
	 * @param w width of the shape
	 * @param h height of the shape
	 */
	public abstract void shape(double w, double h);
	
	/**
	 * draws the current shape
	 * @param x x coordinate
	 * @param y y coordinate
	 * @param z z coordinate
	 * @param w width
	 * @param h height
	 */
	public abstract void shape(double x, double y,double z, double w, double h);

    public abstract void draw(ParticleSystem p);

	protected static final class State{
		float[] pvMatrix = new float[16];
		float[] mvMatrix = new float[16];
		
		float r=0,g=0,b=0,a=1;
		
		float lineWidth;
		
		int drawMode = 0;
		int blendMode = 0;
		int currentShape = 0;
		
		int xalignment = LEFT,yalignment=CENTER;
		
		GLFont font = default_font;
		
		private State(){
			setIdentityM(pvMatrix,0);
			setIdentityM(mvMatrix,0);
		}
		
		public State reset(){
			setIdentityM(pvMatrix,0);
			setIdentityM(mvMatrix,0);
			r=0;g=0;b=0;a=1;
			drawMode = 0;
			blendMode = 0;
			currentShape = 0;
			lineWidth = 1;
			font = default_font;
			return this;
		}
		
		public State set(State s){
			System.arraycopy(s.pvMatrix,0,pvMatrix,0,16);
			System.arraycopy(s.mvMatrix,0,mvMatrix,0,16);
			r=s.r;
			g=s.g;
			b=s.b;
			a=s.a;
			drawMode=s.drawMode;
			currentShape=s.currentShape;
			lineWidth=s.lineWidth;
			font = s.font;
			xalignment = s.xalignment;
			yalignment = s.yalignment;
			blendMode = s.blendMode;
			return this;
		}
		
		public State setColor(float r, float g, float b, float a){
			this.r =r;
			this.g =g;
			this.b =b;
			this.a =a;
			return this;
		}
		
		@Override
		public String toString(){
			return String.format("state: color(%f,%f,%f,%f) drawMode(%s,%d) currentShape(%d) lineWidth(%f)",
					r,g,b,a,(drawMode==FILL)?"FILL":"STROKE",drawMode,currentShape,lineWidth);
		}
	}
	
	private static double t = 0;
	private static int sgn = 1;
	private static double sz = -4;
	private static double shp = 0;
//	private static String ms;
//	
//	static{
//		float[] mt = new float[16];
//		setIdentityM(mt,0);
//		translateM(mt,0,0,0,-1);
//		ms="";
//		for(float f: mt)ms+=f+" ";
//	}
	
	static Sprite sprite = new Sprite("puck",new Transformation()); 
	public static void test(Renderer2d r,double delta){
		
		
		Square screen = new Square();//needs fixing for future tests
		float rt = (float)((Math.PI*2)*(t=(t+delta)%1));
		sz+=delta*sgn;
		sz = bound(sz,-4,0);
		if(sz>=0 || sz<=-4){
			sgn= -sgn;
		}
		shp = (shp+delta)%4;
		
		
		r.start();
			orthoM(r.getProjection(), 0, (float)screen.getX(), (float)(screen.getX()+screen.getWidth()), 
				(float)screen.getY(), (float)(screen.getY()+screen.getHeight()), 100, -100);
			
			r.setDrawMode(Renderer2d.FILL);
			r.push();
				r.translate((float)(screen.x+screen.width/2),(float)(screen.y+screen.height/2));
				r.scale(300,300);
				r.push();
					r.rotate(rt);
					r.color(1,0,0);
					r.setShape(Renderer2d.ELLIPSE);
//					Log.e("r",""+r.getState().mvMatrix[14]);
					r.shape();
				r.pop();
				r.scale(0.5f,0.5f);
				r.translate(0,0,-1);
//				Log.e("r",""+r.getState().mvMatrix[14]);
				r.push();
					r.rotate(-rt);
					r.color(0,1,0,0.5);
					r.setShape(Renderer2d.SQUARE);
//					Log.e("r",""+r.getState().mvMatrix[14]);
					r.shape();
				r.pop();
				r.translate(0,0,-1);
				r.push();
					r.rotate(rt);
					r.color(0,0,1);
					r.setShape(Renderer2d.TRIANGLE);
//					Log.e("r",""+r.getState().mvMatrix[14]);
					r.shape();
				r.pop();
				r.scale(5f,5f);
				r.translate(0,0,-1);
				r.setShape(Renderer2d.SQUARE);
				r.color(1.0,0.0,0.0,0.5);
//				Log.e("r",""+r.getState().mvMatrix[14]);
				r.shape();
				r.translate(0.0f,0.0f,2.5f);
				r.rotate((float)(Math.PI/4));
				r.color(0.0,0.5,0.5,0.8);
//				Log.e("r",""+r.getState().mvMatrix[14]);
				r.shape();
				r.identity();
				r.translate((float)(screen.x+screen.width/2),(float)(screen.y+screen.height/2),0);
				r.push();
					r.translate(0,0,(float)sz);
					r.scale(10,10);
					r.color(1,1,1,1);
					r.align(CENTER,CENTER);
					r.text("Hello World");
				r.pop();
				r.push();
					r.translate(0,0,(float)(-4-sz));
					r.scale(200,200);
					r.draw(sprite);
				r.pop();
				r.push();
					r.translate(0,0,(float)(-10));
					r.scale(300,300);
					r.color(1,1,1,1);
					r.setDrawMode(STROKE);
					r.lineWidth(2);
					r.shape((int)Math.round(Math.floor(shp)));
				r.pop();
			r.pop();
		r.finish();
		
//		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
//		MatrixStack mvs = new MatrixStack();
//		MatrixStack pvs = new MatrixStack();
//		orthoM(pvs.current(), 0, (float)screen.getX(), (float)(screen.getX()+screen.getWidth()), 
//				(float)screen.getY(), (float)(screen.getY()+screen.getHeight()), 100, -100);
//		setIdentityM(mvs.current(),0);
//		translateM(mvs.current(),0,(float)(screen.x+screen.width/2),(float)(screen.y+screen.height/2),0);
//		scaleM(mvs.current(),0,200,200,0);
//		sprite.draw(delta,mvs,pvs);
		
	}
	
	private static void printBuffer(FloatBuffer fb){
		String s = "";
		for(int i = 0; i<fb.limit();i++)s+=fb.get(i)+" ";
		Log.e("r2d",s);
	}
}
