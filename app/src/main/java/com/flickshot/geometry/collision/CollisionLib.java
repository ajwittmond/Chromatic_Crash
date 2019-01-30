/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.flickshot.geometry.collision;

import com.flickshot.geometry.Box;
import com.flickshot.geometry.Vector2d;


/**
 * This class provides a set of static functions for detecting and dealing with
 * collisions between geometry objects.  This module tries to do all of its calculations
 * without making allocations to the heap. This library is not thread safe at all.
 * @author Alex Wittmond
 * @version %1% %G%
 */
public class CollisionLib {
	
	/**
	 * Code taken from later version of java.
	 * found at http://grepcode.com/file/repository.grepcode.com/java/root/jdk/openjdk/6-b14/sun/misc/FpUtils.java#FpUtils.nextAfter%28float%2Cdouble%29
	 * @param start
	 * @param direction
	 * @return
	 */
	public static double  nextAfter(double start, double direction) {

	         /*

	          * The cases:

	          *

	          * nextAfter(+infinity, 0)  == MAX_VALUE

	          * nextAfter(+infinity, +infinity)  == +infinity

	          * nextAfter(-infinity, 0)  == -MAX_VALUE

	          * nextAfter(-infinity, -infinity)  == -infinity

	          *

	          * are naturally handled without any additional testing

	          */

	         // First check for NaN values
	         if (Double.isNaN(start) || Double.isNaN(direction)) {

	             // return a NaN derived from the input NaN(s)

	             return start + direction;

	         } else if (start == direction) {

	             return direction;

	         } else {        // start > direction or start < direction

	             // Add +0.0 to get rid of a -0.0 (+0.0 + -0.0 => +0.0)

	             // then bitwise convert start to integer.

	             long transducer = Double.doubleToRawLongBits(start + 0.0d);

	             /*

	              * IEEE 754 floating-point numbers are lexicographically

	              * ordered if treated as signed- magnitude integers .

	              * Since Java's integers are two's complement,

	              * incrementing" the two's complement representation of a

	              * logically negative floating-point value *decrements*

	              * the signed-magnitude representation. Therefore, when

	              * the integer representation of a floating-point values

	              * is less than zero, the adjustment to the representation

	              * is in the opposite direction than would be expected at

	              * first .

	              */

	             if (direction > start) { // Calculate next greater value

	                 transducer = transducer + (transducer >= 0L ? 1L:-1L);

	             } else  { // Calculate next lesser value

	                 assert direction < start;

	                 if (transducer > 0L)
	                     --transducer;

	                 else

	                     if (transducer < 0L )
	                         ++transducer;

	                     /*

	                      * transducer==0, the result is -MIN_VALUE

	                      *
	                      * The transition from zero (implicitly

	                      * positive) to the smallest negative

	                      * signed magnitude value must be done

	                      * explicitly.


	                      */

	                     else

	                         transducer = 0x8000000000000000L | 1L;

	             }

	             return Double.longBitsToDouble(transducer);

	         }

	     }
	/**
	 * This a a function for floating point comparison.  0 is returned if the two numbers are within the greatest ulp of each other else 1 is returned 
	 * if the first number is greater than the seconde number plus the ulp and -1 is returned if the second number is greater that the first plus the ulp.
	 * @param a number to be compared
	 * @param b number to be compared
	 * @return
	 */
	public static final int fuzzyComp(double a, double b){
        if(a==b)return 0;
        if(Math.ulp(b)>Math.ulp(a)){
            double next = nextAfter(b, a);
            if(b<next){
                if(a>=next){
                    return -1;
                }else{
                    return 0;
                }
            }else{
                if(a<=next){
                    return 1;
                }else{
                    return 0;
                }
            }
        }else{
            double next = nextAfter(a, b);
            if(a<next){
                if(b>=next){
                    return 1;
                }else{
                    return 0;//b is within error of a
                }
            }else{
                if(b<=next){
                    return -1;
                }else{
                    return 0;//b is within error of a
                }
            }
        }
    }
	
	public static final double pythag(double a, double b){
		return Math.sqrt((a*a)+(b*b));
	}
	
	/**
	 * returns true if x1 is within the range defined by x2 to x2+width2
	 * @param x1 point to check against
	 * @param x2 this start point
	 * @param width2 the size of the range
	 * @return true if x1 is within the range defined by x2 to x2+width2
	 */
	public static final boolean boundsCheck(double x1, double x2, double width2){
		return x1>x2 && x1<x2+width2;
	}
	
	/**
	 * returns true if the range defined by x2 to x2+with2 overlaps the range defined by x1 to x1+width1
	 * @param x1 start point
	 * @param width1 size of range
	 * @param x2 point to check against
	 * @return true if the range defined by x2 to x2+with2 overlaps the range defined by x1 to x1+width1
	 */
	public static final boolean boundsCheck(double x1, double width1, double x2, double width2){
		return x1<x2+width2 && x1+width1>x2;
	}
	
	/**
	 * checks if the coordinates of the point are within the passed box dimensions
	 * @param x x coordinate of point being checked
	 * @param y y coordinate of point being checked
	 * @param bx x coordinate of the box's bottom left hand corner
	 * @param by y coordinate of the box's bottom left hand corner
	 * @param bwidth the box's width
	 * @param bheight the box's height
	 * @return
	 */
	public static final boolean pointBox(double x, double y, double bx, double by, double bwidth, double bheight){
		return boundsCheck(x,bx,bwidth) && boundsCheck(y,by,bheight);
	}
	
	/**
	 * checks if the boxes overlap
	 * @param x1 x coordinate of the box's bottom left hand corner
	 * @param y1 y coordinate of the box's bottom left hand corner
	 * @param width1 box's width
	 * @param height1 box's height
	 * @param x2 x coordinate of the box's bottom left hand corner
	 * @param y2 y coordinate of the box's bottom left hand corner
	 * @param width2 box's width
	 * @param height2 box's height
	 * @return
	 */
	public static boolean boxBox(double x1, double y1, double width1, double height1,double x2, double y2, double width2, double height2){
		return boundsCheck(x1,width1,x2,width2) && boundsCheck(y1,height1,y2,height2);
	}
	
	public static boolean boxBox(Box a, Box b){
		return boxBox(a.getX(),a.getY(),a.getWidth(),a.getHeight(),b.getX(),b.getY(),b.getWidth(),b.getHeight());
	}
	
	public static final boolean circleCircle(double cx1,double cy1,double radius1,
            double cx2, double cy2,double radius2){
		double r = (radius1+radius2);
        return Vector2d.distSquared(cx1, cy1,cx2,cy2)<(r*r);
    }
	
	public static final boolean pointCircle(double x, double y, double cx, double cy, double radius){
		return Vector2d.distSquared(x,y,cx,cy)<radius*radius;
	}
	
	public static final Vector2d lineIntersect(double xa1,double ya1, double xa2, double ya2,
            double xb1,double yb1, double xb2, double yb2,Vector2d p){
		if(LineResult.getResults(xa1,ya1,xa2,ya2,xb1,yb1,xb2,yb2)){
			if(p != null){
			    p.set(LineResult.point);
			    return p;
			}else{
			    return LineResult.point;
			}
		}
		return null;
	}
	
	public static final Vector2d lineLine(double xa1,double ya1, double xa2, double ya2,
            double xb1,double yb1, double xb2, double yb2,Vector2d p){
		if(LineResult.getResults(xa1,ya1,xa2,ya2,xb1,yb1,xb2,yb2)){
			if(LineResult.ua>0 && LineResult.ua<1 && LineResult.ub>0 && LineResult.ub<1){
			    if(p != null){
			        p.set(LineResult.point);
			        return p;
			    }else{
			        return LineResult.point;
			    }
			}
		}
		return null;
	}
	
    public static final Vector2d lineRay(double xa1,double ya1, double xa2, double ya2,
            double xb1,double yb1, double xb2, double yb2,Vector2d p){
		if(LineResult.getResults(xa1,ya1,xa2,ya2,xb1,yb1,xb2,yb2)){
			if(LineResult.ua>0 && LineResult.ua<1 && LineResult.ub>0){
			    if(p != null){
			        p.set(LineResult.point);
			        return p;
			    }else{
			        return LineResult.point;
			    }
			}
		}
		return null;
	}

    public static final Vector2d rayRay(double xa1,double ya1, double xa2, double ya2,
            double xb1,double yb1, double xb2, double yb2,Vector2d p){
		if(LineResult.getResults(xa1,ya1,xa2,ya2,xb1,yb1,xb2,yb2)){
			if(LineResult.ua>0 && LineResult.ub>0){
			    if(p != null){
			        p.set(LineResult.point);
			        return p;
			    }else{
			        return LineResult.point;
			    }
			}
		}
		return null;
	}
    
    public static final Vector2d rayIntersect(double xal,double yal, double xbl, double ybl,
        double xar,double yar, double xbr, double ybr,Vector2d p){
		if(LineResult.getResults(xal,yal,xbl,ybl,xar,yar,xbr,ybr)){
			if(LineResult.ub>0){
			    if(p==null){
			        return LineResult.point;
			    }else{
			        p.set(LineResult.point);
			        return p;
			    }
			}
		}
		return null;
	}
    
    public static final boolean pointOnLine(double ax,double ay, double bx, double by, double px, double py){
        if(fuzzyComp(ax,bx)==0){
            return fuzzyComp(px,ax)==0 && py>Math.min(ay,by) 
                    && py<Math.max(ay, by);
        }else if(fuzzyComp(ay,by)==0){
            return fuzzyComp(py,ay)==0 && px>Math.min(ax,bx) 
                    && px<Math.max(ax, bx);
        }else{
            double  ux = ((px-ax)/(bx-ax)),
                    uy = ((py-ay)/(by-ay));
            if(fuzzyComp(ux,uy)==0){
                return (fuzzyComp(ux,0)<=0 && fuzzyComp(ux,1)>=0 && fuzzyComp(uy,0)<=0 && fuzzyComp(uy,1)>=0);
            }
            return false;
        }
    }
    
    public static final boolean pointOnLine(Vector2d la, Vector2d lb, Vector2d p){
        return pointOnLine(la.x,la.y,lb.x,lb.y,p.x,p.y);
    }
    
    public static final boolean circleLine(double xc, double yc, double radius, double xa, 
            double ya, double xb, double yb){
	    CircleLineResult.getRes(xc,yc,radius,xa,ya,xb,yb);
	    if(CircleLineResult.delta<0){
	        return false;
	    }else if(fuzzyComp(CircleLineResult.delta,0)==0){
	    	return false;
	    }else{
	        return (CircleLineResult.u1>0 && CircleLineResult.u1<1)||
	                (CircleLineResult.u2>0 && CircleLineResult.u2<1);
	    }
	}
    
    public static final int circleLineIntersects(double xc, double yc, double radius, double xa, 
            double ya, double xb, double yb, Vector2d a, Vector2d b){
	    int points = 0;
	    double xdif=xb-xa, ydif=yb-ya;
	    CircleLineResult.getRes(xc,yc,radius,xa,ya,xb,yb);
	    if(CircleLineResult.delta>=0){
	        if(fuzzyComp(CircleLineResult.delta,0)!=0){
	            if(CircleLineResult.u1>0 && CircleLineResult.u1<1){
	                points++;
	                double u = CircleLineResult.u1;
	                a.x = xa+(xdif*u);
	                a.y = ya+(ydif*u);
	            }
	            if(CircleLineResult.u2>0 && CircleLineResult.u2<1){
	                points++;
	                Vector2d p = (points>0) ? b:a;
	                double u = CircleLineResult.u2;
	                p.x = xa+(xdif*u);
	                p.y = ya+(ydif*u);
	            }
	        }
	    }
	    return points;
	}
    
    private static final Vector2d axis = new Vector2d();
    private static final Projection proja = new Projection();
    private static final Projection projb = new Projection();
    
    public static final boolean triangleTriangle(double ax1, double ay1, double ax2, double ay2, double ax3, double ay3, 
    		double bx1, double by1, double bx2, double by2, double bx3, double by3){
    	setAxis(axis,ax1,ay1,ax2,ay2);
    	proja.set(axis, ax1, ay1, ax2, ay2, ax3, ay3);
    	projb.set(axis, bx1, by1, bx2, by2, bx3, by3);
    	if(!proja.overlap(proja))return false;
    	setAxis(axis,ax2,ay2,ax3,ay3);
    	proja.set(axis, ax1, ay1, ax2, ay2, ax3, ay3);
    	projb.set(axis, bx1, by1, bx2, by2, bx3, by3);
    	if(!proja.overlap(proja))return false;
    	setAxis(axis,ax3,ay3,ax1,ay1);
    	proja.set(axis, ax1, ay1, ax2, ay2, ax3, ay3);
    	projb.set(axis, bx1, by1, bx2, by2, bx3, by3);
    	if(!proja.overlap(proja))return false;
    	setAxis(axis,bx1,by1,bx2,by2);
    	proja.set(axis, ax1, ay1, ax2, ay2, ax3, ay3);
    	projb.set(axis, bx1, by1, bx2, by2, bx3, by3);
    	if(!proja.overlap(proja))return false;
    	setAxis(axis,bx2,by2,bx3,by3);
    	proja.set(axis, ax1, ay1, ax2, ay2, ax3, ay3);
    	projb.set(axis, bx1, by1, bx2, by2, bx3, by3);
    	if(!proja.overlap(proja))return false;
    	setAxis(axis,bx3,by3,bx1,by1);
    	proja.set(axis, ax1, ay1, ax2, ay2, ax3, ay3);
    	projb.set(axis, bx1, by1, bx2, by2, bx3, by3);
    	if(!proja.overlap(proja))return false;
    	return true;
    }
    
    private static final Vector2d setAxis(Vector2d axis, double x1 ,double y1, double x2, double y2){
    	axis.set(x1-x2,x2-y2);
    	axis.perp();
    	return axis;
    }
    
	private final static class LineResult{
        static double ua;
        static double ub;
        static double denom;
        final static Vector2d point = new Vector2d();
        
        static final boolean getResults( double xa1,double ya1, double xa2, double ya2,
                        double xb1,double yb1, double xb2, double yb2){
            denom = (((yb2-yb1)*(xa2-xa1))-((xb2-xb1)*(ya2-ya1)));
            if(fuzzyComp(denom,0)==0){//lines do not collide
                ua = 0;
                ub = 0;
                point.set(0, 0);
                denom = 0;
                return false;
            }else{
                ua = (((xb2-xb1)*(ya1-yb1))-((yb2-yb1)*(xa1-xb1)))/denom;
                ub = (((xa2-xa1)*(ya1-yb1))-((ya2-ya1)*(xa1-xb1)))/denom;
				point.x= xa1+ua*(xa2-xa1);
				point.y= ya1+ua*(ya2-ya1);
                return true;
            }
        }
    }
	
	private static final class CircleLineResult{//used to avoid unneccessary allocations
        private static final Vector2d p1 = new Vector2d();
        private static final Vector2d p2 = new Vector2d();
        private static final Vector2d pdif = new Vector2d();
        public static  double delta=0;
        public static  double u1=0;
        public static  double u2 = 0;
        
        static void getRes(
                double xc, double yc, double radius, double xa, 
                double ya, double xb, double yb){
            p1.x = xa-xc;
            p1.y = ya-yc;
            p2.x = xb-xc; 
            p2.y = yb-yc;
            pdif.x = p2.x-p1.x;
            pdif.y = p2.y-p1.y;
			
            double  a = (pdif.x*pdif.x)+(pdif.y*pdif.y),
                    b = 2 * ((pdif.x*p1.x)+(pdif.y*p1.y)),
                    c = (p1.x*p1.x) + (p1.y*p1.y) - (radius*radius);
            delta = (b*b) - (4*a*c);
            if(delta>0){
                if(fuzzyComp(delta,0)==0){
                    u1 = -b/(2*a);
                }else{
                    u1 = (-b + Math.sqrt(delta))/(2*a);
                    u2 = (-b - Math.sqrt(delta))/(2*a);
                }
            }
        }
    }
	
	private static final class Projection{
		double min;
		double max;
		
		final void set(Vector2d axis, double x1, double y1, double x2, double y2, double x3, double y3){
			double a = axis.dot(x1,y1);
			double b = axis.dot(x2,y2);
			double c = axis.dot(x3,y3);
			min = Math.min(Math.min(a, b),c);
			max = Math.max(Math.max(a, b),c);
		}
		
		final boolean overlap(Projection p){
			return fuzzyComp(p.min,max)<0 && fuzzyComp(p.max,min)>0;
		}
	}
}
