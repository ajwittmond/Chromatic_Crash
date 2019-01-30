package com.flickshot.components.entities.defs.overlays;

import java.util.ArrayList;


import com.flickshot.FlickShot;
import com.flickshot.GameData;
import com.flickshot.assets.sfx.Sound;
import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.EntityState;
import com.flickshot.components.entities.EntityStateFactory;
import com.flickshot.components.entities.defs.VisibleEntity;
import com.flickshot.components.entities.defs.PuckState;
import com.flickshot.components.entities.defs.enemies.Bomb;
import com.flickshot.components.entities.defs.fx.Explosion;
import com.flickshot.components.entities.defs.fx.Transition;
import com.flickshot.components.entities.defs.gui.Button;
import com.flickshot.components.entities.defs.managers.InfiniteModeManager;
import com.flickshot.components.entities.defs.managers.Medal;
import com.flickshot.components.entities.defs.managers.ScoreTracker;
import com.flickshot.components.entities.defs.managers.TimeAttackManager;
import com.flickshot.components.graphics.Artist;
import com.flickshot.components.graphics.Graphics;
import com.flickshot.components.graphics.Renderer2d;
import com.flickshot.components.graphics.Screen;
import com.flickshot.components.graphics.Sprite;
import com.flickshot.components.input.TouchEvent;
import com.flickshot.components.input.TouchListener;
import com.flickshot.components.input.TouchManager;
import com.flickshot.geometry.collision.CollisionLib;
import com.flickshot.scene.Scene;
import com.flickshot.scene.Updater.UpdateEvent;
import com.flickshot.util.Action;

public class EndOverlay extends VisibleEntity{
	public static final String ENTITY_NAME = "EndOverlay";
	double transitionTime = 0.5;
	double dt;
	
	ScoreTracker score;
	
	int scoreToAdd;
    int prevHighScore;
    int initialScore;
    int timeBonus;
    int maxComboBonus;
	
	double scoreCount;
	int medals;
	boolean killed;

    GameData.LevelData levelData;

    Sprite restart = new Sprite("restart");
    Sprite exit = new Sprite("cancel_button");
    Sprite nexts = new Sprite("next");
    Sprite leaderBoards = new Sprite("google_play_button");

    Sound trill1 = new Sound("trill_1");
    Sound trill2 = new Sound("trill_2");
    Sound trill3 = new Sound("trill_3");
    Sound trill4 = new Sound("trill_4");
    Sound flourish = new Sound("end_flourish");
    Sound blast = new Sound("blast");

    boolean first;
    boolean trilling;

    boolean blasted;

    float flareZ;
    float sparkZ;

    String[][] leaderBoardData;

    final FlickShot.HardwareButtonListener hb = new FlickShot.HardwareButtonListener() {
        @Override
        public void onBack() {
            super.onBack();
            kill();
            Scene.newScene("scene_select");
        }

        public void onHome(){

        }
    };

    final TouchListener tb = new TouchListener() {
        @Override
        public void onDown(TouchEvent evt) {
            if(CollisionLib.pointBox(TouchManager.x(),TouchManager.y(),
                    restart.getX(),restart.getY(),restart.getBoxWidth(),restart.getBoxHeight())){
                Transition t = (Transition)Entities.newInstance(Transition.class,0,0);
                t.target = Scene.getCurrent().id;
            }else if(CollisionLib.pointBox(TouchManager.x(), TouchManager.y(),
                    exit.getX(), exit.getY(), exit.getBoxWidth(), exit.getBoxHeight())){
                kill();
                Scene.newScene("scene_select");
            }else if(CollisionLib.pointBox(TouchManager.x(), TouchManager.y(),
                    nexts.getX(), nexts.getY(), nexts.getBoxWidth(), nexts.getBoxHeight())){
                GameData.LevelData ld = FlickShot.gameData.getLevelData(Scene.getCurrent().id);
                if(ld.medals>0){
                    String next = FlickShot.gameData.getNext(ld.index);
                    if(next!=null){
                        Transition t = (Transition)Entities.newInstance(Transition.class,0,0);
                        t.target = next;
                    }
                }
            }else if(CollisionLib.pointBox(TouchManager.x(), TouchManager.y(),
                    leaderBoards.getX(), leaderBoards.getY(),
                    leaderBoards.getBoxWidth(), leaderBoards.getBoxHeight())){
                FlickShot.showLeaderboard(Scene.getCurrent().id);
            }
        }

        @Override
        public void onMove(TouchEvent evt) {

        }

        @Override
        public void onUp(TouchEvent evt) {

        }
    };

	public EndOverlay(){
		setArtist(new Artist() {

            @Override
            public boolean isOnScreen(double screenX, double screenY,
                                      double screenWidth, double screenHeight) {
                return true;
            }

            @Override
            public void draw(double delta, Renderer2d renderer) {
                Screen screen = Graphics.getCurrentScene().screen;
                double u = (1.0 - (dt / transitionTime));

                exit.draw(delta, renderer);
                restart.draw(delta, renderer);
                nexts.draw(delta, renderer);
                leaderBoards.draw(delta, renderer);

                renderer.setDrawMode(Renderer2d.FILL);

                renderer.push();
                    renderer.translate(screen.getCX(),screen.getY()+screen.getHeight()/8,-99.05);
                    renderer.color(0.5f,0,0.5f);
                    renderer.shape(Renderer2d.SQUARE,screen.getWidth()-screen.getHeight()/4,u*screen.getHeight()/4);
                renderer.pop();



                renderer.color(0, 0, 0, 0.6 * u);
                renderer.setDrawMode(Renderer2d.FILL);
                renderer.shape(Renderer2d.SQUARE, screen.getCX(), screen.getCY(), -99,
                        screen.getWidth(), screen.getHeight());

                renderer.translate(0, 0, -99.1);

                renderer.push();
                    double circleSize = (screen.getHeight() / 4) - 16;
                    renderer.align(Renderer2d.CENTER, Renderer2d.CENTER);
                    renderer.color(0, 0, 0, u);
                    renderer.translate(screen.getX() + screen.getWidth() / 4, screen.getY() + screen.getHeight() * (3.0 / 8.0));
                    renderer.shape(Renderer2d.ELLIPSE, circleSize*u, circleSize*u);
                    renderer.color(1, 1, 1, u);
                    renderer.push();
                        renderer.scale(3*u, 3*u);
                        renderer.text("" + score.getBronze());
                    renderer.pop();
                    renderer.color(0, 0, 0, u);
                    renderer.translate(screen.getWidth() / 4, 0);
                    renderer.shape(Renderer2d.ELLIPSE, circleSize*u, circleSize*u);
                    renderer.color(1, 1, 1, u);
                    renderer.push();
                        renderer.scale(3*u, 3*u);
                        renderer.text("" + score.getSilver());
                    renderer.pop();
                    renderer.color(0., 0, 0., u);
                    renderer.translate(screen.getWidth() / 4, 0);
                    renderer.shape(Renderer2d.ELLIPSE, circleSize*u, circleSize*u);
                    renderer.color(1, 1, 1, u);
                    renderer.push();
                        renderer.scale(3*u, 3*u);
                        renderer.text("" + score.getGold());
                    renderer.pop();
                renderer.pop();
//                renderer.push();
//                    renderer.translate(screen.getCX(), screen.getY() + screen.getHeight() * (3.0 / 8.0),0.5);
//                    renderer.color(0.5,0,0.5);
//                    renderer.shape(Renderer2d.SQUARE,screen.getWidth()/2,circleSize*u);
//                renderer.pop();


                //draw score counter

                if(!killed){
                    renderer.push();
                        renderer.color(0.5,0,0.5);
                        renderer.translate(0,0,0.01);
                        drawRoundedRect(renderer,screen.getX(),screen.getCY()+screen.getHeight()/4,
                                (screen.getWidth()-64)*u,(screen.getHeight()/2),u*screen.getHeight()/8);
                        renderer.push();
                            renderer.color(0,1,1);
                            drawRoundedRect(renderer,screen.getX(),screen.getCY()+screen.getHeight()/4,
                                    (screen.getWidth()-128)*u,(screen.getHeight()/2-64),u*(screen.getHeight()/8)-32);
                        renderer.pop();
                        drawRoundedRect(renderer,screen.getX()+screen.getWidth(),screen.getCY()+screen.getHeight()/8,
                                (screen.getWidth()+64)*u,(screen.getHeight()/4),u*screen.getHeight()/8);
                        renderer.push();
                            renderer.color(0,1,1);
                            drawRoundedRect(renderer,screen.getX()+screen.getWidth(),screen.getCY()+screen.getHeight()/8,
                                    (screen.getWidth())*u,(screen.getHeight()/4-64),u*(screen.getHeight()/8)-32);
                        renderer.pop();
                        drawRoundedRect(renderer,screen.getX()-16+screen.getWidth()*(3.0/4.0),screen.getCY()+screen.getHeight()/2,
                                ((screen.getWidth()/2)+32),(screen.getHeight()/2)*u,u*screen.getHeight()/8);
                        renderer.push();
                            renderer.color(0,1,1);
                            drawRoundedRect(renderer,screen.getX()-16+screen.getWidth()*(3.0/4.0),screen.getCY()+screen.getHeight()/2,
                                ((screen.getWidth()/2)-32),((screen.getHeight()/2)-64)*u,u*(screen.getHeight()/8)-32);
                        renderer.pop();
                    renderer.pop();
                }

                if (dt <= 0) {
                    if (killed) {
                        renderer.push();
                            renderer.translate(screen.getCX(), screen.getY() + (screen.getHeight() * (3.0 / 4.0)));
                            renderer.align(Renderer2d.CENTER, Renderer2d.CENTER);
                            double scale = (screen.getHeight() / 4) / renderer.textHeight();
                            renderer.scale(scale, scale);
                            renderer.color(1, 0, 0, 1);
                            renderer.text("You Died");
                        renderer.pop();
                    } else {
                        double lwidth = screen.getWidth() * (6.0 / 8.0);
                        double margin = (screen.getWidth() - lwidth) / 2;
                        double ly = screen.getY() + (screen.getHeight() * (3.0 / 4.0));

                        renderer.color(0.5f,0,0.5f);
                        //draw score
                        renderer.push();
                            renderer.translate(screen.getX()+screen.getWidth()/4, screen.getY() + (screen.getHeight() * (5.0 / 8.0)));
                            renderer.align(Renderer2d.CENTER, Renderer2d.CENTER);
                            double scale = (screen.getHeight() / 8) / renderer.textHeight();
                            renderer.scale(scale, scale);
                            renderer.text("" + ((int) scoreCount));
                        renderer.pop();

                        //draw HighScore
                        renderer.push();
                            renderer.translate(screen.getCX()+32, screen.getY() + (screen.getHeight() * (5.0 / 8.0)));
                            renderer.align(Renderer2d.LEFT, Renderer2d.CENTER);
                            renderer.color(1, (prevHighScore < scoreCount) ? 1 : 0, (prevHighScore < scoreCount) ? 0 : 1);
                            scale = (screen.getHeight() / 10) / renderer.textHeight();
                            renderer.scale(scale, scale);
                            if((prevHighScore < scoreCount))
                                renderer.color(1,1,0);
                            else
                                renderer.color(0.5,0,0.5);
                            renderer.text("HighScore:");
                        renderer.pop();
                        renderer.push();
                            renderer.translate(
                                    screen.getX() + screen.getWidth() + 32 - margin,
                                    screen.getY() + (screen.getHeight() * (5.0 / 8.0)));
                            renderer.align(Renderer2d.RIGHT, Renderer2d.CENTER);
                            scale = (screen.getHeight() / 8) / renderer.textHeight();
                            renderer.scale(scale, scale);
                            if((prevHighScore < scoreCount))
                                renderer.color(1,1,0);
                            else
                                renderer.color(0.5,0,0.5);
                            renderer.text((int)Math.max(scoreCount,prevHighScore)+"");
                        renderer.pop();

                        //draw median line
                        renderer.push();

                            renderer.lineWidth(2);
                            renderer.line(
                                    screen.getX() + margin, screen.getY() + ly,
                                    screen.getX() + (screen.getWidth()/2) - margin, screen.getY() + ly
                            );
                        renderer.pop();

                        //draw score names
                        double dy = ((screen.getHeight() / 4)-(screen.getHeight()/8)) / 3;
                        margin /= 2;
                        scale = (screen.getHeight() / (8 * 2)) / renderer.textHeight();

                        renderer.push();
                            renderer.align(Renderer2d.LEFT, Renderer2d.CENTER);
                            renderer.translate(screen.getX() + margin/2, screen.getY() + screen.getHeight() - (screen.getHeight()/8));
                            renderer.push();
                                renderer.scale(scale, scale);
                                renderer.text("score: ");
                            renderer.pop();
                            renderer.translate(0, -dy);
                            renderer.push();
                                renderer.scale(scale, scale);
                                if(Entities.getStateCount(InfiniteModeManager.class)<=0) {
                                    renderer.text("time bonus (" + Math.max(0, Math.min(timeBonus, (int) scoreCount - initialScore))/10.0 + "s left): ");
                                }else{
                                    renderer.text("time bonus (" + Math.max(0, Math.min(timeBonus, (int) scoreCount - initialScore))/10.0 + "s survived): ");
                                }
                            renderer.pop();
                            renderer.translate(0, -dy);
                            renderer.push();
                                renderer.scale(scale, scale);
                                renderer.text("max combo bonus (x" + Math.max(0, (Math.min(maxComboBonus, (int) scoreCount - (initialScore + timeBonus)))) / 10 + "): ");
                            renderer.pop();
                        renderer.pop();

                        renderer.push();
                            renderer.align(Renderer2d.RIGHT, Renderer2d.CENTER);
                            renderer.translate(screen.getX() + (screen.getWidth()/2) - margin, screen.getY() + screen.getHeight()  - (screen.getHeight()/8));
                            renderer.push();
                                renderer.scale(scale, scale);
                                renderer.text(Math.min((int) scoreCount, initialScore) + "");
                            renderer.pop();
                            renderer.translate(0, -dy);
                            renderer.push();
                                renderer.scale(scale, scale);
                                renderer.text(Math.max(0, Math.min(timeBonus, (int) scoreCount - initialScore)) + "");
                            renderer.pop();
                            renderer.translate(0, -dy);
                            renderer.push();
                                renderer.scale(scale, scale);
                                renderer.text(Math.max(0, Math.min(maxComboBonus, (int) scoreCount - (initialScore + timeBonus))) + "");
                            renderer.pop();
                        renderer.pop();
                        //draw counts

                        if(!(FlickShot.googleApiClient!=null && FlickShot.googleApiClient.isConnected())){
                            renderer.push();
                                renderer.color(0.5f,0,0.5f);
                                renderer.translate(
                                        screen.getX()+(screen.getWidth()*(3.0/4.0)),
                                        screen.getY()+screen.getHeight()-(screen.getHeight()/8));
                                scale = 64/renderer.textHeight();
                                renderer.scale(scale,scale);
                                renderer.align(Renderer2d.CENTER,Renderer2d.CENTER);
                                renderer.text("not connected");
                            renderer.pop();
                        }else if(leaderBoardData==null){
                            renderer.push();
                                renderer.color(0.5f,0,0.5f);
                                renderer.translate(
                                        screen.getX()+(screen.getWidth()*(3.0/4.0)),
                                        screen.getY()+screen.getHeight()-(screen.getHeight()/8));
                                scale = 64/renderer.textHeight();
                                renderer.scale(scale,scale);
                                renderer.align(Renderer2d.CENTER,Renderer2d.CENTER);
                                renderer.text("loading...");
                            renderer.pop();
                        }else{
                            double w = ((screen.getHeight()/8)/4)+4;
                            renderer.push();
                                renderer.color(0.5f,0,0.5f);
                                renderer.translate(
                                        screen.getX()+(screen.getWidth()*(3.0/4.0)),
                                        screen.getY()+screen.getHeight()-((w/2)+8));
                                scale = 48/renderer.textHeight();
                                for(int i=0; i<leaderBoardData.length; i++){
                                    String name = leaderBoardData[i][0];
                                    String score = leaderBoardData[i][1];
                                    renderer.push();
                                        renderer.align(Renderer2d.LEFT,Renderer2d.CENTER);
                                        renderer.translate(128-screen.getWidth()/4,0);
                                        renderer.scale(scale,scale);
                                        if(
                                                renderer.textWidth(name)*scale >
                                                (-128+screen.getWidth()/4)*2-(renderer.textWidth(score)*scale)-64){
                                            while(
                                                    renderer.textWidth(name+"...")*scale >
                                                    (-128+screen.getWidth()/4)*2-(renderer.textWidth(score)*scale)-64){
                                                name = name.substring(0,name.length()-1);
                                            }
                                            name += "...";
                                        }
                                        renderer.text(name);
                                    renderer.pop();
                                    renderer.push();
                                        renderer.align(Renderer2d.RIGHT,Renderer2d.CENTER);
                                        renderer.translate(-128+screen.getWidth()/4,0);
                                        renderer.scale(scale,scale);
                                        renderer.text(score);
                                    renderer.pop();
                                    renderer.translate(0,-w);
                                }
                            renderer.pop();
                        }


                    }

                }
            }

            private void drawRoundedRect(Renderer2d r,double cx, double cy, double w, double h,double radius){
                r.shape(Renderer2d.SQUARE,cx,cy,0,w-(radius*2),h);
                r.shape(Renderer2d.SQUARE,cx,cy,0,w,h-(radius*2));

                r.shape(Renderer2d.ELLIPSE,cx-(w/2)+radius,cy-(h/2)+radius,0,radius*2,radius*2);
                r.shape(Renderer2d.ELLIPSE,cx+(w/2)-radius,cy-(h/2)+radius,0,radius*2,radius*2);
                r.shape(Renderer2d.ELLIPSE,cx+(w/2)-radius,cy+(h/2)-radius,0,radius*2,radius*2);
                r.shape(Renderer2d.ELLIPSE,cx-(w/2)+radius,cy+(h/2)-radius,0,radius*2,radius*2);
            }

        });
	}

	public void init(double x, double y){
		super.init(x,y);
        leaderBoardData = null;
        blast.setVolume(0.5f,0.5f);
        first = true;
        trilling=false;
		Scene.getCurrent().updater.paused.set(true);
		Screen screen = Graphics.getCurrentScene().screen;
		double bheight = screen.getHeight()/4;

        restart.setCX(screen.getX()+bheight/2);
        restart.setCY(screen.getY() + bheight / 2);
        restart.z = -99.10f;

        nexts.setCX(screen.getX()+screen.getWidth()-bheight/2);
        nexts.setCY(screen.getY() + bheight / 2);
        nexts.z = -99.10f;
        nexts.tintWeight = 0.2f;
        nexts.setTint(0,0,0);

        exit.setCX(screen.getX()+screen.getWidth()*(1.0/3.0));
        exit.setCY(screen.getY() + bheight / 2);
        exit.z = -99.10f;

        leaderBoards.setCX(screen.getX()+screen.getWidth()*(2.0/3.0));
        leaderBoards.setCY(screen.getY() + bheight / 2);
        leaderBoards.z = -99.10f;

        scoreCount = 0;

		score = (ScoreTracker)Entities.getEntity(ScoreTracker.class).getState(0);
		
		scoreToAdd = score.getScore();
        initialScore = scoreToAdd;

        maxComboBonus = score.getMaxTimerCombo()*10;
        scoreToAdd += maxComboBonus;
		
		TimeAttackManager tm = (TimeAttackManager)Entities.getEntity(TimeAttackManager.class).getState(0);
        InfiniteModeManager im = (InfiniteModeManager)Entities.getEntity(InfiniteModeManager.class).getState(0);
        timeBonus = 0;
		if(tm!=null){
            timeBonus = (int)(tm.time.getTimeLeft()*10);
			scoreToAdd += timeBonus;
		}else if(im!=null){
            timeBonus = (int)(im.time*10);
            scoreToAdd += timeBonus;

        }
		killed = Entities.getStateCount(PuckState.class)+Entities.getStateCount(InfiniteModeManager.class) < 1;
		
		dt = transitionTime;


        levelData  = FlickShot.gameData.getLevelData(Scene.getCurrent().id);

        prevHighScore = levelData.topScore;

        if(levelData.medals>0){
            nexts.tintWeight = 0;
        }

        if(FlickShot.gameData.getNext(levelData.index)==null){
            nexts.setTint(0.5f,0,0.5f);
            nexts.tintWeight = 1;
        }

        if(!killed){
            int medals = 0;
            if(scoreToAdd>score.getBronze()){
                medals=1;
                nexts.tintWeight = 0;
                if(scoreToAdd>score.getSilver()){
                    medals=2;
                    if(scoreToAdd>score.getGold()){
                        medals=3;
                    }
                }
            }

            levelData.medals = Math.max(levelData.medals,medals);
            levelData.highestCombo = Math.max(levelData.highestCombo,score.getMaxTimerCombo());
            levelData.topScore = Math.max(levelData.topScore,scoreToAdd);

            if(tm!=null && (levelData.minTime == 0 ||  levelData.minTime>tm.t)){
                levelData.minTime = tm.t;
            }else if(im!=null && (levelData.minTime == 0 ||  levelData.minTime<im.time)){
                levelData.minTime = im.time;
            }
            FlickShot.gameData.write();
            FlickShot.gameData.checkAchievements();
            FlickShot.addListener(hb);
        }



        trill1.setLooping(true);
        trill1.setVolume(1,1);
        trill2.setLooping(true);
        trill2.setVolume(0,0);
        trill3.setLooping(true);
        trill3.setVolume(0,0);
        trill4.setLooping(true);
        trill4.setVolume(0,0);

        medals = 0;
        TouchManager.add(tb);

        Bomb.sparkType.bounds = Graphics.getCurrentScene().screen;
        sparkZ  =Bomb.sparkType.z;
        Bomb.sparkType.z = -99.9f;

        Explosion.flareType.bounds = Graphics.getCurrentScene().screen;
        flareZ  =Explosion.flareType.z;
        Explosion.flareType.z = -99.9f;

        blasted = false;

        if(FlickShot.googleApiClient!=null && FlickShot.googleApiClient.isConnected()){
            FlickShot.putScoreOnLeaderboard(Scene.getCurrent().id,levelData.topScore);
            FlickShot.getLeaderboardScores(Scene.getCurrent().id,new FlickShot.LoadScoresCallback() {
                @Override
                public void onLoad(String[][] scores) {
                    leaderBoardData = scores;
                }
            },5);
        }
    }
	
	public void update(UpdateEvent evt){
		super.update(evt);
        if(!first) {
            dt = Math.max(0, dt - evt.getRealDelta());
            Screen screen = Graphics.getCurrentScene().screen;
            double u = 1.0-(dt/transitionTime);

            double bheight = screen.getHeight()/4;
            restart.setBoxWidth(bheight*u);
            restart.setBoxHeight(bheight*u);

            exit.setBoxWidth(bheight * u);
            exit.setBoxHeight(bheight * u);

            nexts.setBoxWidth(bheight * u);
            nexts.setBoxHeight(bheight * u);

            leaderBoards.setBoxWidth(bheight * u);
            leaderBoards.setBoxHeight(bheight * u);

            if (dt <= 0 && !killed && scoreCount != scoreToAdd) {
                if(!trilling ){
                    trill1.play();
                    trill2.play();
                    trill3.play();
                    trill4.play();
                    trilling = true;
                }
                double rate = score.getGold() / 5.0;
                scoreCount = Math.min(scoreToAdd, scoreCount + evt.getRealDelta() * rate);
                double circleSize = (screen.getHeight() / 4) - 16;
                if (scoreCount >= score.getBronze() && medals < 1) {
                    Medal.create("bronze",
                            screen.getX() + screen.getWidth() / 4,
                            screen.getY() + screen.getHeight() * (3.0 / 8.0),
                            circleSize, circleSize).sprite.z = -99.2f;
                    medals = 1;
                    trill2.setVolume(1,1);
                }
                if (scoreCount >= score.getSilver() && medals < 2) {
                    Medal.create("silver",
                            screen.getX() + screen.getWidth() / 2,
                            screen.getY() + screen.getHeight() * (3.0 / 8.0),
                            circleSize, circleSize).sprite.z = -99.2f;
                    medals = 2;
                    trill3.setVolume(1,1);
                }
                if (scoreCount >= score.getGold() && medals < 3) {
                    Medal.create("gold",
                            screen.getX() + screen.getWidth() * (3.0 / 4.0),
                            screen.getY() + screen.getHeight() * (3.0 / 8.0),
                            circleSize, circleSize).sprite.z = -99.2f;
                    medals = 3;
                    trill4.setVolume(1,1);
                }
            }
            if(!killed && scoreCount==scoreToAdd && trilling){
                trilling=false;
                trill1.stop();
                trill2.stop();
                trill3.stop();
                trill4.stop();
                if(medals==3)
                    flourish.play();
            }
            if(scoreCount>prevHighScore && !blasted){
                blasted = true;
                blast.play();
                double y = screen.getY() + (screen.getHeight() * (5.0 / 8.0));
                double x = screen.getCX()+screen.getWidth()/4;
                double w = (screen.getHeight() / 8);
                double ws = 2;
                for(int i = 0; i<64; i++){
                    double r = w * Math.random();
                    double theta = Math.random()*Math.PI*2;
                    Explosion.flare.create(1,x+Math.cos(theta)*r*ws,y+Math.sin(theta)*r,100+(200)*Math.random(),theta,0,64+(64.0*Math.random()),
                            (float)Math.random(),(float)Math.random(),(float)Math.random(),
                            (float)Math.random(),(float)Math.random(),(float)Math.random(),(float)Math.random(),
                            (float)Math.random(),(float)Math.random());
                }
                for(int i = 0; i<64; i++){
                    double r = w * Math.random();
                    double theta = Math.random()*Math.PI*2;
                    Bomb.spark.create(1, x + Math.cos(theta) * r * ws, y + Math.sin(theta) * r, 300, theta, 0, 64 + (64.0 * Math.random()),
                            (float) Math.random(), (float) Math.random(), (float) Math.random(),
                            (float) Math.random(), (float) Math.random(), (float) Math.random(), (float) Math.random(),
                            (float) Math.random(), (float) Math.random());
                }
            }
        }else {
            first = false;
        }
	}

    @Override
    public void destroy(){
        super.destroy();
        trill1.stop();
        trill2.stop();
        trill3.stop();
        trill4.stop();
        flourish.stop();
        Bomb.sparkType.z = sparkZ;
        Explosion.flareType.z = flareZ;
        TouchManager.remove(tb);
        FlickShot.removeListener(hb);
    }

    @Override
    public void unload(){
        super.unload();
        trill1.stop();
        trill2.stop();
        trill3.stop();
        trill4.stop();
        flourish.stop();
        Bomb.sparkType.z = sparkZ;
        Explosion.flareType.z = flareZ;
        TouchManager.remove(tb);
        FlickShot.removeListener(hb);
    }

	public static final EntityStateFactory getFactory(){
		return new EntityStateFactory(){
			@Override
			public EntityState construct() {
				return new EndOverlay();
			}

			@Override
			public Class<? extends EntityState> getType() {
				return EndOverlay.class;
			}
			
			@Override
			public void getAssets(ArrayList<String[]> assets){
				assets.add(new String[]{"music","scene_select"});
			}
		};
	}
}
