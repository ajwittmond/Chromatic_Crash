package com.flickshot.components.physics;

import java.util.ArrayList;

import com.flickshot.components.physics.shapes.Circle;
import com.flickshot.components.physics.shapes.Polygon;
import com.flickshot.config.Vector2dConfig;
import com.flickshot.geometry.Box;
import com.flickshot.geometry.Matrix2d;
import com.flickshot.geometry.Square;
import com.flickshot.geometry.Vector2d;
import com.flickshot.util.LinkedNodeList;

import static com.flickshot.geometry.collision.CollisionLib.circleCircle;
import static com.flickshot.util.MiscLib.*;

public class Scene implements Box{
	static final double DEFAULT_MARGIN = 64;
	static final double DEFAULT_bounds_STATIC_FRICTION = 0;
	static final double DEFAULT_bounds_DYNAMIC_FRICTION = 0;
	static final double DEFAULT_bounds_ELASTICITY = 0;
	
	public final double x,y,width,height;
	
	private final QuadTree<Collider> colliderTree;
	private final Collider bound_top,bound_bottom,bound_left,bound_right;
	
	public final Vector2d gravity = new Vector2d(0,-200);
	
	public final String id;
	
	private final LinkedNodeList<Mover> colliders = new LinkedNodeList<Mover>();

	public Scene(SceneConfig config){
		this(config.id,config.bounds.x,config.bounds.y,config.bounds.width,
				config.bounds.height,config.margin,config.elasticity,
				config.staticFriction,config.dynamicFriction,config.gravity.toVector());
	}
	
	public Scene(String id,double x, double y, double width, double height,double margin, 
			double boundsElasticity,double boundsStaticFriction, double boundsDynamicFriction,
			Vector2d gravity){
		if(id == null) throw new IllegalStateException("id is null");
		gravity.set(gravity);
		this.id = id;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		colliderTree = new QuadTree<Collider>(x-margin,y-margin,width+margin*2,height+margin*2);
		bound_left = new Collider(0,0,new Bounds(),new PhysMaterial(Double.POSITIVE_INFINITY,
				bound(boundsElasticity,0,1),bound(boundsStaticFriction,0,1),bound(boundsDynamicFriction,0,1)),null,false,false);
		bound_top = new Collider(0,0,new Bounds(),new PhysMaterial(Double.POSITIVE_INFINITY,
				bound(boundsElasticity,0,1),bound(boundsStaticFriction,0,1),bound(boundsDynamicFriction,0,1)),null,false,false);
		bound_bottom = new Collider(0,0,new Bounds(),new PhysMaterial(Double.POSITIVE_INFINITY,
				bound(boundsElasticity,0,1),bound(boundsStaticFriction,0,1),bound(boundsDynamicFriction,0,1)),null,false,false);
		bound_right = new Collider(0,0,new Bounds(),new PhysMaterial(Double.POSITIVE_INFINITY,
				bound(boundsElasticity,0,1),bound(boundsStaticFriction,0,1),bound(boundsDynamicFriction,0,1)),null,false,false);
	}
	
	public final void add(Collider c){
		colliders.add(c);
	}
	
	public void remove(Collider c){
		colliders.remove(c);
	}
	
	public void clear(){
		colliders.clear();
	}
	
	final void update(double delta){
		colliderTree.clear();
		Collider c = (Collider)colliders.getHead();
		while(c != null){
			c.initFrameData();
			colliderTree.add(c);
			c = (Collider)c.next;
		}
		doCollisions(delta);
		Mover m = colliders.getHead();
		while(m != null){
			m.move(delta);
			m = m.next;
		}
		doBoundsErrorCheck();
	}
	
	private final void doBoundsChecks(){
		Collider c = (Collider)colliders.getHead();
		while(c != null){
			Box b = c.getBounds();
			if(c.doBoundsCheck){
				if(b.getX()<x){
					addPair(c,bound_left);
				}else if(b.getX()+b.getWidth()>x+width){
					addPair(c,bound_right);
				}else if(b.getY()<y){
					addPair(c,bound_bottom);
				}else if(b.getY()+b.getHeight()>y+height){
					addPair(c,bound_top);
				}
			}
			c = (Collider)c.next;
		}
	}
	
	private static final ArrayList<Collider> treeList = new ArrayList<Collider>();
	private final void doRoughCheck(){
		doBoundsChecks();
		Collider collider = (Collider)colliders.getHead();
		while(collider!=null){
            if(!collider.isStill()) {
                treeList.clear();
                Box bounds = collider.getBounds();
                colliderTree.get(bounds, treeList);
                for (int i = 0; i < treeList.size(); i++) {
                    Collider b = treeList.get(i);
                    if (!b.isPartOf(collider))
                        addPair(collider, b);
                }
            }
			collider = (Collider)collider.next;
		}
	}
	
	private final void fineCheck(Pair p){
		PhysShape a;
		PhysShape b;
		for(int i = 0; i<p.a.shapes.size(); i++){
			a = p.a.shapes.get(i);
			for(int j = 0; j<p.b.shapes.size(); j++){
				b = p.b.shapes.get(j);
				Manifold m = null;
				if(b instanceof Bounds){
					if(a instanceof Circle){
						m=doFineCheckCircleBounds(p.a,(Circle)a);
					}else if(a instanceof Polygon){
						m=doFineCheckPolygonBounds(p.a,(Polygon)a);
					}else{
						throw new IllegalStateException("unknown shape");
					}
				}else if(p.a.getShapeBounds(i).collision(p.b.getShapeBounds(j))){
                    if(a instanceof Circle){
                        if(b instanceof Circle){
                            m = doFineCheckCircleCircle(p.a,(Circle)a,p.b,(Circle)b);
                        }else if(b instanceof Polygon){
                            m = doFineCheckCirclePolygon(p.a,(Circle)a,p.b,(Polygon)b);
                        }else{
                            throw new IllegalStateException("unknown shape");
                        }
                    }else if(a instanceof Polygon){
                        if(b instanceof Circle){
                            m = doFineCheckCirclePolygon(p.b,(Circle)b,p.a,(Polygon)a);
                        }else if(b instanceof Polygon){
                            m = doFineCheckPolygonPolygon(p.a,(Polygon)a,p.b,(Polygon)b);
                        }else{
                            throw new IllegalStateException("unknown shape");
                        }
                    }else{
                        throw new IllegalStateException("unknown shape");
                    }
                }
				if(m!=null){
					m.sa = a;
					m.sb = b;
					mcount++;
				}
			}
		}
	}
	
	private final static Vector2d misc = new Vector2d();
	
	private static final void doBoundsPoly(Manifold manifold,Collider c, Polygon p){
		misc.set(manifold.normal);
		c.getCurrent().inverseTransform(misc);
		manifold.contacts[0].set(p.getSupport(misc.x,misc.y,support));
		c.getCurrent().transform(manifold.contacts[0]);
	}
	
	private static final Square sbound = new Square();
	private final Manifold doFineCheckCircleBounds(Collider c,Circle circ){
		Manifold m = null;
		circ.calcBounds(c.getCurrent(),sbound);
		boolean left = sbound.x<x;
		boolean right = sbound.x+sbound.width>x+width;
		boolean bottom = sbound.y<y;
		boolean top = sbound.y+sbound.height>y+height;
		
		double penetration = 0;
		if(left){
			if(m==null)m=getManifold();
			penetration += sqr(x-sbound.x);
			m.normal.add(-1,0);
			m.contacts[m.contactCount++].set(sbound.x,sbound.getCY());
			m.b = bound_left;
		}
		if(right){
			if(m==null)m=getManifold();
			penetration += sqr((sbound.x+sbound.width)-(x+width));
			m.normal.add(1,0);
			m.contacts[m.contactCount++].set(sbound.x+sbound.width,sbound.getCY());
			m.b = bound_right;
		}
		if(bottom){
			if(m==null)m=getManifold();
			penetration += sqr(y-sbound.y);
			m.normal.add(0,-1);
			m.contacts[m.contactCount++].set(sbound.getCX(),sbound.y);
			m.b = bound_bottom;
		}
		if(top){
			if(m==null)m=getManifold();
			penetration += sqr((sbound.y+sbound.height)-(y+height));
			m.normal.add(0,1);
			m.contacts[m.contactCount++].set(sbound.getCX(),sbound.y+sbound.height);
			m.b = bound_top;
		}
		if(m!=null){
			m.penetration = Math.sqrt(penetration);
			m.normal.normalize();
			m.a = c;
		}
		return m;
	}
	
	private final Manifold doFineCheckPolygonBounds(Collider c,Polygon p){
		Manifold m = null;
		p.calcBounds(c.getCurrent(),sbound);
		boolean left = sbound.x<x;
		boolean right = sbound.x+sbound.width>x+width;
		boolean bottom = sbound.y<y;
		boolean top = sbound.y+sbound.height>y+height;
		
		if(left){
			m = getManifold();
			m.penetration = x-sbound.x;
			m.contactCount = 1;
			m.normal.set(-1,0);
			doBoundsPoly(m,c,p);
			m.a = c;
			m.b = bound_left;
		}else if(right){
			m = getManifold();
			m.penetration = (sbound.x+sbound.width)-(x+width);
			m.contactCount = 1;
			m.normal.set(1,0);
			doBoundsPoly(m,c,p);
			m.a = c;
			m.b = bound_right;
		}
		if(top){
			if(m!=null){
				mcount++;
			}
			m = getManifold();
			m.penetration = (sbound.y+sbound.height)-(y+height);
			m.contactCount = 1;
			m.normal.set(0,1);
			doBoundsPoly(m,c,p);
			m.a = c;
			m.b = bound_top;
		}else if(bottom){
			if(m!=null){
				mcount++;
			}
			m = getManifold();
			m.penetration = y-sbound.y;
			m.contactCount = 1;
			m.normal.set(0,-1);
			doBoundsPoly(m,c,p);
			m.a = c;
			m.b = bound_bottom;
		}
		return m;
	}
	
	private static final Manifold doFineCheckCircleCircle(Collider a,Circle ca, Collider b,Circle cb){
		Manifold m = null;
		Vector2d centera = v1;
		Vector2d centerb = v2;
		centera.set(ca.position);
		centerb.set(cb.position);
		a.getCurrent().transform(centera);
		b.getCurrent().transform(centerb);
		if(circleCircle(centera.x,centera.y,ca.radius,centerb.x,centerb.y,cb.radius)){
			Manifold manifold = getManifold();
			manifold.a = a;
			manifold.b = b;
			if(centera.equals(centerb)){//special case: circles on same point
				manifold.normal.set(0,1);
				manifold.penetration = Math.max(ca.radius,cb.radius);
				manifold.contactCount = 1;
				manifold.contacts[0].set(centera);
			}else{
				manifold.normal.set(centerb);
				manifold.normal.sub(centera);
				manifold.normal.normalize();
				manifold.penetration = (ca.radius+cb.radius)-centera.dist(centerb);
				manifold.contactCount=1;
				manifold.contacts[0].set(manifold.normal);
				manifold.contacts[0].mul(ca.radius);
				manifold.contacts[0].add(centera);
			}
			m=manifold;
		}
		return m;
	}
	
	private static final int startSize = 16;
	private static double[] pointsa = new double[startSize];
	private static double[] normalsa = new double[startSize];
	private static double[] pointsb = new double[startSize];
	private static double[] normalsb = new double[startSize];
	private static final Vector2d v1 = new Vector2d();
	private static final Vector2d v2 = new Vector2d();
	private static final Vector2d sidePlaneNormal = new Vector2d();
	private static final Vector2d refFaceNormal = new Vector2d();
	
	private static final void checkVertArrays(Polygon p){
		int s = p.getNumOfVerts()*2;
		if(pointsa.length< s){
			pointsa = new double[s];
			normalsa = new double[s];
			pointsb = new double[s];
			normalsb = new double[s];
		}
	}
	
	private static final Manifold doFineCheckCirclePolygon(Collider c,Circle circle, Collider p, Polygon polygon){
		Manifold m = getManifold();
		m.contactCount = 1;
		m.a = c;
		m.b = p;
		double radius = circle.radius;
		checkVertArrays(polygon);
		
		double[] points = pointsa;
		double[] normals = normalsa;
		int size = polygon.getTransformedValues(p.getCurrent(),points,normals);
		
		Vector2d center = v1;
		center.set(circle.position);
		c.getCurrent().transform(center);
//		center.sub(polygon.getTransformation().translation);
		
		double seperation = Double.NEGATIVE_INFINITY;
		int face = 0;
		for(int i = 0; i<size; i+=2){
			double s = Vector2d.dot(normals[i],normals[i+1],center.x-points[i],center.y-points[i+1]);
			
			if(s>radius){
				return null;
			}
			if(s>seperation){
				seperation = s;
				face = i;
			}
		}
		
		Vector2d v1 = v2;
        Vector2d v2 = sidePlaneNormal;
		v1.set(points[face],points[face+1]);
//		v1.sub(polygon.getTransformation().translation);
		int n = (face+2)%size;
		v2.set(points[n],points[n+1]);
//		v2.sub(polygon.getTransformation().translation);
		
		if(seperation<0){
			m.contactCount =1;
			
			m.normal.set(-normals[face],-normals[face+1]);
			
			m.contacts[0].set(m.normal);
			m.contacts[0].mul(circle.radius);
			m.contacts[0].add(center);
			
			m.penetration = radius;
			
			return m;
		}
		
		double dot1 = Vector2d.dot(center.x-v1.x,center.y-v1.y, v2.x-v1.x,v2.y-v1.y);
		double dot2 = Vector2d.dot(center.x-v2.x,center.y-v2.y, v1.x-v2.x,v1.y-v2.y);
		m.penetration = radius-seperation;
		
		if(dot1<=0){
			if(Vector2d.distSquared(center,v1)>(radius*radius)) {
				return null;
			}
			
			m.contactCount = 1;
			
			m.penetration = radius - center.dist(v1);
			
			m.normal.set(v1);
			m.normal.sub(center);
			m.normal.normalize();
			
			m.contacts[0].set(v1);
			
			if(nanCheck(m)) throw new Error("1");
		}else if(dot2<=0){
			if(Vector2d.distSquared(center,v2)>radius*radius) {
				return null;
			}
			
			m.contactCount = 1;
			
			m.penetration = radius - center.dist(v2);
			
			m.normal.set(v2);
			m.normal.sub(center);
			m.normal.normalize();
			
			m.contacts[0].set(v2);
			
		}else{
			if(Vector2d.dot(center.x-v1.x,center.y-v1.y,normals[face],normals[face+1])>radius){
				return null;
			}
			
			m.contactCount = 1;
			
			m.normal.set(-normals[face],-normals[face+1]);
			
			m.contacts[0].set(m.normal);
			m.contacts[0].mul(radius);
			m.contacts[0].add(center);
		}
		return m;
	}
	
	private static boolean nanCheck(Manifold m){
		return Double.isNaN(m.penetration) || Double.isNaN(m.normal.x) || Double.isNaN(m.normal.y) || Double.isNaN(m.contacts[0].x) || Double.isNaN(m.contacts[0].y) || Double.isNaN(m.contacts[1].x) || Double.isNaN(m.contacts[1].y);
	}
	
	private static final Vector2d[] out = new Vector2d[]{new Vector2d(),new Vector2d()};
	
	private static final Vector2d[] incidentFace = new Vector2d[]{new Vector2d(),new Vector2d()};
	
	private static final Vector2d support = new Vector2d();
	
	private static final int[] faceIndex = new int[1];
	
	private static final double findAxisOfLeastPenetration(int[] faceIndex, double[] pa, double[] na, int size,Matrix2d mb, Polygon b){
		double bestDistance = Double.NEGATIVE_INFINITY;
		int bestIndex = 0;
		for(int i = 0; i<size; i+=2){
			misc.set( -na[i], -na[i+1]);
			mb.inverseTransform(misc);
			Vector2d s = b.getSupport(misc.x,misc.y,support);
			mb.transform(s);
			s.sub(pa[i],pa[i+1]);
			
			double d = s.dot(na[i],na[i+1]);
			if(d>bestDistance){
				bestDistance = d;
				bestIndex = i;
			}
		}
		
		faceIndex[0]=bestIndex;
		return bestDistance;
	}
	
	private static final int clip(Vector2d n, double c, Vector2d[] face){
		int sp = 0;
		
		out[0].set(face[0]);
		out[1].set(face[1]);
		
		double d1 = n.dot(face[0]) - c;
		double d2 = n.dot(face[1]) - c;
		
		if(d1<=0)out[sp++].set(face[0]);
		if(d2<=0)out[sp++].set(face[1]);
		
		if(d1*d2 < 0){
			double alpha = d1 / (d1 - d2);
			out[sp].set(face[1]);
			out[sp].sub(face[0]);
			out[sp].mul(alpha);
			out[sp].add(face[0]);
			sp++;
		}
		
		face[0].set(out[0]);
		face[1].set(out[1]);
		
		return sp;
	}
	
	private static final Manifold doFineCheckPolygonPolygon(Collider a,Polygon pa, Collider b,Polygon pb){
		Manifold m = getManifold();
		m.a = a;
		m.b = b;
		checkVertArrays(pa);
		checkVertArrays(pb);
		
		Polygon ref = pa;
		Polygon inc = pb;
		double[] refp = pointsa;
		double[] incp = pointsb;
		double[] refn = normalsa;
		double[] incn = normalsb;
		int refSize;
		int incSize;
		
		Matrix2d ma = a.getCurrent();
		Matrix2d mb = b.getCurrent();
		int sizea = pa.getTransformedValues(ma,pointsa,normalsa);
		int sizeb = pb.getTransformedValues(mb,pointsb,normalsb);
		int faceA;
		double penetrationA = findAxisOfLeastPenetration(faceIndex,pointsa,normalsa,sizea,mb,pb);
		faceA = faceIndex[0];
		if(penetrationA >= 0) return null;
		
		int faceB;
		double penetrationB = findAxisOfLeastPenetration(faceIndex,pointsb,normalsb,sizeb,ma,pa);
		faceB = faceIndex[0];
		if(penetrationB >= 0) return null;
		
		int referenceFaceIndex;
		boolean flip;
		if(penetrationA>penetrationB){
			referenceFaceIndex = faceA;
			refSize = sizea;
			incSize = sizeb;
			flip = false;
		}else{
			referenceFaceIndex = faceB;
			flip = true;
			ref = pb;
			refp = pointsb;
			refn = normalsb;
			inc = pa;
			incp = pointsa;
			incn = normalsa;
			refSize = sizeb;
			incSize = sizea;
		}
		
		int incidentFaceIndex = 0;
		double minDot = Double.POSITIVE_INFINITY;
		for(int i = 0;i<incSize; i+=2){
			double dot = Vector2d.dot(refn[referenceFaceIndex],refn[referenceFaceIndex+1],incn[i],incn[i+1]);
			if(dot<minDot){
				incidentFaceIndex=i;
				minDot = dot;
			}
		}
		
		incidentFace[0].set(incp[incidentFaceIndex],incp[incidentFaceIndex+1]);
		incidentFaceIndex = (incidentFaceIndex+2)%incSize;
		incidentFace[1].set(incp[incidentFaceIndex],incp[incidentFaceIndex+1]);
		
		v1.set(refp[referenceFaceIndex],refp[referenceFaceIndex+1]);
		referenceFaceIndex = (referenceFaceIndex+2)%refSize;
		v2.set(refp[referenceFaceIndex],refp[referenceFaceIndex+1]);
		
		sidePlaneNormal.set(v2);
		sidePlaneNormal.sub(v1);
		sidePlaneNormal.normalize();
		
		refFaceNormal.set(sidePlaneNormal.y,-sidePlaneNormal.x);
		
		double refc = refFaceNormal.dot(v1);
		double negSide = -sidePlaneNormal.dot(v1);
		double posSide = sidePlaneNormal.dot(v2);
		
		sidePlaneNormal.neg();
		if(clip(sidePlaneNormal,negSide,incidentFace)<2){
			//Log.e("phys","hear c");
			return null;
		}
		sidePlaneNormal.neg();
		if(clip(sidePlaneNormal,posSide,incidentFace)<2){
			//Log.e("phys","hear d");
			return null;
		}

		
		m.normal.set(refFaceNormal);
		if(flip)m.normal.neg();
		
		int cp = 0;
		double seperation = refFaceNormal.dot(incidentFace[0])-refc;
		if(seperation<= 0){
			m.contacts[cp].set(incidentFace[0]);
			m.penetration = -seperation;
			cp++;
		}else {
			m.penetration = 0;
		}
		
		seperation = refFaceNormal.dot(incidentFace[1])-refc;
		
		if(seperation<= 0){
			m.contacts[cp].set(incidentFace[1]);
			m.penetration += -seperation;
			cp++;
			
			m.penetration/=(double)cp;//average
		}
		
		m.contactCount = cp;
		return m;
	}
	
	private final void doCollisions(double delta){
		pcount = 0;
		mcount = 0;
		doRoughCheck();
		for(int i = 0; i<pcount; i++){
			fineCheck(pairs.get(i));
		}
		for(int i = 0; i<mcount; i++){
			manifolds.get(i).initialize(delta);
		}
		for(int i = 0; i<mcount; i++){
			manifolds.get(i).applyImpulse();
		}
		for(int i = 0; i<mcount; i++){
			manifolds.get(i).positionalCorrection();
		}
		for(int i = 0; i<mcount; i++){
			manifolds.get(i).fireEvent();
		}
	}
	
	private final void doBoundsErrorCheck(){
		Collider c = (Collider)colliders.getHead();
		while(c != null){
			Box b = c.getBounds();
			if(c.doBoundsCheck){
				if(b.getX()+b.getWidth()<x){
					c.getParentTransformation().translation.x += x-b.getX();
				}
				if(b.getX()>x+width){
					c.getParentTransformation().translation.x += (x+width-b.getWidth())-b.getX();
				}
				if(b.getY()+b.getHeight()<y){
					c.getParentTransformation().translation.y += y-b.getY();
				}
				if(b.getY()>y+height){
					c.getParentTransformation().translation.y += (y+height-b.getHeight())-b.getY();
				}
			}
			c = (Collider)c.next;
		}
	}
	
	private static final ArrayList<Pair> pairs = new ArrayList<Pair>();
	private static int pcount = 0;
	
	private static void addPair(Collider a, Collider b){
		if(a==b)return;
		Pair p;
		if(pcount>=pairs.size()){
			p = new Pair(a,b);
			pairs.add(p);
		}else{
			p = pairs.get(pcount);
			p.set(a,b);
		}
		for(int i = 0; i<pcount; i++){
			if(pairs.get(i).equals(p)) return;
		}
		pcount++;
	}
	
	private static final ArrayList<Manifold> manifolds = new ArrayList<Manifold>();
	private static int mcount;
	
	private static final Manifold getManifold(){
		if(mcount>=manifolds.size()) manifolds.add(new Manifold());
		Manifold m = manifolds.get(mcount);
		m.contactCount = 0;
		m.penetration = 0;
		m.normal.set(0,0);
		return m;
	}
	
	private static final class Pair{
		Collider a;
		Collider b;
		
		Pair(Collider a, Collider b){
			this.a = a;
			this.b = b;
		}
		
		void set(Collider a, Collider b){
			this.a = a;
			this.b = b;
		}
		
		@Override
		public boolean equals(Object o){
			if(o instanceof Pair){
				Pair p = (Pair)o;
				return (p.a==a && p.b==b) || (p.a==b && p.b==a);
			}
			return false;
		}
		
		@Override
		public int hashCode(){
			return a.hashCode()+b.hashCode();
		}
		
	}

	@Override
	public double getX() {
		return x;
	}

	@Override
	public void setX(double x) {}

	@Override
	public double getY() {
		return y;
	}

	@Override
	public void setY(double y) {}

	@Override
	public double getWidth() {
		return width;
	}

	@Override
	public void setWidth(double width) {
	}

	@Override
	public double getHeight() {
		return height;
	}

	@Override
	public void setHeight(double height) {
	}

	@Override
	public double getCX() {
		return x+width/2;
	}

	@Override
	public void setCX(double cx) {
	}

	@Override
	public double getCY() {
		return y+height/2;
	}

	@Override
	public void setCY(double cy) {
	}
}
