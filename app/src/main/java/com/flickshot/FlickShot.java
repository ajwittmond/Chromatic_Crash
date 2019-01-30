package com.flickshot;

import com.flickshot.assets.music.MusicManager;
import com.flickshot.assets.sfx.SFXManager;
import com.flickshot.components.entities.InfiniteModeWaveFactory;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Looper;
import android.support.v4.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;

import com.flickshot.assets.AssetLibrary;
import com.flickshot.components.Component;
import com.flickshot.components.entities.Entities;
import com.flickshot.components.entities.defs.gui.PauseButton;
import com.flickshot.components.entities.defs.managers.InfiniteModeManager;
import com.flickshot.components.entities.defs.managers.TimeAttackManager;
import com.flickshot.components.entities.defs.overlays.TitleMusic;
import com.flickshot.components.graphics.DrawLib;
import com.flickshot.components.graphics.Renderer2d;
import com.flickshot.geometry.Coordinate;
import com.flickshot.scene.Scene;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.achievement.Achievement;
import com.google.android.gms.games.achievement.AchievementBuffer;
import com.google.android.gms.games.achievement.Achievements;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.LeaderboardScoreBuffer;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.leaderboard.Leaderboards;
import com.google.android.gms.plus.Plus;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class FlickShot extends FragmentActivity implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks{
	
	private static boolean initialized = false;
	
	public static final Options options = new Options();
    public static final GameData gameData = new GameData();

    public static GoogleApiClient googleApiClient;

    private static final int REQUEST_RESOLVE_ERROR = 1001;

    private static final int REQUEST_LEADERBOARD = 1002;

    private static final int REQUEST_ACHIEMENTS = 1004;

    private boolean resolvingConnection;

    private static final String dialog_error = "dialog_error";

    public static FlickShot instance;

    public static boolean inApiWindow;


    private String showLeaderBoard;
    private boolean showCheevos;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        instance = this;

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .addApi(Drive.API).addScope(Drive.SCOPE_APPFOLDER)
                .build();

		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		GameView.createDisplay(this);
		setContentView(GameView.getCurrent());
        options.read(getSharedPreferences("options", MODE_PRIVATE));
        gameData.read(getSharedPreferences("gameData", MODE_PRIVATE));

        System.out.println("create");
	}

    @Override
    protected void onStart(){
        super.onStart();
        if(googleApiClient!=null && options.autoSignIn)googleApiClient.connect();
        System.out.println("start");
    }

    @Override
    protected void onStop(){
        super.onStop();
        System.out.println("stop");
        if(googleApiClient!=null)googleApiClient.disconnect();
        if (!initialized) {
            System.exit(0);//prevents errors
            return;
        }
        for (int i = 0; i < listeners.size(); i++) {
            HardwareButtonListener l = listeners.get(i);
            l.onHome();
        }
        GameView.pause();
        Scene s = Scene.getCurrent();
        if (s != null)
            s.updater.freeze();
        Log.i("FlickShot.onPause()", "game paused");
        ((MusicManager) AssetLibrary.getManager("music")).stopAll();
        ((SFXManager) AssetLibrary.getManager("sound")).autoPause();
        TitleMusic m = (TitleMusic)Entities.getEntity(TitleMusic.class).getState(0);
        if(m!=null)m.titleMusic.pause();
    }
	
	protected void onResume(){
        super.onResume();
        if (initialized) {
            if (GameView.paused.get()) {
                GameView.unPause();
                Scene s = Scene.getCurrent();
                if (s != null)
                    s.updater.freeze();
                Log.i("FlickShot.onResume()", "game unpaused");
            }
            ((MusicManager) AssetLibrary.getManager("music")).resumeAll();
            ((SFXManager) AssetLibrary.getManager("sound")).autoResume();
            TitleMusic m = (TitleMusic) Entities.getEntity(TitleMusic.class).getState(0);
            if (
                    Entities.getStateCount(TimeAttackManager.class) <= 0 &&
                            m != null &&
                            Entities.getStateCount(InfiniteModeManager.class) <= 0
                    )
                m.titleMusic.play();
        }
	}
	
	protected void onPause() {
        super.onPause();
        Log.i("FlickShot.onPause()", "paused");
	}

	public void init(){
		if(!initialized){
			try{
				//doInit();
				DrawLib.init(getResources());
				Renderer2d.init();
				Component.init();
				AssetLibrary.init(this);
				Scene.loadManifest(getResources(),getResources().getXml(R.xml.manifest));
                InfiniteModeWaveFactory.init(getResources().getXml(R.xml.infinite_mode_config));
				Scene context = Scene.create(Scene.getDefaultName(),new Scene.SceneInitializer(){
					public void init(){
						try{
							doInit();
						}catch(Exception ex){
							throw new Error("failed to initialize default context",ex);
						}
					}
				});
				context.start();
			}catch(Exception ex){
				throw new Error("failed to initialize default context",ex);
			}
			initialized = true;
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event){
		switch(event.getKeyCode()){
			case KeyEvent.KEYCODE_MENU:
				for(int i = 0; i<listeners.size(); i++){
                    HardwareButtonListener l = listeners.get(i);
                    l.onMenu();
                }
				return true;
			case KeyEvent.KEYCODE_HOME:
				return true;
			case KeyEvent.KEYCODE_BACK:
                for(int i = 0; i<listeners.size(); i++){
                    HardwareButtonListener l = listeners.get(i);
                    l.onBack();
                }
				return true;
			case KeyEvent.KEYCODE_SEARCH:
                for(int i = 0; i<listeners.size(); i++){
                    HardwareButtonListener l = listeners.get(i);
                    l.onSearch();
                }
				return true;
			default:
				return super.onKeyDown(keyCode,event);
		}
	}
	
	private void doInit() throws Exception{
		GameView.setFollower(new Coordinate(){

			@Override
			public double getX() {
				return 600;
			}

			@Override
			public void setX(double x) {
			}

			@Override
			public double getY() {
				return 450;
			}

			@Override
			public void setY(double y) {
			}
			
		});
	}

    private static final ArrayList<HardwareButtonListener> listeners = new ArrayList<HardwareButtonListener>();

    public static void addListener(HardwareButtonListener l){
        if(!listeners.contains(l))
            listeners.add(l);
    }

    public static void removeListener(HardwareButtonListener l){
        listeners.remove(l);
    }

    @Override
    public void onConnected(Bundle bundle) {
        resolvingConnection = false;
        gameData.retrieveSaveData();
        options.autoSignIn = true;
        options.write();
        if(showCheevos){
            showAchievments();
        }
        if(showLeaderBoard!=null){
            showLeaderboard(showLeaderBoard);
        }
        Log.d("com.flickshot.Flickshot","connected to google play service");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("com.flickshot.Flickshot", "connection to google play suspended: " + i);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("com.flickshot.Flickshot","connection to google play failed");
        if(!resolvingConnection){
            if(connectionResult.hasResolution()){
                try {
                    resolvingConnection = true;
                    connectionResult.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
                } catch (IntentSender.SendIntentException e) {
                    // There was an error with the resolution intent. Try again.
                    googleApiClient.connect();
                }

            }else{
                // Show dialog using GooglePlayServicesUtil.getErrorDialog()
                showErrorDialog(connectionResult.getErrorCode());
                resolvingConnection = true;
            }
        }
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == REQUEST_RESOLVE_ERROR) {
            resolvingConnection = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!googleApiClient.isConnecting() &&
                        !googleApiClient.isConnected()) {
                    googleApiClient.connect();
                }
            }else if(resultCode == this.RESULT_CANCELED){
                Log.d("com.flickshot.FlickShot","user canceled login");
                options.autoSignIn = false;
                options.write();
            }
        }else if(requestCode == REQUEST_ACHIEMENTS || requestCode == REQUEST_LEADERBOARD){
            showCheevos = false;
            showLeaderBoard = null;
            inApiWindow = false;
            if(resultCode == GamesActivityResultCodes.RESULT_RECONNECT_REQUIRED){
                Plus.AccountApi.clearDefaultAccount(googleApiClient);
                googleApiClient.disconnect();
            }
        }
    }

    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(dialog_error, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getSupportFragmentManager(), "error_dialog");
    }

    public void onDialogDismissed() {
        resolvingConnection = false;
    }

    public void tryToConnect(){
        if(googleApiClient!=null) {
            System.err.println("tryToLogin");
            googleApiClient.connect();
        }
    }

    public void tryToLogOut(){
        if(googleApiClient!=null) {
            System.err.println("tryToLogout");
            Plus.AccountApi.clearDefaultAccount(googleApiClient);
            googleApiClient.disconnect();
            options.autoSignIn = false;
            options.write();
        }
    }

    public static void showLeaderboard(String sceneId){
        if(googleApiClient!=null && googleApiClient.isConnected()) {
            inApiWindow = true;
            String name = gameData.getLevelData(sceneId).displayName;
            name = name.toLowerCase();
            name = name.replace(" ", "_");
            name = "leaderboard_" + name;
            System.out.println("showing leaderboard:" + name);
            instance.startActivityForResult(Games.Leaderboards.getLeaderboardIntent(googleApiClient,
                    GameView.getMain().getResources().getString(GameView.getMain().getResources().getIdentifier(name, "string", "com.flickshot"))
            ), REQUEST_LEADERBOARD);
            instance.showLeaderBoard=null;
            instance.showCheevos = false;
        }else if(googleApiClient!=null && !googleApiClient.isConnecting()){
            instance.showLeaderBoard = sceneId;
            instance.tryToConnect();
        }
    }

    public static void putScoreOnLeaderboard(String sceneId,int score){
        if(googleApiClient!=null && googleApiClient.isConnected()) {
            String name = gameData.getLevelData(sceneId).displayName;
            name = name.toLowerCase();
            name = name.replace(" ", "_");
            name = "leaderboard_" + name;
            String id = GameView.getMain().getResources()
                    .getString(GameView.getMain().getResources().getIdentifier(name, "string", "com.flickshot"));
            Games.Leaderboards.submitScore(googleApiClient, id, score);
        }
    }

    public static void getLeaderboardScores(String sceneId,final LoadScoresCallback callback,final int scores){
        if(googleApiClient!=null && googleApiClient.isConnected()) {
            String name = gameData.getLevelData(sceneId).displayName;
            name = name.toLowerCase();
            name = name.replace(" ", "_");
            name = "leaderboard_" + name;
            System.out.println(name);
            String id = GameView.getMain().getResources()
                    .getString(GameView.getMain().getResources().getIdentifier(name, "string", "com.flickshot"));
            PendingResult<Leaderboards.LoadScoresResult> result = Games.Leaderboards.loadPlayerCenteredScores
                    (googleApiClient, id, LeaderboardVariant.TIME_SPAN_ALL_TIME, LeaderboardVariant.COLLECTION_PUBLIC, scores);
            result.setResultCallback(new ResultCallback<Leaderboards.LoadScoresResult>() {
                @Override
                public void onResult(Leaderboards.LoadScoresResult loadScoresResult) {
                    LeaderboardScoreBuffer buffer = loadScoresResult.getScores();
                    String[][] scoreStrings = new String[scores][2];
                    int i;
                    for (i = 0; i < buffer.getCount(); i++) {
                        LeaderboardScore score = buffer.get(i);
                        scoreStrings[i][0] = score.getRank() + ". " + score.getScoreHolderDisplayName();
                        scoreStrings[i][1] = score.getDisplayScore();
                    }
                    for (; i < scores; i++) {
                        scoreStrings[i][0] = "N/A";
                        scoreStrings[i][1] = "N/A";
                    }

                    callback.onLoad(scoreStrings);
                    buffer.release();
                }
            }, 30, TimeUnit.SECONDS);
        }
    }

    public static void showAchievments(){
        if(googleApiClient!=null && googleApiClient.isConnected()) {
            inApiWindow = true;
            Intent intent = Games.Achievements.getAchievementsIntent(googleApiClient);
            instance.startActivityForResult(Games.Achievements.getAchievementsIntent(googleApiClient), REQUEST_ACHIEMENTS);
            instance.showLeaderBoard=null;
            instance.showCheevos = false;
        }else if(googleApiClient!=null && !googleApiClient.isConnecting()){
            instance.showCheevos = true;
            instance.tryToConnect();
        }
    }

    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() { }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(dialog_error);
            return GooglePlayServicesUtil.getErrorDialog(errorCode,
                    this.getActivity(), REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((FlickShot)getActivity()).onDialogDismissed();
        }
    }

    public static class LoginDialogFragment extends DialogFragment{

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setMessage("Connect to google play?").setTitle("Sign In?");

            builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    googleApiClient.connect();
                }
            });

            builder.setNegativeButton("no", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    instance.showCheevos = false;
                    instance.showLeaderBoard = null;
                }
            });
            return builder.create();
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((FlickShot)getActivity()).onDialogDismissed();
        }
    }

    public static class LogoutDialogFragment extends DialogFragment{

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setMessage("Do you really want to logut?").setTitle("Logout?");

            builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Plus.AccountApi.clearDefaultAccount(googleApiClient);
                    googleApiClient.disconnect();
                    options.autoSignIn = false;
                    options.write();
                }
            });

            builder.setNegativeButton("no", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });


            return builder.create();
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((FlickShot)getActivity()).onDialogDismissed();
        }
    }

    public static interface LoadScoresCallback{
        public void onLoad(String[][] scores);
    }

    public static abstract class HardwareButtonListener{
        public void onBack(){};
        public void onHome(){};
        public void onMenu(){};
        public void onSearch(){};
    }
	
}
