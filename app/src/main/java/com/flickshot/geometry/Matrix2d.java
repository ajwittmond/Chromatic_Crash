package com.flickshot.geometry;

/**
 * This class stores and provides operations for a 2d transformation matrix
 * @author Alex
 *
 */
public class Matrix2d {
	private static final double[] IDENTITY_MATRIX = new double[]{
		1.0, 
		0.0,
			0.0,
			1.0,
				0.0,
				0.0
	};
	
	private final double[] value = new double[6];
	
	public Matrix2d(){}
	
	public Matrix2d(Matrix2d m){
		System.arraycopy(m.value,0,value,0,6);
	}
	
	public void identity(){
		System.arraycopy(IDENTITY_MATRIX,0,value,0,6);
	}
	
	public void invert(){
		double a1 = value[0], a2=value[1], b1=value[2], b2=value[3], atx=value[4], aty=value[5];
		
		double det = (a1*b2)-(a2*b1);
		if(det==0)throw new IllegalStateException("Matrix is not invertible");
		
		value[0] = b2 * det;
		value[1] = -a2 * det;
		value[2] = -b1 * det;
		value[3] = a1 * det;
		value[4] = ((b1*aty) - (b2*atx)) * det;
		value[5] = ((a2*atx) - (a1*aty)) * det;
	}
	
	public double getDeterminant(){
		return (value[0]*value[3])-(value[1]*value[2]);
	}
	
	public void scale(double x, double y){
		value[0] *=x;
		value[1] *=y;
		value[2] *=x;
		value[3] *=y;
		value[4] *=x;
		value[5] *=y;
	}
	
	public void scale(Vector2d scale){
		scale(scale.x,scale.y);
	}
	
	public void translate(double x, double y){
		value[4]+=x*value[0] + y*value[2];
		value[5]+=x*value[1] + y*value[3];
	}
	
	public void translate(Vector2d translation){
		translate(translation.x,translation.y);
	}
	
	public void rotate(double rad){
		double a1 = value[0], a2=value[1], b1=value[2], b2=value[3];
		double c = Math.cos(rad),s=Math.sin(rad);
		
		value[0] = a1*c + b1*s;
		value[1] = a2*c + b2*s;
		value[2] = -a1*s + b1*c;
		value[3] = -a2*s + b2*c;
	}

	public void set(double x, double y, double w, double h, double theta){
		double c = Math.cos(theta),s=Math.sin(theta);
		
		value[0] = c*w;
		value[1] = s*w;
		value[2] = -s*h;
		value[3] = c*h;
		value[4] = x;
		value[5] = y;
	}
	
	public void set(Vector2d translation, Vector2d scale, double theta){
		double c = Math.cos(theta),s=Math.sin(theta);
		
		value[0] = c*scale.x;
		value[1] = s*scale.x;
		value[2] = -s*scale.y;
		value[3] = c*scale.y;
		value[4] = translation.x;
		value[5] = translation.y;
	}
	
	public void set(Transformation tx){
		double c = Math.cos(tx.theta.val),s=Math.sin(tx.theta.val);
		
		value[0] = c*tx.scale.x;
		value[1] = s*tx.scale.x;
		value[2] = -s*tx.scale.y;
		value[3] = c*tx.scale.y;
		value[4] = tx.translation.x;
		value[5] = tx.translation.y;
	}
	
	public Vector2d transform(Vector2d vec){
		final double u = vec.x,v = vec.y;
		vec.x = value[0]*u + value[2]*v + value[4];
		vec.y = value[1]*u + value[3]*v + value[5];
		return vec;
	}
	
	public Vector2d tranformAxis(Vector2d vec){
		final double u = vec.x,v = vec.y;
		vec.x = value[0]*u + value[2]*v;
		vec.y = value[1]*u + value[3]*v;
		return vec;
	}
	
	public Vector2d transform(Vector2d out,Vector2d vec){
		final double u = vec.x,v = vec.y;
		out.x = value[0]*u + value[2]*v + value[4];
		out.y = value[1]*u + value[3]*v + value[5];
		return out;
	}
	
	public Vector2d inverseTransform(Vector2d vec){
		double det = (value[0]*value[3])-(value[1]*value[2]);
		if(det==0)throw new IllegalStateException("Matrix is not invertible");
		det = 1/det;
		final double u = vec.x,v = vec.y;
		
		vec.x = (value[3] * det)*u + (-value[2] * det)*v;// - value[4];
		vec.y = (-value[1] * det)*u + (value[0] * det)*v;// - value[5];
		return vec;
	}
	
	public Vector2d inverseTransform(Vector2d out,Vector2d vec){
		double det = (value[0]*value[3])-(value[1]*value[2]);
		if(det==0)throw new IllegalStateException("Matrix is not invertible");
		
		final double u = vec.x,v = vec.y;
		
		out.x = (value[3] * det)*u + (-value[2] * det)*v + (((value[2]*value[5]) - (value[3]*value[4])) * det);
		out.y = (-value[1] * det)*u + (value[0] * det)*v + (((value[1]*value[4]) - (value[0]*value[5])) * det);
		return out;
	}
	
	public void transform(int offset,int stride, double[] vecs, int number){
		for(int i = 0; i<number; i++){
			int index = offset+(stride*i);
			double u = vecs[index], v = vecs[index+1];
			vecs[index] = value[0]*u + value[2]*v + value[4];
			vecs[index+1] = value[1]*u + value[3]*v + value[5];
		}
	}
	
	public void transform(int outOffset,int outStride,double[] out,int offset,int stride,double[] vecs,int number){
		for(int i = 0; i<number; i++){
			int index = offset+(stride*i);
			int outIndex = outOffset+(outStride*i);
			double u = vecs[index], v = vecs[index+1];
			out[outIndex] = value[0]*u + value[2]*v + value[4];
			out[outIndex+1] = value[1]*u + value[3]*v + value[5];
		}
	}
	
	public void inverseTransform(int offset,int stride, double[] vecs, int number){
		double det = (value[0]*value[3])-(value[1]*value[2]);
		if(det==0)throw new IllegalStateException("Matrix is not invertible");
		
		double a1 = value[3]*det, a2=-value[1]*det, b1=-value[2]*det, b2=value[0]*det, 
				atx=((value[2]*value[5])-(value[3]*value[4]))*det, 
				aty=((value[1]*value[4])-(value[0]*value[5]))*det;
		
		for(int i = 0; i<number; i++){
			int index = offset+(stride*i);
			double u = vecs[index], v = vecs[index+1];
			vecs[index] = a1*u + b1*v + atx;
			vecs[index+1] = a2*u + b2*v + aty;
		}
	}
	
	public void inverseTransform(int outOffset,int outStride,double[] out,int offset,int stride,double[] vecs,int number){
		double det = (value[0]*value[3])-(value[1]*value[2]);
		if(det==0)throw new IllegalStateException("Matrix is not invertible");
		
		double a1 = value[3]*det, a2=-value[1]*det, b1=-value[2]*det, b2=value[0]*det, 
				atx=((value[2]*value[5])-(value[3]*value[4]))*det, 
				aty=((value[1]*value[4])-(value[0]*value[5]))*det;
		
		for(int i = 0; i<number; i++){
			int index = offset+(stride*i);
			int outIndex = outOffset+(outStride*i);
			double u = vecs[index], v = vecs[index+1];
			out[outIndex] = a1*u + b1*v + atx;
			out[outIndex+1] = a2*u + b2*v + aty;
		}
	}
	
	/**
	 * sets the value of this matrix to equal a(this) so that the resulting matrix represents this transformation then the 
	 * passed transformation
	 * @param a
	 */
	public void mul(Matrix2d a){
		mul(this,this,a);
	}
	
	/**
	 * sets the value of this matrix equal to the value of the matrix representation of the passed transformation 
	 * @param tx
	 */
	public void mul(Transformation tx){
		double aa1 = value[0], aa2=value[1], ab1=value[2], ab2=value[3], atx=value[4], aty=value[5];
		double c = Math.cos(tx.theta.val), s = Math.cos(tx.theta.val);
		
		double ba1 = tx.scale.x*c;
		double ba2 = tx.scale.y*s;
		double bb1 = -tx.scale.x*s;
		double bb2 = tx.scale.y*c;
		double btx = tx.translation.x;
		double bty = tx.translation.y;
		
		value[0] = aa1*ba1 + aa2*bb1;
		value[1] = aa1*ba2 + aa2*bb2;
		value[2] = ab1*ba1 + ab2*bb1;
		value[3] = ab1*ba2 + ab2*bb2;
		value[4] = ba1*atx + bb1*aty + btx;
		value[5] = ba2*atx + bb2*aty + bty;
	}
	
	public float[] toGlMatrix(float[] out){
		out[0] = (float)value[0];
		out[1] = (float)value[1];
		out[2] = 0;
		out[3] = 0;
		
		out[4] = (float)value[2];
		out[5] = (float)value[3];
		out[6] = 0;
		out[7] = 0;
		
		out[8] = 0;
		out[9] = 0;
		out[10] = 1;
		out[11] = 0;
		
		out[12] = (float)value[4];
		out[13] = (float)value[5];
		out[14] = 0;
		out[15] = 0;
		return out;
	}
	
	public Matrix2d clone(){
		return new Matrix2d(this);
	}
	
	/**
	 * Multiplies the two matrices.  Note that the order of operands is reversed from normal such that 
	 * a*b is actual b*a so that the order of argument represents the order of the transformations.
	 * @param out
	 * @param a
	 * @param b
	 * @return
	 */
	public static final Matrix2d mul(Matrix2d out, Matrix2d a, Matrix2d b){
		double aa1 = a.value[0], aa2=a.value[1], ab1=a.value[2], ab2=a.value[3], atx=a.value[4], aty=a.value[5];
		double ba1 = b.value[0], ba2=a.value[1], bb1=b.value[2], bb2=a.value[3], btx=a.value[4], bty=a.value[5];
		
		out.value[0] = aa1*ba1 + aa2*bb1;
		out.value[1] = aa1*ba2 + aa2*bb2;
		out.value[2] = ab1*ba1 + ab2*bb1;
		out.value[3] = ab1*ba2 + ab2*bb2;
		out.value[4] = ba1*atx + bb1*aty + btx;
		out.value[5] = ba2*atx + bb2*aty + bty;
		return out;
	}
}
