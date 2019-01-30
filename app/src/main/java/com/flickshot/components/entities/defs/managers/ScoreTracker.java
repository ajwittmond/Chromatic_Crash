package com.flickshot.components.entities.defs.managers;

import com.flickshot.FlickShot;
import com.flickshot.GameData;
import com.flickshot.assets.AssetLibrary;
import com.flickshot.assets.sfx.Sound;
import com.flickshot.components.entities.CommonEntity;
import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.entities.defs.PuckState;
import com.flickshot.components.entities.defs.VisibleEntity;
import com.flickshot.components.entities.defs.enemies.Enemy;
import com.flickshot.components.entities.defs.enemies.Enemy.ColorType;
import com.flickshot.components.entities.defs.fx.TextBlob;
import com.flickshot.components.entities.defs.gui.VisibleTimer;
import com.flickshot.components.entities.defs.particles.Blob;
import com.flickshot.components.graphics.Graphics;
import com.flickshot.components.graphics.Renderer2d;
import com.flickshot.components.graphics.Screen;
import com.flickshot.components.graphics.Artist;
import com.flickshot.components.graphics.Sprite;
import com.flickshot.components.physics.Physics;
import com.flickshot.config.Config;
import com.flickshot.scene.Scene;
import com.flickshot.scene.Updater.UpdateEvent;
import com.flickshot.util.MiscLib;

public class ScoreTracker extends VisibleEntity{
	public static final String ENTITY_NAME = "scoreTracker";
	private static final double MARGIN = 16;
	private static final String[] TEXTURES = new String[]{
		"bronze","silver","gold","flower_red","flower_green","flower_pink"
	};

    private boolean infiniteMode;

	private int multiHitBonus = 10;
	private int score = 0;
	private int multiHitCombo = 0;
	private int colorCombo = 0;
	private int colorComboBonus = 10;
	private int timerCombo = 0;
	private double timerLength = 5;
	private double timerComboMultiplier = 0.01;
	
	private int maxTimerCombo;
	
	private int bronze=10;
	private int silver=2000;
	private int gold =3000;
	private int medals = 0;
	
	
	private ColorType lastColor;
	
	public boolean multiHitComboEnabled;
	public boolean colorComboEnabled;
	
	private double blobY,blobX;
	private double blobS;
	
	private VisibleTimer comboTimer;
	
	private Sprite sprite1 = new Sprite("flower_red");
	private Sprite sprite2 = new Sprite("flower_red");
	private Sprite sprite3 = new Sprite("flower_red");
	
	private Sound scoreSound;
	private Sound comboSound;
	private Sound comboBreakSound;
	
	private boolean first;
	
	public ScoreTracker(){
		setArtist(new Artist(){
			public boolean isOnScreen(double screenX, double screenY,
					double screenWidth, double screenHeight) {
				return true;
			}
			
			double dst = 3;
			double ds = 0;
			double drt = 1;
			double dr = 0;
			@Override
			public void draw(double delta, Renderer2d renderer) {
				for(String texture:TEXTURES){
					if(!AssetLibrary.has("texture",texture)){
						AssetLibrary.loadAsset("texture",texture);
					}
				}
				
				Screen screen = Graphics.getCurrentScene().screen;
				com.flickshot.components.physics.Scene ps = Physics.getScene(0);
				
				double xstart=screen.getX()+MARGIN;
				double ystart = screen.getY()+MARGIN;
				double width = ((screen.getWidth()-ps.getWidth())/2)-(MARGIN*2);
				double height = screen.getHeight()-(MARGIN*2);

                //InfiniteModeManager ifm = (InfiniteModeManager)Entities.getEntity(InfiniteModeManager.class).getState(0);

				//draw score
				renderer.push();
					String s = String.format("%08d",0);
					
					
					double twidth = renderer.textWidth(s);
					double theight = renderer.textHeight();
					double scale = width/twidth;
					
					
					renderer.align(Renderer2d.CENTER,Renderer2d.BOTTOM);

					renderer.color(1,1,1,1);
					renderer.translate(xstart+width/2,(ystart+height)-(128));
					renderer.push();
						renderer.scale(scale,scale);
						renderer.text(score+"");
					renderer.pop();
					
					renderer.translate(0,-((theight*scale)));
					renderer.lineWidth(4);
					double w = width*(3.0/4.0);
					renderer.line(-w/2,0,w/2,0);

					renderer.translate(0,-6);
					
					String nextScore = "" + getNextStarScore();
					renderer.push();
						renderer.scale(scale,scale);
						renderer.color(1,1,1,1);
						renderer.text(nextScore);
					renderer.pop();
					
					blobY = (((ystart+height)-(128))-((theight*scale)))-(6+(theight/2));
					blobX = xstart+width/2;
					blobS = scale*renderer.textHeight();
				renderer.pop();
				//draw medal box or wave counter
				renderer.push();
					double gap = 8;
					
					double size = (width-(gap*2))/3;
					double y = blobY - (blobS + 8 + (size/2));

					renderer.translate(0,y);
                    renderer.color(0,0,0,1);
                    renderer.push();
                    renderer.setDrawMode(Renderer2d.STROKE);
                    renderer.lineWidth(2);
                    renderer.shape(Renderer2d.SQUARE, xstart + (width / 2), 0, 0, width + 4, size + 4);
                    renderer.pop();
                    renderer.setDrawMode(Renderer2d.FILL);
                    renderer.shape(Renderer2d.ELLIPSE, xstart + (size / 2), 0, 0, size, size);
                    renderer.shape(Renderer2d.ELLIPSE, xstart + size + gap + (size / 2), 0, 0, size, size);
                    renderer.shape(Renderer2d.ELLIPSE, xstart + (size * 2) + (gap * 2) + (size / 2), 0, 0, size, size);
				renderer.pop();
				
				dr+=delta;
				if(dr>=drt)dr=0;
				ds+=delta;
				if(ds>=dst)ds=0;
				//drawScoreCounter
				renderer.push();
					renderer.translate(xstart+width/2,ystart+width/2);
					renderer.push();
						int num = Math.min(5,timerCombo/4);
						double theta = (Math.PI*2*(dr/drt));
						double scaleStep = 1.3;
						renderer.setShape(Renderer2d.SQUARE);
						renderer.setDrawMode(Renderer2d.FILL);
						for(int i = 0; i<num; i++){
							double scaleTheta = Math.PI*2*(ds/dst);
							double scaled = (0.2 * Math.sin(scaleTheta+((Math.PI*2)*((double)i/(double)num))))+1;
							scale = 90*(Math.pow(scaleStep,i))*scaled;
							renderer.translate(0,0,1);
							renderer.push();
								renderer.scale(scale,scale);
								renderer.rotate((i%2==0)?theta:(Math.PI*2)-theta);
								if(i%3==0){
									renderer.color(1,0,0);
								}else if(i%3==1){
									renderer.color(0,1,0);
								}else if(i%3==2){
									renderer.color(0,0,1);
								}
								renderer.shape();
							renderer.pop();
						}
					renderer.pop();
					renderer.push();
						renderer.setDrawMode(Renderer2d.FILL);
						renderer.color(0,0,0);
						renderer.shape(Renderer2d.ELLIPSE,90,90);
					renderer.pop();
					scale = 64.0/renderer.textHeight();
					renderer.color(1,1,1);
					renderer.align(Renderer2d.CENTER,Renderer2d.CENTER);
					renderer.scale(scale,scale);
					renderer.text(timerCombo+"");
				renderer.pop();
				if(colorComboEnabled){
					sprite1.draw(delta,renderer);
					sprite2.draw(delta,renderer);
					sprite3.draw(delta,renderer);
				}
			}
			
		});
		scoreSound = new Sound("chime");
		comboSound = new Sound("chime_chord");
		comboBreakSound= new Sound("mirror_break");
	}
	
	@Override
	public void init(double x, double y){
		super.init(x,y);
		score = 0;
		multiHitCombo = 0;
		colorCombo = 0;
		timerCombo = 0;
		maxTimerCombo = 0;
		lastColor = null;
		comboTimer = null;
		first = true;
		if(!AssetLibrary.has("sound","chime"))
			AssetLibrary.loadAsset("sound","chime");
		if(!AssetLibrary.has("sound","chime_chord"))
			AssetLibrary.loadAsset("sound","chime_chord");
		if(!AssetLibrary.has("sound","mirror_break"))
			AssetLibrary.loadAsset("sound","mirror_break");
		comboSound.setVolume(0.5f,0.5f);
		scoreSound.setVolume(0.5f,0.5f);
	}
	
	public void update(UpdateEvent evt){
		super.update(evt);
		int next = getNextStarScore();
		
		Screen screen = Graphics.getCurrentScene().screen;
		com.flickshot.components.physics.Scene ps = Physics.getScene(0);
		
		double xstart=screen.getX()+MARGIN;
		double ystart = screen.getY()+MARGIN;
		double width = ((screen.getWidth()-ps.getWidth())/2)-(MARGIN*2);
		double height = screen.getHeight()-(MARGIN*2);
		

		double gap = 8;

		double size = (width-(gap*2))/3;
		
		if(first){
			comboTimer = (VisibleTimer)Entities.newInstance(VisibleTimer.class,xstart+(width/2),ystart+width+(width/4));
			comboTimer.tx.scale.set(width/2,width/2);
			comboTimer.showText = false;
			comboTimer.setTimer(1,0);
			
			double y = comboTimer.tx.translation.y+ (comboTimer.tx.scale.y/2)+16+(size/2);
			sprite1.setCY(y);
			sprite1.setCX(xstart+(size/2));
			sprite1.setBoxWidth(size);
			sprite1.setBoxHeight(size);
			sprite1.setTint(0,0,0);
			sprite1.tintWeight =1;
			
			sprite2.setCY(y);
			sprite2.setCX(xstart+size+gap+(size/2));
			sprite2.setBoxWidth(size);
			sprite2.setBoxHeight(size);
			sprite2.setTint(0,0,0);
			sprite2.tintWeight =1;
			
			sprite3.setCY(y);
			sprite3.setCX(xstart+(size*2)+(gap*2)+(size/2));
			sprite3.setBoxWidth(size);
			sprite3.setBoxHeight(size);
			sprite3.setTint(0,0,0);
			sprite3.tintWeight =1;
			
			first = false;
		}
        if (medals < 3 && score >= next) {
            TextBlob.create(next + "", blobX, blobY, blobS, 1024, 1, 1, 1, 1);

            double y = blobY - (blobS + 8 + (size / 2));
            if (medals == 0) {
                Medal.create("bronze", xstart + (size / 2), y, size, size);
            } else if (medals == 1) {
                Medal.create("silver", xstart + size + gap + (size / 2), y, size, size);
            } else if (medals == 2) {
                Medal.create("gold", xstart + (size * 2) + (gap * 2) + (size / 2), y, size, size);
            }
            medals++;
        }
		if(timerCombo>0 && comboTimer.getTimeLeft()<=0){
			TextBlob.create(timerCombo+"",xstart+width/2,ystart+width/2,blobS,1024,1,1,1,1);
			timerCombo = 0;
			comboBreakSound.play();
		}
		if(timerCombo>maxTimerCombo)
			maxTimerCombo = timerCombo;
	}
	
	public int getScore(){
		return score;
	}
	
	public void setScore(int score){
		this.score = score;
	}
	
	public int getNextStarScore(){
		if(medals==0){
			return bronze;
		}else if(medals==1){
			return silver;
		}else if(medals==2){
			return gold;
		}else{
            int highscore = FlickShot.gameData.getLevelData(Scene.getCurrent().id).topScore;
            return (score<highscore) ? highscore : score;
        }
	}
	
	public int addToScore(Enemy enemy){
		int points = (int)(enemy.getPoints() * (1.0+ (timerCombo*timerComboMultiplier)));
		TextBlob.create(points+"",enemy.collider.tx.translation.x,enemy.collider.tx.translation.y,
				64,512,1,1,1,1);
		addToScore(enemy.getPoints());
		scoreSound.play();
		if(multiHitComboEnabled)multiHitCombo++;
		ColorType color = enemy.getColor();
		if(color==lastColor && colorComboEnabled){
			colorCombo++;
			if(colorCombo>=3){
				addToScore(colorComboBonus);
				Screen screen = Graphics.getCurrentScene().screen;
				comboSound.play();
				TextBlob.create("3 OF A KIND!",screen.getCX(),screen.getCY(),512,1024,1,1,1,0.8f);
				colorCombo=0;
				Blob.create(sprite1.getTexture(),sprite1.getCX(),sprite1.getCY(),
						sprite1.getBoxWidth(),sprite1.getBoxHeight(),
						sprite1.getBoxWidth()*10,sprite1.getBoxHeight()*10);
				Blob.create(sprite2.getTexture(),sprite2.getCX(),sprite2.getCY(),
						sprite2.getBoxWidth(),sprite2.getBoxHeight(),
						sprite2.getBoxWidth()*10,sprite2.getBoxHeight()*10);
				Blob.create(sprite3.getTexture(),sprite3.getCX(),sprite3.getCY(),
						sprite3.getBoxWidth(),sprite3.getBoxHeight(),
						sprite3.getBoxWidth()*10,sprite3.getBoxHeight()*10);
			}
		}else{
			colorCombo=1;
		}
		timerCombo++;
		comboTimer.setTimer(timerLength,timerLength);
		lastColor = color;
		
		//set color combo sprites
		String texture = "flower_"+lastColor.name;
		sprite1.setTexture(texture);
		sprite2.setTexture(texture);
		sprite3.setTexture(texture);
		sprite1.tintWeight = (colorCombo>=1)?0:1;
		sprite2.tintWeight = (colorCombo>=2)?0:1;
		sprite3.tintWeight = (colorCombo>=3)?0:1;
		return score;
	}
	
	/**
	 * called by puck to end combo 1
	 */
	public void grab(){
		if(multiHitCombo>1){
			comboSound.play();
			addToScore(multiHitCombo*multiHitBonus);
			Screen screen = Graphics.getCurrentScene().screen;
			TextBlob.create(multiHitCombo+" IN ONE!",screen.getCX(),screen.getCY(),512,1024,1,1,1,0.8f);
		}
		multiHitCombo = 0;
	}

    private void addToScore(int points){
        if(infiniteMode){
            PuckState p = (PuckState)Entities.getEntity(PuckState.class).getState(0);
            System.out.println(p.getHealth()+" "+(p.getHealth()+(points/5.0)));
            p.setHealth(Math.min(100,p.getHealth()+(points/5.0)));
        }
        score+=points;
    }
	
	public int getMedals(){
		return medals;
	}
	
	public int getBronze(){
		return bronze;
	}
	
	public int getSilver(){
		return silver;
	}
	
	public int getGold(){
		return gold;
	}
	
	public int getMaxTimerCombo(){
		return maxTimerCombo;
	}
	
	public void configure(Config c){
		ScoreConfig config = (ScoreConfig)c;
		multiHitComboEnabled = config.multiHitComboEnabled;
		colorComboEnabled = config.colorComboEnabled;
		colorComboBonus = config.colorComboBonus;
		multiHitBonus = config.multiComboBonus;
		bronze = config.bronze;
		silver = config.silver;
		gold = config.gold;
        infiniteMode = config.infiniteMode;
	}
	
	
	public static final EntityStateFactory getFactory(){
		return new Factory();
	}
	
	public static class Factory extends EntityStateFactory{
		@Override
		public EntityState construct() {
			return new ScoreTracker();
		}

		@Override
		public Class<? extends EntityState> getType() {
			return ScoreTracker.class;
		}
		
		public Config getConfig(){
			return new ScoreConfig();
		}
	}
}
