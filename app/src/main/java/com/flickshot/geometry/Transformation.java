package com.flickshot.geometry;

import com.flickshot.util.MatrixStack;
import com.flickshot.util.MutableDouble;

import android.opengl.Matrix;


/**
 * This is a lightweight object for storing transformation information.
 * @author Alex
 *
 */
public class Transformation {
	public Vector2d translation;
	public Vector2d scale;
	public MutableDouble theta;
	
	public Transformation(){
		translation = new Vector2d(0,0);
		scale = new Vector2d(1,1);
		theta = new MutableDouble(0);
	}
	
	public Transformation(Vector2d translation, Vector2d scale, MutableDouble theta){
		this.translation = translation;
		this.scale = scale;
		this.theta = theta;
	}
	
	public Transformation(double x, double y, double w, double h, double theta){
		this.translation = new Vector2d(x,y);
		this.scale = new Vector2d(w,h);
		this.theta = new MutableDouble(theta);
	}
	
	public final void set(Transformation t){
		translation.set(t.translation);
		scale.set(t.scale);
		theta = t.theta;
	}
	
	public final void set(Vector2d translation, Vector2d scale, MutableDouble theta){
		this.translation = translation;
		this.scale = scale;
		this.theta = theta;
	}
	
	public final void set(double x, double y, double w, double h, double theta){
		translation.set(x,y);
		scale.set(w,h);
		this.theta=new MutableDouble(theta);
	}
	
	public final void add(double x, double y, double w, double h, double theta){
		translation.add(x,y);
		scale.add(w,h);
		theta+=theta;
	}
	
	public final void add(Vector2d translation, Vector2d scale, double theta){
		translation.add(translation);
		scale.add(scale);
		theta+=theta;
	}
	
	public final void rotate(double theta){
		this.theta.val+=theta;
	}
	
	public final void translate(double x, double y){
		translation.add(x,y);
	}
	 
	public final void translate(Vector2d t){
		translation.add(t);
	}
	
	public final void scale(double w, double h){
		scale.x*=w;
		scale.y*=h;
	}
	
	public final void scale(Vector2d s){
		scale.x*=s.x;
		scale.y*=s.y;
	}
	
	public final void set(int offset,float[] matrix){
		float s=(float)Math.sin(theta.val), c=(float)Math.cos(theta.val);
		float w=(float)scale.x,h=(float)scale.y;
		if(matrix.length-offset>=16){
			//set translation and identity
			matrix[offset+0] = 1;	matrix[offset+4]=0;	matrix[offset+8]=0;		matrix[offset+12]=(float)translation.x;
			matrix[offset+1] = 0;	matrix[offset+5]=1;	matrix[offset+9]=0;		matrix[offset+13]=(float)translation.y;
			matrix[offset+2] = 0;	matrix[offset+6]=0;	matrix[offset+10]=1;	matrix[offset+14]=0;
			matrix[offset+3] = 0;	matrix[offset+7]=0;	matrix[offset+11]=0;	matrix[offset+15]=0;
			//set rotation and scale
			matrix[offset+0] = w*c; matrix[offset+4]=h*-c;
			matrix[offset+1] = w*s; matrix[offset+5]=h*c;
		}else if(matrix.length-offset>=9){
			//set translation and identity
			matrix[offset+0] = 1;	matrix[offset+3]=0;	matrix[offset+6]=(float)translation.x;
			matrix[offset+1] = 0;	matrix[offset+4]=1;	matrix[offset+7]=(float)translation.y;
			matrix[offset+2] = 0;	matrix[offset+5]=0;	matrix[offset+8]=1;
			//set rotation and scale
			matrix[offset+0] = w*c; matrix[offset+3]=h*-c;
			matrix[offset+1] = w*s; matrix[offset+4]=h*c;
		}else{
			throw new IllegalArgumentException("Passed array is not long enough to be set to transformation matrix");
		}
	}
	
	public final void set(int offset, double[] matrix){
		double s=Math.sin(theta.val), c=Math.cos(theta.val);
		double w=scale.x,h=scale.y;
		if(matrix.length-offset>=16){
			//set translation and identity
			matrix[offset+0] = 1;	matrix[offset+4]=0;	matrix[offset+8]=0;		matrix[offset+12]=translation.x;
			matrix[offset+1] = 0;	matrix[offset+5]=1;	matrix[offset+9]=0;		matrix[offset+13]=translation.y;
			matrix[offset+2] = 0;	matrix[offset+6]=0;	matrix[offset+10]=1;	matrix[offset+14]=0;
			matrix[offset+3] = 0;	matrix[offset+7]=0;	matrix[offset+11]=0;	matrix[offset+15]=0;
			//set rotation and scale
			matrix[offset+0] = w*c; matrix[offset+4]=h*-c;
			matrix[offset+1] = w*s; matrix[offset+5]=h*c;
		}else if(matrix.length-offset>=9){
			//set translation and identity
			matrix[offset+0] = 1;	matrix[offset+3]=0;	matrix[offset+6]=translation.x;
			matrix[offset+1] = 0;	matrix[offset+4]=1;	matrix[offset+7]=translation.y;
			matrix[offset+2] = 0;	matrix[offset+5]=0;	matrix[offset+8]=1;
			//set rotation and scale
			matrix[offset+0] = w*c; matrix[offset+3]=h*-c;
			matrix[offset+1] = w*s; matrix[offset+4]=h*c;
		}else{
			throw new IllegalArgumentException("Passed array is not long enough to be set to transformation matrix");
		}
	}
	
	public final void transform(Vector2d vec){
		double c=Math.cos(theta.val), s = Math.sin(theta.val);
		double u = vec.x*scale.x,v = vec.y*scale.y;
		vec.x=(c*u - s*v)+translation.x;
		vec.y=(s*u + c*v)+translation.y;
	}
	
	public final void invTransform(Vector2d vec){
		double c=Math.cos(-theta.val), s = Math.sin(-theta.val);
		double u = (vec.x-translation.x)/scale.x,v = (vec.y-translation.y)/scale.y;
		vec.x=(c*u - s*v);
		vec.y=(s*u + c*v);
	}
	
	public final void transform(int offset, float[] vec){
		double c=Math.cos(theta.val), s = Math.sin(theta.val);
		double u = vec[offset]*scale.x,v = vec[offset+1]*scale.y;
		vec[offset]=(float)((c*u - s*v)+translation.x);
		vec[offset+1]=(float)((s*u + c*v)+translation.y);
	}
	
	public final void transform(int offset, double[] vec){
		double c=Math.cos(theta.val), s = Math.sin(theta.val);
		double u = vec[offset]*scale.x,v = vec[offset+1]*scale.y;
		vec[offset]=(c*u - s*v)+translation.x;
		vec[offset+1]=(s*u + c*v)+translation.y;
	}
	
	public final void transform(MatrixStack m){
		float[] mat = m.current();
		Matrix.translateM(mat,0,(float)translation.x,(float)translation.y,0);
		Matrix.scaleM(mat,0,(float)scale.x,(float)scale.y,1);
		Matrix.rotateM(mat, 0, (float)(theta.val*(180/Math.PI)), 0, 0, 1);
	}
	
	/**
	 * sets the value of theta to be >= 0 and < PI*2
	 */
	public void setProperAngle(){
		theta.val = theta.val%(Math.PI*2);
		if(theta.val<0)theta.val+=(Math.PI*2);
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof Transformation){
			Transformation t = (Transformation)o;
			return t.theta==theta && t.translation.equals(translation) && t.scale.equals(scale);
		}
		return false;
	}
	
	@Override
	public String toString(){
		return "{translation:"+translation+" scale:"+scale+" theta:"+theta.val+"}";
	}
}
