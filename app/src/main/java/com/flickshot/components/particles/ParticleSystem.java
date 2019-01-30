package com.flickshot.components.particles;

import java.util.ArrayList;

public class ParticleSystem {
	public Particle[] particles;
	public int size = 0;

	public final ParticleType type;

    private final ArrayList<Particle> particlesToAdd = new ArrayList<Particle>();
    int adds;
	
	ParticleSystem(ParticleType type,int size){
		particles = new Particle[size];
		for(int i = 0; i<particles.length; i++) 
			particles[i] = new Particle();
		this.type = type;
        for(int i = 0; i<8; i++)
            particlesToAdd.add(new Particle());
	}
	
	final void update(double delta){
        //add particles
        synchronized (particlesToAdd){
            for(int ind = 0; ind <adds; ind++){
                if(size>=particles.length-1){
                    Particle[] temp = particles;
                    particles = new Particle[particles.length*2];
                    for(int i = 0; i<temp.length; i++){
                        particles[i] = temp[i];
                    }
                    for(int i = temp.length; i<particles.length; i++){
                        particles[i] = new Particle();
                    }
                }
                particles[size++].set(particlesToAdd.get(ind));
            }
            adds = 0;
        }


		double left=0,right=0,top=0,bottom=0;
		if(type.bounds!=null){
			left = type.bounds.getX();
			bottom = type.bounds.getY();
			right = type.bounds.getX()+type.bounds.getWidth();
			top = type.bounds.getY()+type.bounds.getHeight();
		}
		
		for(int i = 0; i<size; i++){
			Particle p = particles[i];
			
			//check life
			p.life-=delta;
			if(p.life<=0){
				particles[i] = particles[size-1];
				particles[size - 1] = p;
				size--;
				continue;
			}
			
			//set color
			float u = (float)(p.life/type.life);
			if(u<0.5f){
				u/=0.5f;
				p.r = (p.r3*(1.0f-u))+(p.r2*u);
				p.g = (p.g3*(1.0f-u))+(p.g2*u);
				p.b = (p.b3*(1.0f-u))+(p.b2*u);
				p.a = (type.a3*(1.0f-u))+(type.a2*u);
			}else{
				u=(u-0.5f)/0.5f;
				p.r = (p.r2*(1.0f-u))+(p.r1*u);
				p.g = (p.g2*(1.0f-u))+(p.g1*u);
				p.b = (p.b2*(1.0f-u))+(p.b1*u);
				p.a = (type.a2*(1.0f-u))+(type.a1*u);
			}
			
			p.scale += type.scaleInc*delta;
			if(type.scaleWiggle!=0)
				p.scale = p.scale+(Math.random()*type.scaleWiggle*delta*2)-type.scaleWiggle*delta;

            p.scale = Math.max(p.scale,0);
			
			p.speed += type.speedInc*delta;
			if(type.speedWiggle!=0)
				p.speed += (Math.random()*type.speedWiggle*delta*2)-type.speedWiggle*delta;
			
			p.dir += type.dirInc*delta;
			if(type.dirWiggle!=0)
				p.dir += (Math.random()*type.dirWiggle*delta*2)-type.dirWiggle*delta;
			
			p.orientation+=type.orientationInc*delta;
			if(type.orientationWiggle!=0)
				p.orientation += (Math.random()*delta*type.orientationWiggle*2)-type.orientationWiggle*delta;
			
			double vx =Math.cos(p.dir)*p.speed;
			double vy =Math.sin(p.dir)*p.speed;
			p.x+=vx*delta;
			p.y+=vy*delta;
			
			if(type.bounds!=null){
				if(p.x<left){
					p.x = left;
					vx = -vx;
				}
				if(p.x>right){
					p.x = right;
					vx = -vx;
				}
				if(p.y<bottom){
					p.y = bottom;
					vy = -vy;
				}
				if(p.y>top){
					p.y = top;
					vy = -vy;
				}
				p.dir = Math.atan2(vy,vx);
			}
			
		}

	}
	
	public final void create(int num,double x, double y, double speed, double dir, double orientation, double scale,
			float r1, float g1, float b1,float r2, float g2, float b2,float r3, float g3, float b3){
        synchronized (particlesToAdd) {
            for (int n = 0; n < num; n++) {
                if(adds >= particlesToAdd.size()-1){
                    int i = particlesToAdd.size();
                    for(;i>0;i--)
                        particlesToAdd.add(new Particle());
                }
                Particle p = particlesToAdd.get(adds++);
                p.x = x;
                p.y = y;
                p.speed = speed;
                p.dir = dir;
                p.orientation = orientation;
                p.scale = scale;
                p.life = type.life;

                p.r1 = r1;
                p.g1 = g1;
                p.b1 = b1;

                p.r2 = r2;
                p.g2 = g2;
                p.b2 = b2;

                p.r3 = r3;
                p.g3 = b3;
                p.b3 = g3;
            }
        }
	}
	
	public final void create(int num,double x, double y, double speed, double dir, 
			double orientation, double scale,float r, float g, float b){
		create(num,x,y,speed,dir,orientation,scale,r,g,b,r,g,b,r,g,b);
	}
	
	public final void create(int num,double x, double y, double speed, double dir, double orientation, double scale){
        synchronized(particlesToAdd) {
            for (int n = 0; n < num; n++) {
                if (adds >= particlesToAdd.size()-1) {
                    int i = particlesToAdd.size();
                    for (; i > 0; i--)
                        particlesToAdd.add(new Particle());
                }
                Particle p = particlesToAdd.get(adds++);
                p.x = x;
                p.y = y;
                p.speed = speed;
                p.dir = dir;
                p.orientation = orientation;
                p.scale = scale;
                p.life = type.life;

                float u = (float) Math.random();
                p.r1 = (type.r11 * u) + (type.r21 * (1.0f - u));
                p.g1 = (type.g11 * u) + (type.g21 * (1.0f - u));
                p.b1 = (type.b11 * u) + (type.b21 * (1.0f - u));

                u = (float) Math.random();
                p.r2 = (type.r12 * u) + (type.r22 * (1.0f - u));
                p.g2 = (type.g12 * u) + (type.g22 * (1.0f - u));
                p.b2 = (type.b12 * u) + (type.b22 * (1.0f - u));

                u = (float) Math.random();
                p.r3 = (type.r13 * u) + (type.r23 * (1.0f - u));
                p.g3 = (type.g13 * u) + (type.g23 * (1.0f - u));
                p.b3 = (type.b13 * u) + (type.b23 * (1.0f - u));
            }
        }
	}
	
	public final void create(int num,double x, double y){
		for(int i = 0; i<num; i++)
			create(1,x,y,
					type.speedMin+((type.speedMax-type.speedMin)*Math.random()),
					type.dirMin+((type.dirMax-type.dirMin)*Math.random()),
					type.orientationMin+((type.orientationMax-type.orientationMin)*Math.random()),
					type.scaleMin+((type.scaleMax-type.scaleMin)*Math.random()));
	}

    public void clear(){
        size = 0;
    }

	public class Particle{
		public double x,y;
		double speed;
		double dir;
        public double orientation;
        public double scale;
        public float r,g,b,a;
		float r1,g1,b1;
		float r2,g2,b2;
		float r3,g3,b3;
		double life;

        final void set(Particle p){
            x = p.x;
            y = p.y;
            speed = p.speed;
            dir = p.dir;
            orientation = p.orientation;
            scale = p.scale;
            r = p.r;
            g = p.g;
            b = p.b;
            a = p.a;
            r1 = p.r1;
            g1 = p.g1;
            b1 = p.b1;
            r2 = p.r2;
            g2 = p.g2;
            b2 = p.b2;
            r3 = p.r3;
            g3 = p.g3;
            b3 = p.b3;
            life = p.life;
        }
	}
}
