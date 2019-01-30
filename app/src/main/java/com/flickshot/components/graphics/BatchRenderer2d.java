package com.flickshot.components.graphics;

import static com.flickshot.components.graphics.Primitives2d.*;
import static com.flickshot.util.MiscLib.*;

import java.util.ArrayList;

import android.content.res.Resources;
import android.opengl.Matrix;

import com.flickshot.GameView;
import com.flickshot.components.graphics.GLFont.FontData;
import com.flickshot.components.graphics.program.GLProgram;
import com.flickshot.components.particles.ParticleSystem;
import com.flickshot.components.physics.shapes.Circle;
import com.flickshot.components.physics.shapes.Polygon;
import com.flickshot.geometry.Box;
import com.flickshot.geometry.Matrix2d;
import com.flickshot.geometry.Vector2d;

import static android.opengl.GLES20.*;

/**
 * This renderer does batching of sprites and shapes.
 * @author Alex
 *
 */
public class BatchRenderer2d extends Renderer2d{
	private static final int MAX_VERTS = 256;
	private static final int MAX_MATRICES = 24;
	
	private static final float[] DEFAULT_TEX_COORDS = new float[2];
	
	private Batch lineBatch = new Batch(MAX_VERTS,MAX_MATRICES);
	private Batch triangleBatch = new Batch(MAX_VERTS,MAX_MATRICES);
	
	private BatchTree batchTree = new BatchTree();
	
	GLProgram program;
	@Override
	public void startFrame() {
		if(program==null){
			Resources resources = GameView.getMain().getResources();
			try{
				program = new GLProgram("shape",resources.getAssets().open("general_batch.vs"),
						resources.getAssets().open("general.fs"));
			}catch(Exception ex){
				throw new IllegalStateException("failed to init shader",ex);
			}
		}
		lineBatch.startBatch();
		triangleBatch.startBatch();
		batchTree.start();
	}

	@Override
	protected void finishFrame() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		float[] pvMatrix = getState().pvMatrix;
		lineBatch.finish(program,GL_LINES,pvMatrix,default_texture);
		triangleBatch.finish(program,GL_TRIANGLES,pvMatrix,default_texture);
		
		glEnable(GL_BLEND);
		
		batchTree.finish(program,GL_TRIANGLES,pvMatrix);
	}
	
	private static final float[] ln = new float[4];
	
	private static float[] getLineVerts(float x1, float y1, float x2, float y2){
		ln[0]=x1;
		ln[1]=y1;
		ln[2]=x2;
		ln[3]=y2;
		return ln;
	}

	private static float[] getLineVerts(double x1, double y1, double x2, double y2){
		ln[0]=(float)x1;
		ln[1]=(float)y1;
		ln[2]=(float)x2;
		ln[3]=(float)y2;
		return ln;
	}
	
	public void lines(int offset, int size, int num, float[] lines){
		State s = getState();
		if(s.a<1){//transparent
			batchTree.add(STROKE,default_texture,offset,size,num,lines,0,2,1,DEFAULT_TEX_COORDS,1,s.r,s.g,s.b,s.a,s.mvMatrix);
		}else{//solid
			lineBatch.setMVMatrix(s.mvMatrix);
			lineBatch.draw(offset,size,num,lines,0,2,1,DEFAULT_TEX_COORDS,1,s.r,s.g,s.b,s.a);
		}
	}
	
	@Override
	public void line(float x1, float y1, float x2, float y2) {
		lines(0,2,2,getLineVerts(x1,x2,y1,y2));
	}

	@Override
	public void line(double x1, double y1, double x2, double y2) {
		lines(0,2,2,getLineVerts(x1,x2,y1,y2));
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
	public void lineLoop(int offset, int size, int num, float[] points) {
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
	
	private void addShape(float[] vertices){
		State s = getState();
		if(s.a<1){
			batchTree.add(
					s.drawMode,default_texture,
					0,2,vertices.length/2,vertices,
					0,2,1,DEFAULT_TEX_COORDS,
					1,s.r,s.g,s.b,s.a,
					s.mvMatrix);
		}else{
			((s.drawMode==STROKE)?lineBatch:triangleBatch).draw(
					0,2,vertices.length/2,vertices,
					0,2,1,DEFAULT_TEX_COORDS,
					1,s.r,s.g,s.b,s.a);
		}
	}

	@Override
	public void shape(Matrix2d matrix, Circle circle) {
		push();
			State s = getState();
			transform(matrix);
			Matrix.translateM(s.mvMatrix,0,(float)circle.position.x,(float)circle.position.y,0);
			float w = (float)(circle.radius*2);
			Matrix.scaleM(s.mvMatrix,0,w,w,1);
			addShape((s.drawMode==STROKE)?A_CIRCLE_FAN_LINES:A_CIRCLE_TRIANGLES);
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
			Matrix.scaleM(s.mvMatrix,0,8,8,1);
			shape();
		pop();
	}

	@Override
	public void shape() {
		State s = getState();
		boolean stroke = s.drawMode==STROKE;
		switch(s.drawMode){
			case ELLIPSE:
				addShape((stroke)?A_CIRCLE_LINES:A_CIRCLE_TRIANGLES);
				break;
			case SQUARE:
				addShape((stroke)?A_SQUARE_LINES:A_SQUARE_TRIANGLES);
				break;
			case TRIANGLE:
				addShape((stroke)?A_TRIANGLE_LINES:A_TRIANGLE);
				break;
			case ORIENTABLE_ELLIPSE:
				push();
					setDrawMode(STROKE);
					addShape(A_CIRCLE_FAN_LINES);
				pop();
				break;
			case LINE:
				push();
					setDrawMode(STROKE);
					addShape(A_LINE);
				pop();
				break;
			default:
				throw new IllegalStateException();
		}
	}

	@Override
	public void shape(float w, float h) {
		push();
			scale(w,h);
			shape();
		pop();
	}

	@Override
	public void shape(float x, float y, float z, float w, float h) {
		push();
			translate(x,y);
			scale(w,h);
			shape();
		pop();
	}

	@Override
	public void shape(double w, double h) {
		push();
			scale((float)w,(float)h);
			shape();
		pop();
	}

	@Override
	public void shape(double x, double y, double z, double w, double h) {
		push();
			translate((float)x,(float)y);
			scale((float)w,(float)h);
			shape();
		pop();
	}

    @Override
    public void draw(ParticleSystem p) {

    }

    private static final class BatchSet{
		private static int largest = 16;
		
		private Batch[] batchMap = new Batch[largest];
		private final ArrayList<Batch> batches = new ArrayList<Batch>();
		private final ArrayList<Integer> textures = new ArrayList<Integer>();
		
		BatchSet(){}
		
		private void setMatrix(int texture, float[] mvMatrix){
			if(texture>batchMap.length){
				largest = Math.max(largest,texture);
				Batch[] temp = batchMap;
				batchMap = new Batch[largest];
				System.arraycopy(temp,0,batchMap,0,temp.length);
			}
			texture--;
			if(batchMap[texture]==null){
				batchMap[texture]=new Batch(MAX_VERTS,MAX_MATRICES);
				batchMap[texture].startBatch();
				batches.add(batchMap[texture]);
				textures.add(texture+1);
			}
			batchMap[texture].setMVMatrix(mvMatrix);
		}
		
		void add(
				int texture,
				int poffset,int psize,int pnum,float[] verts,
				int toffset,int tsize, int tnum,float[] texCoords,
				float tintWeight, float r, float g, float b, float a,
				float[] mvMatrix){
			setMatrix(texture--,mvMatrix);
			batchMap[texture].draw(poffset,psize,pnum,verts,toffset,tsize,tnum,texCoords,tintWeight,r,g,b,a);
		}
		
		void add(int texture,
				float x, float y, float u, float v, 
				float tintWeight, float r, float g, float b, float a,
				float[] mvMatrix){
			setMatrix(texture--,mvMatrix);
			batchMap[texture].draw(x,y,u,v,tintWeight,r,g,b,a);
		}
		
		void start(){
			for(int i = 0; i<batches.size(); i++){
				batches.get(i).startBatch();
			}
		}
		
		void finish(GLProgram program,int type,float[] pvMatrix){
			for(int i = 0; i<batches.size(); i++){
				batches.get(i).finish(program,type,pvMatrix,textures.get(i));
			}
		}
	}
	
	void addTint(float r, float g, float b, float a){
		
	}
	
	private static final class BatchTree{
		private Node tree;
		
		void start(){
		}
		
		void add(
				int drawMode,int texture,
				int poffset,int psize, int pnum,float[] verts,
				int toffset,int tsize,int tnum,float[] texCoords,
				float tintWeight, float r, float g, float b, float a,
				float[] mvMatrix){
			get(drawMode,mvMatrix[15]).add(
					texture,
					poffset,psize,pnum,verts,
					toffset,tsize,tnum,texCoords,
					tintWeight, r, g, b, a, 
					mvMatrix);
		}
		
		void add(int drawMode,int texture,float x, float y, float u, float v, float tintWeight, float r, float g, float b, float a,float[] mvMatrix){
			get(drawMode,mvMatrix[15]).add(texture,x,y,u,v,tintWeight,r,g,b,a,mvMatrix);
		}
		
		void finish(GLProgram program,int type,float[] pvMatrix){
			finish(tree,program,type,pvMatrix);
			tree = null;
			nodeCount = 0;
		}
		
		private void finish(Node n,GLProgram program,int type,float[] pvMatrix){
			if(n!=null){
				finish(n.left,program,type,pvMatrix);
				n.triangles.finish(program,type,pvMatrix);
				n.lines.finish(program,type,pvMatrix);
				finish(n.right,program,type,pvMatrix);
			}
		}
		
		private BatchSet get(int drawMode,float z){
			Node n;
			if(tree==null){
				n = (tree = getNode(z));
			}else{
				n = get(tree,z);
			}
			return (drawMode==FILL) ? n.triangles : n.lines;
		}
		
		private Node get(Node n, float z){
			if(n.z == z){
				return n;
			}else if(n.z > z){
				if(n.left==null) n.left = getNode(z);
				return get(n.left,z);
			}else{
				if(n.right==null) n.right = getNode(z);
				return get(n.right,z);
			}
		}
		
		private final ArrayList<Node> nodePool = new ArrayList<Node>();
		int nodeCount;
		
		private Node getNode(float z){
			if(nodePool.size()<=nodeCount)nodePool.add(new Node());
			Node n = nodePool.get(nodeCount++);
			n.left=null;
			n.right=null;
			n.z = z;
			if(n.lines==null)n.lines = new BatchSet();
			n.lines.start();
			if(n.triangles==null)n.triangles = new BatchSet();
			n.triangles.start();
			return n;
		}
		
		static final class Node{
			float z;
			Node left;
			Node right;
			BatchSet lines;
			BatchSet triangles;
		}
	}

	@Override
	public void draw(Sprite sprite) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void text(CharSequence text) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void text(char[] text) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void text(CharSequence text, int offset, int length) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void text(char[] text, int offset, int length) {
		// TODO Auto-generated method stub
		
	}

	@Override
	void background(Background b, Box screen) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double textWidth(CharSequence text) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double textWidth(char[] text) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double textWidth(CharSequence text, int offset, int length) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double textWidth(char[] text, int offset, int length) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public double textHeight(){
		FontData data = getState().font.data;
		return data.top-data.bottom;
	}

	@Override
	public void lineLoop(int offset, int size, int num, double[] points) {
		// TODO Auto-generated method stub
		
	}
}
