package com.flickshot.components.graphics;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;

import android.opengl.GLES20;

import com.flickshot.components.graphics.program.GLProgram;

public class Batch {
	private static final int POSITION_OFFSET=0;
	private static final int POSITION_SIZE=2;
	
	private static final int TEXTURE_COORDINATE_OFFSET=2;
	private static final int TEXTURE_COORDINATE_SIZE=2;
	
	private static final int COLOR_OFFSET=4;
	private static final int COLOR_SIZE=4;
	
	private static final int TINT_WEIGHT_OFFSET=8;
	private static final int TINT_WEIGHT_SIZE=1;
	
	private static final int MATRIX_INDEX_OFFSET=9;
	private static final int MATRIX_INDEX_SIZE=1;
	
	private static final int SIZE = MATRIX_INDEX_SIZE+TINT_WEIGHT_SIZE+COLOR_SIZE+TEXTURE_COORDINATE_SIZE+POSITION_SIZE;
	private static final int STRIDE = SIZE*4;
	
	private final int maxVerts;
	private final int maxMatrices;
	
	private final float[] vertexBuffer;
	
	private final float[] matrices;
	private final float[] pvMatrix = new float[16];
	
	private final int[] matrixCodes;
	
	private FloatBuffer vertices;
	
	private int currentMatrix;
	private int matrixCount;
	private int count;
	
	private boolean firstMatrix = false;
	private boolean full = false;
	private boolean childUsed = false;
	
	private Batch child;
	
	Batch(int maxVerts,int maxMatrices){
		
		vertexBuffer = new float[SIZE*maxVerts];
		matrices = new float[maxMatrices*16];
		matrixCodes = new int[maxMatrices];
		
		ByteBuffer bb = ByteBuffer.allocateDirect(SIZE*maxVerts*4);
		bb.order(ByteOrder.nativeOrder());
		vertices = bb.asFloatBuffer();
		
		this.maxVerts = maxVerts;
		this.maxMatrices = maxMatrices;
	}
	
	private void setChild(){
		if(child==null){
			child=new Batch(maxVerts,maxMatrices);
		}
		if(!childUsed){
			child.startBatch();
			child.setMVMatrix(matrices,currentMatrix);
			childUsed = true;
		}
	}
	
	private void bind(GLProgram program,float[] pvMatrix,int texture){
		program.useEnableAll();
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
		program.getUniform("uSampler").set1(0);
		program.getUniform("uPVMatrix").setMatrix4(1,false,pvMatrix,0);
	}
	
	private void unbind(GLProgram program){
		program.disableAll();
	}
	
	private void doDraw(GLProgram program,int type){
		program.getUniform("uMVMatrixArray").setMatrix4(matrixCount,false,matrices,0);
		
		vertices.put(vertexBuffer);
		
		vertices.position(POSITION_OFFSET);
		program.getAttribute("aVertexPosition").setAttributePointer(POSITION_SIZE,GLES20.GL_FLOAT,false,STRIDE,vertices);
		vertices.position(TEXTURE_COORDINATE_OFFSET);
		program.getAttribute("aTextureCoord").setAttributePointer(TEXTURE_COORDINATE_SIZE,GLES20.GL_FLOAT,false,STRIDE,vertices);
		vertices.position(COLOR_OFFSET);
		program.getAttribute("aVertexColor").setAttributePointer(COLOR_SIZE,GLES20.GL_FLOAT,false,STRIDE,vertices);
		vertices.position(TINT_WEIGHT_OFFSET);
		program.getAttribute("aTintWeight").setAttributePointer(TINT_WEIGHT_SIZE,GLES20.GL_FLOAT,false,STRIDE,vertices);
		vertices.position(MATRIX_INDEX_OFFSET);
		program.getAttribute("aMatrixIndex").setAttributePointer(MATRIX_INDEX_SIZE,GLES20.GL_FLOAT,false,STRIDE,vertices);
		
		GLES20.glDrawArrays(type,0,count);
	}
	
	private final float[] misc = new float[16];
	
	private int findMatrix(float[] mvMatrix){
		int code = Arrays.hashCode(mvMatrix);
		for(int i = 0; i<matrixCount; i++){
			if(matrixCodes[i]==code){
				System.arraycopy(matrices,i*16,misc,0,16);
				if(Arrays.equals(mvMatrix,misc))return i;
			}
		}
		return -1;
	}
	
	public void startBatch(){
		count = 0;
		currentMatrix=0;
		matrixCount = 0;
		firstMatrix = true;
		full = false;
		childUsed = false;
	}
	
	public void finish(GLProgram program,int type,float[] pvMatrix,int texture){
		bind(program,pvMatrix,texture);
		if(child!=null && childUsed)child.doDraw(program,type);
		unbind(program);
	}
	
	public void setMVMatrix(float[] mvMatrix){
		setMVMatrix(mvMatrix,0);
	}
	
	public void setMVMatrix(float[] mvMatrix, int offset){
		if(firstMatrix){
			System.arraycopy(mvMatrix,0,matrices,0,16);
			matrixCodes[0] = Arrays.hashCode(mvMatrix);
			firstMatrix = false;
		}if(full){
			child.setMVMatrix(mvMatrix);
		}else if((currentMatrix=findMatrix(mvMatrix))<0){
			if(matrixCount>=maxMatrices){
				full=true;
				setChild();
				child.setMVMatrix(mvMatrix);
			}else{
				currentMatrix = matrixCount;
				System.arraycopy(mvMatrix,offset,matrices,matrixCount++*16,16);
			}
		}
	}
	
	public void draw(int offset,int size,int num,float[] verts){
		if(firstMatrix)throw new IllegalStateException("attempt to draw before setting matrix");
		if(full){
			child.draw(offset,size,num,verts);
		}else if(count+num>maxVerts){
			setChild();
			child.draw(offset,size,num,verts);
		}else{
			
			for(int i =0; i<num; i++){
				int boffset = SIZE*count++;
				int voffset = offset+size*i;
				for(int j = 0;j<SIZE-1;j++)vertexBuffer[boffset++]=verts[voffset++];
				vertexBuffer[boffset++]=Float.intBitsToFloat(currentMatrix);
			}
			if(count==maxVerts){
				full=true;
				setChild();
			}
		}
	}
	
	public void draw(int poffset,int psize,int pnum,float[] pos,int toffset,int tsize, int tnum,
			float[] texCoords, float tintWeight, float r, float g, float b, float a){
		if(firstMatrix)throw new IllegalStateException("attempt to draw before setting matrix");
		if(full){
			child.draw(poffset,psize,pnum,pos,toffset,tsize,tnum,texCoords,tintWeight,r,g,b,a);
		}else if(count+pnum>maxVerts){
			setChild();
			child.draw(poffset,psize,pnum,pos,toffset,tsize,tnum,texCoords,tintWeight,r,g,b,a);
		}else{
			int tl = tsize*tnum;
			for(int i =0; i<pnum;i++){
				int boffset = SIZE*count++;
				int voffset = poffset+psize*i;
				int ttoffset = toffset+tsize*(i%tnum);
				vertexBuffer[boffset++]=pos[voffset++];
				vertexBuffer[boffset++]=pos[voffset];
				vertexBuffer[boffset++]=texCoords[ttoffset++];
				vertexBuffer[boffset++]=texCoords[ttoffset];
				vertexBuffer[boffset++]=r;
				vertexBuffer[boffset++]=g;
				vertexBuffer[boffset++]=b;
				vertexBuffer[boffset++]=a;
				vertexBuffer[boffset++]=tintWeight;
				vertexBuffer[boffset]=Float.intBitsToFloat(currentMatrix);
			}
			if(count==maxVerts){
				full=true;
				setChild();
			}
		}
	}
	
	public void draw(float x, float y, float u, float v, float tintWeight, float r, float g, float b, float a){
		if(full){
			child.draw(x,y,u,v,tintWeight,r,g,b,a);
		}else{
			int offset = count*SIZE;
			vertexBuffer[offset++]=x;
			vertexBuffer[offset++]=y;
			vertexBuffer[offset++]=u;
			vertexBuffer[offset++]=v;
			vertexBuffer[offset++]=r;
			vertexBuffer[offset++]=g;
			vertexBuffer[offset++]=b;
			vertexBuffer[offset++]=a;
			vertexBuffer[offset++]=tintWeight;
			vertexBuffer[offset]=Float.intBitsToFloat(currentMatrix);
			count++;
			if(count==maxVerts){
				full=true;
				setChild();
			}
		}
	}

}
