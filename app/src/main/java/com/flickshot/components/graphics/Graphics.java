package com.flickshot.components.graphics;

import java.util.HashMap;

import com.flickshot.components.Component;
import com.flickshot.config.Config;
import com.flickshot.scene.Updater.UpdateEvent;

import static android.opengl.GLES20.*;

public class Graphics extends Component{
	public static final String tag = "graphics";
	
	static double aspect_ratio = 1;// screen width divided by screen height
	static double screen_height = 960;
	
	private static Graphics current;
	
	private static final HashMap<String,Scene> scenes = new HashMap<String,Scene>();
	
	private static Scene currentScene;
	private static String currentSceneId = "default";
	
	private static int width, height;
	
	public static final void create(){
		currentScene = scenes.put("default",new Scene(getRenderer(),null,new Screen()));
	}
	
	public static final Graphics getCurrent(){
		return current;
	}
	
	public static final String getTag(){
		return tag;
	}
	
	private static final void addScene(String id, Scene s){
		Scene curr = scenes.get(id);
		if(curr!=null){
			s.transferArtists(curr,id);
			curr.destroyed = true;
		}
		scenes.put(id,s);
		s.setIConversion(width,height);
	}
	
	public static final void configure(Config c){
		GraphicsConfig config = (GraphicsConfig)c;
		for(SceneConfig conf: config.scenes){
			addScene(conf.id,new Scene(getRenderer(),conf.background.toBackground(),conf.screen.toScreen()));
		}
		currentScene = scenes.get(config.currentScene);
		currentSceneId = config.currentScene;
		screen_height = config.screenHeight;
	}
	
	public static final Config getConfig(){
		return new GraphicsConfig();
	}
	
	public static final void setCurrentScene(String id){
		if((currentScene=scenes.get(id))==null)throw new IllegalStateException();
	}
	
	public static final void setCurrentScene(Scene current){
		if((currentScene=current)==null)throw new IllegalStateException();
	}
	
	public static final Scene getCurrentScene(){
		return currentScene;
	}
	
	public static final String getCurrentSceneId(){
		return currentSceneId;
	}
	
	public static final Scene getScene(String id){
		return scenes.get(id);
	}
	
	public static final boolean hasScene(String id){
		return scenes.containsKey(id);
	}
	
	private static final Renderer2d getRenderer(){
		return new DirectRenderer2d();
	}
	
	public static final void doDraw(double delta) {
		currentScene.start();
		currentScene.draw(delta);
	}
	
	public static final void finish(double delta){
		currentScene.finish(delta);
	}

	public static final void onSurfaceChanged(int width, int height) {
		glViewport(0, 0, width, height);
		Graphics.width = width;
		Graphics.height = height;
		aspect_ratio = (double)width/(double)height;
		for(String s:scenes.keySet()) if(scenes.get(s)!=null) scenes.get(s).setIConversion(width,height);
	}

	public static final void onSurfaceCreated() {
		glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
	}

	@Override
	public void preUpdate(UpdateEvent evt) {
	}

	@Override
	public void update(UpdateEvent evt) {
	}

	@Override
	public void postUpdate(UpdateEvent evt) {
	}

	@Override
	public void reset() {
	}
}
